package com.example.book_shop.entity;

public class PublisherDTO 
{
    private final Long id;
    private final String name;
    private final long bookCount;

    public PublisherDTO(Long id, String name, long bookCount) 
    {
        this.id = id;
        this.name = name;
        this.bookCount = bookCount;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public long getBookCount() { return bookCount; }
}
