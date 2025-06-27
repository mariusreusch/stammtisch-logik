# 3 Types of Exception

The Three Types of Exception pattern implements a structured approach to exception handling in applications. This pattern categorizes exceptions into three distinct types based on who can resolve the issue:
1. **BusinessException** - User/client fixable issues
2. **ConsistencyException** - Developer team fixable issues
3. **SystemException** - Operations team fixable issues


## Guidelines
- Use the three types of exception: see for more details [Example project](../examples/three-types-of-exception/README.md).
- Re-use problem codes as less as possible. Give them a very concrete name.
- We do not communicate different business exception via different http error code. We only use one (see next point) and in the frontend they will be differentiated by the problem code.
- The application typically returns HTTP status 400 requests.
- We use HTTP 400 for business exceptions in IDM apps.
- In adapters and low level implementations, we should never leak external system specific exceptions. Ports and usecases
  should only know about exceptions mentioned above. Therefore, we should catch system specific exceptions, and throw
  SystemExceptions instead. In rare cases, other exceptions, mentioned in the link in the first point, can be used as well.
