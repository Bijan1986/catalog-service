
<!-- TOC -->
* [Spring cloud native chapter 3: Getting started with cloud native development](#spring-cloud-native-chapter-3--getting-started-with-cloud-native-development)
    * [Endpoint Requirements:](#endpoint-requirements-)
    * [Development](#development)
      * [Domain](#domain)
      * [Repository Interface and InMemoryBookRepository](#repository-interface-and-inmemorybookrepository)
      * [Service](#service)
      * [Exception classes](#exception-classes)
      * [Controller](#controller)
    * [Data validation and error handling](#data-validation-and-error-handling)
      * [ControllerAdvice](#controlleradvice)
    * [Testing a restful application with Spring](#testing-a-restful-application-with-spring)
  * [Summary](#summary)
* [Spring cloud native chapter 4: Externalized Configuration Management](#spring-cloud-native-chapter-4--externalized-configuration-management)
    * [Custom Properties](#custom-properties)
      * [1. Defining custom properties](#1-defining-custom-properties)
    * [Profiles: feature flags and configuration groups](#profiles--feature-flags-and-configuration-groups)
    * [Centralized Configuration Management with Spring Cloud Config Sever](#centralized-configuration-management-with-spring-cloud-config-sever)
      * [Using Git to store configuration data](#using-git-to-store-configuration-data)
      * [Setting-up a config server](#setting-up-a-config-server)
      * [Making config-service more resilient](#making-config-service-more-resilient)
      * [Understanding the Configuration server rest-api](#understanding-the-configuration-server-rest-api)
    * [Using Configuration server with the config client](#using-configuration-server-with-the-config-client)
      * [Setting up configuration client](#setting-up-configuration-client)
      * [Making the configuration client resilient](#making-the-configuration-client-resilient)
      * [Refreshing configuration service at runtime](#refreshing-configuration-service-at-runtime)
<!-- TOC -->






# Spring cloud native chapter 3: Getting started with cloud native development

### Endpoint Requirements:
                 
> **The catalog service requirements are mentioned below**


| End points    | Http methods | Request body | status | Response body | Description           |
|---------------|--------------|--------------|--------|---------------|-----------------------|
| /books        | GET          |              | 200    | Book[ ]       | Get all books         |
| /books        | Post         | Book         | 201    | Book          | Add new books         |
|               |              |              | 422    |               | Book already exists   |
| /books/{ISBN} | GET          |              | 200    | Book          | Get book with ISBN    |
|               |              |              | 404    |               | No Books found        |
| /books/{ISBN} | PUT          | Book         | 200    | Book          | Update book with ISBN |
|               |              |              | 201    | Book          | Create book with ISBN |
| /books/{ISBN} | DELETE       |              | 204    | Book          | Delete book with ISBN |
                                             
### Development
  > **Step 1. Create the domain using record**
> 
> Book
> > isbn, title, author, price

  > Step 2. Create the Repository interface
> 
> > Iterable<Book> findAll();
  > > Optional<Book> findByIsbn(String isbn);
> > boolean existsByIsbn(String isbn);
> > Book save(Book book);
  >>   void deleteByIsbn(String isbn)

> Step 3. Create Repository Implementation

> Step 4. Crate Service and its implementation

> Step 5. Create Controllers
> 
#### Domain
Book record

```java

public record Book(ISBN isbn,
                   String title,
                   String author,
                   String price) {}

```
#### Repository Interface and InMemoryBookRepository

```java
public interface BookRepository {
    Iterable<Book> findAllBooks();
    Optional<Book> findBookByIsbn(String isbn);
    boolean existsByIsbn(String isbn);
    Book save(Book book);
    void deleteByIsbn(String isbn);
}

@Repository
public class InMemoryBookRepository implements BookRepository{
  private static final Map<String, Book> books = new ConcurrentHashMap<>();

  @Override
  public Iterable<Book> findAllBooks() {
    return books.values();
  }

  @Override
  public Optional<Book> findBookByIsbn(String isbn) {
    return existsByIsbn(isbn)?Optional.of(books.get(isbn)):Optional.empty();
  }

  @Override
  public boolean existsByIsbn(String isbn) {
    return books.containsKey(isbn);
  }

  @Override
  public Book save(Book book) {
    books.put(book.isbn().toString(),book);
    return book;
  }

  @Override
  public void deleteByIsbn(String isbn) {
    books.remove(isbn);
  }
}
```

#### Service

```java
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    // view list of books
    public Iterable<Book> findAllBook() {
        return bookRepository.findAllBooks();
    }

    // view book details
    public Book viewBookDetails(String isbn) {
        return bookRepository.findBookByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    // add book to catolog
    public Book addBookToCatalog(Book book) {
        if (bookRepository.existsByIsbn(book.isbn().toString())) {
            throw new BookAlreadyExistsException(book.isbn().toString());
        }
        return bookRepository.save(book);
    }

    // remove book from the catalog
    public void removeBookFromCatalog(String isbn) {
        bookRepository.deleteByIsbn(isbn);
    }
    
    // edit book details
    public Book editBook(Book book) {
        return bookRepository.existsByIsbn(book.isbn().toString())
                ?
                bookRepository.save(book) : addBookToCatalog(book);

    }
}

```

#### Exception classes

```java
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String isbn){
        super("Book with isbn "+isbn+" was not found .");
    }
}
public class BookAlreadyExistsException extends RuntimeException{
  public BookAlreadyExistsException(String isbn){
    super("Book with isbn: "+isbn+" already exists .");
  }
}
```

#### Controller

```java

@RestController
@RequestMapping("books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    
    @GetMapping
    public Iterable<Book> getAllBooks(){
        return bookService.findAllBook();
    }
    
    @GetMapping("{isbn}")
    public Book getBookByIsbn(@PathVariable String isbn){
        return bookService.viewBookDetails(isbn);
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book saveBook(@RequestBody Book book){
        return bookService.addBookToCatalog(book);
    }
    
    @DeleteMapping("{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable String isbn){
        bookService.removeBookFromCatalog(isbn);
    }
    
    @PutMapping
    public Book editBook(@RequestBody Book book){
        return bookService.editBook(book);
    }
    
}
```
### Data validation and error handling

```Java
public record Book(
        @NotBlank(message = "The book ISBN must not be blank")
        @Pattern(
                regexp = "^([0-9]{10} | [0-9]{13})$",
                message = "The isbn format must be valid. "
        )
        String isbn,

        @NotBlank(message = "The book ISBN must not be blank")
        String title,

        @NotBlank(message = "The book ISBN must not be blank")
        String author,

        @NotNull(message = "the book price must be defined .")
        @Positive(
                message = "The book price must be greater than zero"
        )
        Double price) {}
```

Now in the controller add **@Valid** wherever you have used **@RequestBody**

```
    @PutMapping
    public Book editBook(@Valid @RequestBody Book book){
        return bookService.editBook(book);
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book saveBook(@Valid @RequestBody Book book){
        return bookService.addBookToCatalog(book);
    }
    
```

When the validation fails we will get **MethodArgumentNotValidException**

#### ControllerAdvice

```java
@RestControllerAdvice
public class BookControllerAdvice {

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String bookNotFoundHandler(BookNotFoundException ex) {
        return ex.getMessage();
    }


    @ExceptionHandler(BookAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String bookAlreadyExistsHandler(BookAlreadyExistsException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        var errors = new HashMap<String, String>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}

```


### Testing a restful application with Spring

learn it later

## Summary
> use **@Valid** only in case of **@ResponseBody**
> 
> When the validation fails we will get **MethodArgumentNotValidException**

# Spring cloud native chapter 4: Externalized Configuration Management

### Custom Properties

#### 1. Defining custom properties

1. Annotate the Application class with **@ConfigurationPropertiesScan**
2. Create a package config and create a class PolarProperties and annotate it with **@ConfigurationProperties**
 > @ConfigurationProperties(prefix="polar")
> 
3. Add the dependency **spring-boot-configuration-processor**  in the controller class.
4. in the application.properties add **polar.greeting = Welcome to the local properties**


```java
//step 1: 
@SpringBootApplication
@ConfigurationPropertiesScan
public class CatalogServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CatalogServiceApplication.class, args);
  }

}

//step 2: 

@ConfigurationProperties(prefix = "polar")
@Data
public class PolarProperties {
  private String greeting;
}

//step 3: 
// polar.greeting=Welcome to local host polar properties

//step 4:

@RestController
@RequestMapping("hello")
@RequiredArgsConstructor
public class HomeController {
  private final PolarProperties polarProperties;

  @GetMapping
  public String getProperty() {
    return polarProperties.getGreeting();
  }

}

```

### Profiles: feature flags and configuration groups

> it is a good practice to use profiles as feature flags

1. create a class called BookDataLoader
2. add the **@Profile("testdata")**
3. Autowire Repository
4. create a method to load up data in the start-up
5. annotate that method with **@EventListener(ApplicationReadyEvent.class)**
6. update application.properties with **spring.profiles.active=testdata**
7. start up the application and call **localhost:8080/books**

```java

@Component
@Profile("testdata")
@AllArgsConstructor
public class BookDataLoader {
    private final BookRepository bookRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void loadBookTestData(){
        var book1 = new Book("132131434","Northern Lights","Lyra Silverstar",9.90);
        var book2 = new Book("1234124412","Polar Journey","Lyra Polarson",9.90);
        bookRepository.save(book1);
        bookRepository.save(book2);
    }
}

// spring.profiles.active=testdata

```
```json
[
  {
    "isbn": "1234124412",
    "title": "Polar Journey",
    "author": "Lyra Polarson",
    "price": 9.9
  },
  {
    "isbn": "132131434",
    "title": "Northern Lights",
    "author": "Lyra Silverstar",
    "price": 9.9
  }
]

```

### Centralized Configuration Management with Spring Cloud Config Sever

#### Using Git to store configuration data

1. Crate a new **config-repo** Git repository
2. Create a file named **catalog-service.properties**
3. Define the value for **polar.greeting** property there
4. create another file named **catalog-service-prod.properties**
5. add different text for **polar.greeting** property there
6. commit and push


#### Setting-up a config server

1. create a new spring boot project named **config-service**
2. Add the dependency **config server**
3. Enable the config server by adding **@EnableConfigSever** to the application
4. now give access to the config data stored in github **config-repo** repository .
5. in config-service application go to Application.properties file
6. **spring.cloud.config.server.git.uri** should point to the git repository .
7. you can configure from which branch the spring should take the properties file.
8. **spring.cloud.config.server.git .default-label = main/master** will do the trick

#### Making config-service more resilient

```yaml
spring:
  application:
    name: config-service
  cloud:
    config:
      server:
        git:
          uri: <your-config-repo-github-url>
          default-label: main
          timeout: 5
          clone-on-start: true
          force-pull: true
```

#### Understanding the Configuration server rest-api

The config server which we built just now connects to github properties via rest-api.

If you will start up the application and try to make a call to the below you will see the result
> http :8888/catalog-service/default
> 
> http :8888/catalog-service/prod
> 

### Using Configuration server with the config client

So we have set up the config server, and it seems to work fine.<br>
Now we need to make our spring boot application use it .

In this case, the spring boot application becomes the client .

#### Setting up configuration client

1. Add the dependency **
   implementation 'org.springframework.cloud:spring-cloud-starter-config' **
2. Instruct Catalog-service to fetch the details/properties from the config-service
```yaml

spring:
application:
name: catalog-service
config:
import: "optional:configserver:"
cloud:
config:
uri: http://localhost:8888

```
3. Run and test

#### Making the configuration client resilient

```yaml
spring:
  application:
    name: catalog-service
  config:
    import: "optional:configserver:"
  cloud:
    config:
      uri: http://localhost:8888
      request-connect-timeout: 5000
      request-read-timeout: 5000
      fail-fast: true
      retry:
        max-attempts: 6
        initial-interval: 1000
        max-interval: 2000
        multiplier: 1.1
```

add the dependency

```yaml
dependencies {
  ...
  implementation 'org.springframework.retry:spring-retry' 
}
```

#### Refreshing configuration service at runtime

1. add actuator dependency
```yaml
dependencies {
  ...
  implementation 'org.springframework.boot:spring-boot-starter-actuator' 
}
```
2. **/actuator/refresh** event triggers the refresh event
3. add this to catalog-service project

```yaml
management:
  endpoints:
    web:
      exposure:
        include: refresh
```


