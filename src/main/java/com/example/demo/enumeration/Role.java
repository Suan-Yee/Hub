package com.example.demo.enumeration;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    USER(EnumSet.of(
        Permission.POST_CREATE,
        Permission.POST_UPDATE,
        Permission.POST_DELETE,
        Permission.COMMENT_CREATE,
        Permission.MESSAGE_SEND
    )),
    ADMIN(EnumSet.of(
        Permission.POST_CREATE,
        Permission.POST_UPDATE,
        Permission.POST_DELETE,
        Permission.COMMENT_CREATE,
        Permission.MESSAGE_SEND,
        Permission.GROUP_MANAGE
    )),
    SYS_ADMIN(EnumSet.copyOf(Arrays.asList(Permission.values())));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public Set<String> asAuthorityStrings() {
        Set<String> authorities = permissions.stream()
            .map(permission -> permission.name())
            .collect(Collectors.toSet());
        authorities.add("ROLE_" + name());
        return authorities;
    }
}
