package com.capstone.safeGuard.apis.member.presentation.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class LocationDto {

    @JsonProperty("lat_locate")
    @NotNull
    private Double latLocate;

    @JsonProperty("long_locate")
    @NotNull
    private Double longLocate;
}
