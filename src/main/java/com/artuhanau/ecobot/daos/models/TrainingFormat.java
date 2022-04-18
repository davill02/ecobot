package com.artuhanau.ecobot.daos.models;

import com.artuhanau.ecobot.daos.models.enums.ClassTime;
import com.artuhanau.ecobot.daos.models.enums.WeekTime;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvRecurse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TrainingFormat
{
    @Id
    @CsvBindByName(column = "training_format_id")
    private Long id;

    @CsvBindByName
    private Integer hoursPerWeek;

    @CsvBindByName
    private Integer boostValue;

    @CsvBindByName
    private Double price;

    @CsvBindByName
    private Boolean isOnlyHighEducation;

    @CsvBindByName
    private Boolean IsOnlyMiddleEducation;

    @CsvBindByName
    private Boolean isPaid;

    @CsvBindByName
    private String formatName;

    @CsvBindByName
    private String description;

    @CsvBindByName
    private String formatKeyWords;

    @CsvBindByName
    private String note;

    @CsvBindByName
    private String additionalInfoUrl;

    @ManyToOne
    @CsvRecurse
    private EducationalOrganisation organisation;
}
