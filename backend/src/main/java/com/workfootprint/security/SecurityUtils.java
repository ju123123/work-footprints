package com.workfootprint.security;

import com.workfootprint.controller.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static UserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new UnauthorizedException();
        }
        if (auth.getPrincipal() instanceof UserPrincipal p) {
            return p;
        }
        throw new UnauthorizedException();
    }
}

