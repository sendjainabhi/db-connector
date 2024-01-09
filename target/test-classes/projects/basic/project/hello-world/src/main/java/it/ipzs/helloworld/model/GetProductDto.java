package it.ipzs.helloworld.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetProductDto {

    private int id;
    private String name;
}

