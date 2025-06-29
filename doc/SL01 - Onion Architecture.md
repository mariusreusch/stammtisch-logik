# Onion Architecture

**Onion Architecture Rings:**

The onion architecture consists of four concentric rings, with dependencies flowing inward:

1. **Domain (Core/Innermost Ring)**: Contains the business logic core
   - Aggregates: Main domain entry points
   - Entities: Objects with identity within aggregates
   - Value Objects: Immutable objects representing values

2. **Application Ring**: Orchestrates domain objects and defines use cases
   - Use Cases: Application-specific business rules
   - Use Case Objects: Input/output objects for use cases
   - Ports: Interfaces for external dependencies (following dependency inversion)

3. **Interface Ring**: Entry points to the application
   - REST Controllers: HTTP endpoints
   - Event Listeners: Message-based entry points

4. **Infrastructure Ring (Outermost)**: External concerns and technical implementations
   - Domain Repository Implementations: Data persistence logic
   - JPA Repositories: Database access layer
   - DTOs: Data transfer objects
   - Adapters: Implementations of application ports

Each inner ring can only depend on rings inside it, never on outer rings. This ensures that business logic remains independent of technical implementation details.

### Domain
- **Aggregate**: Main entry point to a domain. Consists of Entities and Value Objects but does not contain object references to other Aggregates. Other Aggregates should only be referenced via ID.
- **Entity**: Part of an Aggregate. Can only be modified via the aggregate. Entities have an identity (e.g., Role Assignment Id).
- **Value Objects**: Small objects that represent values like Scn, SubjectId, etc. Can be used within the whole application. Value objects have no own identity.
- **Domain Repository**: There should be one Domain Repository for each Aggregate. It is the interface for persisting and loading aggregates. Only the interface is part of the domain ring, therefore it is a special kind of a Port. The implementation is placed in the infrastructure layer.

### Application
- **Use Case**: See [SL02 - Use cases.md](SL02%20-%20Use%20cases.md)
- **Use Case Objects**: See [SL02 - Use cases.md](SL02%20-%20Use%20cases.md)
- **Port**: An interface for any kind of infrastructure or interface logic. Ports are always interfaces, responsible for decoupling between the application ring and the two outermost layers (Infrastructure and Interfaces). The application ring should not know these outer rings (Dependency Inversion). This is a key aspect of the onion architecture approach. Ports are implemented by Adapters in the Interface or Infrastructure ring. A standard purpose of a Port is the retrieval of information from peripheral systems within a Use Case (e.g., an IdpPort to fetch information from IDP, with technical details implemented in the Adapter).

### Interfaces
- **Rest Controller**: HTTP entry point of an application.
- **Event Listener**: Message-based entry point of an application.
- **DTO**: Data Transfer Objects (DTOs) act as very simple data structure to exchange data between two applications or modules. They should not contain any business logic, they should only contain simple data fields. They can contain value objects to describe a type of a field.
- **DTO Mapper** Responsible for mapping DTOs to Use Case Objects or Domain Aggregates or Domain Entities.

### Infrastructure
- **Domain Repository Implementations**: Contains persistence and fetching logic for domain Aggregates. May reference other domain repository implementations and JpaRepositories.
- **JpaRepository**: Simple and straightforward DB repository to persist and fetch DbEntities.
- **DbEntity**: Regular JPA entities in order to persist and fetch data into and from the database. It should not contain any business logic. It should not be confused with a domain Entity nor an Aggregate. 
- **Adapter (Port Implementation)** Implementation of a Port. Contains the technical details of a Port interface like the connection and retrieval logic for peripheral systems.
- **DTO**: Data Transfer Objects (DTOs) act as very simple data structure to exchange data between two applications or modules. They should not contain any business logic, they should only contain simple data fields. They can contain value objects to describe a type of a field.
- **DTO Mapper** Responsible for mapping DTOs to Use Case Objects or Domain Aggregates or Domain Entities.

---

## Code Sample (Quality Change)

### Interface Layer
```java
@RestController
@RequiredArgsConstructor
@RequestMapping(path = UcPaths.EC_PATH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
@EventLogRequestMetrics
public class QualityChangedEventRestController implements QualityChangedEventApi {

    private final EventProcessor eventProcessor;
    private final ChangeQualityUseCase changeQualityUseCase;

    @Override
    @Retryable(include = {CannotAcquireEventLockException.class, RetryableConflictException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ResponseEntity<Void> handleQualityChangeEvent(@RequestBody QualityChangedEventDto qualityChangedEventDto) {
        Runnable qualityChange = () -> changeQualityUseCase.changeQuality(
                ChangeQualityUseCase.Request.builder()
                        .stammtischLoginId(new StammtischLoginId(qualityChangedEventDto.getStammtischLoginId()))
                        .newQualityLevel(new QualityLevel(qualityChangedEventDto.getNewQualityLevel()))
                        .build()
        );
        eventProcessor.process(qualityChange, qualityChangedEventDto);

        return noContent().build();
    }
}
```

### Infrastructure Layer
```java
@Repository
@RequiredArgsConstructor
class StammtischLoginRepositoryImpl implements StammtischLoginRepository {

    private final RoleAssignmentRepositoryImpl roleAssignmentRepository;
    private final RoleAssignmentJpaRepository roleAssignmentJpaRepository;
    private final SubjectJpaRepository subjectJpaRepository;
    private final SubjectHierarchyJpaRepository subjectHierarchyJpaRepository;
    private final FederatedStammtischLoginRepositoryImpl federatedStammtischLoginRepository;
    private final EntityManager entityManager;

    @Override
    public Optional<StammtischLogin> findByStammtischLoginId(StammtischLoginId stammtischLoginId) {
        // implementation
    }

    @Override
    public void save(StammtischLogin stammtischLogin) {
        SubjectEntity subjectEntity = saveSubjectAttributesAndRelations(stammtischLogin);

        saveFederations(stammtischLogin, subjectEntity);
        saveRoleAssignments(stammtischLogin);
    }

    @Override
    public void delete(StammtischLoginId stammtischLoginId) {
        // implementation
    }
}
```

### Application Layer (see ADR06 for further details regarding Use Cases.)
```java
@Service
@RequiredArgsConstructor
public class ChangeQualityUseCase {

    private final StammtischLoginRepository stammtischLoginRepository;

    @Data
    public static class Request {
        private final StammtischLoginId stammtischLoginId;
        private final QualityLevel qualityLevel;
    }

    @Transactional
    public void invoke(Request request) {
        Optional<StammtischLogin> stammtischLoginOptional = stammtischLoginRepository.findByStammtischLoginId(request.stammtischLoginId);

        if (stammtischLoginOptional.isEmpty()) {
            log("Quality change is not processed because the affected StammtischLogin does not exist.", request);
            return;
        }

        StammtischLogin stammtischLogin = stammtischLoginOptional.get();

        if (!stammtischLogin.isRes()) {
            log("Quality change is not processed because the affected StammtischLogin is not in segment RES.", request);
            return;
        }

        stammtischLogin.changeQualityTo(request.qualityLevel);

        stammtischLoginRepository.save(stammtischLogin);

        log("Successfully changed quality", request);

    }

    //...
}
```

### Domain Layer
```java
public class StammtischLogin implements Validatable {

    private final StammtischLoginId stammtischLoginId;
    private Scn relatedScn;
    private ContactId contactId;
    private Services services;
    private FederatedStammtischLogins federatedStammtischLogins;

    //...

    public void changeQualityLevelTo(QualityLevel qualityLevel) {
       // imple
    }

    //...
}

public interface StammtischLoginRepository {

    Optional<StammtischLogin> findByStammtischLoginId(StammtischLoginId stammtischLoginId);

    void save(StammtischLogin stammtischLogin);

    void delete(StammtischLoginId stammtischLoginId);
}
```

---

## Consequences
What becomes easier or more difficult to do because of this change?
For some cases, we might want to use direct database access without going through multiple layers (mostly read access). We should think about including this into our DDD approach.
