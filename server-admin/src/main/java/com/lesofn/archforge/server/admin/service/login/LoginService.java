package com.lesofn.archforge.server.admin.service.login;

import static com.lesofn.archforge.infrastructure.auth.errors.AdminAuthErrorCode.*;

import com.google.code.kaptcha.Producer;
import com.lesofn.archforge.common.encrypt.RsaEncrypter;
import com.lesofn.archforge.common.utils.ip.IpRegionUtil;
import com.lesofn.archforge.common.utils.ip.IpUtil;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.config.CaptchaType;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import com.lesofn.archforge.infrastructure.dictionary.EnumDictionaryRegistry;
import com.lesofn.archforge.server.admin.dto.CaptchaResponse;
import com.lesofn.archforge.server.admin.dto.LoginConfigResponse;
import com.lesofn.archforge.server.admin.dto.LoginRequest;
import com.lesofn.archforge.server.admin.service.cache.RedisCacheService;
import com.lesofn.archforge.user.api.domain.SysLoginLog;
import com.lesofn.archforge.user.api.service.SysLoginLogService;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.core.env.Environment;
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
    private final SysLoginLogService loginLogService;
    private final ArchForgeProperties appForgeConfig;
    private final Environment environment;
    private final EnumDictionaryRegistry enumDictionaryRegistry;

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
    public LoginResult login(LoginRequest loginCommand) {
        try {
            // 登录失败锁定检查
            loginAttemptService.checkNotLocked(loginCommand.getUsername());

            // 验证码校验
            validateCaptcha(loginCommand.getCaptchaCodeKey(), loginCommand.getCaptchaCode());

            // 用户验证
            String decryptedPassword = decryptPassword(loginCommand.getPassword());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginCommand.getUsername(), decryptedPassword));

            SystemLoginUser loginUser = (SystemLoginUser) authentication.getPrincipal();
            // 登录成功，清除失败计数
            loginAttemptService.clearAttempts(loginCommand.getUsername());
            // 生成token
            String token = tokenService.createTokenAndPutUserInCache(loginUser);

            recordLoginLog(loginUser, 1, "登录成功");
            return new LoginResult(token, loginUser);
        } catch (BadCredentialsException e) {
            log.info("用户[{}]登录失败，用户名或密码错误", loginCommand.getUsername());
            loginAttemptService.recordFailure(loginCommand.getUsername());
            recordLoginLog(loginCommand.getUsername(), 0, "登录失败");
            throw new AdminAuthException(USERNAME_PASSWORD_ERROR);
        } catch (AdminAuthException e) {
            log.info("用户[{}]登录失败：{}", loginCommand.getUsername(), e.getMessage());
            recordLoginLog(loginCommand.getUsername(), 0, "登录失败");
            throw e;
        } catch (Exception e) {
            log.error("用户[{}]登录失败", loginCommand.getUsername(), e);
            loginAttemptService.recordFailure(loginCommand.getUsername());
            recordLoginLog(loginCommand.getUsername(), 0, "登录失败");
            throw new AdminAuthException(LOGIN_ERROR);
        }
    }

    /** 登录结果封装类 */
    public static class LoginResult {
        private final String token;
        private final SystemLoginUser loginUser;

        public LoginResult(String token, SystemLoginUser loginUser) {
            this.token = token;
            this.loginUser = loginUser;
        }

        public String getToken() { return token; }

        public SystemLoginUser getLoginUser() { return loginUser; }
    }

    private void recordLoginLog(SystemLoginUser loginUser, int status, String behavior) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(loginUser.getUsername());
        loginLog.setStatus(status);
        loginLog.setBehavior(behavior);
        if (loginUser.getLoginInfo() != null) {
            loginLog.setIp(loginUser.getLoginInfo().getIpAddress());
            loginLog.setAddress(loginUser.getLoginInfo().getLocation());
            loginLog.setSystemName(loginUser.getLoginInfo().getOperationSystem());
            loginLog.setBrowser(loginUser.getLoginInfo().getBrowser());
        } else {
            fillLoginLogFromRequest(loginLog);
        }
        loginLog.setLoginTime(LocalDateTime.now());
        saveLoginLog(loginLog);
    }

    private void recordLoginLog(String username, int status, String behavior) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setStatus(status);
        loginLog.setBehavior(behavior);
        fillLoginLogFromRequest(loginLog);
        loginLog.setLoginTime(LocalDateTime.now());
        saveLoginLog(loginLog);
    }

    private void fillLoginLogFromRequest(SysLoginLog loginLog) {
        HttpServletRequest request = ScopedValueContext.getServletRequest();
        String ip = request == null ? "" : IpUtil.getRealIpAddr(request);
        UserAgent userAgent = UserAgent.parseUserAgentString(request == null ? "" : request.getHeader("User-Agent"));
        loginLog.setIp(ip);
        loginLog.setAddress(IpRegionUtil.getBriefLocationByIp(ip));
        loginLog.setSystemName(userAgent.getOperatingSystem().getName());
        loginLog.setBrowser(userAgent.getBrowser().getName());
    }

    private void saveLoginLog(SysLoginLog loginLog) {
        try {
            loginLogService.create(loginLog);
        } catch (Exception ex) {
            log.warn("Failed to save login log", ex);
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

        Object cacheCode = redisCacheService.getCaptchaCache().get(uuid);
        redisCacheService.getCaptchaCache().delete(uuid);

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
    public CaptchaResponse generateCaptchaImg() {
        if (!appForgeConfig.getCaptcha().isEnabled()) {
            return new CaptchaResponse(false, "", "");
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
        redisCacheService.getCaptchaCache().set(uuid, answer);

        // 转换流信息写出
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            String base64 = Base64.encodeBase64String(outputStream.toByteArray());
            return new CaptchaResponse(appForgeConfig.getCaptcha().isEnabled(), uuid, base64);
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
    public LoginConfigResponse getConfig() {
        LoginConfigResponse configDTO = new LoginConfigResponse();
        boolean isCaptchaOn = appForgeConfig.getCaptcha().isEnabled();
        configDTO.setIsCaptchaOn(isCaptchaOn);
        configDTO.setDictionary(enumDictionaryRegistry.asDictionaryDataMap());
        return configDTO;
    }

    /**
     * 解密密码。生产环境 RSA 解密失败直接拒绝；非生产环境保留明文回退用于开发/测试。
     *
     * @param encryptedPassword RSA 加密后的密码或明文密码
     * @return 解密后的密码
     */
    public String decryptPassword(String encryptedPassword) {
        try {
            return RsaEncrypter.decrypt(encryptedPassword, appForgeConfig.getRsaPrivateKey());
        } catch (Exception e) {
            if (environment.matchesProfiles("prod")) {
                log.warn("RSA密码解密失败，拒绝明文回退: {}", e.getMessage());
                throw new BadCredentialsException("密码解密失败");
            }
            log.warn("RSA密码解密失败，尝试作为明文密码处理: {}", e.getMessage());
            return encryptedPassword;
        }
    }
}
