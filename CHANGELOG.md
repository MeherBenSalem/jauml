# Changelog

All notable changes to the JAUML JSON Utility Library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-06-25

### Added
- **`JsonLib`**: Modern JSON utility class providing:
  - `safeParse` for exception-free parsing.
  - `strictParse` to enforce standard RFC 8259 compliance.
  - `stableStringify` for key-sorted deterministic JSON serialization.
  - `deepClone` for clean nested JSON copies.
  - `getByPath` and `setByPath` for dot-bracket path navigation (e.g. `settings.users[0].name`).
  - `merge` to recursively combine configuration layers.
  - `normalize` to apply type coercion and default templates to user configurations.
  - `detectVersion` for finding schema version tags.
- **`JsonSchema`**: Embedded lightweight JSON Schema validator supporting type checks, required keys constraint, object property validation, and array item schema checking.
- **`JsonMigrator`**: Sequence-based directed BFS path-finding migrator for sequential version upgrades.
- **`JsonException`**: Unified, typed exception class for all library errors.
- **`JaumlInitializer`**: Mod-level startup checker verifying configuration health, executing migrations, and performing library/app compatibility checks.
- **JUnit 5 Test Framework**: Shared test suite inside `common-shared` running tests natively across all supported Minecraft versions.
- **Launch Verification script**: PowerShell runner `verify_launch.ps1` to clean, compile, run tests, and package mod jars automatically.

### Changed
- **`ConfigFile`**: Enhanced in-place with:
  - Graceful recovery of corrupted config files (backs up corrupted configs to `.json.bak` and re-creates clean configs from default templates).
  - Implicit type coercion and template normalization during loads.
  - Schema validation check support.
  - Auto-migrations before initialization.
- **`JaumlConfig`**: Added overloaded `open` methods supporting schemas, migrators, and default templates. Added runtime checks (`LIBRARY_VERSION` and `isCompatible`).

### Deprecated
- Legacy static methods in `JaumlConfigLib` are fully preserved but remain deprecated (since 2.0). Applications are encouraged to migrate to instance-based `JaumlConfig.open` methods.
