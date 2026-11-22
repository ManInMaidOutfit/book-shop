package com.example.book_shop.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity 
public class Book 
{
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(nullable = false) 
    private String title;

    private LocalDate publishedAt;

    @Column(nullable = false, unique = true) 
    private String ISBN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;  

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_author", 
    joinColumns = @JoinColumn(name = "book_id"), 
    inverseJoinColumns = @JoinColumn(name = "author_id"))
    private List<Author> authors = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_genre", 
    joinColumns = @JoinColumn(name = "book_id"), 
    inverseJoinColumns = @JoinColumn(name = "genre_id")) 
    private List<Genre> genres = new ArrayList<>();

    public Book() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDate publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public List<Author> getAuthors() {
        return authors;
    }


    public List<Genre> getGenres() {
        return genres;
    }

    public void addAuthor(Author author) 
    {
        if (author == null || authors.contains(author)) {
            return;
        }
        authors.add(author);
        author.getBooks().add(this);
    }

    public void removeAuthor(Author author) 
    {
        if (author == null || !(authors.contains(author))) {
            return;
        }
        authors.remove(author);
        author.getBooks().remove(this);
    }

    public void addGenre(Genre genre) 
    {
        if (genre == null || genres.contains(genre)) {
            return;
        }
        genres.add(genre);
        genre.getBooks().add(this);
    }

    public void removeGenre(Genre genre) 
    {
        if (genre == null || !(genres.contains(genre))) {
            return;
        }
        genres.remove(genre);
        genre.getBooks().remove(this);
    }
}