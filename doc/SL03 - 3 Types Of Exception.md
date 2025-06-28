# 3 Types of Exception

The Three Types of Exception pattern implements a structured approach to exception handling in applications. This pattern categorizes exceptions into three distinct types based on who can resolve the issue:
1. **BusinessException** - User/client fixable issues
2. **ConsistencyException** - Developer team fixable issues
3. **SystemException** - Operations team fixable issues


## Guidelines
- Use the three types of exception: see for more details [Example project](../examples/three-types-of-exception/README.md).
- Re-use problem codes as less as possible. Give them a very concrete name.
- We do not communicate different business exception via different http error code. We only use one (see next point) and in the frontend they will be differentiated by the problem code.
- In adapters and low level implementations, we should never leak external system specific exceptions. Ports and usecases
  should only know about exceptions mentioned above. Therefore, we should catch system specific exceptions, and throw
  SystemExceptions instead. In rare cases, other exceptions, mentioned in the link in the first point, can be used as well.

## Overview
The Three Types of Exception pattern implements a structured approach to exception handling in Java applications. This pattern categorizes exceptions into three distinct types based on who can resolve the issue:
1. **BusinessException** - User/client fixable issues
2. **ConsistencyException** - Developer team fixable issues
3. **SystemException** - Operations team fixable issues

This pattern provides clear guidelines for creating new exceptions and mapping them to appropriate HTTP status codes in API responses.
## Exception Hierarchy
``` mermaid
classDiagram
    RuntimeException <|-- GenericException
    GenericException <|-- BusinessException
    GenericException <|-- ConsistencyException
    GenericException <|-- SystemException
    GenericException --> Problem
    
    class GenericException {
        +Problem problem
        +GenericException(Problem)
        +GenericException(Problem, Throwable)
    }
    
    class BusinessException {
        +BusinessException(Problem)
    }
    
    class ConsistencyException {
        +ConsistencyException(Problem)
    }
    
    class SystemException {
        +SystemException(Problem)
        +SystemException(Problem, Throwable)
    }
    
    class Problem {
        +String code
        +String message
    }
```
## Exception Types
### 1. BusinessException
**Purpose**: Thrown when a business rule is violated.
**Decision Question**: "Who could fix this problem?" If the answer is "the client or user," use a BusinessException.
**Usage Context**: In domain and application (use case) layers of an onion architecture.
**Example scenarios**:
- Invalid input data
- Insufficient permissions
- Resource not found
- Validation errors

**HTTP Status**: Typically mapped to 400 Bad Request
### 2. ConsistencyException
**Purpose**: Thrown for programming errors or data inconsistencies.
**Decision Question**: "Who could fix this problem?" If the answer is "The dev team," use a ConsistencyException.
**Usage Context**: Can be used in all layers of an onion architecture.
**Example scenarios**:
- Unexpected data state
- Logic errors
- Invalid configuration
- Constraint violations

**HTTP Status**: Typically mapped to 500 Internal Server Error
### 3. SystemException
**Purpose**: Thrown for problems with peripheral systems (databases, queues, external services).
**Decision Question**: "Who could fix this problem?" If the answer is "Ops team," use a SystemException.
**Usage Context**: Used in infrastructure/interfaces layers of an onion architecture.
**Example scenarios**:
- Database connection failures
- External API timeouts
- Queue processing issues
- System resource exhaustion

**HTTP Status**: Typically mapped to 503 Service Unavailable
## Decision Flow
``` mermaid
flowchart TD
    A([Start]) --> B{Can the user/client fix the problem?}
    B -- Yes --> C[BusinessException<br><span style="color:orange">User/Client has to fix the issue</span>]
    B -- No --> D{Can the Dev Team fix the problem?}
    D -- Yes --> E[ConsistencyException<br><span style="color:orange">Dev Team has to fix the issue</span>]
    D -- No --> F[SystemException<br><span style="color:orange">Ops Team has to fix the issue</span>]
```
## Best Practices
1. **Problem Codes**: Give problem codes concrete, specific names and avoid reusing them.
2. **Layer-Appropriate Usage**: Follow the prescribed layers for each exception type.
3. **External System Exceptions**: Never leak external system-specific exceptions - catch them and throw appropriate SystemExceptions instead.
4. **HTTP Status Codes**: Map exceptions to consistent HTTP status codes:
  - BusinessException: 400 Bad Request
  - ConsistencyException: 500 Internal Server Error
  - SystemException: 503 Service Unavailable

5. **Frontend Handling**: In frontend code, differentiate between exception types using problem codes rather than HTTP status codes.

## Benefits
- Clear and consistent exception handling
- Improved error communication to different stakeholders
- Easier debugging and problem resolution
- Better separation of concerns
- Enhanced maintainability

By following this pattern, teams can establish a standardized approach to exception handling that communicates clearly about who needs to take action to resolve different types of errors.

