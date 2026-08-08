package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.converter.SavedFrameworkEntryConverter;
import org.mentorship.reflectly.dto.SavedFrameworkEntryRequestDto;
import org.mentorship.reflectly.dto.SavedFrameworkEntryResponseDto;
import org.mentorship.reflectly.dto.SavedFrameworkEntryUpdateRequestDto;
import org.mentorship.reflectly.exception.NotFoundException;
import org.mentorship.reflectly.model.ConversationEntity;
import org.mentorship.reflectly.model.FrameworkType;
import org.mentorship.reflectly.model.PersonEntity;
import org.mentorship.reflectly.model.SavedFrameworkEntryEntity;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.ConversationRepository;
import org.mentorship.reflectly.repository.PersonRepository;
import org.mentorship.reflectly.repository.SavedFrameworkEntryRepository;
import org.mentorship.reflectly.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedFrameworkEntryService {

    private final SavedFrameworkEntryRepository savedFrameworkEntryRepository;
    private final ConversationRepository conversationRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final SavedFrameworkEntryConverter converter;

    @Transactional(readOnly = true)
    public Page<SavedFrameworkEntryResponseDto> getAllEntries(Long userId, FrameworkType frameworkType, Pageable pageable) {
        Page<SavedFrameworkEntryEntity> page = frameworkType == null
                ? savedFrameworkEntryRepository.findByUserIdOrderByCreatedDateDesc(userId, pageable)
                : savedFrameworkEntryRepository.findByUserIdAndFrameworkTypeOrderByCreatedDateDesc(userId, frameworkType, pageable);
        return page.map(converter::toResponseDto);
    }

    @Transactional(readOnly = true)
    public SavedFrameworkEntryResponseDto getEntryById(Long userId, String id) {
        return converter.toResponseDto(findOwnedEntry(userId, id));
    }

    public SavedFrameworkEntryResponseDto createEntry(Long userId, SavedFrameworkEntryRequestDto requestDto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ConversationEntity conversation = null;
        if (requestDto.getConversationId() != null && !requestDto.getConversationId().isBlank()) {
            conversation = conversationRepository.findByIdAndUserId(requestDto.getConversationId(), userId)
                    .orElseThrow(() -> new NotFoundException("Conversation not found"));
        }

        PersonEntity person = null;
        if (requestDto.getPersonId() != null && !requestDto.getPersonId().isBlank()) {
            person = personRepository.findByIdAndUserId(requestDto.getPersonId(), userId)
                    .orElseThrow(() -> new NotFoundException("Person not found"));
        }

        SavedFrameworkEntryEntity entry = new SavedFrameworkEntryEntity(
                UUID.randomUUID().toString(),
                user,
                conversation,
                person,
                requestDto.getFrameworkType(),
                requestDto.getTitle(),
                requestDto.getPayload()
        );

        SavedFrameworkEntryEntity saved = savedFrameworkEntryRepository.save(entry);
        return converter.toResponseDto(saved);
    }

    public SavedFrameworkEntryResponseDto updateEntry(Long userId, String id, SavedFrameworkEntryUpdateRequestDto requestDto) {
        SavedFrameworkEntryEntity entry = findOwnedEntry(userId, id);

        entry.setTitle(requestDto.getTitle());
        entry.setPayload(requestDto.getPayload());
        // frameworkType and conversation link are intentionally immutable after creation —
        // editing swaps content within the same template, not what template/source it came from.
        // personId IS editable (e.g. correcting which relationship a LIFE_POSITIONS entry is about).
        if (requestDto.getPersonId() == null || requestDto.getPersonId().isBlank()) {
            entry.setPerson(null);
        } else {
            entry.setPerson(personRepository.findByIdAndUserId(requestDto.getPersonId(), userId)
                    .orElseThrow(() -> new NotFoundException("Person not found")));
        }

        SavedFrameworkEntryEntity saved = savedFrameworkEntryRepository.save(entry);
        return converter.toResponseDto(saved);
    }

    public void deleteEntry(Long userId, String id) {
        SavedFrameworkEntryEntity entry = findOwnedEntry(userId, id);
        savedFrameworkEntryRepository.delete(entry);
    }

    private SavedFrameworkEntryEntity findOwnedEntry(Long userId, String id) {
        return savedFrameworkEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Saved framework entry not found"));
    }
}
