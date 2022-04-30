package com.artuhanau.ecobot.daos.models.repos;

import java.util.Optional;

import com.artuhanau.ecobot.daos.models.EducationalOrganisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationalOrganisationRepository extends JpaRepository<EducationalOrganisation,Long> {
    Optional<EducationalOrganisation> findEducationalOrganisationByName(String orgName);
}
