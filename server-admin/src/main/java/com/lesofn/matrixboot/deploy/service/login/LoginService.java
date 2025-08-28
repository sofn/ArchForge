package com.lesofn.matrixboot.deploy.service.login;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.lesofn.matrixboot.common.exception.ApiException;
import com.lesofn.matrixboot.deploy.error.LoginExcepFactor;
import com.lesofn.matrixboot.deploy.dto.CaptchaDTO;
import com.lesofn.matrixboot.deploy.dto.ConfigDTO;
import com.lesofn.matrixboot.deploy.dto.LoginCommand;
import com.lesofn.matrixboot.deploy.service.cache.AuthRedisCacheService;
import com.lesofn.matrixboot.infrastructure.auth.model.SystemLoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.UUID;

/**
 * 登录服务
 * @author sofn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final AuthRedisCacheService redisCacheService;
    
    @Value("${captcha.enabled:true}")
    private boolean captchaEnabled;
    
    @Value("${register.enabled:false}")
    private boolean registerEnabled;
    
    private static final String CAPTCHA_CODE_KEY = "captcha_codes:";
    private static final int CAPTCHA_EXPIRATION = 2; // 验证码有效期（分钟）
    
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
        if (!captchaEnabled) {
            return;
        }
        
        if (!StringUtils.hasText(code)) {
            throw new ApiException(LoginExcepFactor.CAPTCHA_REQUIRED);
        }
        if (!StringUtils.hasText(uuid)) {
            throw new ApiException(LoginExcepFactor.CAPTCHA_EXPIRED);
        }
        
        String verifyKey = CAPTCHA_CODE_KEY + uuid;
        Object cacheCode = redisCacheService.loginUserCache.getObjectOnlyInCacheById(uuid);
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
        if (!captchaEnabled) {
            return new CaptchaDTO(false, "", "");
        }
        
        // 生成验证码
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String verifyKey = CAPTCHA_CODE_KEY + uuid;
        
        DefaultKaptcha defaultKaptcha = createDefaultKaptcha();
        String code = defaultKaptcha.createText();
        BufferedImage image = defaultKaptcha.createImage(code);
        
        // 保存验证码信息
        redisCacheService.loginUserCache.set(uuid, code);
        
        // 转换流信息写出
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            String base64 = "data:image/jpg;base64," + Base64.encodeBase64String(outputStream.toByteArray());
            return new CaptchaDTO(captchaEnabled, uuid, base64);
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
        return new ConfigDTO(captchaEnabled, registerEnabled);
    }
    
    /**
     * 创建验证码生成器
     * 
     * @return 验证码生成器
     */
    private DefaultKaptcha createDefaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 是否有边框
        properties.setProperty("kaptcha.border", "yes");
        // 边框颜色
        properties.setProperty("kaptcha.border.color", "105,179,90");
        // 验证码文本字符颜色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        // 验证码图片宽度
        properties.setProperty("kaptcha.image.width", "160");
        // 验证码图片高度
        properties.setProperty("kaptcha.image.height", "60");
        // 验证码文本字符大小
        properties.setProperty("kaptcha.textproducer.font.size", "40");
        // 验证码session key
        properties.setProperty("kaptcha.session.key", "kaptchaCode");
        // 验证码文本字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 验证码文本字体样式
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");
        // 验证码噪点颜色
        properties.setProperty("kaptcha.noise.color", "white");
        // 验证码文本字符内容范围
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        
        return defaultKaptcha;
    }
}