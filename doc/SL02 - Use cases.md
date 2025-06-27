# Use case specific service classes

Introduce one class per use case.

Example: The CustomerEventService currently contains two use cases "Deactivate Customer" and "Change Customer Segment".  Such a class design should be avoid and the class should be divided into two use case classes: DeactivateCustomerUseCase and ChangeCustomerSegmentUseCase
All uses case classes should be placed in the application layer (see SL01).
The name of a use case class should not end with something like "Service", "Manager", "Helper", or something similar. Instead it should simply end with "UseCase".
The name of a use case class should always contain a verb to easily communicate the business reason for its existence: e.g. GrantUsageUseCase
The use case shouldn't know anything about its "caller", so it should not matter if the use case is invoked from a RestController, or from a QueueListener or a JobTrigger or whatever (this should already be given by following the rules of (SL01).
The use case class should contain only one public method (in regular cases). In case of different input parameter for exactly the same use case you could simply overload the method. Or an additional exception to the rule is, if you have a multi-step use case that requires multiple HTTP (or whatever) incoming requests from your client all of the methods that handle those requests can be public and placed in the same use case class.
Typically the only public method of each use case should has the name "invoke" (i.e. acceptInvitationUseCase.invoke()).
In case of a "Read" use case, the public methods could also start with "findBy[Id | SCN | ...]"
In case of multi-step use cases you should try to find suitable names that describes the single steps and use them as names for the public methods.
The public method could also act as the transaction boundary for the whole use case (so it is annotated with @Transactional)
The use case class itself should be annotated with @Service.
Input and Output of a use case
The input parameters should either be made up of value objects and primitives or a dedicated use case input object. DTOs from the interface layer should never act as Input for the use case, this would break also the dependency rules defined in SL01.
Try to avoid using domain aggregates or domain entities as input or output parameter of the use case, but if it make sense → do it.
In case of a use case object the use case object should be an inner class with the name "Input" of the use case class. The response of the use case should also be an inner class of the use case object and should be named "Output". In case of multi-step use cases the Input and Output classes should be prefixed with the step name.
Dependencies between use cases where one use case calls another use case should be avoided. Before creating dependencies between use cases try to move the shared logic into the domain (aggregate, entity, or domain service) or application services. Reason for that is that there might be some "side effects" like logging, metrics, auditing and so on that are specific to a single use case and should not be (accidentally) shared between use cases.
Maybe you are confused between an application service and a use case. You can find a explanation of both types in the "Standard Objects"-table in SL01, both are describes in the" application" column.
SL01 contains code samples for the different layers / onion rings. The "application layer" code sample section contains a use case class that shows how a use could look like.
Some quotes regarding use cases from Robert C. Martin's book "Clean Architecture" (page 189ff.):
"Use cases contain the rules that specify how and when Critical Business Rules within the Entities are invoked. Use cases control the dance of the entities."
"From the use case, it is impossible to tell whether the application is delivered on the web, or on a thick client, or on a console, or is a pure service".
"Entities have no knowledge of the use cases controlling them."
"You might be tempted to have these data structures [editor's note: input and output data of a use case] contain references to Entity objects. You might think this makes sense because the Entities and the request/response models share so much data. Avoid this temptation! The purpose of these two objects is very different. Over time they will change for very different reasons, so tying them together in any way violates the Common Closure and Single Responsibility Principles. The result would be lots of tramp data, and lots of conditionals in your code."
