# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
