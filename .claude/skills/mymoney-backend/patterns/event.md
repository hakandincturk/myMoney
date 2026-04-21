# Pattern: Event, Listener

## Event Class

```java
package com.hakandincturk.core.events;

@Getter
@AllArgsConstructor
public class DomainCreatedEvent {
    private final Domain domain;
}
```

## Event Listener

```java
package com.hakandincturk.eventListeners;

@Component
@RequiredArgsConstructor
public class DomainEventListener {

    private final SomeService someService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDomainCreated(DomainCreatedEvent event) {
        // Side effect logic — runs in new transaction AFTER original commits
        someService.recalculate(event.getDomain());
    }
}
```

## Rules
- Events are simple POJOs with `@Getter` and `@AllArgsConstructor`
- Published via `applicationEventPublisher.publishEvent(new Event(data))`
- Listeners use `AFTER_COMMIT` — never run if original transaction rolls back
- Listeners use `REQUIRES_NEW` — isolated transaction for side effects
- Never call event listeners directly — always go through the event system
