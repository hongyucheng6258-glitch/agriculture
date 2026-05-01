package com.aquaculture.util;

import com.aquaculture.entity.SysUser;

import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {
    private static final ConcurrentHashMap<String, SysUser> TOKEN_MAP = new ConcurrentHashMap<>();

    public static void put(String token, SysUser user) {
        TOKEN_MAP.put(token, user);
    }

    public static SysUser get(String token) {
        return TOKEN_MAP.get(token);
    }

    public static void remove(String token) {
        TOKEN_MAP.remove(token);
    }

    public static boolean contains(String token) {
        return TOKEN_MAP.containsKey(token);
    }
}
