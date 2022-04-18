package com.artuhanau.ecobot.daos.models.repos;

import com.artuhanau.ecobot.daos.models.TrainingFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingFormatRepository extends JpaRepository<TrainingFormat, Long>
{
}
