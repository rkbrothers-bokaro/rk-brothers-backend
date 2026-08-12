package com.boltblazers.rkbrothers.core.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method whose invocation should be recorded in the audit log.
 * The annotated entity name is used to group audit entries by domain object;
 * the entity id is resolved from the method's return value (if it exposes
 * a getId()) or, failing that, from its first argument.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {

    String entityName();

    AuditAction action();
}
