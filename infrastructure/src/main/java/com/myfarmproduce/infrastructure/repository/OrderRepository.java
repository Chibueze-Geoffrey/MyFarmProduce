package com.myfarmproduce.infrastructure.repository;

import com.myfarmproduce.domain.entity.Order;
import com.myfarmproduce.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    Optional<Order> findByIdAndCustomerId(Integer id, Integer customerId);

    Optional<Order> findFirstByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    @Query("""
            select o from Order o
            where (:status is null or o.status = :status)
              and (:from is null or o.createdAt >= :from)
              and (:to is null or o.createdAt < :to)
            order by o.createdAt desc
            """)
    List<Order> search(@Param("status") OrderStatus status, @Param("from") Instant from, @Param("to") Instant to);
}
