package com.example.book_shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.book_shop.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long>
{
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors LEFT JOIN FETCH b.genres")
    List<Book> findAllWithAuthorsAndGenres(); 
}
