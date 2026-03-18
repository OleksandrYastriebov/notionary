package com.api.wishoria.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

public final class IdGenerationUtil {

    private IdGenerationUtil() {
    }

    /**
     * Generate unique UUID from given alphabet
     *
     * @return unique UUID
     */
    public static String generateNanoId() {
        char[] alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, alphabet, 12);
    }
}
