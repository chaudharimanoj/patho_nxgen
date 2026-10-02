package com.pathonxgen.modules.laboratory.dto;

import jakarta.validation.constraints.NotBlank;

public record SampleTransitionRequest(@NotBlank String actorRole, String note) {
}
