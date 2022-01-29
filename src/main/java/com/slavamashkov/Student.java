package com.slavamashkov;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Table(title = "students")
public class Student {
    @Column
    private String name;

    @Column
    private int score;
}
