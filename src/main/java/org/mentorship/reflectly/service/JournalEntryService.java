// File: src/main/java/org/mentorship/reflectly/user/UserService.java
package org.mentorship.reflectly.service;

import org.mentorship.reflectly.model.JournalEntryEntity;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.JournalEntryRepository;
import org.mentorship.reflectly.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    public JournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    public List<JournalEntryEntity> getAllJournalEntities() {
        return journalEntryRepository.findAll();
    }
}