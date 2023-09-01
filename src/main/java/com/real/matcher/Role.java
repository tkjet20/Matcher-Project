package com.real.matcher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class Role {

    @NonNull
    private Integer movieId;

    private String name;

    private String role;
}
