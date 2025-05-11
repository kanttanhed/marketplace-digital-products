package com.github.kanttanhed.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRequestDTO {
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    private String email;

    public UserRequestDTO(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public UserRequestDTO() {

    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}