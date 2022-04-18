package com.artuhanau.ecobot.daos.models;

import com.artuhanau.ecobot.daos.models.enums.EducationStep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class UserData
{
    private String cityName;

    private String education;

    private EducationStep educationStep;

    private Integer hoursPerWeek;

    private Boolean paid;

    private String formatKeywords;
}
