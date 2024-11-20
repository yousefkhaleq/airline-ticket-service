# Airline Ticket Service

This project implements a simple airline ticket service that retrieves seats, places temporary holds, and reserves seats within an airline.

---

## Features

1. **Seat Availability**:
    - Fetch available seats by level and price range.

2. **Hold Seats**:
    - Temporarily hold a specific number of seats for a customer.

3. **Reserve Held Seats**:
    - Commit held seats to a reservation for a customer.

4. **Direct Reservation**:
    - Reserve the best available seats directly with optional level and price filtering.

---

## Technologies

- **Java 17**
- **Spring Boot 3.2.x**
- **Maven** for build automation
- **JUnit** and **Mockito** for unit and integration testing

---

## Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/yousefkhaleq/airline-ticket-service.git
cd airline-ticket-service
```

---

### 2. Prerequisites
Ensure the following are installed:
- **Java 17**
- **Spring Boot 3.2.x**
- **Maven**

---

### 3. Build and Run the Application
```bash
./mvnw clean install
```
```bash
./mvnw spring-boot:run
```
The application will start at http://localhost:8080

---

## Testing the API

### Using Postman or curl

| Endpoint               | Method | Description                          | Parameters                                                                                                                                 |
|------------------------|--------|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `/seats/available`     | `GET`  | Fetch available seats               | `levelNames` (optional, comma-separated, e.g., `First Class,Economy`)                                                                     |
| `/seats/hold`          | `POST` | Hold seats temporarily              | **Body**: `{ "numSeats": <int>, "customerEmail": <string> }`                                                                              |
| `/seats/reserve`       | `POST` | Reserve held seats                  | **Query Params**: `holdId` (required), `customerEmail` (required)                                                                         |
| `/seats/reserve-direct`| `POST` | Reserve best available seats directly | **Query Params**: `numSeats` (required), `maxPrice` (required), `minPrice` (optional), `levelNames` (optional, comma-separated), `customerEmail` (optional) |

---

### Examples

#### 1. Fetch Available Seats
##### Request
```bash
curl -X GET "http://localhost:8080/seats/available?levelNames=First%20Class"
```
Postman```GET http://localhost:8080/seats/available```
#### Response

```json
[
    {
        "levelName": "First Class",
        "availableSeats": 10
    },
    {
        "levelName": "Economy",
        "availableSeats": 50
    }
]
```
#### 2. Hold Seats
##### Request
```bash
curl -X POST "http://localhost:8080/seats/hold" -H "Content-Type: application/json" -d '{"numSeats": 3, "customerEmail": "test@example.com"}'
```
Postman - ```POST http://localhost:8080/seats/hold```
```json
{
  "numSeats": 1,
  "customerEmail": "example@test.com"
}
```
#### Response

```json
{
  "holdId": 1,
  "heldSeats": [
    {
      "seatNumber": "1A",
      "level": "First Class",
      "held": true,
      "reserved": false
    },
    {
      "seatNumber": "1B",
      "level": "First Class",
      "held": true,
      "reserved": false
    }
  ],
  "customerEmail": "test@example.com",
  "expirationTime": "2024-11-18T15:00:00",
  "expired": false
}

```
#### 3. Reserve Held Seats
##### Request (Must have existing hold id from the previous request)
```bash
curl -X POST "http://localhost:8080/seats/reserve"  -H "Content-Type: application/json" -d '{"holdId": 1, "customerEmail": "test@example.com"}'
```
Postman - ```POST http://localhost:8080/seats/reserve```
```json
{
  "holdId": 1,
  "customerEmail": "example@test.com"
}
```
#### Response

```json
"CONFIRM-123456789"
```
#### 4. Reserve Best Available Seats
##### Request
```bash
curl -X POST "http://localhost:8080/seats/reserve-direct"  -H "Content-Type: application/json" -d '{"numSeats": 1, "maxPrice": 1, "minPrice": 1, "levelNames": ["Business", "Economy"], "customerEmail": "test@example.com"}'
```
Postman - ```POST http://localhost:8080/seats/reserve-direct```
```json
{
  "numSeats": 10,
  "maxPrice": 1200,
  "minPrice": 100,
  "levelNames": ["Business", "Economy"],
  "customerEmail": "customer@example.com"
}
```
#### Response

```json
"CONFIRM-DIRECT-123456789"
```