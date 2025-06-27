# ADR04 3 Types of Exception

### Context  <span style="font-size:6pt; color:grey;">(*What is the issue that we're seeing that is motivating this decision or change?*)</span>
We do not have clear rules for creating new exceptions and to which HTTP status codes they should be resolved.


### Proposal  <span style="font-size:6pt; color:grey;">(*What is the change that we're proposing and/or doing?*)</span>
- Use the three types of exception: see for more details https://github.com/mariusreusch/three-types-of-exception
- Re-use problem codes as less as possible. Give them a very concrete name. (see also [ADR20 Usage of problem codes](./20%20-%20Usage%20of%20problem%20codes.md))
- We do not communicate different business exception via different http error code. We only use one (see next point) and in the frontend they will be differentiated by the problem code.
- IDM apps typically return HTTP status 400 requests. This is different from the exceptions we use in ACM.
- We use HTTP 400 for business exceptions in IDM apps.
- In adapters and low level implementations, we should never leak external system specific exceptions. Ports and usecases
  should only know about exceptions mentioned above. Therefore, we should catch system specific exceptions, and throw
  SystemExceptions instead. In rare cases, other exceptions, mentioned in the link in the first point, can be used as well.


### Consequences  <span style="font-size:6pt; color:grey;">(*What becomes easier or more difficult to do because of this change?*)</span>
- We have a clear, consistent and easy to maintain exception concept.


### Status
ACCEPTED
