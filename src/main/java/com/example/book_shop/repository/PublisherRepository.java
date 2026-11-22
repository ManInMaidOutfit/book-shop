package com.example.book_shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.book_shop.entity.Publisher;

public interface PublisherRepository extends JpaRepository<Publisher, Long>
{
    @Override 
    @EntityGraph(attributePaths = "books")
    List<Publisher> findAll(); 
}
