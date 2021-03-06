package com.artuhanau.ecobot.daos.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.artuhanau.ecobot.daos.models.enums.EducationStep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
@Entity
public class UserData
{
    private String cityName;

    private String education;

    private EducationStep educationStep;

    private Integer hoursPerWeek;

    private Boolean paid;

    private String formatKeywords;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
}
