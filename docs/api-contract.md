# API Contract — Soar Backend ↔ Frontend

> **Canonical source for the wire contract between `soar-be` and `soar-fe`.**
> Every statement here is extracted from backend code, not from observation of running traffic.
> When BE and FE documentation disagree, **this file wins** and the FE doc is corrected.
>
> **Last verified against code**: 2026-08-14

---

## 1. Response envelope

Every endpoint returns `CommonResult<T>` — no exceptions, including errors raised inside servlet
filters.

```json
{ "code": 0, "msg": "", "data": { } }
```

| Field | Type | Notes |
| ----- | ---- | ----- |
| `code` | `Integer` | `0` = success. Any other value = failure. |
| `msg` | `String` | Empty string on success. Human-readable message on failure. |
| `data` | `T` | Present on success. Absent (`null`) on failure. |

Paginated payloads use `PageResult<T>` as the `data`:

```json
{ "code": 0, "msg": "", "data": { "total": 137, "list": [ ] } }
```

`total` is a `Long`, `list` is the page's rows. Note the field is **`list`**, not `records`/`items`.

Source: `soar-common/.../pojo/CommonResult.java`, `.../pojo/PageResult.java`

---

## 2. HTTP status is always 200 — read `code`, not the status line

**This is the single most important rule in this document.**

`ServletUtils.writeJSON()` writes the JSON body and content type but never calls
`response.setStatus(...)`. Every path that produces an error goes through it:

| Situation | Component | HTTP status | Body `code` |
| --------- | --------- | ----------- | ----------- |
| Not authenticated | `AuthenticationEntryPointImpl` | **200** | `401` |
| Authenticated but lacks permission | `AccessDeniedHandlerImpl` | **200** | `403` |
| Missing `tenant-id` header | `TenantSecurityWebFilter` | **200** | `400` |
| Cross-tenant access attempt | `TenantSecurityWebFilter` | **200** | `403` |
| Validation failure, business error, uncaught exception | `GlobalExceptionHandler` | **200** | see §3 |

Consequences for the frontend:

- An axios interceptor keyed on `error.response.status` will **never fire**. Soar's FE correctly keys
  its auth interceptor on `CommonResult.code === 401` instead.
- A `200` response is not proof of success. Every caller must inspect `code`.

Source: `soar-common/.../util/servlet/ServletUtils.java` (no `setStatus` call),
`soar-spring-boot-starter-security/.../core/handler/`

---

## 3. Error codes

Codes 0–999 are reserved for global/system errors; business modules use longer numeric codes.

| Code | Constant | Meaning |
| ---- | -------- | ------- |
| `0` | `SUCCESS` | Success |
| `400` | `BAD_REQUEST` | Invalid request parameters |
| `401` | `UNAUTHORIZED` | Not logged in |
| `403` | `FORBIDDEN` | No permission for this operation |
| `404` | `NOT_FOUND` | Request not found |
| `405` | `METHOD_NOT_ALLOWED` | Unsupported request method |
| `423` | `LOCKED` | Concurrent request rejected, retry later |
| `429` | `TOO_MANY_REQUESTS` | Rate limited |
| `500` | `INTERNAL_SERVER_ERROR` | System error |
| `501` | `NOT_IMPLEMENTED` | Feature not implemented or not enabled |
| `502` | `ERROR_CONFIGURATION` | Invalid configuration |
| `900` | `REPEATED_REQUESTS` | Duplicate request |
| `901` | `DEMO_DENY` | Demo mode, writes disabled |
| `999` | `UNKNOWN` | Unknown error |

**Business codes** are declared per module in `ErrorCodeConstants`, numbered `{module}_{group}_{seq}` —
e.g. infra file metadata uses `1_001_003_00x`, file config uses `1_001_006_00x`.

The frontend should treat any non-zero code it does not specifically handle as "show `msg` to the user".
`msg` is written to be user-facing.

Source: `soar-common/.../exception/enums/GlobalErrorCodeConstants.java`

---

## 4. URL layout

### Prefix is derived from the controller's package — never hardcoded

| Package pattern | Prefix |
| --------------- | ------ |
| `**.controller.admin.**` | `/admin-api` |
| `**.controller.app.**` | `/app-api` |

A controller annotated `@RequestMapping("/system/auth")` in `controller.admin.auth` is reachable at
`/admin-api/system/auth/**`. Configurable under `soar.web.admin-api` / `soar.web.app-api`.

Source: `soar-spring-boot-starter-web/.../config/WebProperties.java`

### Action paths, not REST resources

Soar follows yudao's action-path style. The FE must not assume REST verbs map to resource URLs.

| Action | Method | Path |
| ------ | ------ | ---- |
| Page | `GET` | `/{module}/{entity}/page` |
| List (no paging) | `GET` | `/{module}/{entity}/list` or `/list-all-simple` |
| Get one | `GET` | `/{module}/{entity}/get?id=` |
| Create | `POST` | `/{module}/{entity}/create` |
| Update | `PUT` | `/{module}/{entity}/update` |
| Delete | `DELETE` | `/{module}/{entity}/delete?id=` |
| Batch delete | `DELETE` | `/{module}/{entity}/delete-list?ids=` |
| Export | `GET` | `/{module}/{entity}/export-excel` |

Create and update both take a single `*SaveReqDTO` with a nullable `id` — there are no separate
Create/Update DTOs.

---

## 5. Pagination and sorting

| Param | Type | Default | Constraint |
| ----- | ---- | ------- | ---------- |
| `pageNo` | `Integer` | `1` | `@NotNull`, `@Min(1)` — **1-based**, not 0-based |
| `pageSize` | `Integer` | `10` | `@NotNull`, `@Min(1)`, `@Max(200)` |

`pageSize = -1` (`PageParam.PAGE_SIZE_NONE`) disables pagination, intended for export endpoints. Note
that `@Min(1)` rejects `-1` on any DTO validated as a normal `PageParam`; endpoints wanting the escape
hatch must handle it deliberately.

Endpoints extending `SortablePageParam` also accept `sortingFields`, a **list of objects**:

```
?sortingFields[0].field=createTime&sortingFields[0].order=desc
```

`order` is the string `"asc"` or `"desc"` (`SortingField.ORDER_ASC` / `ORDER_DESC`).

> **Why the FE needs a custom `paramsSerializer`**: `ids` is a primitive array serialized in `repeat`
> form (`?ids=1&ids=2`), while `sortingFields` is an object array needing `allowDots` + indices. One
> global axios array format cannot satisfy both. This is the backend-side reason for the per-key
> serializer described in FE ADR 0006.

Source: `soar-common/.../pojo/PageParam.java`, `.../SortablePageParam.java`, `.../SortingField.java`

---

## 6. Request headers

| Header | Required | Notes |
| ------ | -------- | ----- |
| `Authorization` | On authenticated requests | `Bearer <accessToken>`. Header name is configurable (`SecurityProperties.tokenHeader`, default `Authorization`). A `?token=` query param is also accepted, used by the WebSocket handshake. |
| `tenant-id` | On nearly every request | Numeric tenant id. See below. |
| `visit-tenant-id` | Optional | Lets a system-tenant operator act inside another tenant's context. |

### When `tenant-id` may be omitted

`TenantSecurityWebFilter` rejects any request without `tenant-id` unless the URL is registered as
tenant-ignored. A URL becomes ignored by:

1. A controller method annotated `@TenantIgnore` (auto-registered at startup), or
2. An entry in `soar.tenant.ignore-urls` — **currently unset** in `application.yaml`.

Practical consequence: **login requires `tenant-id`.** `/system/auth/login` is `@PermitAll` but not
`@TenantIgnore`, so a login attempt without the header fails with `code: 400`,
`msg: "Missing tenant-id request header"`.

If a logged-in user sends a `tenant-id` that differs from their own, the filter returns `code: 403`
("No permission to access this tenant's data") rather than silently ignoring it.

Source: `soar-spring-boot-starter-biz-tenant/.../core/security/TenantSecurityWebFilter.java`,
`soar-spring-boot-starter-web/.../core/util/WebFrameworkUtils.java` (`HEADER_TENANT_ID = "tenant-id"`)

---

## 7. Auth flow

### 7.1 Boot — resolve the tenant before anything else

```
GET /admin-api/system/tenant/get-by-website?website=<location.host>
```

`@PermitAll` + `@TenantIgnore` — the one endpoint reachable with no `tenant-id` and no token.

- `website` is validated against `^[a-zA-Z0-9.-]+(:\d{1,5})?$` (host with optional port).
- **On a miss the endpoint returns `code: 0` with `data: null`** — not an error code. It also returns
  `null` when the tenant exists but is disabled. The frontend must branch on `data === null`, not on a
  non-zero `code`.

### 7.2 Login

```
POST /admin-api/system/auth/login
```

Request (`AuthLoginReqDTO`):

| Field | Required | Constraint |
| ----- | -------- | ---------- |
| `username` | yes | 4–30 chars, `^[a-zA-Z0-9]{4,30}$` — letters and digits only |
| `password` | yes | 4–16 chars |
| `captchaVerification` | only when captcha enabled | Gated by validation group; `soar.captcha.enable` is `false` in `application.yaml` today |
| `socialType`, `socialCode`, `socialState` | only for social binding | If `socialType` is set, the other two become mandatory |

Response (`AuthLoginRespDTO`):

```json
{ "userId": 1024, "accessToken": "…", "refreshToken": "…", "expiresTime": "2026-08-14T10:30:00Z" }
```

### 7.3 Refresh

```
POST /admin-api/system/auth/refresh-token?refreshToken=<token>
```

`@PermitAll`. The refresh token travels as a **query parameter on a POST with no body** — not a JSON
body. Returns the same `AuthLoginRespDTO` shape.

A failed refresh raises a `ServiceException` whose message is `"Invalid refresh token"`; that message is
on `GlobalExceptionHandler.IGNORE_ERROR_MESSAGES` so it is returned to the client without a stack trace
in the logs.

### 7.4 Logout

```
POST /admin-api/system/auth/logout
```

`@PermitAll` — a request with an expired or already-revoked token still logs out cleanly.

### 7.5 Permission info

```
GET /admin-api/system/auth/get-permission-info
```

Annotated `@DataPermission(enable = false)` so a scoped user can always read their own permission
payload. Returns `AuthPermissionInfoRespDTO`:

| Field | Type | Notes |
| ----- | ---- | ----- |
| `user` | object | `{ id, nickname, avatar, deptId, username, email }` |
| `roles` | `Set<String>` | Role codes, e.g. `super_admin` |
| `permissions` | `Set<String>` | Permission codes `{module}:{entity}:{action}` |
| `menus` | `MenuDTO[]` | Nested tree via `children` |

---

## 8. Menu DTO — the FE coupling point

`AuthPermissionInfoRespDTO.MenuDTO` drives the entire frontend shell: sidebar, tab dispatch, and
keep-alive. Changing it breaks the FE at runtime, not at build time.

| Field | Type | Consumed by FE for |
| ----- | ---- | ------------------ |
| `id`, `parentId` | `Long` | Tree assembly |
| `name` | `String` | Sidebar label |
| `tabKey` | `String` | **`?tab=<tabKey>` URL dispatch.** Soar-specific — not present in yudao |
| `component` | `String` | File lookup, e.g. `system/user/index` → `src/pages/system/user/index.tsx` |
| `componentName` | `String` | Legacy yudao field |
| `path` | `String` | Route path (yudao heritage; FE uses flat URLs) |
| `icon` | `String` | Iconify seed string |
| `visible` | `Boolean` | Hide from sidebar |
| `keepAlive` | `Boolean` | Whether the FE wraps the tab in `<Activity>` |
| `alwaysShow` | `Boolean` | Show a single-child directory as a directory |
| `children` | `MenuDTO[]` | Nesting |

`tabKey` and `component` are **two independent dispatcher keys** — the URL uses `tabKey`, the file
loader uses `component`. A seeded menu row needs both to be correct or the tab renders not-found.

BUTTON-type menu rows do not appear in `menus`; their `permission` codes appear in `permissions`.

---

## 9. File upload

Two modes, both under `/admin-api/infra/file`:

**Mode 1 — through the backend** (works with every storage type)

```
POST /admin-api/infra/file/upload      Content-Type: multipart/form-data
  file      → @RequestPart, the binary
  directory → @RequestParam, optional sub-directory
```

Returns `CommonResult<String>` where `data` is the access URL.

**Mode 2 — presigned direct upload** (S3-compatible storage only)

```
GET  /admin-api/infra/file/presigned-url?name=<filename>&directory=<optional>
PUT  <the returned uploadUrl>          (direct to storage, no Soar involvement)
POST /admin-api/infra/file/create      (record metadata; body = FileCreateReqDTO)
```

Both modes require the `infra:file:create` permission.

Source: `soar-module-infra/.../controller/admin/file/FileController.java`

---

## 10. Serialization

| Concern | Setting | Effect on the wire |
| ------- | ------- | ------------------ |
| Dates | `spring.jackson.serialization.write-dates-as-timestamps: false` | `Instant` serialises as ISO-8601 (`"2026-08-14T10:30:00Z"`), not epoch millis |
| Unknown fields | `deserialization.fail-on-unknown-properties: false` | The FE may send extra fields without a 400; they are ignored |
| Empty beans | `serialization.fail-on-empty-beans: false` | An object with no serialisable properties yields `{}` rather than an error |
| Upload limits | `max-file-size: 16MB`, `max-request-size: 32MB` | Exceeding either returns `code: 400`, "Uploaded file is too large" |

Backend timestamp columns are `timestamptz` and Java-side `Instant`, so values are UTC on the wire; the
frontend formats to local time via `formatDateTime()`.

Source: `soar-server/src/main/resources/application.yaml`

---

## 11. CORS

`SoarWebAutoConfiguration.corsFilterBean()` registers a `CorsFilter` for `/**` with
`allowedOriginPattern("*")`, all headers, all methods, and `allowCredentials(true)`.

The frontend therefore calls the backend origin directly. **A Vite dev proxy is not needed and must not
be added** — see the FE rule against dev-only workarounds masking BE config gaps.

`soar.web.admin-ui.url` (`http://localhost:5173` in dev) is used for links the backend generates, not
for CORS.

Source: `soar-spring-boot-starter-web/.../config/SoarWebAutoConfiguration.java`

---

## 12. Changing this contract

A change is **breaking** if it alters any of: the envelope shape, an error code's meaning, a header name,
a path, a required request field, or a field the FE reads from `MenuDTO` / `AuthLoginRespDTO` /
`AuthPermissionInfoRespDTO`.

For a breaking change:

1. Update the backend code.
2. Update this file in the same change, including the "Last verified" date.
3. Open the corresponding change in `soar-fe` before merging, or state explicitly that the FE is
   knowingly left behind and record it in that repo's `TECH_DEBT.md`.
4. If the change stems from a decision rather than a fix, write an ADR (`docs/decisions/adr/`) and link
   it here.

Additive changes (a new optional response field, a new endpoint) do not require FE coordination —
`fail-on-unknown-properties: false` on both sides makes them safe.

---

## 13. Open points

| # | Item | Status |
| - | ---- | ------ |
| 1 | `pageSize = -1` (`PAGE_SIZE_NONE`) conflicts with `@Min(1)` on `PageParam.pageSize`. No endpoint currently relies on it. | Unresolved; decide when the first export endpoint needs it |
| 2 | `GlobalExceptionHandler.handleTableNotExists` branches on yudao module table prefixes (`bpm_`, `erp_`, `crm_`, `mp_`, `iot_`, …) that do not exist in Soar, and returns links to `iocoder.cn`. | Dead port artifact; harmless but should be trimmed |
| 3 | No generated client. The FE hand-writes types that mirror BE DTOs, so drift is caught at runtime. | OpenAPI JSON is served at `/v3/api-docs`; codegen not adopted |

---

## References

- `CONVENTIONS.md § API Response Format`, `§ API Endpoints`, `§ Permission Codes`
- `soar-framework/soar-spring-boot-starter-security/README.md` — token lifecycle in depth
- `docs/decisions/adr/0001-opaque-tokens-instead-of-jwt.md` — why tokens are opaque
- `docs/decisions/adr/0004-active-multi-tenancy-via-hibernate-tenantid.md` — why `tenant-id` is mandatory
- `../../soar-fe/AGENTS.md § API Conventions` — the frontend's mirror of this contract
- Swagger UI `/swagger-ui/index.html`, OpenAPI JSON `/v3/api-docs`
