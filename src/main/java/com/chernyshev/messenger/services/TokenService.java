package com.chernyshev.messenger.services;

import com.chernyshev.messenger.dtos.TokenDto;
import com.chernyshev.messenger.models.TokenEntity;
import com.chernyshev.messenger.models.TokenType;
import com.chernyshev.messenger.models.UserEntity;
import com.chernyshev.messenger.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TokenService {
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

    public TokenDto makeTokenDto(String accessToken, String refreshToken){
        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
