package com.mgnrega.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MgnregaRecord {

    @JsonProperty("fin_year")
    private String fin_year;

    @JsonProperty("month")
    private String month;

    @JsonProperty("state_code")
    private String state_code;

    @JsonProperty("state_name")
    private String state_name;

    @JsonProperty("district_code")
    private String district_code;

    @JsonProperty("district_name")
    private String district_name;

    // Use Object to handle both numbers and strings from API
    @JsonProperty("Total_Households_Worked")
    private Object Total_Households_Worked;

    @JsonProperty("Average_days_of_employment_provided_per_Household")
    private Object Average_days_of_employment_provided_per_Household;

    @JsonProperty("Wages")
    private Object Wages;

    @JsonProperty("Number_of_Ongoing_Works")
    private Object Number_of_Ongoing_Works;

    @JsonProperty("Number_of_Completed_Works")
    private Object Number_of_Completed_Works;

    @JsonProperty("Total_Exp")
    private Object Total_Exp;

    @JsonProperty("Average_Wage_rate_per_day_per_person")
    private Object Average_Wage_rate_per_day_per_person;
}