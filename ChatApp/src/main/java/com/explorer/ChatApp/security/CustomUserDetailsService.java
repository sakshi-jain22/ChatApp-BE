package com.explorer.ChatApp.security;

import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.exception.ResourceNotFoundException;
import com.explorer.ChatApp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Loads a user using their email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws ResourceNotFoundException {
        String normalizedEmail = email
                .trim()
                .toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return new CustomUserDetails(user);
    }

    public static class CustomUserDetails implements UserDetails {
        private final Long userId;
        private final String name;
        private final String email;
        private final String passwordHash;

        public CustomUserDetails(User user) {
            this.userId = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.passwordHash = user.getPasswordHash();
        }

        public Long getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public String getPassword() {
            return passwordHash;
        }

        @Override
        public String getUsername() {
            return email;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
