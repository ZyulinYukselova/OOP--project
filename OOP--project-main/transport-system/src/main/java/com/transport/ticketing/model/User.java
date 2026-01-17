package com.transport.ticketing.model;

import com.transport.ticketing.util.IdGenerator;

public class User extends BaseEntity {
    private final String email;
    private String displayName;
    private Role role;
    private boolean active;

    public User(String email, String displayName, Role role) {
        super(IdGenerator.nextUserId());
        this.email = email;
        this.displayName = displayName;
        this.role = role;
        this.active = true;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }
}
