# alt_payment_crud_ms
# Functional requirements
1. Payment is described by: unique identifier created during persistence process, amount,
currency, user ID and target bank account number
2. Payments should be stored by the service
3. Service should expose an API, which should be able to:
4. Fetch payment resources
5. Create, update and delete payment resources
6. List a collection of payment resources
# Non-Functional requirements
1. API should be RESTFUL
2. Application should be able to store payments in a CSV file
3. The code should be open for extensions, i.e possibility to add a support for an in-memory
database storage, so the type of storage engine could be passed as a configuration
parameter
4. You should use best practices, for example TDD/BDD, SOLID etc.
5. Consider using Clean Architecture or Hexagonal/Ports and Adapters patterns
6. Try to simplify your code by using well proven open source frameworks and libraries
7. Write the code with production ready quality in mind
Submitting the exercise
Application code should be published on github or gitlab public repository.
