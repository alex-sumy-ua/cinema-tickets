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
- **JUnit 4 + Mockito** (for testing)

## Getting Started

### Prerequisites
- Java 11+
- Maven

## Contacts

### For any questions, reach out at plachkovskyy@gmail.com

## Next Steps:
1. **Let’s implement the missing logic in `TicketServiceImpl`!**
2. **Then we will move on to JUnit testing.**

## Installation
Clone the repository:
```sh
git clone https://github.com/alex-sumy-ua/cinema-tickets.git
cd cinema-tickets

java-project:
cd cinema-tickets-java
