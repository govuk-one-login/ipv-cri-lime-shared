# ipv-cri-lime-shared

A shared library repository maintained by the Lime team.

## Project Structure

```
ipv-cri-lime-shared/
├── lib-limeade/              # Publishable library subproject
│   ├── src/
│   ├── build.gradle
│   └── gradle.properties     # Subproject artifact ID, version, and description
├── build.gradle              # Root build — shared build logic and publishing config
├── settings.gradle           # Declares included subprojects
├── gradle.properties         # Root properties — group ID, Java version, Gradle flags
└── gradle/
    └── libs.versions.toml    # Centralised dependency version catalog
```

Publishable subprojects follow the `lib-*` naming convention. Only these are included in the `publishAll` task.

## Versioning

Each subproject manages its own version in its `gradle.properties`:

```properties
# lib-limeade/gradle.properties
artifactId=limeade
version=1.0.0
description=Limeade shared library
```

The group ID is defined once at the root level in `gradle.properties`:

```properties
# gradle.properties
group=uk.gov.account.ipv.cri.lime
```

Published coordinates take the form: `{group}:{artifactId}:{version}`

## Publishing

Libraries are published to Maven Central. Publishing is triggered automatically on merge to `main` via the `publish.yml` GitHub Actions workflow.

**The build will skip publishing any version that already exists on Maven Central.** Only a version bump in the subproject's `gradle.properties` will result in a new artifact being published. Non-versioned changes (refactors, test updates, formatting fixes, etc.) merged to `main` will not produce a new release.

To publish locally for testing:

```bash
./gradlew publishAllToMavenLocal
```

Don't forget to include `mavenLocal()` in the consuming projects maven repositories configuration.

## Adding a New Library

1. **Create the subproject directory** following the `lib-*` naming convention:
   ```
   lib-mylib/
   ├── src/main/java/
   ├── build.gradle
   └── gradle.properties
   ```

2. **Set the artifact ID and initial version** in the subproject's `gradle.properties`:
   ```properties
   artifactId=mylib
   version=1.0.0
   description=My new shared library
   ```

3. **Register the subproject** in the root `settings.gradle`:
   ```groovy
   include 'lib-limeade'
   include 'lib-mylib'   // add this line
   ```

4. **Configure the subproject build** in `lib-mylib/build.gradle`. Follow the pattern in `lib-limeade/build.gradle`. Apply the required plugins, declare dependencies from `libs.versions.toml`, and apply any publishing configuration inherited from the root.

The root `build.gradle` automatically picks up any `lib-*` subproject and includes it in `publishAll`.

## Build System

All shared build logic — publishing configuration, code quality rules (Spotless/Google Java Format), test setup, Jacoco coverage thresholds — is managed at the root `build.gradle`. Subproject builds only define what is specific to that module (plugins, dependencies).

**Requirements:**
- Java 21 (Amazon Corretto) — see `.sdkmanrc`
- Gradle 9 (via wrapper — use `./gradlew`)

Run checks locally:

```bash
./gradlew spotlessCheck unitTestCodeCoverageReport
```
