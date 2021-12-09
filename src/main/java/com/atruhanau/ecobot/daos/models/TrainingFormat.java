package com.atruhanau.ecobot.daos.models;

import com.atruhanau.ecobot.daos.models.enums.ClassTime;
import com.atruhanau.ecobot.daos.models.enums.WeekTime;
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
public class TrainingFormat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean isFree;
    private Double price;
    private Boolean isRequiredSpecialEducation;
    @ManyToMany
    private List<Skill> skills;
    private ClassTime classTime;
    private WeekTime weekTime;
}
