package com.tproject.dto;

import lombok.*;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor

public class UserDto {

    private int id;
    private String username;
    private String password;
}
