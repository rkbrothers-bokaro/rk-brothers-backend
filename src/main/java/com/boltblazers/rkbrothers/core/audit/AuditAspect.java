package com.boltblazers.rkbrothers.core.audit;

import com.boltblazers.rkbrothers.core.auth.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final int DETAILS_MAX_LENGTH = 2000;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(auditable)")
    public Object recordAudit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Object result = joinPoint.proceed();

        try {
            String entityId = resolveEntityId(result, args);

            AuditLog log = AuditLog.builder()
                    .entityName(auditable.entityName())
                    .entityId(entityId)
                    .action(auditable.action())
                    .performedBy(currentUsername())
                    .details(buildDetails(joinPoint, args, result))
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            // Auditing must never break the primary business operation.
            log.warn("Failed to record audit log for {}: {}", auditable.entityName(), e.getMessage());
        }

        return result;
    }

    private String buildDetails(ProceedingJoinPoint joinPoint, Object[] args, Object result) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("method", joinPoint.getSignature().toShortString());
        details.put("userId", currentUserId());
        details.put("ip", currentRemoteIp());
        // True pre-mutation state isn't available here (proceed() already ran), so
        // "oldValue" reflects the submitted payload and "newValue" the persisted result.
        details.put("oldValue", toJsonSafe(args.length > 0 ? args[args.length - 1] : null));
        details.put("newValue", toJsonSafe(result));

        String json = toJsonSafe(details);
        if (json != null && json.length() > DETAILS_MAX_LENGTH) {
            return json.substring(0, DETAILS_MAX_LENGTH - 3) + "...";
        }
        return json;
    }

    private String toJsonSafe(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return "\"<unserializable:" + value.getClass().getSimpleName() + ">\"";
        }
    }

    private String resolveEntityId(Object result, Object[] args) {
        String id = extractId(result);
        if (id != null) {
            return id;
        }
        for (Object arg : args) {
            id = extractId(arg);
            if (id != null) {
                return id;
            }
        }
        // Fall back to a bare identifier argument, e.g. delete(Long id).
        for (Object arg : args) {
            if (arg instanceof Long || arg instanceof Integer || arg instanceof String) {
                return arg.toString();
            }
        }
        return null;
    }

    private String extractId(Object target) {
        if (target == null) {
            return null;
        }
        try {
            Method getId = target.getClass().getMethod("getId");
            Object value = getId.invoke(target);
            return value != null ? value.toString() : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        return null;
    }

    private String currentRemoteIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest().getRemoteAddr();
        }
        return null;
    }
}
