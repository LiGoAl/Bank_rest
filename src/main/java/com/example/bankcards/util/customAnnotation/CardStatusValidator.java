package com.example.bankcards.util.customAnnotation;

import com.example.bankcards.util.CardStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CardStatusValidator implements ConstraintValidator<ValidCardStatus, CardStatus> {
    @Override
    public boolean isValid(CardStatus cardStatus, ConstraintValidatorContext constraintValidatorContext) {
        if (cardStatus == null) {
            return false;
        }

        try {
            CardStatus.valueOf(cardStatus.name());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void initialize(ValidCardStatus constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
