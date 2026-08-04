package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.converter.PersonConverter;
import org.mentorship.reflectly.dto.PersonRequestDto;
import org.mentorship.reflectly.dto.PersonResponseDto;
import org.mentorship.reflectly.exception.NotFoundException;
import org.mentorship.reflectly.model.PersonEntity;
import org.mentorship.reflectly.model.RelationshipEventEntity;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.PersonRepository;
import org.mentorship.reflectly.repository.RelationshipEventRepository;
import org.mentorship.reflectly.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonService {

    private final PersonRepository personRepository;
    private final RelationshipEventRepository relationshipEventRepository;
    private final UserRepository userRepository;
    private final PersonConverter personConverter;

    @Transactional(readOnly = true)
    public List<PersonResponseDto> getAllPeople(Long userId) {
        List<PersonEntity> people = personRepository.findByUserIdOrderByLastMentionedAtDesc(userId);
        Map<String, Double> sentimentByPersonId = new HashMap<>();
        for (PersonEntity person : people) {
            sentimentByPersonId.put(person.getId(), computeRecentSentimentAvg(person.getId()));
        }
        return personConverter.toResponseDtoList(people, sentimentByPersonId);
    }

    @Transactional(readOnly = true)
    public PersonResponseDto getPersonById(Long userId, String personId) {
        PersonEntity person = personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new NotFoundException("Person not found"));
        return personConverter.toResponseDto(person, computeRecentSentimentAvg(personId));
    }

    public PersonResponseDto createPerson(Long userId, PersonRequestDto requestDto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        PersonEntity person = new PersonEntity(
                UUID.randomUUID().toString(),
                user,
                requestDto.getName(),
                requestDto.getRelationshipType(),
                requestDto.getNotes()
        );

        PersonEntity saved = personRepository.save(person);
        return personConverter.toResponseDto(saved, null);
    }

    public PersonResponseDto updatePerson(Long userId, String personId, PersonRequestDto requestDto) {
        PersonEntity person = personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new NotFoundException("Person not found"));

        person.setName(requestDto.getName());
        person.setRelationshipType(requestDto.getRelationshipType());
        person.setNotes(requestDto.getNotes());

        PersonEntity saved = personRepository.save(person);
        return personConverter.toResponseDto(saved, computeRecentSentimentAvg(personId));
    }

    private Double computeRecentSentimentAvg(String personId) {
        List<RelationshipEventEntity> recentEvents = relationshipEventRepository
                .findTop5ByPersonIdOrderByCreatedDateDesc(personId);
        return recentEvents.stream()
                .map(RelationshipEventEntity::getSentimentScore)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .stream().boxed().findFirst().orElse(null);
    }
}
