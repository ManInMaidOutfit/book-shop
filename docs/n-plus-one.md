# Лаборатория N+1

Учебный проект `book-shop`: одну и ту же выборку данных загружаем разными способами
и считаем, сколько SQL-запросов уходит в базу. Цель - увидеть проблему N+1 своими
глазами, освоить способы её решения и честно задокументировать варианты, которые ломаются.

## Теория: что такое N+1

Приложение делает 1 запрос за списком сущностей, а затем по одному запросу за коллекцию
каждой сущности - итого 1 + N запросов. На нашем стенде: 1 запрос за издателями +
6 запросов за книгами каждого издателя = 7 запросов.

## Стенд

| Компонент | Значение |
|---|---|
| Java | 17 |
| Spring Boot | 4.2.0 |
| Hibernate | 7.4.15.Final (сверь со строкой "Hibernate ORM core version" в логе запуска) |
| База | H2, in-memory, create-drop (пересоздаётся при каждом запуске) |
| SQL в консоль | show-sql=true, format_sql=true |
| Статистика | generate_statistics=true |
| OSIV | выключен (spring.jpa.open-in-view=false) |

Данные: 6 издателей - у Эксмо, АСТ, Альпина, МИФ по 3 книги (всего 12),
HarperCollins и Penguin без книг.
Демо: `NPlusOneDemo` (CommandLineRunner, @Transactional): `publisherRepository.findAll()`,
затем `getBooks().size()` у каждого.

## Baseline: ничего не предпринимаем - 7 запросов

Репозиторий пустой, коллекция LAZY (умолчание для @OneToMany):
```java
    public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    }
```
select
        p1_0.id,
        p1_0.country,
        p1_0.name 
    from
        publisher p1_0
select
        b1_0.publisher_id,
        b1_0.id,
        b1_0.isbn,
        b1_0.published_at,
        b1_0.title 
    from
        book b1_0 
    where
        b1_0.publisher_id=?
select
        b1_0.publisher_id,
        b1_0.id,
        b1_0.isbn,
        b1_0.published_at,
        b1_0.title 
    from
        book b1_0 
    where
        b1_0.publisher_id=?

Разбор: 1 (издатели) + 6 (книги каждого издателя) = 7.
Интересно: 2 из 6 запросов ушли на HarperCollins и Penguin - издателей БЕЗ книг.
Пустая LAZY-коллекция тоже стоит SELECT: Hibernate обязан сходить в базу и убедиться, что там пусто.

Точка отсчёта в истории: коммит `2079b14`.

## Эксперимент 1: @EntityGraph - 7 -> 1
```java
    @Override
    @EntityGraph(attributePaths = "books")
    List<Publisher> findAll();
```

select
        p1_0.id,
        b1_0.publisher_id,
        b1_0.id,
        b1_0.isbn,
        b1_0.published_at,
        b1_0.title,
        p1_0.country,
        p1_0.name 
    from
        publisher p1_0 
    left join
        book b1_0 
            on p1_0.id=b1_0.publisher_id

Результат: 1 запрос на всю демо-фазу. Книги те же: 3, 3, 3, 3, 0, 0.

Наблюдения:
- join LEFT - издатели без книг не выпали из выборки;
- размножение строк: SQL вернул 14 строк (12 пар "издатель+книга" и 2 строки с NULL),
  Hibernate свернул их в 6 объектов Publisher;
- @EntityGraph - "eager по требованию": меняет один конкретный запрос, а не сущность.

Коммит: `72d4dd6`.

## Эксперимент 2: @Query + JOIN FETCH - 7 -> 1

```java
@Query("SELECT DISTINCT p FROM Publisher p LEFT JOIN FETCH p.books")
List<Publisher> findAllWithJoinFetch();
```
Результат: 1 запрос на всю демо-фазу. Книги те же: 3, 3, 3, 3, 0, 0.

Наблюдения:

- JOIN FETCH явно указывает Hibernate инициализировать коллекцию в том же запросе.
- Ключевое слово DISTINCT критически важно: без него Hibernate может вернуть дубликаты корневых сущностей в списке (по одной на каждую дочернюю строку), которые затем будут отфильтрованы в памяти, что неэффективно.
- В отличие от @EntityGraph, этот подход позволяет гибко настраивать условия (например, добавить WHERE p.name = :name), но требует написания JPQL вручную.

Коммит: `34aa507`.

## Эксперимент 3: @BatchSize - 7 -> 3

**Идея:** решить проблему N+1 без использования `JOIN FETCH`, избегая декартовых произведений и проблем с пагинацией.

```java
// Publisher.java
@OneToMany(mappedBy = "publisher", cascade = CascadeType.ALL, orphanRemoval = true)
@org.hibernate.annotations.BatchSize(size = 3)
private List<Book> books = new ArrayList<>();

// PublisherRepository.java (метод должен быть "чистым")
List<Publisher> findAll();
```
```sql
Результат: 3 запроса вместо 7 (1 на издателей + 2 батча по 3 книги).

-- Запрос 1: издатели
select p1_0.id, p1_0.country, p1_0.name from publisher p1_0

-- Запрос 2: книги для первых 3-х издателей
select b1_0.publisher_id, b1_0.id, ... from book b1_0 where b1_0.publisher_id in (?, ?, ?)

-- Запрос 3: книги для остальных 3-х издателей
select b1_0.publisher_id, b1_0.id, ... from book b1_0 where b1_0.publisher_id in (?, ?, ?)
```

Наблюдения:
- @BatchSize работает прозрачно на уровне сущности, не требуя изменений в JPQL-запросах репозитория;
- нет декартова произведения строк (в отличие от JOIN FETCH);
- идеально совместим с пагинацией (Pageable), так как основной запрос остаётся простым SELECT без JOIN;
- важный нюанс: метод репозитория должен быть «чистым». Если на findAll() повесить @EntityGraph, он перебьёт @BatchSize и вернёт JOIN-запрос.

Коммит: `08f848e`.

## Эксперимент 4: @EntityGraph + @Query — работает!

**Идея:** проверить, можно ли комбинировать кастомный `@Query` с `@EntityGraph` для оптимизации выборки.

```java
// PublisherRepository.java
@Query("SELECT p FROM Publisher p")
@EntityGraph(attributePaths = "books")
List<Publisher> findAllWithCustomQueryAndGraph();
```

Результат: 1 запрос с `LEFT JOIN`. Работает корректно.

```sql
select p1_0.id, b1_0.publisher_id, b1_0.id, ... 
from publisher p1_0 
left join book b1_0 on p1_0.id=b1_0.publisher_id
```

Наблюдения:
- В старых версиях Spring Data JPA (2.x) и Hibernate (5.x) @EntityGraph молча игнорировался при наличии @Query, что приводило к N+1;
- В Spring Data JPA 4.2.0 + Hibernate 7.4 эта проблема исправлена: аннотации корректно комбинируются;
- это позволяет гибко настраивать запросы (добавлять WHERE, ORDER BY) без потери оптимизации загрузки коллекций.

Коммит: <ХЭШ>.

## Таблица результатов

| Вариант | Запросов (6 издателей) | Статус |
|---|---|---|
| Baseline | 7 | точка отсчёта |
| @EntityGraph | 1 | работает |
| @Query + JOIN FETCH | 1 | работает |
| @BatchSize (size=3) | 3 | работает (оптимально для больших данных и пагинации) |
| @EntityGraph + @Query | 1 | работает (в новых версиях Spring Data JPA 4+) |
| fail/eager-everything | 14 | провал: скрытый N+1 |
| fail/join-fetch-two-bags | Краш приложения | MultipleBagFetchException |
| fail/join-fetch-pagination | Краш/Неверный результат | Игнорирование LIMIT, загрузка в память |

## Дорожная карта

- [ ] @Query + JOIN FETCH
- [ ] @BatchSize и hibernate.default_batch_fetch_size
- [ ] Ветки fail/*: eager-everything; JOIN FETCH двух List сразу (MultipleBagFetchException);
      JOIN FETCH + Pageable (HHH000104); EntityGraph + @Query
- [ ] DTO-проекции
- [ ] @DataJpaTest + Hibernate Statistics
- [ ] Этап 2: Testcontainers + PostgreSQL
```
