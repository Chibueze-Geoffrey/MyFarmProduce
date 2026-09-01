package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.PasswordHasher;
import com.myfarmproduce.application.service.UserAdminService;
import com.myfarmproduce.common.AppConstants;
import com.myfarmproduce.domain.entity.Customer;
import com.myfarmproduce.domain.entity.ProfileChangeRequest;
import com.myfarmproduce.domain.enums.ChangeRequestStatus;
import com.myfarmproduce.infrastructure.repository.CustomerRepository;
import com.myfarmproduce.infrastructure.repository.ProfileChangeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserAdminServiceImpl implements UserAdminService {

    private final CustomerRepository customers;
    private final ProfileChangeRequestRepository changeRequests;
    private final PasswordHasher hasher;

    public UserAdminServiceImpl(CustomerRepository customers, ProfileChangeRequestRepository changeRequests,
                                 PasswordHasher hasher) {
        this.customers = customers;
        this.changeRequests = changeRequests;
        this.hasher = hasher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getCustomers(String search) {
        return StringUtils.hasText(search)
                ? customers.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNameAsc(search.trim(), search.trim())
                : customers.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> getCustomer(Integer id) {
        return customers.findById(id);
    }

    @Override
    public Optional<Customer> createCustomer(String name, String email, String phone) {
        String normalizedEmail = email.trim().toLowerCase();
        if (customers.existsByEmail(normalizedEmail)) return Optional.empty();

        Customer customer = new Customer();
        customer.setName(name.trim());
        customer.setEmail(normalizedEmail);
        customer.setPhone(phone.trim());
        customer.setPasswordHash(hasher.hash(AppConstants.DEFAULT_USER_PASSWORD));
        customer.setMustChangePassword(true);
        customers.save(customer);
        return Optional.of(customer);
    }

    @Override
    public void updateCustomer(Integer id, String name, String email, String phone) {
        Customer customer = customers.findById(id)
                .orElseThrow(() -> new IllegalStateException("Customer not found."));
        customer.setName(name.trim());
        customer.setEmail(email.trim().toLowerCase());
        customer.setPhone(phone.trim());
    }

    @Override
    public void deleteCustomer(Integer id) {
        customers.findById(id).ifPresent(customers::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileChangeRequest> getChangeRequests(boolean pendingOnly) {
        return pendingOnly
                ? changeRequests.findByStatusOrderByCreatedAtDesc(ChangeRequestStatus.Pending)
                : changeRequests.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public void approveChangeRequest(Integer requestId, String note) {
        ProfileChangeRequest request = changeRequests.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Request not found."));

        if (request.getStatus() == ChangeRequestStatus.Pending && request.getCustomer() != null) {
            if ("Email".equalsIgnoreCase(request.getField()))
                request.getCustomer().setEmail(request.getRequestedValue().toLowerCase());
            else
                request.getCustomer().setPhone(request.getRequestedValue());
        }

        request.setStatus(ChangeRequestStatus.Approved);
        request.setResolvedAt(Instant.now());
        request.setAdminNote(note);
    }

    @Override
    public void rejectChangeRequest(Integer requestId, String note) {
        ProfileChangeRequest request = changeRequests.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Request not found."));
        request.setStatus(ChangeRequestStatus.Rejected);
        request.setResolvedAt(Instant.now());
        request.setAdminNote(note);
    }
}
