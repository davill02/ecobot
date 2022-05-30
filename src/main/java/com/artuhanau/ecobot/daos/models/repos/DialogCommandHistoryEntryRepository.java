package com.artuhanau.ecobot.daos.models.repos;

import com.artuhanau.ecobot.daos.models.DialogCommandHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DialogCommandHistoryEntryRepository extends JpaRepository<DialogCommandHistoryEntry, Long>
{
}
