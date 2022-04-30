package com.artuhanau.ecobot.daos.models.repos;

import java.util.Optional;

import com.artuhanau.ecobot.daos.models.OrganisationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisationCategoryRepository extends JpaRepository<OrganisationCategory,Long>
{
    Optional<OrganisationCategory> findOrganisationCategoriesByName(String name);
}
