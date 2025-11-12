package com.example.bankcards.util;

public class CardMask {
    public static String formatCardNumber(String cardNumber) {
        return cardNumber.replaceAll("[0-9]{4} ", "**** ");
    }
}
