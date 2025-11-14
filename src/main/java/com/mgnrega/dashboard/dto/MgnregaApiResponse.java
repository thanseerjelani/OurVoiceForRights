package com.mgnrega.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// External API Response (MGNREGA API)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MgnregaApiResponse {
    private String status;
    private List<MgnregaRecord> records;
    private Integer total;
}
