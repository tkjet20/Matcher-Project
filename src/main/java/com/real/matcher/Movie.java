package com.real.matcher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class Movie {

    @NonNull
    private Integer id;

    @NonNull
    private String title;

    @NonNull
    private Integer year;
}
