package com.lesofn.archsmith.server.admin.service.login;

import static com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthErrorCode.*;

import com.google.code.kaptcha.Producer;
import com.lesofn.archsmith.common.encrypt.RsaEncrypter;
import com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archsmith.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archsmith.infrastructure.config.ArchSmithConfig;
import com.lesofn.archsmith.infrastructure.config.CaptchaType;
import com.lesofn.archsmith.infrastructure.frame.utils.MapCache;
import com.lesofn.archsmith.server.admin.dto.CaptchaDTO;
import com.lesofn.archsmith.server.admin.dto.ConfigDTO;
import com.lesofn.archsmith.server.admin.dto.LoginCommand;
import com.lesofn.archsmith.server.admin.service.cache.RedisCacheService;
import jakarta.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登录服务
 *
 * @author sofn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RedisCacheService redisCacheService;
    private final LoginAttemptService loginAttemptService;
    private final ArchSmithConfig appForgeConfig;

    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    /**
     * 登录验证
     *
     * @param loginCommand 登录参数
     * @return LoginResult 包含token和用户信息
     */
    public LoginResult login(LoginCommand loginCommand) {
        // 登录失败锁定检查
        loginAttemptService.checkNotLocked(loginCommand.getUsername());

        // 验证码校验
        validateCaptcha(loginCommand.getCaptchaCodeKey(), loginCommand.getCaptchaCode());

        // 用户验证
        Authentication authentication;
        try {
            String decryptedPassword = decryptPassword(loginCommand.getPassword());
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginCommand.getUsername(), decryptedPassword));
        } catch (BadCredentialsException e) {
            log.info("用户[{}]登录失败，用户名或密码错误", loginCommand.getUsername());
            loginAttemptService.recordFailure(loginCommand.getUsername());
            throw new AdminAuthException(USERNAME_PASSWORD_ERROR);
        } catch (Exception e) {
            log.error("用户[{}]登录失败", loginCommand.getUsername(), e);
            loginAttemptService.recordFailure(loginCommand.getUsername());
            throw new AdminAuthException(LOGIN_ERROR);
        }

        SystemLoginUser loginUser = (SystemLoginUser) authentication.getPrincipal();
        // 登录成功，清除失败计数
        loginAttemptService.clearAttempts(loginCommand.getUsername());
        // 生成token
        String token = tokenService.createTokenAndPutUserInCache(loginUser);

        return new LoginResult(token, loginUser);
    }

    /** 登录结果封装类 */
    public static class LoginResult {
        private final String token;
        private final SystemLoginUser loginUser;

        public LoginResult(String token, SystemLoginUser loginUser) {
            this.token = token;
            this.loginUser = loginUser;
        }

        public String getToken() {
            return token;
        }

        public SystemLoginUser getLoginUser() {
            return loginUser;
        }
    }

    private void validateCaptcha(String uuid, String code) {
        if (!appForgeConfig.getCaptcha().isEnabled()) {
            return;
        }

        if (!StringUtils.hasText(code)) {
            throw new AdminAuthException(CAPTCHA_REQUIRED);
        }
        if (!StringUtils.hasText(uuid)) {
            throw new AdminAuthException(CAPTCHA_EXPIRED);
        }

        Object cacheCode = redisCacheService.captchaCache.get(uuid);
        redisCacheService.loginUserCache.delete(uuid);

        if (cacheCode == null) {
            throw new AdminAuthException(CAPTCHA_EXPIRED);
        }

        String verifyCode = String.valueOf(cacheCode);
        if (!code.equalsIgnoreCase(verifyCode)) {
            throw new AdminAuthException(CAPTCHA_ERROR);
        }
    }

    /**
     * 生成验证码
     *
     * @return 验证码信息
     */
    public CaptchaDTO generateCaptchaImg() {
        if (!appForgeConfig.getCaptcha().isEnabled()) {
            return new CaptchaDTO(false, "", "");
        }

        // 生成验证码
        String uuid = UUID.randomUUID().toString().replace("-", "");

        String expression;
        String answer;
        BufferedImage image;

        // 根据验证码类型选择对应的实现
        CaptchaType captchaType = appForgeConfig.getCaptchaType();
        if (captchaType == CaptchaType.MATH) {
            String capText = captchaProducerMath.createText();
            String[] expressionAndAnswer = capText.split("@");
            expression = expressionAndAnswer[0];
            answer = expressionAndAnswer[1];
            image = captchaProducerMath.createImage(expression);
        } else if (captchaType == CaptchaType.CHAR) {
            expression = answer = captchaProducer.createText();
            image = captchaProducer.createImage(expression);
        } else {
            // 默认使用字符验证码
            expression = answer = captchaProducer.createText();
            image = captchaProducer.createImage(expression);
        }

        // 保存验证码信息（保存答案）
        redisCacheService.captchaCache.set(uuid, answer);

        // 转换流信息写出
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            String base64 = Base64.encodeBase64String(outputStream.toByteArray());
            return new CaptchaDTO(appForgeConfig.getCaptcha().isEnabled(), uuid, base64);
        } catch (Exception e) {
            log.error("生成验证码异常", e);
            throw new AdminAuthException(CAPTCHA_GENERATE_ERROR);
        }
    }

    /**
     * 获取系统配置
     *
     * @return 配置信息
     */
    public ConfigDTO getConfig() {
        ConfigDTO configDTO = new ConfigDTO();
        boolean isCaptchaOn = appForgeConfig.getCaptcha().isEnabled();
        configDTO.setIsCaptchaOn(isCaptchaOn);
        configDTO.setDictionary(MapCache.dictionaryCache());
        return configDTO;
    }

    /**
     * 解密密码，如果RSA解密失败则尝试作为明文密码处理（兼容前端未加密的场景）
     *
     * @param encryptedPassword 加密后的密码或明文密码
     * @return 解密后的密码
     */
    public String decryptPassword(String encryptedPassword) {
        try {
            return RsaEncrypter.decrypt(encryptedPassword, appForgeConfig.getRsaPrivateKey());
        } catch (Exception e) {
            log.warn("RSA密码解密失败，尝试作为明文密码处理: {}", e.getMessage());
            return encryptedPassword;
        }
    }
}
