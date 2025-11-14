package com.mgnrega.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeocodingResponse {
    private String district;
    private String state;
    private Double lat;
    private Double lon;
    private Address address;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String county;

        @JsonProperty("state_district")
        private String stateDistrict;

        private String city;
        private String town;
        private String village;
        private String state;
        private String country;

        @JsonProperty("country_code")
        private String countryCode;

        private String suburb;
        private String municipality;
    }
}