package com.artuhanau.ecobot.daos.models;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvRecurse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.net.URL;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EducationalOrganisation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @CsvBindByName(column = "organisation_id")
    private Long id;
    @CsvBindByName(column = "organisation_name")
    private String name;
    @ManyToOne
    @CsvRecurse
    private City city;
    @ManyToOne
    @CsvRecurse
    private OrganisationCategory category;
    @OneToMany
    private List<TrainingFormat> formats;
    @CsvBindByName
    private URL site;
}
