package com.retailmanagement.security;

import com.retailmanagement.entity.User;
import com.retailmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Role hợp lệ: ADMIN, SALES, INVENTORY, CUSTOMER
     * Spring Security authority sẽ là: ROLE_ADMIN, ROLE_SALES, ROLE_INVENTORY, ROLE_CUSTOMER
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
