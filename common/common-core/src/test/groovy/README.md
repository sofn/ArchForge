# Spock Tests for common-core Module

This directory contains Spock tests for the utility classes in the `common-core` module.

## Running the Tests

To run the tests, use the following Gradle command:

```bash
./gradlew :common:common-core:test
```

## Test Coverage

The following utility classes have Spock tests:

1. `IPUtils` - IP address utility methods
2. `CollectionUtils` - Collection utility methods
3. `JacksonUtil` - Jackson JSON utility methods
4. `URLUtils` - URL encoding/decoding and query parsing utilities
5. `ServletHolderUtil` - Servlet utility methods
6. `MessageUtils` - Internationalization message utilities
7. `IpUtil` - IP address validation utilities
8. `IpRegionUtil` - IP region/geolocation utilities
9. `OfflineIpRegionUtil` - Offline IP region lookup utilities
10. `OnlineIpRegionUtil` - Online IP region lookup utilities

## Dependencies

The tests use the Spock framework with the following dependencies:

- `org.spockframework:spock-core:2.3-groovy-3.0`
- `org.spockframework:spock-spring:2.3-groovy-3.0`
- `org.codehaus.groovy:groovy:3.0.17`

These dependencies are already configured in the `common/common-core/build.gradle.kts` file.