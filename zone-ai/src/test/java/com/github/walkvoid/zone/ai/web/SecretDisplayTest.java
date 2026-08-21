package com.github.walkvoid.zone.ai.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecretDisplayTest {

    @Test
    void maskBlank() {
        assertEquals("", SecretDisplay.mask(null));
        assertEquals("", SecretDisplay.mask("  "));
    }

    @Test
    void maskShortAndLong() {
        assertEquals("••••", SecretDisplay.mask("abcd"));
        assertEquals("••••wxyz", SecretDisplay.mask("secret-wxyz"));
    }
}
