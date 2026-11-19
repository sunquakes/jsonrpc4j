## [3.0.0] - 2026-11-19
- Upgrade JDK from 8 to 17 and adapt to Spring Boot 3 and Spring 6.
- Replace HttpClient and CosurlClient with NettyHttpClient for discovery HTTP requests.
- Improve client and server request handling, including shared client request execution and safer Optional usage.
- Improve channel lifecycle handling and expand client and discovery test coverage.

---

## [3.0.0-alpha] - 2024-02-19
- Upgrade jdk from 8 to 17.
- Adapt to spring-boot 3 and spring 6.
- Remove HttpClient and CosurlClient, Using NettyHttpClient as discovery http client.