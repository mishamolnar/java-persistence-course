package com.example.persistancedemo.controller;

import com.example.persistancedemo.entitly.Note;
import com.example.persistancedemo.service.PersonalNotesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/person/{personId}/notes")
public class PersonalNotesController {
    private final PersonalNotesService personalNotesService;

    @PostMapping
    public void addNote(@PathVariable Long personId,
                        @RequestBody Note note) {
        personalNotesService.addNote(personId, note);
    }
}
