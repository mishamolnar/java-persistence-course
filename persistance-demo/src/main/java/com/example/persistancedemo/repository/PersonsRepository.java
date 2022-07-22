package com.example.persistancedemo.repository;

import com.example.persistancedemo.entitly.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonsRepository extends JpaRepository<Person, Long> {

}
