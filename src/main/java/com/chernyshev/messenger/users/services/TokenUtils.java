package com.chernyshev.messenger.users.services;

import com.chernyshev.messenger.users.models.TokenEntity;
import com.chernyshev.messenger.users.models.UserEntity;
import com.chernyshev.messenger.users.repositories.TokenRepository;
import com.chernyshev.messenger.users.models.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TokenUtils {
    private final TokenRepository tokenRepository;
    public void saveUserToken(UserEntity user, String jwtToken) {
        var token = TokenEntity.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
    public void revokeAllUserToken(UserEntity user){
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if(validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(t->{
                    t.setExpired(true);
                    t.setRevoked(true);
                }
        );
    }
}
