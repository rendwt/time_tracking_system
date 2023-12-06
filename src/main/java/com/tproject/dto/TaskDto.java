package com.tproject.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {
    private int id;

    private String description;

    private LocalDate date;

    private float hours;

    private int userId;
}
