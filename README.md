# Bank Application
A simple bank application implemented in JPA. This project intentionally uses application managed 
persistence context, as it aims to illustrate what JPA code looks like outside a Spring or JavaEE 
container. 

## setup
* Open project in intellij
* Verify that you have Java 8 installed and configured as SDK **1.8** in Intellij
* Verify that Annotation processing is enabled in Intellij, Settings -> Build -> Compiler -> Annotation Processor  
* If you use Intellij Ultimate you can add the JPA facet to enable JPA support 

### JPA Entities
JPA entities require a no-arguments constructor with visibility protected or public. However, the job of a 
constructor is not just to instantiate objects, but to instantiate valid/usable objects. Since many entities have
columns with non-null constraints, we use static factory methods to create valid entities.

### Improvements
* Container managed transactions would improves readability
* Spring Data JPA Repositories would improve readability
* If tables are owned by this application they should be managed with Flyway
* DB Connections should not be pooled by Hibernate, use Hikari or other production ready DB pool 