package com.goteego.user.dto;


import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;

    public UserDto() {
    }

    public UserDto(Long id, String email) {
        this.id = id;
        this.email = email;
    }
}

