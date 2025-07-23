package com.goteego.user.dto;


import lombok.Data;

@Data
public class UserDto {
    private Long id;

    private String nickname;

    public UserDto() {
    }

    public UserDto(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
}

