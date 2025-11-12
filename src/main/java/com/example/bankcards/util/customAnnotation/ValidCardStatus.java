package com.example.bankcards.util.customAnnotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CardStatusValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCardStatus {
    String message() default "Card status must be ACTIVE, BLOCKED or EXPIRED";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
