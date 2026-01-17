package com.transport.ticketing.service;

import com.transport.ticketing.exception.AccessDeniedException;
import com.transport.ticketing.model.Role;
import com.transport.ticketing.model.User;

import java.util.Arrays;
import java.util.List;

final class SecurityGuard {
    private SecurityGuard() {}

    static void requireRole(User actor, Role... allowed) {
        if (actor == null) {
            throw new AccessDeniedException("Missing actor");
        }
        if (!actor.isActive()) {
            throw new AccessDeniedException("Actor is inactive");
        }
        List<Role> roles = Arrays.asList(allowed);
        if (!roles.contains(actor.getRole())) {
            throw new AccessDeniedException("Role " + actor.getRole() + " not permitted");
        }
    }
}
