package com.myfarmproduce.common;

import java.math.BigDecimal;

public final class AppConstants {
    private AppConstants() {}

    /** Flat delivery fee applied at checkout (MVP - no zone logic yet). */
    public static final BigDecimal FLAT_DELIVERY_FEE = new BigDecimal("1500.00");

    public static final class Roles {
        private Roles() {}
        public static final String ADMIN = "ADMIN";
        public static final String CUSTOMER = "CUSTOMER";
    }

    /** Default password assigned to admin-created users (must be changed on first login). */
    public static final String DEFAULT_USER_PASSWORD = "Password@1234";

    public static final class UploadFolders {
        private UploadFolders() {}
        public static final String PRODUCTS = "products";
        public static final String AVATARS = "avatars";
    }
}
