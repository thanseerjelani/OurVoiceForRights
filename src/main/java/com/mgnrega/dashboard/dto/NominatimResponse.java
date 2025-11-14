package com.mgnrega.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NominatimResponse {

    @JsonProperty("place_id")
    private Long placeId;

    private String licence;

    @JsonProperty("osm_type")
    private String osmType;

    @JsonProperty("osm_id")
    private Long osmId;

    private String lat;
    private String lon;

    @JsonProperty("display_name")
    private String displayName;

    private GeocodingResponse.Address address;

    private List<String> boundingbox;

    @JsonProperty("class")
    private String classification;

    private String type;
    private Double importance;
}