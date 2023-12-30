package com.chernyshev.messenger.repositories;

import com.chernyshev.messenger.models.MessageEntity;
import com.chernyshev.messenger.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity,Long> {
    @Query("""
            SELECT m from MessageEntity m WHERE 
            m.receiver=:receiver and m.sender=:sender
            or 
            m.receiver=:sender and m.sender=:receiver
            order by m.sentAt asc 
            """)
    List<MessageEntity> findBySenderAndReceiver(UserEntity sender, UserEntity receiver);
}
