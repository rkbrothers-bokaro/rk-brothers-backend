package com.boltblazers.rkbrothers.core.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Users authenticate by phone number, not username — the "username"
     * parameter here (per the UserDetailsService contract) is the phone.
     */
    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        log.info("Loading user by phone: {}", phone);
        User user = userRepository.findByPhone(phone)
                .filter(User::isEnabled)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + phone));
        log.info("User found: {}", user.getRole());
        return new UserPrincipal(user);
    }
}
