package backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        return (principal instanceof Long id) ? id : null;
    }

    /** 返回 0=志愿者 1=活动管理员 2=超级管理员 */
    public static Integer getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return 0;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> switch (r) {
                    case "ROLE_ACTIVITY_ADMIN" -> 1;
                    case "ROLE_SUPER_ADMIN"    -> 2;
                    default                    -> 0;
                })
                .findFirst()
                .orElse(0);
    }
}
