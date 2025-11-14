package com.mgnrega.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// District Response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictResponse {
    private Long id;
    private String name;
    private String districtCode;
    private Long stateId;
    private String stateName;
}
