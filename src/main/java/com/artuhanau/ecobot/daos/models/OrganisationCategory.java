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
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OrganisationCategory {
    @Id
    @CsvBindByName(column = "category_id")
    private Long id;
    @CsvBindByName(column = "category_name")
    private String name;
}
