# ADR06 - Use Case Specific Service Classes

## Context
What is the issue that is motivating this decision or change?

Currently, we have many *Service classes that contain several methods with only loosely related logic. For example, the `ScnEventService` contains two public methods: one to deactivate a customer and another to change a customer's segment.

---

## Proposal
### What is the change that we're proposing and/or doing?

- **Introduce one class per use case.**
  - Example: The current `ScnEventService` contains two use cases: "Deactivate Customer" and "Change Customer Segment". This class should be split into two use case classes: `DeactivateCustomerUseCase` and `ChangeCustomerSegmentUseCase`.
- **Placement:**
  - All use case classes should be placed in the application layer (see ADR-01).
- **Naming:**
  - The name of a use case class should not end with "Service", "Manager", "Helper", or similar. It should end with `UseCase`.
  - The name should always contain a verb to communicate the business reason (e.g., `GrantUsageUseCase`).
- **Decoupling:**
  - The use case should not know about its caller. It should not matter if it is invoked from a `RestController`, `QueueListener`, `JobTrigger`, etc. (see ADR-01).
- **Public Methods:**
  - Each use case class should contain only one public method (in regular cases), typically named `invoke` (e.g., `acceptInvitationUseCase.invoke()`).
  - For "Read" use cases, public methods may start with `findBy[Id|SCN|...]`.
  - For multi-step use cases, use descriptive method names for each step. All such methods can be public and placed in the same use case class.
  - The public method can act as the transaction boundary (annotated with `@Transactional`).
- **Annotation:**
  - The use case class should be annotated with `@Service`.
- **Input and Output:**
  - Input parameters should be value objects, primitives, or a dedicated use case input object. DTOs from the interface layer should **never** be used as input, to avoid breaking dependency rules (see ADR-01).
  - Avoid using domain aggregates/entities as input/output unless it makes sense.
  - For use case objects, define an inner class named `Input` for input and `Output` for response. For multi-step use cases, prefix with the step name.
- **Dependencies:**
  - Avoid dependencies between use cases. Shared logic should be moved to the domain (aggregate, entity, or domain service) or application services. This prevents unintended sharing of side effects (logging, metrics, auditing, etc.).
- **Clarification:**
  - For the difference between application services and use cases, see the "Standard Objects" table in ADR-01 (application column).
  - ADR-01 contains code samples for the different layers/onion rings. The "application layer" section shows a sample use case class.

---

## Quotes from Robert C. Martin's "Clean Architecture" (p. 189ff.)

> "Use cases contain the rules that specify how and when Critical Business Rules within the Entities are invoked. Use cases control the dance of the entities."
>
> "From the use case, it is impossible to tell whether the application is delivered on the web, or on a thick client, or on a console, or is a pure service."
>
> "Entities have no knowledge of the use cases controlling them."
>
> "You might be tempted to have these data structures [input and output data of a use case] contain references to Entity objects. You might think this makes sense because the Entities and the request/response models share so much data. Avoid this temptation! The purpose of these two objects is very different. Over time they will change for very different reasons, so tying them together in any way violates the Common Closure and Single Responsibility Principles. The result would be lots of tramp data, and lots of conditionals in your code."

---

## Consequences
### What becomes easier or more difficult to do because of this change?

- Better understanding of business requirements and context
- Improved readability and maintainability
- Enhanced testability
- Clear guidelines
- Clear layering (in combination with ADR-01)
