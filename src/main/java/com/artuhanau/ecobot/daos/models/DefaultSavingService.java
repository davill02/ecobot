package com.artuhanau.ecobot.daos.models;

import java.util.Optional;
import javax.annotation.Resource;

import com.artuhanau.ecobot.daos.models.repos.AddressRepository;
import com.artuhanau.ecobot.daos.models.repos.CityRepository;
import com.artuhanau.ecobot.daos.models.repos.EducationalOrganisationRepository;
import com.artuhanau.ecobot.daos.models.repos.OrganisationCategoryRepository;
import com.artuhanau.ecobot.daos.models.repos.TrainingFormatRepository;
import com.artuhanau.ecobot.yandex.translate.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultSavingService implements SavingService
{
    private final static Logger LOG = LoggerFactory.getLogger(DefaultSavingService.class);

    @Resource
    private TranslationService translationService;

    @Resource
    private CityRepository cityRepository;

    @Resource
    private OrganisationCategoryRepository organisationCategoryRepository;

    @Resource
    private EducationalOrganisationRepository educationalOrganisationRepository;

    @Resource
    private TrainingFormatRepository trainingFormatRepository;

    @Resource
    private AddressRepository addressRepository;

    @Override
    public void saveOrganisation(final EducationalOrganisation organisation)
    {
        educationalOrganisationRepository.findEducationalOrganisationByName(organisation.getName()).ifPresent(foundOrganisation -> {
                organisation.setId(foundOrganisation.getId());
            }
        );
        if (organisation.getCity() != null && organisation.getCity().getName() != null) {
            saveCity(organisation.getCity());
        }
        else {
            organisation.setCity(null);
        }
        if (organisation.getCategory() != null && organisation.getCategory().getName() != null) {
            saveOrganisationCategory(organisation.getCategory());
        }
        else {
            organisation.setCategory(null);
        }
        educationalOrganisationRepository.save(organisation);
        if (organisation.getId() == null) {
            educationalOrganisationRepository.flush();
        }
    }

    private void saveOrganisationCategory(final OrganisationCategory organisationCategory)
    {
        if (organisationCategory.getName() != null) {
            Optional<OrganisationCategory> optionalOrganisationCategory = organisationCategoryRepository.findOrganisationCategoriesByName(
                organisationCategory.getName());
            optionalOrganisationCategory.ifPresent(found -> organisationCategory.setId(found.getId()));
            if (!optionalOrganisationCategory.isPresent()) {
                organisationCategoryRepository.save(organisationCategory);
                organisationCategoryRepository.flush();
            }
        }
    }

    @Override
    public void saveTrainingFormat(final TrainingFormat trainingFormat)
    {
        try {
            saveOrganisation(trainingFormat.getOrganisation());

            if (trainingFormat.getAddress() != null && trainingFormat.getAddress().getAddress() != null) {
                Optional<Address> optionalAddress = addressRepository.findAddressByAddress(trainingFormat.getAddress().getAddress());
                optionalAddress.ifPresent(trainingFormat::setAddress);
                if (!optionalAddress.isPresent()) {
                    addressRepository.save(trainingFormat.getAddress());
                    addressRepository.flush();
                }
            }
            Optional<TrainingFormat> formatOptional = trainingFormatRepository.findTrainingFormatByOrganisationAndAddressAndFormatName(
                trainingFormat.getOrganisation(),
                trainingFormat.getAddress(), trainingFormat.getFormatName());
            if (formatOptional.isPresent()) {
                trainingFormat.setId(formatOptional.get().getId());
                trainingFormatRepository.save(trainingFormat);
            }
            else {
                trainingFormatRepository.saveAndFlush(trainingFormat);
            }
        }
        catch (Exception e) {
            LOG.error("Training: {}, Organisation: {}, Category: {}, City: {}", trainingFormat.getFormatName(),
                trainingFormat.getOrganisation().getName(), trainingFormat.getOrganisation().getCategory(),
                trainingFormat.getOrganisation().getCity());
            LOG.error("Error during organisation saving", e);
        }
    }

    @Override
    public void saveCity(final City city)
    {
        if (!city.getName().isEmpty()) {
            cityRepository.findCityByName(city.getName()).ifPresent(foundCity -> {
                city.setId(foundCity.getId());
                if (city.getNameEnglish().isEmpty()) {
                    if (!foundCity.getNameEnglish().isEmpty()) {
                        city.setNameEnglish(foundCity.getNameEnglish());
                    }
                    else {
                        city.setNameEnglish(translationService.translateToEnglish(city.getName()));
                    }
                }
            });
            cityRepository.saveAndFlush(city);
        }
    }
}
