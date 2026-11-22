package com.example.book_shop.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.book_shop.entity.Publisher;
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
        List<Publisher> publishers = publisherRepository.findAllWithJoinFetch();
        logger.info("Найдено издателей: {}", publishers.size());

        for(Publisher publisher : publishers)
        {
            int books = publisher.getBooks().size();
            logger.info("{} — {} книг", publisher.getName(), books);
        }

        Page<Publisher> pagedPublishers = publisherRepository.findAllWithBooksPaged(PageRequest.of(0, 2));

        logger.info("Запрошено на странице: 2 издателя. Фактически получено: {}", pagedPublishers.getContent().size());
        logger.info("Всего издателей в базе (по подсчёту Hibernate): {}", pagedPublishers.getTotalElements());
    }
}
