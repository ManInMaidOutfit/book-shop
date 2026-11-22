package com.example.book_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.book_shop.entity.Publisher;

public interface PublisherRepository extends JpaRepository<Publisher, Long>
{
    
}
