package com.artuhanau.ecobot.daos.models;

import java.util.List;
import java.util.Set;
import javax.persistence.*;

import com.artuhanau.ecobot.daos.models.enums.EducationStep;
import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "telegram_user")
public class User
{
    @Id
    @CsvBindByName
    private Long telegramId;

    @CsvBindByName
    private String name;

    @ManyToOne
    @CsvBindByName
    private City baseLocation;

    @CsvBindAndSplitByName(splitOn = "\\|", elementType = TrainingFormat.class)
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<TrainingFormat> completedTrainings;

    @CsvBindByName
    private EducationStep step;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date")
    private List<DialogCommandHistoryEntry> history;

    @OneToOne(cascade = CascadeType.ALL)
    private UserData userData;
}