package com.chernyshev.messenger.users.repositories;

import com.chernyshev.messenger.users.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity>  findById(Long id);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailConfirmationToken(String emailConfirmationToken);
    @Query("select u from UserEntity u WHERE u.username=:username and u.isActive=false")
    Optional<UserEntity> findDeactivatedByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
