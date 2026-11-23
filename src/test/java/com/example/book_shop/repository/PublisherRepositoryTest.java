package com.example.book_shop.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.book_shop.entity.Author;
import com.example.book_shop.entity.Book;
import com.example.book_shop.entity.Genre;
import com.example.book_shop.entity.Publisher;
import com.example.book_shop.entity.PublisherDTO;

import jakarta.persistence.EntityManager;

@DataJpaTest 
public class PublisherRepositoryTest 
{
    @Autowired 
    private EntityManager entityManager;

    @Autowired 
    private PublisherRepository publisherRepository;

    @Autowired 
    private BookRepository bookRepository;

    @Autowired 
    private AuthorRepository authorRepository;

    @Autowired 
    private GenreRepository genreRepository;


    @BeforeEach 
    void resetStats()
    {
        entityManager
        .getEntityManagerFactory()
        .unwrap(SessionFactory.class)
        .getStatistics()
        .clear();
    }

    @Test 
    void findAllWithEntityGraph_runsOneQuery()
    {
        seed();
        long before = statements();

        List<Publisher> all = publisherRepository.findAll();

        int[] bookCounts = all.stream().mapToInt(p -> p.getBooks().size()).toArray();
        int[] check = {3, 3, 3, 3, 0, 0};

        assertEquals(1, statements() - before);
        assertEquals(6, all.size());
        assertArrayEquals(check, bookCounts);
    }

    @Test 
    void findAllWithJoinFetch_runsOneQuery_withoutDuplicates() 
    {
        seed();
        long before = statements();

        List<Publisher> all = publisherRepository.findAllWithJoinFetch();

        int[] bookCounts = all.stream().mapToInt(p -> p.getBooks().size()).toArray();
        int[] check = {3, 3, 3, 3, 0, 0};

        assertEquals(1, statements() - before);
        assertEquals(6, all.size());
        assertArrayEquals(check, bookCounts);
    }

    @Test
    void findAllWithoutGraph_touchingBooks_runsBatchQueries() 
    {
        seed();
        long before = statements();

        List<Publisher> all = publisherRepository.findAllWithoutGraph();

        int[] bookCounts = all.stream().mapToInt(p -> p.getBooks().size()).toArray();
        int[] check = {3, 3, 3, 3, 0, 0};

        assertEquals(3, statements() - before);
        assertArrayEquals(check, bookCounts);
    }

    @Test
    void findAllByOrderByNameAscWithGraph_runsOneQuery() 
    {
        seed();
        long before = statements();

        List<Publisher> all = publisherRepository.findAllByOrderByNameAsc();

        int[] bookCounts = all.stream().mapToInt(p -> p.getBooks().size()).toArray();
        int[] check = {3, 3, 3, 3, 0, 0};

        assertEquals(1, statements() - before);
        assertEquals(6, all.size());
        assertArrayEquals(check, bookCounts);
    }

    @Test 
    void findPublisherDtos_runsOneQuery() 
    {
        seed();
        long before = statements();

        List<PublisherDTO> all = publisherRepository.findAllPublishersWithBookCount();
        long[] check = {3, 3, 3, 3, 0, 0};

        assertEquals(1, statements() - before);
        assertEquals(6, all.size());
        assertArrayEquals(check, all.stream().mapToLong(PublisherDTO::getBookCount).toArray());
    }

    private long statements()
    {
        return entityManager
        .getEntityManagerFactory()
        .unwrap(SessionFactory.class)
        .getStatistics()
        .getPrepareStatementCount();
    }

    private void seed() {
        Author a1 = new Author();
        a1.setName("Автор 1");
        authorRepository.save(a1);
        Author a2 = new Author();
        a2.setName("Автор 2");
        authorRepository.save(a2);
        Author a3 = new Author();
        a3.setName("Автор 3");
        authorRepository.save(a3);
        Author a4 = new Author();
        a4.setName("Автор 4");
        authorRepository.save(a4);
        Author a5 = new Author();
        a5.setName("Автор 5");
        authorRepository.save(a5);
        Author a6 = new Author();
        a6.setName("Автор 6");
        authorRepository.save(a6);

        Genre g1 = new Genre();
        g1.setName("Жанр 1");
        genreRepository.save(g1);
        Genre g2 = new Genre();
        g2.setName("Жанр 2");
        genreRepository.save(g2);
        Genre g3 = new Genre();
        g3.setName("Жанр 3");
        genreRepository.save(g3);

        Publisher p1 = new Publisher();
        p1.setName("Издатель 1");
        p1.setCountry("Россия");
        publisherRepository.save(p1);
        Publisher p2 = new Publisher();
        p2.setName("Издатель 2");
        p2.setCountry("Россия");
        publisherRepository.save(p2);
        Publisher p3 = new Publisher();
        p3.setName("Издатель 3");
        p3.setCountry("Россия");
        publisherRepository.save(p3);
        Publisher p4 = new Publisher();
        p4.setName("Издатель 4");
        p4.setCountry("Россия");
        publisherRepository.save(p4);
        Publisher p5 = new Publisher();
        p5.setName("Издатель 5");
        p5.setCountry("Россия");
        publisherRepository.save(p5);
        Publisher p6 = new Publisher();
        p6.setName("Издатель 6");
        p6.setCountry("Россия");
        publisherRepository.save(p6);


        Book b1 = new Book();
        b1.setTitle("Книга 1");
        b1.setISBN("1");
        b1.setPublisher(p1);
        b1.getAuthors().addAll(List.of(a1, a2));
        b1.getGenres().addAll(List.of(g1, g2, g3));

        Book b2 = new Book();
        b2.setTitle("Книга 2");
        b2.setISBN("2");
        b2.setPublisher(p1);
        b2.getAuthors().addAll(List.of(a3, a4));
        b2.getGenres().addAll(List.of(g1, g2, g3));

        Book b3 = new Book();
        b3.setTitle("Книга 3");
        b3.setISBN("3");
        b3.setPublisher(p1);
        b3.getAuthors().addAll(List.of(a5, a6));
        b3.getGenres().addAll(List.of(g1, g2, g3));

        Book b4 = new Book();
        b4.setTitle("Книга 4");
        b4.setISBN("4");
        b4.setPublisher(p2);
        b4.getAuthors().add(a1);
        b4.getGenres().addAll(List.of(g1, g2, g3));

        Book b5 = new Book();
        b5.setTitle("Книга 5");
        b5.setISBN("5");
        b5.setPublisher(p2);
        b5.getAuthors().add(a2);
        b5.getGenres().addAll(List.of(g1, g2, g3));

        Book b6 = new Book();
        b6.setTitle("Книга 6");
        b6.setISBN("6");
        b6.setPublisher(p2);
        b6.getAuthors().add(a3);
        b6.getGenres().addAll(List.of(g1, g2, g3));

        Book b7 = new Book();
        b7.setTitle("Книга 7");
        b7.setISBN("7");
        b7.setPublisher(p3);
        b7.getAuthors().add(a4);
        b7.getGenres().addAll(List.of(g1, g2, g3));

        Book b8 = new Book();
        b8.setTitle("Книга 8");
        b8.setISBN("8");
        b8.setPublisher(p3);
        b8.getAuthors().add(a5);
        b8.getGenres().addAll(List.of(g1, g2, g3));

        Book b9 = new Book();
        b9.setTitle("Книга 9");
        b9.setISBN("9");
        b9.setPublisher(p3);
        b9.getAuthors().add(a6);
        b9.getGenres().addAll(List.of(g1, g2, g3));

        Book b10 = new Book();
        b10.setTitle("Книга 10");
        b10.setISBN("10");
        b10.setPublisher(p4);
        b10.getAuthors().add(a1);
        b10.getGenres().addAll(List.of(g1, g2, g3));

        Book b11 = new Book();
        b11.setTitle("Книга 11");
        b11.setISBN("11");
        b11.setPublisher(p4);
        b11.getAuthors().add(a2);
        b11.getGenres().addAll(List.of(g1, g2, g3));

        Book b12 = new Book();
        b12.setTitle("Книга 12");
        b12.setISBN("12");
        b12.setPublisher(p4);
        b12.getAuthors().add(a3);
        b12.getGenres().addAll(List.of(g1, g2, g3));

        bookRepository.saveAll(List.of(b1, b2, b3, b4, b5, b6,
                            b7, b8, b9, b10, b11, b12));

        entityManager.flush();
        entityManager.clear();
    }
}
