package com.eciwise.study.user;

/**
 * Comprobaciones de rol reutilizables sobre {@link AppUser}. Los valores coinciden
 * con los claims del JWT ("estudiante", "tutor", "admin"), igual que en SecurityConfig.
 */
public final class Roles {

    public static final String ESTUDIANTE = "estudiante";
    public static final String TUTOR = "tutor";
    public static final String ADMIN = "admin";

    private Roles() {
    }

    public static boolean isAdmin(AppUser user) {
        return ADMIN.equals(user.getRole());
    }

    public static boolean isTutor(AppUser user) {
        return TUTOR.equals(user.getRole());
    }

    public static boolean isAdminOrTutor(AppUser user) {
        return isAdmin(user) || isTutor(user);
    }
}
