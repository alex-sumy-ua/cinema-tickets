# Cinema Ticket Service
## Java project

## Overview
This project is a ticket purchasing service for a cinema. It ensures that business rules are followed when customers purchase tickets and integrates with payment and seat reservation services.

## Features
- Supports **Adult, Child, and Infant** ticket types.
- Ensures valid purchases based on business rules.
- Calculates **total cost** and calls `TicketPaymentService`.
- Determines **required seats** and calls `SeatReservationService`.
- Ensures **Infants do not require seats**.
- Allows purchase of up to **25 tickets** per transaction.

## Tech Stack
- **Java 11**
- **Maven**
- **JUnit 4 + Mockito** (for testing with simulating external dependencies)
- **JUnit 4 + Reflection** (for testing private methods)

## Getting Started

### Prerequisites
- Java 11+
- Maven

## Tests JUnit: two kinds have been developed
- Using Reflection to test the private methods of TicketServiceImpl (pure tests)
- Using Mockito to test all methods of TicketServiceImpl with simulation of external service dependencies

## Contacts

### For any questions, reach out at plachkovskyy@gmail.com

## Next Step:
**Implementation the missing logic in `TicketServiceImpl`!**

## Installation
Clone the repository:
```sh
git clone https://github.com/alex-sumy-ua/cinema-tickets.git
cd cinema-tickets

java-project:
cd cinema-tickets-java
