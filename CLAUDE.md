# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew build          # Compile and package
./gradlew bootRun        # Run the application
./gradlew test           # Run all tests
./gradlew test --tests "com.subcharacter.springbatchforharness.SomeTest"  # Run a single test
./gradlew clean build    # Clean rebuild
```

Java 21 is required (configured via Gradle toolchain).

## Stack

- **Spring Boot 4.0.5** + **Spring Batch** — batch job framework
- **H2** (in-memory) — default database; Spring Batch uses it for job metadata tables
- **Lombok** — used for boilerplate reduction; annotate models with `@Data`, `@Builder`, etc.

## Architecture

Spring Batch jobs follow a `Job → Step → ItemReader/ItemProcessor/ItemWriter` pattern. The expected structure as jobs are added:

```
src/main/java/com/subcharacter/springbatchforharness/
├── SpringBatchForHarnessApplication.java   # Entry point
├── job/          # @Configuration classes defining Job beans
├── step/         # Step definitions (readers, processors, writers)
└── domain/       # Entity/model classes
```

Batch job metadata (execution history, step context) is persisted automatically to H2 via `spring-batch-jdbc`. The H2 console is available at `/h2-console` when running locally.

## Worktree

모든 작업은 worktree에서 진행한다. worktree는 프로젝트 내부 `.worktrees/` 폴더에 생성한다.

```bash
git worktree add .worktrees/<작업명> -b <브랜치명>
```

> 형제 폴더 방식(`git worktree add ../<작업명> -b <브랜치명>`)도 가능하지만, 이 프로젝트에서는 사용하지 않는다.
