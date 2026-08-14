# 0005. Dynamic queries via JPA `Specification` + `SpecUtils` + Metamodel

Date: 2026-08-14 (retro-documented; decision predates this ADR)
Status: Accepted

## Context

Every admin list endpoint takes a `*PageReqDTO` where most fields are optional: filter by name *if
provided*, by status *if provided*, by a create-time range *if provided*. yudao expresses this with
MyBatis-Plus `LambdaQueryWrapper` and its `xxx(condition, column, value)` overloads.

Spring Data JPA offers three ways to do the same thing, and mixing them produces a repository interface
that is impossible to reason about.

## Decision

Dynamic filtering is expressed as a JPA `Specification`, built with `SpecUtils` helpers and Metamodel
constants:

```java
Specification<FilePO> spec = (root, query, cb) -> {
    List<Predicate> predicates = new ArrayList<>();
    likeIfPresent(predicates, cb, root, FilePO_.name, reqDTO.getName());
    betweenIfPresent(predicates, cb, root, FilePO_.createTime, reqDTO.getCreateTime());
    return cb.and(predicates.toArray(new Predicate[0]));
};
Page<FilePO> page = fileRepository.findAll(spec,
        PageUtils.toPageable(reqDTO, Sort.by(Sort.Direction.DESC, FilePO_.ID)));
return PageUtils.toPageResult(page);
```

Components:

- **`SpecUtils`** (`soar-spring-boot-starter-jpa`) — `eqIfPresent`, `likeIfPresent`, `gteIfPresent`,
  `betweenIfPresent`. Each is a no-op when the value is absent, which is what removes the `if (x != null)`
  ladder.
- **JPA Metamodel** (`FilePO_.name`, `AdminUserPO_.deptId`) — generated at compile time, so a renamed
  field breaks the build instead of failing at runtime like a string attribute name would.
- **`PageUtils`** — converts `PageParam` (`pageNo`/`pageSize`) to `Pageable` and `Page<T>` back to
  `PageResult<T>`.

Derived query methods (`findByStatus`, `findAllByRoleIdIn`) remain fine for **fixed** criteria.

## Alternatives considered

**Derived query methods for everything.** Rejected: N optional filters means 2^N method names. It does
not scale past two filters.

**QueryDSL.** Rejected: an extra code generator and dependency for benefits `Specification` + Metamodel
already provide here.

**Native SQL / JPQL strings per endpoint.** Rejected: string column names, no compile-time safety, and
`Specification` composes with Spring Data's paging and sorting for free.

## Consequences

**Positive**

- Optional filters are declarative one-liners; the shape is identical across every list endpoint.
- Renaming an entity field is a compile error at every query site.
- Paging and sorting come from Spring Data rather than being reimplemented.

**Negative**

- The Criteria API is verbose and unfamiliar compared to `LambdaQueryWrapper`, which makes yudao ports
  slightly less mechanical than elsewhere.
- `SpecUtils` covers only the four predicate shapes above; anything else is hand-written Criteria code.

**Risks**

- Related trap, documented separately in `CONVENTIONS.md § Spring Data @Query projection rule`: a derived
  query whose declared return type is not the entity type (e.g. `Set<Long> findAllByRoleIdIn(...)`)
  compiles but fails at runtime. Non-entity return types require an explicit `@Query` projection.

## References

- `soar-framework/soar-spring-boot-starter-jpa/.../core/util/SpecUtils.java`
- `CONVENTIONS.md § Service Pattern`, `§ Spring Data @Query projection rule`
