package com.myfarmproduce.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminUserForm {

    private Integer id;

    @NotBlank @Size(max = 150)
    private String name = "";

    @NotBlank @Email @Size(max = 256)
    private String email = "";

    @NotBlank @Size(max = 30)
    private String phone = "";

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
