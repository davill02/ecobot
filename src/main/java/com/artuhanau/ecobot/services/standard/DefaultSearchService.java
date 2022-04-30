package com.artuhanau.ecobot.services.standard;

import java.util.List;
import javax.annotation.Resource;

import com.artuhanau.ecobot.daos.models.DialogCommand;
import com.artuhanau.ecobot.daos.models.TrainingFormat;
import com.artuhanau.ecobot.daos.models.UserData;
import com.artuhanau.ecobot.daos.models.enums.EducationStep;
import com.artuhanau.ecobot.daos.models.repos.CityRepository;
import com.artuhanau.ecobot.daos.models.repos.EducationalOrganisationRepository;
import com.artuhanau.ecobot.daos.models.repos.TrainingFormatJdbcRepository;
import com.artuhanau.ecobot.daos.models.repos.TrainingFormatRepository;
import com.artuhanau.ecobot.services.SearchService;
import org.springframework.stereotype.Component;

@Component
public class DefaultSearchService implements SearchService
{
    @Resource
    private TrainingFormatJdbcRepository trainingFormatJdbcRepository;

    @Resource
    private CityRepository cityRepository;

    @Resource
    private TrainingFormatRepository trainingFormatRepository;

    @Resource
    private EducationalOrganisationRepository educationalOrganisationRepository;

    @Override
    public List<TrainingFormat> search(final UserData userData)
    {
        prepareData(userData);
        List<Long> ids = trainingFormatJdbcRepository.relevantSearch(userData);
        return trainingFormatRepository.findAllById(ids);
    }

    @Override
    public boolean isEligibleForSearch(final UserData userData, List<DialogCommand> history)
    {
        boolean cityEligible = userData.getCityName() != null && countOfCommandInHistory(history, "LOCATION") > 3;
        boolean educationEligible = userData.getEducationStep() != null;
        boolean paidEligible = userData.getPaid() != null && countOfCommandInHistory(history, "COST") > 2;
        boolean otherEligible = userData.getHoursPerWeek() != null || countOfCommandInHistory(history,"TIME") > 0;
        return paidEligible && cityEligible && educationEligible && otherEligible;
    }

    private Long countOfCommandInHistory(List<DialogCommand> commands, String commandName){
        return commands.stream().map(DialogCommand::getCommand)
            .filter(command -> command.equals(commandName))
            .count();
    }

    private void prepareData(final UserData userData)
    {
        if (userData.getCityName() != null) {
            findSimilarCity(userData);
        }
        if (userData.getEducation() != null) {
            mapEducationToEducationStep(userData);
        }
    }

    private void mapEducationToEducationStep(final UserData userData)
    {
        userData.setEducationStep(EducationStep.OTHER);
    }

    private void findSimilarCity(UserData userData)
    {
        userData.setCityName(cityRepository.findMostSimilarCity(userData.getCityName()).getNameEnglish());
    }
}
