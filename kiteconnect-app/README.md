# Kite Connect Spring Boot Application

This is a Spring Boot application that demonstrates how to use the [Kite Connect Java library](https://github.com/zerodha/javakiteconnect) to:
- Authenticate with the Kite Connect API.
- Fetch historical data.
- Stream live data using Server-Sent Events (SSE).
- Place trading orders.

## Prerequisites

- Java 21 or higher
- Gradle
- A Zerodha Kite Connect developer account with an API key and secret.

## Configuration

This application is configured to read sensitive information from environment variables for enhanced security. You must set the following environment variables before running the application:

- `KITE_API_KEY`: Your Kite Connect API key.
- `KITE_API_SECRET`: Your Kite Connect API secret.
- `KITE_USER_ID`: Your Zerodha user ID.

Example:
```bash
export KITE_API_KEY="your_api_key"
export KITE_API_SECRET="your_api_secret"
export KITE_USER_ID="your_user_id"
```

## Running the Application

1.  **Clone the repository.**
2.  **Set the environment variables** as described above.
3.  **Build and run the application** using the Gradle wrapper:
    ```bash
    ./gradlew bootRun
    ```
The application will start on `http://localhost:8080`.

## Authentication Flow

The Kite Connect API requires an authenticated session to make API calls. This application handles the authentication flow:

1.  Navigate to `http://localhost:8080/login` in your browser.
2.  You will be redirected to the Zerodha Kite login page. Log in with your credentials.
3.  After successful login, you will be redirected back to the application's `/callback` endpoint. The application will automatically generate an `accessToken` and establish a session.

Once the session is established, you can use the other API endpoints.

## API Endpoints

### 1. Get Historical Data

- **Endpoint:** `GET /historical-data`
- **Description:** Fetches historical candle data for a given instrument.
- **Query Parameters:**
  - `instrumentToken` (long): The instrument token for the stock or contract.
  - `from` (String): The start date/time in `"yyyy-MM-dd HH:mm:ss"` format.
  - `to` (String): The end date/time in `"yyyy-MM-dd HH:mm:ss"` format.
  - `interval` (String): The candle interval (e.g., `minute`, `day`, `5minute`).
- **Example:**
  ```
  http://localhost:8080/historical-data?instrumentToken=256265&from=2024-01-01%2009:15:00&to=2024-01-01%2015:30:00&interval=minute
  ```

### 2. Get Live Data (Server-Sent Events)

- **Endpoint:** `GET /live-data`
- **Description:** Streams live market ticks for a list of instruments using Server-Sent Events (SSE).
- **Query Parameters:**
  - `instrumentTokens` (List<Long>): A comma-separated list of instrument tokens.
- **Example:**
  You can use a client like `curl` to connect to the stream:
  ```bash
  curl -N http://localhost:8080/live-data?instrumentTokens=256265,260105
  ```

### 3. Place an Order

- **Endpoint:** `POST /place-order`
- **Description:** Places a new trading order.
- **Request Body:** A JSON object representing the `OrderParams`.
- **Example:**
  ```bash
  curl -X POST http://localhost:8080/place-order \
  -H "Content-Type: application/json" \
  -d '{
        "exchange": "NSE",
        "tradingsymbol": "INFY",
        "transactionType": "BUY",
        "orderType": "LIMIT",
        "quantity": 1,
        "product": "CNC",
        "price": 1500.0,
        "validity": "DAY"
      }'
  ```
