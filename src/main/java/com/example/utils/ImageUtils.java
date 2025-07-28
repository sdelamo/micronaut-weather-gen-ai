package com.example.utils;

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public final class ImageUtils {
    private ImageUtils() {
    }

    public static byte[] fetchBytes(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        try (InputStream is = url.openStream()) {
            return is.readAllBytes();
        }
    }

    public static String toBase64DataUrl(String urlStr, String mimeType) throws Exception {
        URL url = new URL(urlStr);
        try (InputStream is = url.openStream()) {
            byte[] bytes = is.readAllBytes();
            return toBase64DataUrl(bytes, mimeType);
        }
    }

    public static String toBase64DataUrl(InputStream inputStream, String mimeType) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        return toBase64DataUrl(bytes, mimeType);
    }

    public static String toBase64DataUrl(byte[] bytes, String mimeType) {
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + mimeType + ";base64," + base64;
    }
}