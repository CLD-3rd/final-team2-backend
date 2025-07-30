package com.goteego.user.dto;


import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String nickname;
    private String email;

    public UserDto() {
    }

    public UserDto(Long id, String nickname, String email) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
    }
}

