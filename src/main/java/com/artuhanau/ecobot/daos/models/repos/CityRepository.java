package com.artuhanau.ecobot.daos.models.repos;

import com.artuhanau.ecobot.daos.models.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City,Long> {
    @Query(value = "SELECT c FROM city c WHERE SIMILARITY(name,?1) > 0.3 ORDER BY SIMILARITY(name,?1) DESC LIMIT 1", nativeQuery = true)
    City findMostSimilarCity(String name);
}
