# Skill: Audit & Replace Legacy Dependencies

## When to Use
When porting dependencies from RuoYi-Vue-Pro (or any Spring Boot 2.x project) to Soar (Spring Boot 3.x / Java 21).
Also use when reviewing a new third-party dependency before adding to `soar-dependencies`.

## Context
RuoYi-Vue-Pro was originally built on Spring Boot 2.x.
Many dependencies use `javax.*` namespace, older library versions, or patterns replaced in Spring Boot 3.x.
Soar runs Spring Boot 3.5.14 / Java 21 — all dependencies must be Jakarta EE 10 compatible.

## Detection Rules

### Rule 1: `javax.*` → Remove or Replace with `jakarta.*`
Any dependency with `javax.*` in groupId is Spring Boot 2.x era.

| Legacy (remove) | Modern (Spring Boot BOM manages) |
|---|---|
| `javax.servlet:javax.servlet-api` | Managed — `jakarta.servlet:jakarta.servlet-api` |
| `javax.validation:validation-api` | Managed — `jakarta.validation:jakarta.validation-api` |
| `javax.persistence:javax.persistence-api` | Managed — `jakarta.persistence:jakarta.persistence-api` |
| `javax.annotation:javax.annotation-api` | Managed — `jakarta.annotation:jakarta.annotation-api` |
| `javax.mail:javax.mail-api` | Managed — `jakarta.mail:jakarta.mail-api` |

**Action:** Do NOT declare version. Spring Boot BOM manages all `jakarta.*` APIs.

### Rule 2: Spring Boot BOM Already Manages — Remove Version
If a dependency appears in [Spring Boot Managed Dependencies](https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html), do NOT declare its version in `soar-dependencies`.

Common ones developers accidentally re-declare:
- `org.springframework:spring-*` (framework modules)
- `org.springframework.security:spring-security-*`
- `org.springframework.data:spring-data-*`
- `org.hibernate.orm:hibernate-core`
- `com.fasterxml.jackson.*:jackson-*`
- `org.flywaydb:flyway-core`
- `org.postgresql:postgresql`
- `com.github.ben-manes.caffeine:caffeine`
- `org.projectlombok:lombok`
- `io.lettuce:lettuce-core`
- `org.slf4j:slf4j-api`, `ch.qos.logback:logback-*`
- `org.apache.commons:commons-lang3`
- `commons-codec:commons-codec`

**How to verify:** Search the artifact name at the Spring Boot managed dependencies page for the exact version Soar uses (3.5.14).

### Rule 3: Springdoc v1 → Knife4j (which bundles Springdoc v2)
| Legacy | Reason | Replacement |
|---|---|---|
| `org.springdoc:springdoc-openapi-ui` (v1.x) | Uses `javax.*`, Spring Boot 2.x only | `com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter` |
| `org.springdoc:springdoc-openapi-webmvc-core` (v1.x) | Same | Bundled inside knife4j-openapi3 |
| `io.springfox:springfox-*` | Abandoned, Swagger 2.x | Same as above |

**Note:** `knife4j-openapi3-jakarta-spring-boot-starter` already includes `springdoc-openapi-starter-webmvc-ui` (v2.x) as transitive dependency. Do NOT declare both.

### Rule 4: Libraries That Changed ArtifactId in New Versions
| Legacy artifactId | Modern artifactId | Notes |
|---|---|---|
| `mysql:mysql-connector-java` | `com.mysql:mysql-connector-j` | Soar uses PostgreSQL, but flag if found |
| `org.mybatis.spring.boot:mybatis-spring-boot-starter` | N/A | Soar uses JPA, remove entirely |
| `com.baomidou:mybatis-plus-boot-starter` | N/A | Same — remove, Soar uses JPA |
| `com.alibaba:druid-spring-boot-starter` | Evaluate need | Spring Boot HikariCP is default and sufficient |
| `commons-io:commons-io` | Still valid | But check if Spring/JDK built-in alternatives exist |
| `cn.hutool:hutool-all` | Evaluate per-use | Prefer JDK 21 built-ins or targeted `hutool-*` modules |

### Rule 5: Redundant With Spring Boot Starters
Some dependencies are already included transitively via starters. Declaring them explicitly causes version conflicts.

| Redundant declaration | Already included via |
|---|---|
| `org.hibernate.validator:hibernate-validator` | `spring-boot-starter-validation` |
| `com.fasterxml.jackson.core:jackson-databind` | `spring-boot-starter-web` |
| `org.aspectj:aspectjweaver` | `spring-boot-starter-aop` |
| `io.lettuce:lettuce-core` | `spring-boot-starter-data-redis` |
| `org.yaml:snakeyaml` | `spring-boot-starter` |

### Rule 6: China-Ecosystem Libraries — Evaluate Before Porting
RuoYi uses many libraries popular in Chinese development but less common internationally. Each needs evaluation:

| Library | Purpose | Decision Guide |
|---|---|---|
| `cn.hutool:hutool-*` | Utility (string, date, http, etc.) | Check if JDK 21 or Apache Commons covers the use case. Port only specific hutool modules actually used, not `hutool-all` |
| `com.alibaba:fastjson2` | JSON parsing | Spring Boot uses Jackson — do NOT add a second JSON library |
| `com.alibaba:easyexcel` | Excel export/import | Keep if Soar needs Excel features |
| `com.alibaba:transmittable-thread-local` | ThreadLocal across thread pools | Evaluate if needed — Spring Security has its own propagation |
| `com.xingyuv:spring-boot-starter-captcha-plus` | CAPTCHA | Evaluate if needed for Soar auth |
| `com.github.binarywang:wx-java-*` | WeChat integration | Remove — Soar does not need WeChat |

## Audit Process

### Step 1: Extract dependency list
```bash
# From RuoYi dependencies POM, list all managed artifacts
grep -E "<artifactId>|<version>" yudao-dependencies/pom.xml | paste - - | sort
```

### Step 2: Classify each dependency
For each dependency, ask in order:
1. Is it `javax.*`? → **Remove** (Rule 1)
2. Is it in Spring Boot BOM? → **Remove version** (Rule 2)
3. Is it Spring Boot 2.x only? → **Find replacement** (Rule 3, 4)
4. Is it redundant with a starter? → **Remove** (Rule 5)
5. Is it China-ecosystem? → **Evaluate** (Rule 6)
6. None of the above → **Keep**, declare version in `soar-dependencies`

### Step 3: Verify replacement
For each replacement candidate:
1. Check Maven Central for latest stable version
2. Confirm it supports Jakarta EE 10 / Spring Boot 3.x (check release notes or README)
3. Check if Spring Boot BOM already manages it
4. If not managed, add to `soar-dependencies` with version property

### Step 4: Verify no conflicts
```bash
# After updating POMs
mvn dependency:tree -Dverbose | grep "omitted for conflict"
# Should output nothing
```

## Output Format
When reporting audit results, use this format:

```
## Dependency Audit: {source}

### Remove (legacy)
- `javax.servlet:javax.servlet-api:2.5` — javax namespace, BOM manages jakarta equivalent

### Remove version (BOM managed)
- `org.postgresql:postgresql` — Spring Boot BOM manages, remove <version>

### Replace
- `org.springdoc:springdoc-openapi-ui:1.8.0` → `com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter:4.5.0`

### Evaluate
- `cn.hutool:hutool-all:5.8.32` — check which hutool modules are actually used

### Keep
- `io.jsonwebtoken:jjwt-api:0.13.0` — not in BOM, needed for JWT auth
```

## Verification Checklist
- [ ] No `javax.*` dependencies remain
- [ ] No version declared for Spring Boot BOM-managed libraries
- [ ] No duplicate JSON library (only Jackson)
- [ ] No MyBatis/MyBatis-Plus dependencies (Soar uses JPA)
- [ ] No Spring Boot 2.x-only libraries (springdoc v1, springfox)
- [ ] `mvn dependency:tree` shows no version conflicts
- [ ] All non-BOM dependencies have version property in `soar-dependencies`
