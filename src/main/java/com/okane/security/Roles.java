package com.okane.security;

public final class Roles {
    public static final String ADMIN   = "hasRole('ADMIN')";
    public static final String MANAGER = "hasRole('MANAGER')";
    public static final String AGENT   = "hasRole('AGENT')";
    public static final String CLIENT  = "hasRole('CLIENT')";

    public static final String ADMIN_OR_MANAGER = "hasAnyRole('ADMIN','MANAGER')";
    public static final String ADMIN_OR_AGENT   = "hasAnyRole('ADMIN','AGENT')";
    public static final String ALL_STAFF        = "hasAnyRole('ADMIN','MANAGER','AGENT')";
    public static final String ALL              = "hasAnyRole('ADMIN','MANAGER','AGENT','CLIENT')";

    private Roles() {}
}