# Pattern: Controller

## Interface (Abstract)

```java
package com.hakandincturk.webapi.controllers.abstracts;

@Tag(name = "Domain Name", description = "Domain description")
@RequestMapping("/domain")
public interface DomainController {

    @Operation(summary = "Short description of the endpoint")
    @GetMapping("/my/active")
    ResponseEntity<ApiResponse<PagedResponse<ListMyDomainsResponseDto>>> listMyActiveDomains(
            @Valid @ModelAttribute DomainFilterRequestDto pageData
    );

    @Operation(summary = "Create a new domain entity")
    @PostMapping("/my")
    ResponseEntity<ApiResponse<Void>> createDomain(
            @Valid @RequestBody CreateDomainRequestDto body
    );
}
```

## Implementation

```java
package com.hakandincturk.webapi.controllers.impl;

@RestController
@RequiredArgsConstructor
public class DomainControllerImpl extends BaseController implements DomainController {

    private final DomainService domainService;

    @Override
    public ResponseEntity<ApiResponse<PagedResponse<ListMyDomainsResponseDto>>> listMyActiveDomains(
            DomainFilterRequestDto pageData) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthentication jwtAuth) {
            Long userId = jwtAuth.getUserId();
            Page<ListMyDomainsResponseDto> result = domainService.listMyActiveDomains(userId, pageData);
            return successPaged("Veriler getirildi", result);
        }
        return error("Yetkisiz islem");
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> createDomain(CreateDomainRequestDto body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthentication jwtAuth) {
            Long userId = jwtAuth.getUserId();
            domainService.createDomain(userId, body);
            return success("Kayit olusturuldu");
        }
        return error("Yetkisiz islem");
    }
}
```

## Rules

- Controller interface holds ALL Spring MVC annotations (`@GetMapping`, `@PostMapping`, `@Valid`, `@Operation`, `@Tag`)
- Implementation has ONLY `@RestController` and `@Override` — no duplicate mapping annotations
- Always extract `userId` from `JwtAuthentication` before calling service
- Use `success()`, `successPaged()`, `error()` from `BaseController`
- For paginated GET: use `@ModelAttribute` on filter DTO
- For POST body: use `@RequestBody` with `@Valid`
- For path params: use `@PathVariable`
