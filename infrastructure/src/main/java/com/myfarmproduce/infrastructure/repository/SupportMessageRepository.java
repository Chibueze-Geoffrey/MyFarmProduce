package com.myfarmproduce.infrastructure.repository;

import com.myfarmproduce.domain.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Integer> {
}
