# yeroon play plugado no my-tube — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** O my-tube envia, lista e toca vídeos pela API v1 do yeroon
play, com o mesmo visual do yeroon play, e recebe o webhook assinado.

**Architecture:** O `-core` tem um cliente da v1 (camada anticorrupção) e
guarda só `video(id = videoId do yeroon)` e `webhook_receipt`. O `-api` expõe
`/videos/**`, que repassa à v1 com a chave, mais `/webhooks/yeroon`. O `-web`
copia o visual e o upload do yeroon-stream-poc-web e fala só com o próprio
Next, que repassa ao `-api`. A Task 1 acrescenta ao yeroon a rota de partes
enviadas, para a retomada do upload.

**Tech Stack:** Java 25, Spring Boot 4.1, Gradle, Postgres 17 + Flyway,
`RestClient`, Testcontainers (`RestTestClient`), Next.js 16.4, Tailwind v4,
Vitest.

**Spec:** `docs/superpowers/specs/2026-10-21-yeroon-play-plugado-design.md`

## Global Constraints

**Repos e pacotes**
- **Repo my-tube:** `/Users/diegolirio/Documents/Github/my-tube`,
  pacote base `br.com.mytube`. Siga o `docs/superpowers/INSTRUCTIONS.md`:
  - rota em `-api/presenter/routes/<agregado>/<casoDeUso>/XRoute`, com o
    Request no mesmo pacote;
  - Command/Query, Handler e Result em
    `-core/application/<agregado>/<casoDeUso>/`;
  - interfaces de repositório em `domain/repositories/`;
  - JPA em `infrastructure/repositories/`;
  - cliente externo em `infrastructure/data/anticorruptionLayer/yeroon/`,
    com a implementação em `.../yeroon/impl/`.
- **Repo yeroon:** `/Users/diegolirio/Documents/Github/yeroon-stream-poc`,
  pacote `br.com.yeroon.streampoc`. Trabalhe num branch novo a partir da
  `origin/main`.
- **Contratos do buildingBlocks:**
  - `ResultCommand<R>` + `ResultCommandHandler<C,R>`;
  - `QueryBase<R>` + `QueryHandler<Q,R>`;
  - regra quebrada lança `BusinessRulesBrokenException`.

**Erros e testes**
- Erros em `ProblemDetail`, com `spring.mvc.problemdetails.enabled=true` no
  `application.properties` do `-api`:
  - regra quebrada → 422 com `errors[{code,message}]`;
  - não encontrado → 404 com corpo genérico.
- Todo `@RestController` tem `<Nome>IT extends BaseIntegrationTest` (regra
  ArchUnit). Nenhum teste chama a v1 real: o dublê `@Primary` fica em
  `support/TestConfig`.
- Os testes de integração precisam do Docker ligado.

**Endereços e variáveis**
- **API do yeroon dev:** `https://yeroon.streaming.diegolirio.com`.
- **Web do yeroon dev** (`embedUrl`): `https://yeroon.streaming.poc.diegolirio.com`.
- **Variáveis novas, todas sem valor no README** (seção *Variáveis de
  ambiente*, no mesmo commit que a introduz):
  - `YEROON_API_URL`, com padrão a URL da API acima;
  - `YEROON_API_KEY`;
  - `YEROON_WEBHOOK_SECRET`;
  - `MY_TUBE_API_URL`, com padrão `http://localhost:8080`.

**Segredos**
- A chave `yk_` e o segredo `whsec_` nunca aparecem no `-web`, em log ou no
  git. No log, use no máximo os 10 primeiros caracteres da chave.

**Mesmo formato JSON da `/api` do yeroon**, para o `lib/upload.ts` copiado
funcionar sem mudança:
- init `{videoId, partSizeBytes, partCount}`;
- part-urls `{urls:[{partNumber,url}]}`;
- parts `{parts:[{partNumber,etag,sizeBytes}]}`;
- complete `{videoId,status}`;
- list `{videos:[{videoId,fileName,status,createdAt,thumbnailUrl}]}`;
- get `{videoId,fileName,status,failureReason,renditions,thumbnailUrl}`.

**Webhook**
- Assinatura `X-Yeroon-Signature: t=<epoch>,v1=<hex>`, com
  `v1 = HMAC-SHA256(YEROON_WEBHOOK_SECRET, "<epoch>.<corpo bruto>")`.
- Compare em tempo constante e recuse diferença de mais de 300 s (`401`).
- Sem segredo configurado, recuse sempre (`401`).

## Review Focus

1. **Vídeo de outro cliente do yeroon, ou id inventado, nas rotas
   `/videos/{id}/**`.** O esperado é `404` sem chamar a v1. Os ITs da Task 4
   e da Task 5 começam por esse caso.
2. **O yeroon fora do ar ou com chave errada.** O esperado é `502`
   `ProblemDetail` com mensagem genérica, sem a chave no corpo nem no log
   inteira. Teste no `YeroonClientImplTest` da Task 3 (401 → `YeroonUnavailableException`)
   e no `ListVideosRouteIT` da Task 5 (o dublê lança → 502).
3. **Webhook repetido, com corpo alterado ou com relógio adiantado.** O
   esperado é `200` para o repetido (sem segunda linha) e `401` para os
   outros dois. Teste no `ReceiveYeroonWebhookRouteIT` da Task 6.
4. **Tocar um vídeo que ainda não está pronto.** O esperado é `409` e a tela
   mostrando o status, sem iframe. Teste no `IssuePlaybackRouteIT` da Task 5
   e no `VideoDetail.test.tsx` da Task 7.
5. **A chave vazando para o navegador.** O esperado é que nenhum `yk_` apareça
   em `.next/static`. Passo de verificação da Task 7 (`grep -r "yk_" .next/static`
   → vazio).

---

### Task 1: yeroon — `GET /v1/videos/{id}/upload/parts`

**Files** (repo yeroon, branch `feat/v1-upload-parts` a partir de `origin/main`):
- Create: `yeroon-stream-poc-api/src/main/java/br/com/yeroon/streampoc/presenter/routes/v1/video/listUploadedParts/V1ListUploadedPartsRoute.java`
- Test: `yeroon-stream-poc-integration-tests/src/test/java/br/com/yeroon/streampoc/presenter/routes/v1/V1ListUploadedPartsRouteIT.java`
- Modify: `docs/api/v1.md`, para incluir na tabela de Upload a linha
  `GET /v1/videos/{id}/upload/parts` → `{"parts":[{"partNumber","etag","sizeBytes"}]}`
  ("partes já enviadas, para retomar").

**Interfaces:**
- Consumes: `ListUploadedPartsQuery(UUID tenantId, UUID videoId)`,
  `ListUploadedPartsHandler`, `ListUploadedPartsResult(List<UploadedPart>)`
  (já existem no core) e `TenantIds.of(Authentication)`.
- Produces: `@GetMapping("/v1/videos/{id}/upload/parts") ListUploadedPartsResult list(Authentication caller, @PathVariable UUID id)`.

- [ ] **Step 1: Escrever o IT**, estendendo `PlatformHttpSupport`, com o
  `V1PresignUploadPartsRouteIT` como molde. Testes:
  - `listaAsPartesEnviadasDoProprioVideo`: `iniciar(cliente, 12*MB)`, depois
    `enviarParte(urlDaParte(cliente,id,1), 10*MB)`. O GET devolve `parts` com
    um item, `partNumber == 1` e `etag` igual ao retornado.
  - `videoDeOutroClienteRetorna404`.
  - `semChaveRetorna401`.
- [ ] **Step 2: Rodar `./gradlew :yeroon-stream-poc-integration-tests:integrationTest --tests '*V1ListUploadedPartsRouteIT'`.**
  Esperado: FAIL, com 404 de rota inexistente no primeiro teste.
- [ ] **Step 3: Criar a rota**, que chama
  `handler.handle(new ListUploadedPartsQuery(TenantIds.of(caller), id))`.
  A cadeia `/v1/**` do Spring Security já exige a chave.
- [ ] **Step 4: Rodar o mesmo comando.** Esperado: PASS. Rode também
  `./gradlew build`.
- [ ] **Step 5: Commit, push do branch e PR para a `main`.** O CI
  (`ci.yml`) precisa ficar verde. O merge dispara o deploy, junto com o dos
  secrets (pré-requisito do usuário no spec).

### Task 2: Base — migration, domínio, erros

**Files** (my-tube):
- Create: `my-tube-core/src/main/resources/db/migration/V1__video_e_webhook.sql`,
  com:
  - `video(id uuid primary key, created_at timestamptz not null)`;
  - `webhook_receipt(delivery_id varchar(100) primary key, event_type varchar(50) not null, video_id uuid, received_at timestamptz not null)`.
- Create, no `domain/`:
  - `Video.java`, com `static Video register(UUID yeroonVideoId, Instant now)`
    e os getters `getId()` e `getCreatedAt()`;
  - `VideoNotFoundException.java`.
- Create, no `domain/repositories/`:
  - `VideoRepository.java`, com `void save(Video)`, `boolean existsById(UUID)`
    e `Set<UUID> findAllIds()`;
  - `WebhookReceiptRepository.java`, com
    `boolean registerIfNew(String deliveryId, String eventType, UUID videoId, Instant at)`.
- Create, no `infrastructure/repositories/`: `VideoJpaEntity`,
  `VideoJpaRepository`, `VideoRepositoryImpl`, `WebhookReceiptJpaRepository`
  e `WebhookReceiptRepositoryImpl`. O `registerIfNew` usa um insert nativo
  `... on conflict (delivery_id) do nothing` e devolve `linhas == 1`.
- Create: `my-tube-api/src/main/java/br/com/mytube/presenter/configuration/exception/GlobalExceptionHandler.java`.
  Mapeamentos:
  - `BusinessRulesBrokenException` → 422 `errors`;
  - `VideoNotFoundException` → 404 "Vídeo não encontrado.".

  As exceções do yeroon entram na Task 3.
- Modify: `my-tube-api/src/main/resources/application.properties`,
  com `spring.mvc.problemdetails.enabled=true`.
- Test: `my-tube-integration-tests/src/test/java/br/com/mytube/infrastructure/VideoRepositoryIT.java`.

- [ ] **Step 1: Escrever o `VideoRepositoryIT`:**
  - `salvaELeOsIds`: dois `Video.register`, e `findAllIds()` contém os dois;
  - `existsById`;
  - `registerIfNewSoAceitaAPrimeiraVez`: o mesmo `deliveryId` duas vezes →
    `true` e depois `false`.
- [ ] **Step 2: Rodar `make test-integration`.** Esperado: FAIL (classes
  inexistentes).
- [ ] **Step 3: Implementar** a migration, o domínio, os repositórios e o
  handler de erros.
- [ ] **Step 4: Rodar `make test-integration` e `make test-backend`.**
  Esperado: PASS.
- [ ] **Step 5: Commit**: `feat: video and webhook_receipt tables with repositories`.

### Task 3: Cliente da v1 (anticorrupção)

**Files:**
- Create: `my-tube-core/src/main/java/br/com/mytube/infrastructure/configuration/YeroonProperties.java`.
  É `@ConfigurationProperties("yeroon") record YeroonProperties(URI apiUrl, String apiKey, String webhookSecret, Duration timeout)`,
  com padrão `apiUrl=https://yeroon.streaming.diegolirio.com` e `timeout=10s`.
  No `application.properties` do `-api`:
  - `yeroon.api-url=${YEROON_API_URL:https://yeroon.streaming.diegolirio.com}`;
  - `yeroon.api-key=${YEROON_API_KEY:}`;
  - `yeroon.webhook-secret=${YEROON_WEBHOOK_SECRET:}`.

  E as linhas no README.
- Create, em `infrastructure/data/anticorruptionLayer/yeroon/`:
  - `YeroonClient.java` (interface);
  - os records do contrato;
  - as exceções.
- Create: `.../yeroon/impl/YeroonClientImpl.java`.
- Modify: `GlobalExceptionHandler`, com:
  - `YeroonRejectedException` → 422, repassando `errors`;
  - `YeroonNotFoundException` → 404 genérico;
  - `YeroonConflictException` → 409;
  - `YeroonUnavailableException` → 502 "Serviço de vídeo indisponível.".
- Test: `my-tube-core/src/test/java/.../infrastructure/data/anticorruptionLayer/yeroon/impl/YeroonClientImplTest.java`.
  É unitário, contra um `com.sun.net.httpserver.HttpServer` local, como o
  `StorageConfigurationTest` do yeroon.

**Interfaces:**

Produces:

```java
public interface YeroonClient {
    YeroonUpload initUpload(String fileName, String contentType, long sizeBytes);
    List<YeroonPartUrl> partUrls(UUID videoId, List<Integer> partNumbers);
    List<YeroonUploadedPart> uploadedParts(UUID videoId);
    YeroonCompleted complete(UUID videoId, List<YeroonPart> parts);
    void cancel(UUID videoId);
    YeroonVideo get(UUID videoId);
    List<YeroonVideoSummary> list();
    YeroonPlayback playbackToken(UUID videoId);
}
record YeroonUpload(UUID videoId, long partSizeBytes, int partCount) {}
record YeroonPartUrl(int partNumber, URI url) {}
record YeroonUploadedPart(int partNumber, String etag, long sizeBytes) {}
record YeroonPart(int partNumber, String etag) {}
record YeroonCompleted(UUID videoId, String status) {}
record YeroonVideo(UUID videoId, String fileName, String status, String failureReason, List<String> renditions, URI thumbnailUrl) {}
record YeroonVideoSummary(UUID videoId, String fileName, String status, Instant createdAt, URI thumbnailUrl) {}
record YeroonPlayback(URI embedUrl, Instant expiresAt) {}
class YeroonRejectedException extends RuntimeException { List<YeroonError> errors(); }   // 422
record YeroonError(String code, String message) {}
class YeroonNotFoundException extends RuntimeException {}    // 404
class YeroonConflictException extends RuntimeException {}    // 409
class YeroonUnavailableException extends RuntimeException {} // 401/403/5xx/rede
```

- [ ] **Step 1: Escrever o `YeroonClientImplTest`**, com o servidor local
  gravando método, caminho, `Authorization` e corpo:
  - `initUploadMandaBearerECorpo`: espera `POST /v1/videos/uploads`,
    `Authorization: Bearer yk_teste` e corpo com
    `fileName`/`contentType`/`sizeBytes`; mapeia a resposta 201.
  - `uploadedPartsLeAsPartes`: `GET /v1/videos/{id}/upload/parts`.
  - `playbackLeEmbedUrl`: `POST /v1/videos/{id}/playback-token` → só
    `embedUrl` e `expiresAt`.
  - `erro422ViraRejectedComOsErros`: corpo `{"errors":[{"code":"video.size","message":"…"}]}`.
  - `erro404ViraNotFound`.
  - `erro409ViraConflict`.
  - `erro401ViraUnavailableSemAChaveNaMensagem`: `getMessage()` não contém
    `yk_teste`.
  - `servidorForaViraUnavailable`: porta fechada.
- [ ] **Step 2: Rodar `./gradlew :my-tube-core:test --tests '*YeroonClientImplTest'`.**
  Esperado: FAIL.
- [ ] **Step 3: Implementar o `YeroonClientImpl`** com `RestClient`
  (`baseUrl=apiUrl`, header padrão `Authorization`, timeout pelo
  `JdkClientHttpRequestFactory`) e um `defaultStatusHandler` que mapeia os
  status para as exceções. Os corpos são o JSON de `docs/api/v1.md`.
- [ ] **Step 4: Rodar o teste.** Esperado: PASS. Rode também
  `make test-backend`.
- [ ] **Step 5: Commit**: `feat: yeroon v1 client in the anticorruption layer`.

### Task 4: Rotas de upload

**Files:**
- Create, em `my-tube-core/.../application/video/`, Command (ou Query),
  Handler e Result de cada um:
  - `initUpload`: grava `Video.register(videoId)` e devolve `YeroonUpload`;
  - `presignParts`;
  - `listUploadedParts`;
  - `completeUpload`;
  - `cancelUpload`.

  Todos, menos o `initUpload`, começam com
  `if (!videos.existsById(id)) throw new VideoNotFoundException()`.
- Create, em `my-tube-api/.../presenter/routes/video/<casoDeUso>/`:
  - `InitUploadRoute` (`POST /videos/uploads` → 201);
  - `PresignUploadPartsRoute` (`POST /videos/{id}/upload/part-urls`);
  - `ListUploadedPartsRoute` (`GET /videos/{id}/upload/parts`);
  - `CompleteUploadRoute` (`POST /videos/{id}/upload/complete`);
  - `CancelUploadRoute` (`DELETE /videos/{id}/upload` → 204).
- Create: `my-tube-integration-tests/.../support/FakeYeroonClient.java`,
  um `implements YeroonClient` em memória, com:
  - `Map<UUID, YeroonVideo>`;
  - um contador de chamadas;
  - `failNext(RuntimeException)`;
  - `reset()`.

  Registre-o como `@Bean @Primary` no `TestConfig` e chame `reset()` num
  `@BeforeEach` da base.
- Test: um `<Rota>IT` por rota, em `presenter/routes/video/`.

**Interfaces:**
- Consumes: `YeroonClient` (Task 3) e `VideoRepository` (Task 2).
- Produces: os caminhos e formatos JSON das *Global Constraints*, que a
  Task 7 usa.

- [ ] **Step 1: Escrever os ITs.**
  - `InitUploadRouteIT`:
    - 201 com `videoId` igual ao do fake;
    - `videos.existsById` fica verdadeiro;
    - o fake lança `YeroonRejectedException` → 422 com `errors[0].code == "video.size"`.
  - Nas outras quatro rotas:
    - `idForaDaTabelaRetorna404SemChamarAV1`: o contador do fake fica em 0;
    - o caso feliz confere o JSON;
    - no `ListUploadedPartsRouteIT`, `parts[0].etag` igual ao do fake.
- [ ] **Step 2: Rodar `make test-integration`.** Esperado: FAIL.
- [ ] **Step 3: Implementar** os handlers e as rotas.
- [ ] **Step 4: Rodar `make test-integration`.** Esperado: PASS. A regra
  ArchUnit também passa, porque cada rota tem o seu IT.
- [ ] **Step 5: Commit**: `feat: upload routes proxied to yeroon v1`.

### Task 5: Rotas de leitura e reprodução

**Files:**
- Create, em `-core/application/video/`:
  - `listVideos`: `list()` da v1, filtrado por `videos.findAllIds()`;
  - `getVideo`;
  - `issuePlayback`: devolve `YeroonPlayback`.
- Create, nas rotas:
  - `ListVideosRoute` (`GET /videos`);
  - `GetVideoRoute` (`GET /videos/{id}`);
  - `IssuePlaybackRoute` (`POST /videos/{id}/playback`, com
    `Cache-Control: no-store`).
- Test: `ListVideosRouteIT`, `GetVideoRouteIT` e `IssuePlaybackRouteIT`.

- [ ] **Step 1: Escrever os ITs.**
  - `ListVideosRouteIT`: o fake tem os vídeos A e B, e só A está na tabela →
    a lista tem só A. Se o fake lança `YeroonUnavailableException` → 502, e
    o corpo não contém `yk_`.
  - `GetVideoRouteIT`: 404 para id fora da tabela; 200 com `status` do fake.
  - `IssuePlaybackRouteIT`:
    - 200 `{embedUrl, expiresAt}`, sem `token` nem `hlsUrl` no corpo;
    - o fake lança `YeroonConflictException` → 409;
    - id fora da tabela → 404.
- [ ] **Step 2: Rodar `make test-integration`.** Esperado: FAIL.
- [ ] **Step 3: Implementar.**
- [ ] **Step 4: Rodar `make test-integration`.** Esperado: PASS.
- [ ] **Step 5: Commit**: `feat: list, get and playback routes over yeroon v1`.

### Task 6: Webhook do yeroon

**Files:**
- Create: `my-tube-api/.../presenter/configuration/security/YeroonWebhookSignature.java`,
  com `static boolean verify(String secret, String header, String rawBody, Instant now)`
  e tolerância de 300 s.
- Create: `-core/application/webhook/receiveYeroonEvent/`, com
  `ReceiveYeroonEventCommand(String deliveryId, String eventType, UUID videoId)`
  e um handler que chama `receipts.registerIfNew(...)` e loga
  `"yeroon {eventType} video={videoId} delivery={deliveryId} nova={bool}"`.
- Create: `-api/presenter/routes/webhook/receiveYeroonWebhook/ReceiveYeroonWebhookRoute.java`,
  com `POST /webhooks/yeroon`, `@RequestBody String rawBody` e os headers
  `X-Yeroon-Signature`, `X-Yeroon-Delivery` e `X-Yeroon-Event`.
  - Assinatura inválida → 401 `ProblemDetail` sem detalhe.
  - Válida → lê `data.videoId` do corpo e responde 200.
- Test:
  - `my-tube-api/src/test/java/.../YeroonWebhookSignatureTest.java`,
    unitário;
  - `ReceiveYeroonWebhookRouteIT`.

- [ ] **Step 1: Escrever o `YeroonWebhookSignatureTest`.** Monte o header com
  `HmacSHA256(secret, t + "." + body)`.
  - `assinaturaValidaPassa`;
  - `corpoAlteradoFalha`;
  - `epochComMaisDe300sFalha` (`now = t + 301`);
  - `headerMalformadoFalha` (`"v1=abc"` e `""`);
  - `segredoVazioFalhaSempre`.
- [ ] **Step 2: Escrever o `ReceiveYeroonWebhookRouteIT`**, com o segredo
  `whsec_teste` vindo de `@DynamicPropertySource` ou das properties de teste:
  - `avisoValidoRegistraEResponde200`;
  - `avisoRepetidoResponde200SemSegundaLinha`: `select count(*)` = 1;
  - `assinaturaErradaResponde401SemRegistrar`.
- [ ] **Step 3: Rodar `make test-backend` e `make test-integration`.**
  Esperado: FAIL.
- [ ] **Step 4: Implementar.**
- [ ] **Step 5: Rodar de novo.** Esperado: PASS.
- [ ] **Step 6: Commit**: `feat: signed yeroon webhook receiver with dedupe`.

### Task 7: Web com o visual do yeroon play

**Files** (copiar de
`/Users/diegolirio/Documents/Github/yeroon-stream-poc/yeroon-stream-poc-web/src/`
para `my-tube-web/src/`):
- **Copiar como estão** (com os testes):
  - `app/globals.css`;
  - `components/icons.tsx`, `AppShell.tsx`, `VideoList.tsx`,
    `UploadDialog.tsx`, `UploadForm.tsx`;
  - `lib/upload.ts`, `lib/videoStatus.ts`, `lib/repasse.ts`.
- **Ajustes:**
  - `lib/repasse.ts`: usa `process.env.MY_TUBE_API_URL ?? "http://localhost:8080"`.
  - `AppShell`: o logo é "my <span italic accent>tube</span>".
  - `app/layout.tsx`: fontes Geist, Geist Mono e Source Serif 4,
    `lang="pt-BR"` e título "my tube".
  - `app/page.tsx`: o mesmo estado vazio "Escolha um vídeo".
  - `app/upload/page.tsx`.
  - `app/videos/[id]/page.tsx`.
- **Route Handlers** em `app/api/videos/` que repassam ao `-api` `/videos/...`:
  - `route.ts` (GET);
  - `uploads/route.ts` (POST);
  - `[id]/route.ts` (GET);
  - `[id]/upload/route.ts` (DELETE);
  - `[id]/upload/parts/route.ts` (GET);
  - `[id]/upload/part-urls/route.ts` (POST);
  - `[id]/upload/complete/route.ts` (POST);
  - `[id]/playback/route.ts` (POST).

  Cada um chama `repasse(request, "/videos/...")` e, quando lê só dados
  locais, faz `await connection()`, como no yeroon.
- **Create** `components/VideoDetail.tsx`, que substitui o player HLS:
  - acompanha o status (polling de 5 s até `PRONTO`/`FALHOU`, como no yeroon);
  - em `PRONTO`, faz `POST /api/videos/{id}/playback` e renderiza
    `<iframe title="Player" src={embedUrl} allow="autoplay; fullscreen; picture-in-picture" allowFullScreen className="aspect-video w-full rounded-2xl border border-line">`;
  - ouve `message` com `data.source === "yeroon-player"`, e `type === "error"`
    mostra "Não foi possível tocar o vídeo." no quadro.
  - Não há `VideoPlayer.tsx` nem `hls.js`.
- **Test:**
  - os `.test.tsx` copiados, ajustando o texto do logo;
  - `components/VideoDetail.test.tsx`.
- **Modify:** `next.config.ts`, para tirar `cacheComponents` e
  `partialPrefetching` como no yeroon. Isso só é obrigatório se o build
  travar; senão, deixe como está.

- [ ] **Step 1: Copiar os testes e escrever o `VideoDetail.test.tsx`:**
  - `prontoMostraOIframeComOEmbedUrl`: fetch mockado; `/api/videos/v1` →
    `PRONTO`; `/api/videos/v1/playback` → `{embedUrl:"https://p/embed/x"}`.
    Assert: `getByTitle("Player")` tem `src` `"https://p/embed/x"`.
  - `processandoMostraStatusSemIframe`: `queryByTitle("Player")` é null, e o
    texto "CONVERTENDO" aparece.
  - `erroDoPlayerMostraMensagem`: dispara
    `new MessageEvent("message",{data:{source:"yeroon-player",type:"error"}})`,
    e "Não foi possível tocar o vídeo." aparece.
- [ ] **Step 2: Rodar `make test-web`.** Esperado: FAIL.
- [ ] **Step 3: Copiar, ajustar e implementar** como acima.
- [ ] **Step 4: Rodar `make test-web`.** Esperado: lint, typecheck e testes
  PASS.
- [ ] **Step 5: Rodar `cd my-tube-web && npm run build && grep -r "yk_" .next/static | wc -l`.**
  Esperado: `0`.
- [ ] **Step 6: Commit**: `feat(web): yeroon play look with upload modal and embedded player`.

### Task 8: Runbook, variáveis e contexto

**Files:**
- Create: `docs/checkpoints/yeroon-play-plugado.md` (`type: screen-web`), com
  as quatro seções do `docs/checkpoints/README.md`. Conteúdo:
  - **Preparação:**
    - os pré-requisitos do spec;
    - o `curl` de cadastro com `allowedOrigins=["http://localhost:3000"]`;
    - `YEROON_API_KEY` e `YEROON_WEBHOOK_SECRET` no `.env` local (fora do
      git);
    - `make up`, `make run`.
  - **O que precisa bater:**
    - enviar pelo modal, com os PUTs indo para `r2.cloudflarestorage.com`;
    - a lista virando "Pronto";
    - o iframe tocando;
    - nenhum `yk_` no DevTools (Network e Sources).
  - **Tentar quebrar:**
    - fechar a aba no meio do upload e retomar com o mesmo arquivo;
    - um id inventado na URL → "Vídeo não encontrado.";
    - parar a rede do yeroon (`YEROON_API_URL` errada) → mensagem de
      indisponível.
  - **Opcional:** webhook ponta a ponta com
    `cloudflared tunnel --url http://localhost:8080`, cadastrando
    `webhookUrl` = URL do túnel + `/webhooks/yeroon`.
- Modify: `docs/superpowers/INSTRUCTIONS.md`.
  - Seção *Contexto: hospedagem de vídeo pela yeroon play*: o contrato agora
    é da `main`, e "**Nada disto está implementado ainda**" vira "implementado
    na fatia `yeroon-play-plugado`".
  - Seção *Variáveis de ambiente*: as quatro variáveis novas.
- Modify: `README.md`, seção *Variáveis de ambiente*, com as quatro variáveis
  sem valor (se alguma task anterior já as colocou, só confira).

- [ ] **Step 1: Escrever o runbook e atualizar os docs.**
- [ ] **Step 2: Rodar `make test`.** Esperado: tudo PASS (backend, integração
  e web).
- [ ] **Step 3: Commit**: `docs: runbook and env for the yeroon play integration`.
