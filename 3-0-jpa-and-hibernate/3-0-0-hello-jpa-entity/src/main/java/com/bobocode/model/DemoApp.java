package com.bobocode.model;

import javax.persistence.Persistence;

public class DemoApp {
    public static void main(String[] args) {
        var emf = Persistence.createEntityManagerFactory("MoviePersistence");

        var em = emf.createEntityManager();
        Movie movie = new Movie();
        movie.setDirector("DIR");
        movie.setDurationSeconds(12312312);
        movie.setName("Some movie");
        em.getTransaction().begin();
        em.persist(movie);
        em.getTransaction().commit();
        em.close();

        Long id = movie.getId();
        System.out.println(id);

        var emTwo = emf.createEntityManager();
        emTwo.getTransaction().begin();
        var movie1 = emTwo.find(Movie.class, id);
        movie1.setName("some second movie");
        emTwo.getTransaction().commit();
        emTwo.close();

        System.out.println(movie1);
    }
}
