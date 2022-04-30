package com.artuhanau.ecobot.daos.models.repos;

import java.util.Optional;

import com.artuhanau.ecobot.daos.models.Address;
import com.artuhanau.ecobot.daos.models.EducationalOrganisation;
import com.artuhanau.ecobot.daos.models.TrainingFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingFormatRepository extends JpaRepository<TrainingFormat, Long>
{
    Optional<TrainingFormat> findTrainingFormatByOrganisationAndAddressAndFormatName(EducationalOrganisation organisation, Address address, String formatName);
}
