package com.okane.aspect;

import com.okane.entity.JournalAudit;
import com.okane.entity.User;
import com.okane.service.JournalAuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditAspect {

    private final JournalAuditService journalAuditService;

    public AuditAspect(JournalAuditService journalAuditService) {
        this.journalAuditService = journalAuditService;
    }

    // ------------------------------------------------------------------ //
    //  Pointcuts
    // ------------------------------------------------------------------ //

    /** Every public method in every class whose name ends with ServiceImpl. */
    @Pointcut("execution(public * com.okane.service.impl.*ServiceImpl.*(..))")
    public void sensitiveServices() {}

    /** Exclude the audit service itself to avoid infinite recursion. */
    @Pointcut("!execution(* com.okane.service.impl.JournalAuditServiceImpl.*(..))")
    public void excludeAuditSelf() {}

    @Pointcut("execution(public * com.okane.service.impl.AuthServiceImpl.me(..)) || " +
            "execution(public * com.okane.service.impl.AuthServiceImpl.login(..)) || " +
            "execution(public * com.okane.service.impl.AuthServiceImpl.refresh(..))")
    public void excludeReadOnlyAuth() {}

    // ------------------------------------------------------------------ //
    //  Advice
    // ------------------------------------------------------------------ //

    /**
     * Fires after any intercepted method returns normally (no exception).
     *
     * @param joinPoint  metadata about the intercepted call
     * @param returnValue the value returned by the service method
     */
    @AfterReturning(
            pointcut  = "sensitiveServices() && excludeAuditSelf() && !excludeReadOnlyAuth()",
            returning = "returnValue"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object returnValue) {
        try {
            String action  = buildAction(joinPoint);
            String details = buildDetails(joinPoint, returnValue);
            String type    = resolveType(joinPoint);
            String ip      = resolveIp();
            User   user    = resolveUser();

            if (user == null) {
                // Skip audit for unauthenticated calls (e.g. login itself)
                return;
            }

            JournalAudit entry = JournalAudit.builder()
                    .action(action)
                    .details(details)
                    .type(type)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(ip)
                    .utilisateur(user)
                    .build();

            journalAuditService.save(entry);

        } catch (Exception ex) {
            // Never let the audit logic break the real business flow
            System.err.println("[AuditAspect] Failed to persist audit log: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    /**
     * e.g.  "TransactionServiceImpl.createTransaction"
     */
    private String buildAction(JoinPoint jp) {
        String className  = jp.getTarget().getClass().getSimpleName();
        String methodName = jp.getSignature().getName();
        return className + "." + methodName;
    }

    /**
     * Summarises the arguments that were passed to the method.
     * Truncated at 1 000 chars to match the column definition.
     */
    private String buildDetails(JoinPoint jp, Object returnValue) {
        StringBuilder sb = new StringBuilder();

        Object[] args = jp.getArgs();
        if (args != null && args.length > 0) {
            sb.append("args=[");
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i]);
                if (i < args.length - 1) sb.append(", ");
            }
            sb.append("] ");
        }

        if (returnValue != null) {
            sb.append("return=").append(returnValue);
        }

        String result = sb.toString();
        return result.length() > 1000 ? result.substring(0, 1000) : result;
    }

    /**
     * Maps the method name prefix to a semantic audit type.
     * Feel free to extend this mapping.
     */
    private String resolveType(JoinPoint jp) {
        String method = jp.getSignature().getName().toLowerCase();

        if (method.startsWith("create") || method.startsWith("save")
                || method.startsWith("add")    || method.startsWith("register")) {
            return "CREATE";
        }
        if (method.startsWith("update") || method.startsWith("edit")
                || method.startsWith("modify") || method.startsWith("change")) {
            return "UPDATE";
        }
        if (method.startsWith("delete") || method.startsWith("remove")
                || method.startsWith("cancel")) {
            return "DELETE";
        }
        if (method.startsWith("login")  || method.startsWith("logout")
                || method.startsWith("auth")   || method.startsWith("verify")
                || method.startsWith("send")) {
            return "AUTH";
        }
        return "ACCESS";
    }

    /**
     * Returns the authenticated User entity, or null if the request
     * is anonymous or the principal is not our User type.
     */
    private User resolveUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof User) return (User) principal;
        return null;
    }

    /**
     * Best-effort IP resolution from the current HTTP request.
     * Returns "unknown" outside a request context (e.g. scheduled tasks).
     */
    private String resolveIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "unknown";

            String ip = attrs.getRequest().getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) {
                // X-Forwarded-For may contain a chain; take the first one
                return ip.split(",")[0].trim();
            }
            return attrs.getRequest().getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }
}