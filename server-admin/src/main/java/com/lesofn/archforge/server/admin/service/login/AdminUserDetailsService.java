package com.lesofn.archforge.server.admin.service.login;

import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminLoginUserFactory adminLoginUserFactory;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminLoginUserFactory.load(username);
    }

    public RoleInfo getRoleInfo(@Nullable Long roleId, boolean isAdmin) {
        return adminLoginUserFactory.getRoleInfo(roleId, isAdmin);
    }
}
