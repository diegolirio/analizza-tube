# analizza-tube

Monorepo: `analizza-tube-api` (Spring Boot, entrada HTTP), `analizza-tube-core`
(application/domain/infrastructure), `buildingBlocks` (contratos base),
`analizza-tube-web` (Next.js) e `analizza-tube-mobile` (Expo). Convenções em
[docs/superpowers/INSTRUCTIONS.md](docs/superpowers/INSTRUCTIONS.md).

`make help` lista os alvos. `make run` sobe o Postgres, a api (`:8080`) e o web
(`:3000`); o mobile roda à parte com `make run-mobile`.

## Instalação

Os vídeos são hospedados pela **yeroon play** (API v1 em
`https://yeroon.streaming.diegolirio.com`). Sem a chave dela, o app sobe, mas
a lista responde "Não foi possível carregar os vídeos.".

### 1. Requisitos

| Ferramenta | Versão | Para quê |
|---|---|---|
| JDK | 25 | api e core (o Gradle Wrapper vem no repo) |
| Node.js | 20.9 ou mais nova | web e mobile |
| Docker | com `docker compose` | Postgres local e testes de integração |
| `make`, `curl`, `jq` | — | alvos do Makefile e cadastro na yeroon |

As portas `5432`, `8080` e `3000` precisam estar livres. Se outro projeto
local estiver usando alguma delas (por exemplo, o `yeroon-stream-poc`), pare-o
antes.

### 2. Dependências e banco

```bash
make install   # npm ci do web e do mobile
make up        # Postgres em Docker; as migrations rodam quando a api sobe
```

### 3. Chave da yeroon play (uma vez)

O administrador da yeroon cadastra o analizza-tube como cliente. A resposta
traz a `apiKey` (`yk_…`) e o `webhookSecret` (`whsec_…`), e é a **única vez**
em que esses valores aparecem. Grave a resposta direto num arquivo, assim os
valores não passam pela tela nem ficam no histórico do shell:

```bash
curl -sS -X POST https://yeroon.streaming.diegolirio.com/admin/tenants \
  -H "X-Admin-Token: $(cat ~/.yeroon-admin-token)" \
  -H "Content-Type: application/json" \
  -d '{"name":"analizza-tube","allowedOrigins":["http://localhost:3000"]}' \
  -o ~/.analizza-tube-yeroon.json -w '%{http_code}\n' && chmod 600 ~/.analizza-tube-yeroon.json
```

A saída esperada é `201`. Rode **uma vez só**, porque cada chamada cria outro
cliente. Depois, gere o `.env` na raiz do repositório (ele está no
`.gitignore`):

```bash
jq -r '"YEROON_API_KEY=\(.apiKey)\nYEROON_WEBHOOK_SECRET=\(.webhookSecret)"' \
  ~/.analizza-tube-yeroon.json > .env && chmod 600 .env
```

O `allowedOrigins` é a origem que pode embutir o player. Se o web rodar em
outro endereço, cadastre esse endereço.

### 4. Subir

O Makefile não lê o `.env`. Exporte-o no shell antes de subir:

```bash
set -a; . ./.env; set +a
make run
```

### 5. Conferir

```bash
curl -s localhost:8080/actuator/health   # {"status":"UP"...}
curl -s localhost:3000/api/videos        # {"videos":[...]}, não 502
```

Abra http://localhost:3000 e envie um vídeo pelo botão **Enviar vídeo**. A
conferência completa, cobrindo upload, retomada, reprodução e falhas, está no
runbook [docs/checkpoints/yeroon-play-plugado.md](docs/checkpoints/yeroon-play-plugado.md).

### Problemas comuns

| Sintoma | Causa provável |
|---|---|
| `/api/videos` responde 502 e o log da api mostra `WARN YEROON_API_KEY ... esta vazia` | O `.env` não foi exportado no shell do `make run`. |
| `/api/videos` responde 502 e o log mostra `ERROR yeroon recusou a chave de API (status 401)` | A chave está errada ou o cliente foi recriado. Refaça o passo 3. |
| `/api/videos` responde 502 e o log mostra `WARN yeroon indisponivel (...)` | A yeroon está fora do ar, em deploy, ou o `YEROON_API_URL` está errado. |
| O `PUT` das partes falha por CORS no navegador | A origem do web não está no CORS do bucket R2 da yeroon. |
| O player não abre no iframe | A origem do web não está nas `allowedOrigins` do cadastro. |
| Erro de porta em uso | Outro projeto ocupa a `5432`, a `8080` ou a `3000`. |

## Variáveis de ambiente

Lidas por `analizza-tube-api/src/main/resources/application.properties`; os
padrões moram lá.

```
YEROON_API_URL=
YEROON_API_KEY=
YEROON_WEBHOOK_SECRET=
```

O web (`analizza-tube-web`) lê `ANALIZZA_API_URL` (padrão `http://localhost:8080`),
o endereço do `-api` para onde seus Route Handlers repassam:

```
ANALIZZA_API_URL=
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
