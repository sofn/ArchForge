package com.lesofn.appforge.infrastructure.user.base;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lesofn.appforge.common.utils.ServletHolderUtil;
import com.lesofn.appforge.common.utils.ip.IpRegionUtil;
import com.lesofn.appforge.common.utils.ip.IpUtil;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 登录用户身份权限
 * @author sofn
 */
@Data
@NoArgsConstructor
public class BaseLoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    protected Long userId;

    /**
     * 用户唯一标识，缓存的key
     */
    protected String cachedKey;

    protected String username;

    protected String password;

    protected List<GrantedAuthority> authorities = new ArrayList<>();
    /**
     * 登录信息
     */
    protected final LoginInfo loginInfo = new LoginInfo();


    public BaseLoginUser(Long userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    /**
     * 设置用户代理信息
     *
     */
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
            this.getLoginInfo().setBrowser(userAgent.getBrowser() != null ? userAgent.getBrowser().getName() : "unknown");
            this.getLoginInfo().setOperationSystem(userAgent.getOperatingSystem() != null ? userAgent.getOperatingSystem().getName() : "unknown");
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
        authorities.add(new SimpleGrantedAuthority(appName));
    }


    @Override
    public String getUsername() {
        return this.username;
    }


    @JsonIgnore
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * 账户是否未过期,过期无法验证
     * 未实现此功能
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 指定用户是否解锁,锁定的用户无法进行身份验证
     * 未实现此功能
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 指示是否已过期的用户的凭据(密码),过期的凭据防止认证
     * 未实现此功能
     */
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否可用 ,禁用的用户不能身份验证
     * 未实现此功能
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }


}
