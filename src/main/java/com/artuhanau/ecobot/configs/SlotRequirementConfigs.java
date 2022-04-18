package com.artuhanau.ecobot.configs;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class SlotRequirementConfigs {
    private boolean isCityRequire;
    private boolean isOrganisationTypeRequire;
    private boolean isPriceRequire;
    private boolean isEducationRequire;
    private boolean isEducationalRequirementRequire;
}
