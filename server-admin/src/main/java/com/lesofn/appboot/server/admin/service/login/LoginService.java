package com.lesofn.appboot.server.admin.service.login;

import com.google.code.kaptcha.Producer;
import com.lesofn.appboot.common.exception.ApiException;
import com.lesofn.appboot.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.appboot.infrastructure.config.AppBootConfig;
import com.lesofn.appboot.infrastructure.config.CaptchaType;
import com.lesofn.appboot.server.admin.dto.CaptchaDTO;
import com.lesofn.appboot.server.admin.dto.ConfigDTO;
import com.lesofn.appboot.server.admin.dto.LoginCommand;
import com.lesofn.appboot.server.admin.error.LoginExcepFactor;
import com.lesofn.appboot.server.admin.service.cache.RedisCacheService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

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
    private final AppBootConfig appBootConfig;

    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    private static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 登录验证
     *
     * @param loginCommand 登录参数
     * @return token
     */
    public String login(LoginCommand loginCommand) {
        // 验证码校验
        validateCaptcha(loginCommand.getUuid(), loginCommand.getCode());

        // 用户验证
        Authentication authentication;
        try {
            // 该方法会去调用 UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginCommand.getUsername(), loginCommand.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.info("用户[{}]登录失败，用户名或密码错误", loginCommand.getUsername());
            throw new ApiException(LoginExcepFactor.USERNAME_PASSWORD_ERROR);
        } catch (Exception e) {
            log.error("用户[{}]登录失败", loginCommand.getUsername(), e);
            throw new ApiException(LoginExcepFactor.USERNAME_PASSWORD_ERROR, e.getMessage());
        }

        SystemLoginUser loginUser = (SystemLoginUser) authentication.getPrincipal();
        // 生成token
        return tokenService.createTokenAndPutUserInCache(loginUser);
    }

    private void validateCaptcha(String uuid, String code) {
        if (!appBootConfig.getCaptcha().isEnabled()) {
            return;
        }

        if (!StringUtils.hasText(code)) {
            throw new ApiException(LoginExcepFactor.CAPTCHA_REQUIRED);
        }
        if (!StringUtils.hasText(uuid)) {
            throw new ApiException(LoginExcepFactor.CAPTCHA_EXPIRED);
        }

        String verifyKey = CAPTCHA_CODE_KEY + uuid;
        Object cacheCode = redisCacheService.captchaCache.getObjectById(uuid);
        redisCacheService.loginUserCache.delete(uuid);

        if (cacheCode == null) {
            throw new ApiException(LoginExcepFactor.CAPTCHA_EXPIRED);
        }

        String verifyCode = String.valueOf(cacheCode);
        if (!code.equalsIgnoreCase(verifyCode)) {
            throw new ApiException(LoginExcepFactor.CAPTCHA_ERROR);
        }
    }

    /**
     * 生成验证码
     *
     * @return 验证码信息
     */
    public CaptchaDTO generateCaptchaImg() {
        if (!appBootConfig.getCaptcha().isEnabled()) {
            return new CaptchaDTO(false, "", "");
        }

        // 生成验证码
        String uuid = UUID.randomUUID().toString().replace("-", "");

        String expression;
        String answer;
        BufferedImage image;

        // 根据验证码类型选择对应的实现
        CaptchaType captchaType = appBootConfig.getCaptchaType();
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
            String base64 = "data:image/jpg;base64," + Base64.encodeBase64String(outputStream.toByteArray());
            return new CaptchaDTO(appBootConfig.getCaptcha().isEnabled(), uuid, base64);
        } catch (Exception e) {
            log.error("生成验证码异常", e);
            throw new ApiException(LoginExcepFactor.CAPTCHA_GENERATE_ERROR);
        }
    }

    /**
     * 获取系统配置
     *
     * @return 配置信息
     */
    public ConfigDTO getConfig() {
        return new ConfigDTO(appBootConfig.getCaptcha().isEnabled(), appBootConfig.getRegister().isEnabled());
    }

}