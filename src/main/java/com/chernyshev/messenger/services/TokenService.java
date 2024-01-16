package com.chernyshev.messenger.services;

import com.chernyshev.messenger.dtos.RecoverTokenDto;
import com.chernyshev.messenger.dtos.TokenDto;
import com.chernyshev.messenger.models.TokenEntity;
import com.chernyshev.messenger.models.UserEntity;
import com.chernyshev.messenger.repositories.TokenRepository;
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
    public RecoverTokenDto getRecoverTokenDto(UserEntity user){
        String recoverToken = jwtService.generateRecoverToken(user);
        revokeAllUserToken(user);
        return RecoverTokenDto.builder().recoverToken(recoverToken).build();
    }
}
