package com.artuhanau.ecobot.daos.models;

public interface SavingService
{
    void saveOrganisation(EducationalOrganisation organisation);

    void saveTrainingFormat(TrainingFormat trainingFormat);

    void saveCity(City city);
}
