package com.mgnrega.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// State Response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateResponse {
    private Long id;
    private String name;
    private String stateCode;
}

