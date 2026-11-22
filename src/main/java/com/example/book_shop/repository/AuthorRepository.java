package com.example.book_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.book_shop.entity.Author;

public interface AuthorRepository extends JpaRepository<Author, Long>
{
    
}
