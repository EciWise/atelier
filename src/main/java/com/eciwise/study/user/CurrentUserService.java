package com.eciwise.study.user;

import com.eciwise.study.auth.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resuelve el usuario actual a partir del JWT y lo persiste si aun no existe
 * (auto-provision just-in-time). Debe llamarse al inicio de cualquier accion.
 */
@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUser getOrCreate() {
        AuthenticatedUser principal = currentPrincipal();
        return appUserRepository.findByExternalId(principal.externalId())
                .map(existing -> syncSnapshot(existing, principal))
                .orElseGet(() -> appUserRepository.save(AppUser.builder()
                        .externalId(principal.externalId())
                        .email(principal.email())
                        .firstName(principal.firstName())
                        .lastName(principal.lastName())
                        .role(principal.role())
                        .build()));
    }

    private AppUser syncSnapshot(AppUser existing, AuthenticatedUser principal) {
        boolean changed = false;
        if (!equalsNullable(existing.getEmail(), principal.email())) {
            existing.setEmail(principal.email());
            changed = true;
        }
        if (!equalsNullable(existing.getFirstName(), principal.firstName())) {
            existing.setFirstName(principal.firstName());
            changed = true;
        }
        if (!equalsNullable(existing.getLastName(), principal.lastName())) {
            existing.setLastName(principal.lastName());
            changed = true;
        }
        if (!equalsNullable(existing.getRole(), principal.role())) {
            existing.setRole(principal.role());
            changed = true;
        }
        return changed ? appUserRepository.save(existing) : existing;
    }

    private AuthenticatedUser currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new IllegalStateException("No hay un usuario autenticado en el contexto");
        }
        return principal;
    }

    private boolean equalsNullable(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
