package com.aquaculture.common;

/**
 * System constants
 */
public final class Constants {

    private Constants() {
    }

    /**
     * Default page size
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Maximum page size
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Token header name
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token prefix
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Default deleted flag value (not deleted)
     */
    public static final int NOT_DELETED = 0;

    /**
     * Deleted flag value
     */
    public static final int DELETED = 1;

    /**
     * User session key prefix in Redis (for future use)
     */
    public static final String USER_TOKEN_PREFIX = "user:token:";

}
