package com.myfarmproduce.infrastructure.repository;

import com.myfarmproduce.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByReference(String reference);
}
