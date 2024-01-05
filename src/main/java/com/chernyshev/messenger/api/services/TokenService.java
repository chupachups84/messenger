package com.chernyshev.messenger.api.services;

import com.chernyshev.messenger.api.dtos.TokenDto;
import com.chernyshev.messenger.store.models.TokenEntity;
import com.chernyshev.messenger.store.models.enums.TokenType;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TokenService {
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    public void saveUserToken(UserEntity user, String jwtToken) {
        var token = TokenEntity.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.saveAndFlush(token);
    }
    public void revokeAllUserToken(UserEntity user){
        tokenRepository.findAllValidTokensByUser(user.getId())
                .filter(tokens->!tokens.isEmpty())
                    .ifPresent(
                        tokenList->tokenList.forEach(
                                t->{
                                    t.setExpired(true);
                                    t.setRevoked(true);
                                }
                        )
                );
    }


    public TokenDto getTokenDto(UserEntity user) {
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserToken(user);
        saveUserToken(user,jwtToken);
        return TokenDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
}
