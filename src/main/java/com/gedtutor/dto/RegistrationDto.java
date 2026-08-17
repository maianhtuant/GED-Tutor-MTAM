package com.gedtutor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationDto {

    @NotBlank
    @Size(min = 3, max = 80)
    private String username;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 120)
    private String fullName;

    @NotBlank
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters.")
    private String password;

    @NotBlank
    private String confirmPassword;

    public RegistrationDto() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
