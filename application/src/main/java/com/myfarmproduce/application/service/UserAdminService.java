package com.myfarmproduce.application.service;

import com.myfarmproduce.domain.entity.Customer;
import com.myfarmproduce.domain.entity.ProfileChangeRequest;

import java.util.List;
import java.util.Optional;

public interface UserAdminService {
    List<Customer> getCustomers(String search);

    Optional<Customer> getCustomer(Integer id);

    Optional<Customer> createCustomer(String name, String email, String phone);

    void updateCustomer(Integer id, String name, String email, String phone);

    void deleteCustomer(Integer id);

    List<ProfileChangeRequest> getChangeRequests(boolean pendingOnly);

    void approveChangeRequest(Integer requestId, String note);

    void rejectChangeRequest(Integer requestId, String note);
}
