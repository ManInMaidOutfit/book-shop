package com.example.book_shop.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.book_shop.entity.Book;
import com.example.book_shop.entity.Publisher;
import com.example.book_shop.repository.BookRepository;
import com.example.book_shop.repository.PublisherRepository;

@Component 
@Order(2) 
public class NPlusOneDemo implements CommandLineRunner
{
    private static final Logger logger = LoggerFactory.getLogger(NPlusOneDemo.class);

    public final PublisherRepository publisherRepository;
    public final BookRepository bookRepository;

    public NPlusOneDemo(PublisherRepository publisherRepository, BookRepository bookRepository)
    {
        this.publisherRepository = publisherRepository;
        this.bookRepository = bookRepository;
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

        List<Book> books = bookRepository.findAll();
        logger.info("Найдено книг: " + books.size());

        for(Book book : books)
        {
            int authors = book.getAuthors().size();
            logger.info("\"{}\" — {} авторов", book.getTitle(), authors);
        }
    }
}
