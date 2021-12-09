package com.atruhanau.ecobot.daos.models;

import com.atruhanau.ecobot.daos.models.enums.EducationStep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "telegram_user")
public class User {
    @Id
    private Long telegramId;
    private String name;
    @ManyToOne
    private City baseLocation;
    @ManyToMany
    private List<TrainingFormat> completedTrainings;
    private EducationStep step;
}
