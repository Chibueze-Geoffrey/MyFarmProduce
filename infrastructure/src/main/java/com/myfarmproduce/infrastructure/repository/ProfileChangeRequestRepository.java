package com.myfarmproduce.infrastructure.repository;

import com.myfarmproduce.domain.entity.ProfileChangeRequest;
import com.myfarmproduce.domain.enums.ChangeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileChangeRequestRepository extends JpaRepository<ProfileChangeRequest, Integer> {
    List<ProfileChangeRequest> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    List<ProfileChangeRequest> findAllByOrderByCreatedAtDesc();

    List<ProfileChangeRequest> findByStatusOrderByCreatedAtDesc(ChangeRequestStatus status);
}
