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


    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa");
        }

        return new CustomUserDetails(user);
    }

}
