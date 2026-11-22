package com.example.book_shop.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.book_shop.entity.Publisher;
import com.example.book_shop.entity.PublisherDTO;
import com.example.book_shop.repository.PublisherRepository;

@Component 
@Order(2) 
public class NPlusOneDemo implements CommandLineRunner
{
    private static final Logger logger = LoggerFactory.getLogger(NPlusOneDemo.class);

    public final PublisherRepository publisherRepository;

    public NPlusOneDemo(PublisherRepository publisherRepository)
    {
        this.publisherRepository = publisherRepository;
    }

    @Override 
    @Transactional 
    public void run(String...args)
    {
        List<PublisherDTO> dtos = publisherRepository.findAllPublishersWithBookCount();
        logger.info("Найдено DTO: {}", dtos.size());

        for (PublisherDTO dto : dtos)
        {
            logger.info("{} — {} книг (DTO)", dto.getName(), dto.getBookCount());
        }
    }
}
