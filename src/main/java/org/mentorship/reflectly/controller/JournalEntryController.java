package org.mentorship.reflectly.controller;

import org.mentorship.reflectly.model.JournalEntryEntity;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.service.JournalEntryService;
import org.mentorship.reflectly.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/entries")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @GetMapping
    public List<JournalEntryEntity> getAllJournalEntities() {
        return journalEntryService.getAllJournalEntities();
    }
}