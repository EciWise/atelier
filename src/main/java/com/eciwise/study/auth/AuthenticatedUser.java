package com.eciwise.study.auth;

/**
 * Datos del usuario autenticado extraidos del JWT. Se usa como principal en el
 * SecurityContext para permitir la auto-provision de usuarios (ver CurrentUserService).
 */
public record AuthenticatedUser(
        String externalId,
        String email,
        String firstName,
        String lastName,
        String role
) {
}
