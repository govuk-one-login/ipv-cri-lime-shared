lib-limeade is a shared utility library for Java based Lime CRIs. It provides:

#### HTTP retry framework (HttpRetryer2)

- Executes HTTP requests against third-party APIs with configurable exponential backoff retry
- Configurable retry status codes (default: 408, 425, 429, 500–504), success codes, max retries, and max retry delay

#### Metrics & observability

- Integrates with AWS Lambda Powertools v2 for structured CloudWatch metrics
- Captures HTTP request/response metrics with dimensions: endpoint name, status code, latency, retry attempts, and strategy
  (STUB/UAT/LIVE)

#### Common AWS integrations

- DynamoDbClientFactory, singleton DynamoDB client with OpenTelemetry tracing
- AWSSDKHttpClientConfig, standardised AWS SDK config (region, credentials, CRT HTTP client)
- LoggingSupport, populates SLF4J MDC with Lambda environment variables

#### Domain models

- Strategy enum, environment-aware routing: STUB, UAT, LIVE, NO_CHANGE
- ThirdPartyApiError / ThirdPartyApiErrorResults, standardised error flags for third-party API failures
- HTTPReply, immutable record wrapping HTTP response (status, headers, body)

#### Timing utilities

- Common utils for handling wait/sleep
