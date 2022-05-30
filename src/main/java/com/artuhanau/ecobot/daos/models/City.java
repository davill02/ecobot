package com.artuhanau.ecobot.daos.models;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @CsvBindByName(column = "city_id")
    private Integer id;
    @CsvBindByName(column = "city_name")
    private String name;
    @CsvBindByName(column = "city_name_en")
    private String nameEnglish;
    @CsvBindByName
    private String nameTranslitEn;
}
