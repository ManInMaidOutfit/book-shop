package com.example.book_shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.book_shop.entity.Publisher;
import com.example.book_shop.entity.PublisherDTO;

public interface PublisherRepository extends JpaRepository<Publisher, Long>
{
    List<Publisher> findAll(); 

    @Query("select distinct p from Publisher p left join fetch p.books")
    List<Publisher> findAllWithJoinFetch();

    @Query("SELECT new com.example.book_shop.entity.PublisherDTO(p.id, p.name, COUNT(b.id)) " +
           "FROM Publisher p LEFT JOIN p.books b GROUP BY p.id, p.name")
    List<PublisherDTO> findAllPublishersWithBookCount();
}
