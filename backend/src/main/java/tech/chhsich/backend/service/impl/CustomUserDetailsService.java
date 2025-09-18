package tech.chhsich.backend.service.impl;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tech.chhsich.backend.mapper.AdminMapper;
import tech.chhsich.backend.entity.Administrator;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation that uses the database for authentication.
 *
 * This service loads user details from the administrators table and converts them
 * to Spring Security UserDetails objects for authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminMapper adminMapper;

    /**
     * Creates a new CustomUserDetailsService with the required AdminMapper.
     *
     * @param adminMapper the mapper for accessing administrator data
     */
    public CustomUserDetailsService(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    /**
     * Loads user details by username from the database.
     *
     * @param username the username to search for
     * @return UserDetails object containing the user's information and authorities
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Administrator admin = adminMapper.findByUsername(username);

        if (admin == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // Determine role based on the role field
        String role = getRoleString(admin.getRole());

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword()) // This should be the BCrypt encoded password
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Converts the numeric role to Spring Security role string.
     *
     * @param role the numeric role from the database
     * @return the Spring Security role string (e.g., "ROLE_ADMIN")
     */
    private String getRoleString(Integer role) {
        if (role == null) {
            return "ROLE_USER";
        }

        return switch (role) {
            case 1 -> "ROLE_ADMIN";
            case 2 -> "ROLE_WAITER";
            default -> "ROLE_USER";
        };
    }
}