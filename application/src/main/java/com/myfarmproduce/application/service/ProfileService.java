package com.myfarmproduce.application.service;

import com.myfarmproduce.domain.entity.Admin;
import com.myfarmproduce.domain.entity.Customer;
import com.myfarmproduce.domain.entity.ProfileChangeRequest;

import java.util.List;
import java.util.Optional;

public interface ProfileService {
    Optional<Customer> getCustomer(Integer customerId);

    void updateCustomerProfile(Integer customerId, String name, String photoUrl);

    ProfileChangeRequest requestFieldChange(Integer customerId, String field, String requestedValue);

    List<ProfileChangeRequest> getMyChangeRequests(Integer customerId);

    Optional<Admin> getAdmin(Integer adminId);

    void updateAdminProfile(Integer adminId, String name, String photoUrl);
}
