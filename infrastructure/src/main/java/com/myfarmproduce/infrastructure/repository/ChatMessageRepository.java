package com.myfarmproduce.infrastructure.repository;

import com.myfarmproduce.domain.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
