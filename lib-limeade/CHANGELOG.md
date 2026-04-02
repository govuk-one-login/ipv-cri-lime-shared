# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.2.0] - 2026-04-02

### Removed

- `limeade.service.http.retryer`
- `CRI Common Library`

### Fixed

- `HttpResonseFixtures`
  - `createHttpResponse` now correctly evaluates all provided headers in the headerMap. This fixes an issue where only the
    first header would be included in the response fixture

## [1.1.2] - 2026-04-02

### Changed

- Updated `AWS SDK` to 2.42.26
- Updated `ApectJ` to 1.9.25.1
- Updated `CRI Common Library` to 8.3.4

## [1.1.1] - 2026-03-24

### Changed

- Updated `AWS SDK` to 2.42.16
- Updated `AWS PowerTools` to 2.10.0
- Updated `CRI Common Library` to 8.3.3

To address vulnerability in Jackson-core.

## [1.1.0] - 2026-03-10

### Added

- `Lambda Powertools 2` upgrades backported from fraud api

### Changed

- `HTTPReply` updated to Record type, breaking change as public field accessors have been removed

### Removed

- `log4j2` has been removed in favour of `SLF4J`
- `Lambda Powertools` is no longer applied via AOP, instrumentation is now handled explicitly in code

## [1.0.1] - 2026-02-17

### Removed

- `apache-client` excluded from transitive deps
- `netty` excluded from transitive deps
- `url-connection-client` excluded from transitive deps

## [1.0.0] - 2026-02-17

### Added

- `lib-limeade` ported from fraud api

<!--- version template

## [x.y.z] - YYY-MM-DD

### Added

### Fixed

### Changed

### Removed

--->
