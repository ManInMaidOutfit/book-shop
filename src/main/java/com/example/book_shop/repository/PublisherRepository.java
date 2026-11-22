package com.example.book_shop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.book_shop.entity.Publisher;

public interface PublisherRepository extends JpaRepository<Publisher, Long>
{
    @Override 
    @EntityGraph(attributePaths = "books")
    List<Publisher> findAll(); 

    @Query("select distinct p from Publisher p left join fetch p.books")
    List<Publisher> findAllWithJoinFetch();

    @Query("select distinct p from Publisher p left join fetch p.books")
    Page<Publisher> findAllWithBooksPaged(Pageable pageable);
}
