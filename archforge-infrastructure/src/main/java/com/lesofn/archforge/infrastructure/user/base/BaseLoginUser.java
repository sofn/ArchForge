package com.lesofn.archforge.infrastructure.user.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lesofn.archforge.common.utils.ServletHolderUtil;
import com.lesofn.archforge.common.utils.ip.IpRegionUtil;
import com.lesofn.archforge.common.utils.ip.IpUtil;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户身份权限
 *
 * @author sofn
 */
@Data
@NoArgsConstructor
public class BaseLoginUser implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    protected Long userId;

    /** 用户唯一标识，缓存的key */
    protected String cachedKey;

    protected String username;

    @JsonIgnore
    protected String password;

    protected List<String> authorities = new ArrayList<>();

    /** 登录信息 */
    protected final LoginInfo loginInfo = new LoginInfo();

    public BaseLoginUser(Long userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    /** 设置用户代理信息 */
    public void fillLoginInfo() {
        HttpServletRequest request;
        try {
            request = ServletHolderUtil.getRequest();
        } catch (Exception e) {
            // 如果获取请求上下文失败，使用默认值
            setDefaultLoginInfo();
            return;
        }

        if (request == null) {
            // 如果请求上下文不可用，设置默认值
            setDefaultLoginInfo();
            return;
        }

        try {
            String userAgentHeader = request.getHeader("User-Agent");
            if (userAgentHeader == null) {
                userAgentHeader = "unknown";
            }

            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentHeader);
            String ip = IpUtil.getRealIpAddr(request);

            this.getLoginInfo().setIpAddress(ip);
            this.getLoginInfo().setLocation(IpRegionUtil.getBriefLocationByIp(ip));
            this.getLoginInfo()
                    .setBrowser(
                            userAgent.getBrowser() != null
                                    ? userAgent.getBrowser().getName()
                                    : "unknown");
            this.getLoginInfo()
                    .setOperationSystem(
                            userAgent.getOperatingSystem() != null
                                    ? userAgent.getOperatingSystem().getName()
                                    : "unknown");
            this.getLoginInfo().setLoginTime(System.currentTimeMillis());
        } catch (Exception e) {
            // 如果处理请求信息时发生异常，使用默认值
            setDefaultLoginInfo();
        }
    }

    private void setDefaultLoginInfo() {
        this.getLoginInfo().setIpAddress("unknown");
        this.getLoginInfo().setLocation("unknown");
        this.getLoginInfo().setBrowser("unknown");
        this.getLoginInfo().setOperationSystem("unknown");
        this.getLoginInfo().setLoginTime(System.currentTimeMillis());
    }

    public void grantAppPermission(String appName) {
        if (appName != null && !appName.isBlank() && !authorities.contains(appName)) {
            authorities.add(appName);
        }
    }

    @JsonIgnore
    public List<String> getAuthorities() { return authorities; }
}
