package com.example.book_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.book_shop.entity.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long>
{
    
}
