# ADR20 Template

### Context  <span style="font-size:6pt; color:grey;">(*What is the issue that we're seeing that is motivating this decision or change?*)</span>

We use problem codes with our Business and Consistency Exceptions to help achieve two things:

- communicate errors to the frontend
- analyse and identify problems based on error codes in responses and in the logs

In cases where problem codes are re-used in several places it can make it hard to understand what causes an error in our logs as we typically only have a single business log.

### Proposal  <span style="font-size:6pt; color:grey;">(*What is the change that we're proposing and/or doing?*)</span>

Where possible we try and use problem codes in a single place in our code.

An arch unit test ProblemCodesUsageRuleTest is used to ensure that problem codes are only used in a single place.

The annotation @MultiUseProblemCode is used to override this default behaviour and allow a problem code to be used in multiple places.

### Consequences  <span style="font-size:6pt; color:grey;">(*What becomes easier or more difficult to do because of this change?*)</span>

It will become easier to analyse the root cause of exceptions based on their problem codes.

### Status
ACCEPTED 
