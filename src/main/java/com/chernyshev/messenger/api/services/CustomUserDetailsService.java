package com.chernyshev.messenger.api.services;

import com.chernyshev.messenger.api.exceptions.custom.InvalidUsernameOrPasswordException;
import com.chernyshev.messenger.api.exceptions.custom.UserNotFoundException;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username){
        var user =userRepository.findByUsername(username).filter(UserEntity::isActive)
                .orElseThrow(()-> new InvalidUsernameOrPasswordException("Неверный логин или пароль"));
        return new User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

}

