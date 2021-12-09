package com.atruhanau.ecobot.services;

import com.atruhanau.ecobot.configs.SlotRequirementConfigs;
import com.atruhanau.ecobot.daos.models.User;
import com.atruhanau.ecobot.daos.models.UserRecommendation;
import com.atruhanau.ecobot.daos.models.slots.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class UserRecommendationService {
    @Resource
    private SlotRequirementConfigs configs;

    public UserRecommendation createRecommendation(User user) {
        UserRecommendation userRecommendation = new UserRecommendation();
        createSlots(userRecommendation);
        fillSlots(user, userRecommendation);
        return userRecommendation;
    }

    private void fillSlots(User user, UserRecommendation userRecommendation) {
        if (user != null) {
            if (user.getBaseLocation() != null) {
                ((CitySlot) userRecommendation.getSlots().get(0)).setName(user.getBaseLocation().getName());
            }
            if (user.getStep() != null) {
                ((EducationSlot) userRecommendation.getSlots()).setStep(user.getStep().name());
            }
        }
    }

    private void createSlots(UserRecommendation userRecommendation) {
        userRecommendation.getSlots().add(new CitySlot());
        userRecommendation.getSlots().get(0).setRequired(configs.isCityRequire());
        userRecommendation.getSlots().add(new EducationalRequirementSlot());
        userRecommendation.getSlots().get(1).setRequired(configs.isEducationalRequirementRequire());
        userRecommendation.getSlots().add(new EducationSlot());
        userRecommendation.getSlots().get(2).setRequired(configs.isEducationRequire());
        userRecommendation.getSlots().add(new OrganisationTypeSlot());
        userRecommendation.getSlots().get(3).setRequired(configs.isOrganisationTypeRequire());
        userRecommendation.getSlots().add(new PriceSlot());
        userRecommendation.getSlots().get(4).setRequired(configs.isPriceRequire());
    }

    public boolean isAllMandatoryFieldsFilled(UserRecommendation userRecommendation) {
        long countOfUnfilled = userRecommendation.getSlots().stream()
                .filter(Slot::isFilled)
                .filter(Slot::isRequired)
                .count();
        return countOfUnfilled == 0;
    }

    public Slot getRandomMandatorySlot(UserRecommendation userRecommendation) {
        List<Slot> notFilledAndMandatory = userRecommendation.getSlots().stream()
                .filter(Slot::isFilled)
                .filter(Slot::isRequired)
                .collect(Collectors.toList());
        Random rand = new Random();
        return notFilledAndMandatory.get(rand.nextInt(notFilledAndMandatory.size()));
    }
    
}
