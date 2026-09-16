# Stream Caching Strategy

- YouTube stream URLs expire after a fixed duration (parsed from `expire=` query param).
- Implement ConcurrentHashMap cache with 20-minute safe TTL window.
