package com.example.demo.domain;

import com.example.demo.entity.User;
import com.example.demo.exception.ApiException;
import com.example.demo.security.UserPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ApiAuthentication extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;
    private final User user;
    private final String email;

    private ApiAuthentication(String email, String password) {
        super(null);
        this.principal = email;
        this.credentials = password;
        this.user = null;
        this.email = email;
        super.setAuthenticated(false);
    }

    private ApiAuthentication(UserPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        this.user = principal.getUser();
        this.email = principal.getUsername();
        super.setAuthenticated(true);
    }

    public static ApiAuthentication unauthenticated(String email, String password) {
        return new ApiAuthentication(email, password);
    }

    public static ApiAuthentication authenticated(UserPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
        return new ApiAuthentication(principal, authorities);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public User getUser() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return credentials == null ? null : credentials.toString();
    }

    @Override
    public void eraseCredentials() {
        this.credentials = null;
        super.eraseCredentials();
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new ApiException("Use authenticated() factory to set an authenticated token");
        }
        super.setAuthenticated(false);
    }
}
