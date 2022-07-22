package com.example.persistancedemo.service;

import com.example.persistancedemo.entitly.Note;
import com.example.persistancedemo.repository.PersonsRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class PersonalNotesService {
    private final PersonsRepository personsRepository;

    @Transactional
    public void addNote(Long personId, Note note) {
        var person = personsRepository.findById(personId)
                .orElseThrow();
        person.addNote(note);

    }

}
