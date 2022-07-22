package com.example.persistancedemo.controller;

import com.example.persistancedemo.entitly.Person;
import com.example.persistancedemo.repository.PersonsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/persons")
public class PersonController {
    private final PersonsRepository personsRepository;

    @PostMapping
    public ResponseEntity<Person> createPerson(@RequestBody Person person) {
        personsRepository.save(person);
        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(person.getId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(person);
    }
}
