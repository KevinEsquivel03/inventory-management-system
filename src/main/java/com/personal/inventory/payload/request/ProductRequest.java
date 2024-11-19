package com.personal.inventory.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Min(value = 0, message = "Quantity must be 0 or greater")
    private int quantity;
}
