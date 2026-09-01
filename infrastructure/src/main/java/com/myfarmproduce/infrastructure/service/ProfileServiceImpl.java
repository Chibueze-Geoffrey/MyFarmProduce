package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.ProfileService;
import com.myfarmproduce.domain.entity.Admin;
import com.myfarmproduce.domain.entity.Customer;
import com.myfarmproduce.domain.entity.ProfileChangeRequest;
import com.myfarmproduce.domain.enums.ChangeRequestStatus;
import com.myfarmproduce.infrastructure.repository.AdminRepository;
import com.myfarmproduce.infrastructure.repository.CustomerRepository;
import com.myfarmproduce.infrastructure.repository.ProfileChangeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final CustomerRepository customers;
    private final AdminRepository admins;
    private final ProfileChangeRequestRepository changeRequests;

    public ProfileServiceImpl(CustomerRepository customers, AdminRepository admins,
                               ProfileChangeRequestRepository changeRequests) {
        this.customers = customers;
        this.admins = admins;
        this.changeRequests = changeRequests;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> getCustomer(Integer customerId) {
        return customers.findById(customerId);
    }

    @Override
    public void updateCustomerProfile(Integer customerId, String name, String photoUrl) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new IllegalStateException("Customer not found."));
        customer.setName(name.trim());
        if (photoUrl != null) customer.setPhotoUrl(photoUrl);
    }

    @Override
    public ProfileChangeRequest requestFieldChange(Integer customerId, String field, String requestedValue) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new IllegalStateException("Customer not found."));

        String current = "Email".equalsIgnoreCase(field) ? customer.getEmail() : customer.getPhone();

        ProfileChangeRequest request = new ProfileChangeRequest();
        request.setCustomer(customer);
        request.setField(field);
        request.setCurrentValue(current);
        request.setRequestedValue(requestedValue.trim());
        request.setStatus(ChangeRequestStatus.Pending);
        return changeRequests.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileChangeRequest> getMyChangeRequests(Integer customerId) {
        return changeRequests.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Admin> getAdmin(Integer adminId) {
        return admins.findById(adminId);
    }

    @Override
    public void updateAdminProfile(Integer adminId, String name, String photoUrl) {
        Admin admin = admins.findById(adminId)
                .orElseThrow(() -> new IllegalStateException("Admin not found."));
        admin.setName(name.trim());
        if (photoUrl != null) admin.setPhotoUrl(photoUrl);
    }
}
