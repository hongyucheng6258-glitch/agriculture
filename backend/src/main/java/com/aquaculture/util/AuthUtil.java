package com.aquaculture.util;

import com.aquaculture.entity.SysUser;
import org.springframework.util.StringUtils;

public class AuthUtil {
    
    public static SysUser getCurrentUser(String authHeader) {
        if (!StringUtils.hasText(authHeader)) {
            return null;
        }
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        return TokenManager.get(token);
    }
    
    public static boolean isAdmin(String authHeader) {
        SysUser user = getCurrentUser(authHeader);
        return user != null && "admin".equals(user.getRole());
    }
    
    public static boolean isAuthenticated(String authHeader) {
        return getCurrentUser(authHeader) != null;
    }
}
