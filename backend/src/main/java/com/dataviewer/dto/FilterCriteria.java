package com.dataviewer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FilterCriteria {

    @NotBlank(message = "Filterveld mag niet leeg zijn")
    private String field;

    @NotBlank(message = "Filteroperator mag niet leeg zijn")
    @Pattern(
        regexp = "contains|startsWith|endsWith|equals|notEquals",
        message = "Ongeldige operator. Toegestaan: contains, startsWith, endsWith, equals, notEquals"
    )
    private String operator;

    @NotBlank(message = "Filterwaarde mag niet leeg zijn")
    private String value;
}
