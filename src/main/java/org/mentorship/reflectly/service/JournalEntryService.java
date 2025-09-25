package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.model.JournalEntryEntity;
import org.mentorship.reflectly.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    public List<JournalEntryEntity> getAllJournalEntities() {
        return journalEntryRepository.findAll();
    }
}