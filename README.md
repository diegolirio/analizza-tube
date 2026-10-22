# analizza-tube

Monorepo: `analizza-tube-api` (Spring Boot, entrada HTTP), `analizza-tube-core`
(application/domain/infrastructure), `buildingBlocks` (contratos base),
`analizza-tube-web` (Next.js) e `analizza-tube-mobile` (Expo). Convenções em
[docs/superpowers/INSTRUCTIONS.md](docs/superpowers/INSTRUCTIONS.md).

`make help` lista os alvos. `make run` sobe o Postgres, a api (`:8080`) e o web
(`:3000`); o mobile roda à parte com `make run-mobile`.

## Variáveis de ambiente

Lidas por `analizza-tube-api/src/main/resources/application.properties`; os
padrões moram lá.

```
YEROON_API_URL=
YEROON_API_KEY=
YEROON_WEBHOOK_SECRET=
```

## Testes

| Comando | O que roda |
|---|---|
| `make test-backend` | `./gradlew test`: testes unitários, sem banco |
| `make test-integration` | `./gradlew integrationTest`: classes `*IT` sobre Postgres em Testcontainers (precisa de Docker) |
| `make test-mutation` | `./gradlew :analizza-tube-core:pitest`: teste de mutação dos unitários do `-core`; diagnóstico, fora do `make test` |
| `make test-web` | lint, `typecheck` e Vitest do `analizza-tube-web` |
| `make test-mobile` | `typecheck` e jest-expo do `analizza-tube-mobile` |
| `make test` | todos acima, menos o de mutação |

Os testes de integração moram em `analizza-tube-integration-tests/`, todos
estendendo `support/BaseIntegrationTest`. A regra
`architecture/EntrypointHasIntegrationTestIT` exige um `<Nome>IT` para todo
entrypoint (`@RestController`, método `@Scheduled`, …).
