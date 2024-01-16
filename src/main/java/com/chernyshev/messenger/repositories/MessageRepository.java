package com.chernyshev.messenger.repositories;

import com.chernyshev.messenger.models.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity,Long> {
    @Query("""
            from MessageEntity m
            join fetch m.sender
            join fetch m.receiver
            WHERE
            m.receiver.username=:username2 and m.sender.username=:username1
            or
            m.receiver.username=:username1 and m.sender.username=:username2
            order by m.sentAt asc
            """)
    List<MessageEntity> findBySenderAndReceiver(String username1, String username2);
}
