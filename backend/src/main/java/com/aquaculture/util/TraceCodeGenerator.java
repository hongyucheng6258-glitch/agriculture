package com.aquaculture.util;

import java.util.UUID;

public class TraceCodeGenerator {
    public static String generate() {
        return "TRACE-" + UUID.randomUUID().toString();
    }
}
