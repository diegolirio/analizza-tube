# yeroon play plugado no analizza-tube — design

Data: 2026-10-21. Status: rascunho para aprovação (revisado pelo builder check).

## Intenção

O analizza-tube vai ser uma plataforma de **cursos**, e os vídeos das aulas
ficam hospedados no **yeroon play**: storage, conversão HLS e player são da
plataforma, consumidos pela API pública v1. O contrato está em
`diegolirio/yeroon-streaming-poc`, `docs/api/v1.md`, que já está na `main` (o
`INSTRUCTIONS.md` ainda cita o branch `feat/plataforma-api-v1`; o que vale é a
`main`).

Os endereços do yeroon dev são dois:
- **API:** `https://yeroon.streaming.diegolirio.com`. É o que o `-api` do
  analizza-tube chama.
- **Web:** `https://yeroon.streaming.poc.diegolirio.com`. É de onde vêm o
  `embedUrl` e o player do iframe. O
projeto está no scaffold vazio, sem telas nem tabelas.

Esta é a **primeira parte**: só plugar o yeroon play. O analizza-tube passa a
enviar, listar e tocar vídeos pela v1, com **o mesmo visual do yeroon play**.
Curso, módulo e aula vêm depois, em cima disto.

## Critério de sucesso

Rodando o analizza-tube **na máquina do dev** (api `:8080`, web `:3000`),
contra o yeroon play dev já no ar (`https://yeroon.streaming.diegolirio.com`):

1. **Enviar:** "Enviar vídeo" abre o modal, e o vídeo sobe com barra de
   progresso. Os bytes vão do navegador direto ao R2, sem passar pelo
   analizza-tube nem pelo yeroon.
2. **Listar:** a lista lateral mostra os vídeos do analizza-tube, com status e
   miniatura, e um vídeo em conversão vira "Pronto" sozinho.
3. **Assistir:** abrir um vídeo pronto mostra o player do yeroon num
   `<iframe>` (o `embedUrl`), tocando.
4. **Webhook:** um `POST` assinado do yeroon (`video.ready` / `video.failed`)
   é aceito, um repetido é ignorado e uma assinatura errada é recusada.
   Isso é coberto pelos testes de integração. Ponta a ponta, fica opcional no
   runbook, via túnel.
5. **Chave:** a chave `yk_` nunca chega ao navegador. Para conferir, basta
   procurar `yk_` nos bundles do web.

## Não-objetivos (próximas partes)

- Curso, módulo, aula, aluno, matrícula e progresso por aluno. O `postMessage`
  de progresso fica de fora agora: só se ouve `ready` e `error` para o estado
  da tela.
- Login e autorização. O analizza-tube local não tem usuários ainda.
- Deploy do analizza-tube e mobile. O `-mobile` não muda nesta parte.
- Trocar de provedor de vídeo. Não há porta genérica `VideoProvider`; o
  cliente fala yeroon, e isso basta.

## Anti-objetivos

- A chave `yk_` (ou o segredo do webhook) no `-web`, no `-mobile`, em log ou
  versionada.
- Guardar no analizza-tube o que é da plataforma: arquivo, partes, status,
  renditions ou URL de mídia.
- Chamar a v1 de verdade nos testes. Um dublê `@Primary` substitui o cliente,
  como manda o `INSTRUCTIONS.md`.

## Pré-requisitos no yeroon play (fora deste repo)

0. **Rota nova na v1, feita pelo builder no repo do yeroon:**
   `GET /v1/videos/{id}/upload/parts` → `{"parts":[{"partNumber","etag","sizeBytes"}]}`,
   com chave e escopo do cliente, no mesmo formato da `GET /api/videos/{id}/upload/parts`
   da casa. É o que mantém a **retomada** do upload. Leva IT e a linha em
   `docs/api/v1.md`, e entra no mesmo deploy dos secrets.

1. **Deploy da v1.** O merge do PR #2 falhou no deploy do backend (timeout de
   SSH do runner até a VPS), então a v1 ainda não está no ar: `/v1/videos`
   responde 404.
2. **Antes de rodar o deploy de novo**, criar no environment `development` de
   `diegolirio/yeroon-streaming-poc`:
   - os secrets `STREAM_PLATFORM_ADMIN_TOKEN` e
     `STREAM_PLATFORM_PLAYBACK_TOKEN_SECRET` (32+ bytes);
   - a variável `STREAM_PLATFORM_PLAYER_BASE_URL=https://yeroon.streaming.poc.diegolirio.com`.

   Sem o primeiro, ninguém cadastra cliente; sem o segundo, o token sai assinado
   com o segredo de dev que está no código.
Quem faz cada passo: os secrets e o cadastro são do usuário, porque
envolvem valores secretos. Rodar o deploy de novo e conferir a v1 fica com o
builder. **Isto bloqueia só a conferência manual:** todo o desenvolvimento e
os testes rodam contra o dublê do cliente yeroon.

3. **Cadastro do cliente**, com `curl` do administrador (`docs/api/v1.md`; o
   exemplo ali usa a porta `3001`, mas aqui vale a `3000`):
   `name=analizza-tube`, `allowedOrigins=["http://localhost:3000"]` e sem
   `webhookUrl` por enquanto. A resposta traz `apiKey` e `webhookSecret`,
   que vão para o `.env` local do analizza-tube e nunca para o git.
4. **CORS do R2:** já libera `http://localhost:3000` (`deploy/r2-cors.json`),
   então o PUT das partes funciona daqui.

## Desenho

### Dados (Flyway no `-core`)

- `video`: `id uuid` (o **`videoId` do yeroon**, que é a chave) e
  `created_at`. É só isto, porque o projeto decidiu guardar **só o
  `videoId`**. A tabela existe para registrar quais vídeos são do
  analizza-tube e para as próximas partes (a aula vai apontar para ela).
- `webhook_receipt`: `delivery_id` (pk, o `X-Yeroon-Delivery`), `event_type`,
  `video_id`, `received_at`. Serve para deduplicar.

### Cliente da v1 (saída, no `-core`)

- Interface em `infrastructure/data/anticorruptionLayer/yeroon/` e
  implementação em `.../yeroon/impl/`, com `RestClient`, timeout curto e
  `Authorization: Bearer ${YEROON_API_KEY}`.
- Operações:
  - `initUpload`;
  - `partUrls`;
  - `complete`;
  - `cancel`;
  - `get`;
  - `list`;
  - `playbackToken`.

  Os corpos e as respostas são os do `docs/api/v1.md`.
- Erros da v1:
  - `422` → repassa os `errors[]` como `422` do analizza-tube;
  - `404` → `404`;
  - `409` do playback-token → `409`;
  - `401` ou falha de rede → `502` com mensagem genérica, registrando no log o
    prefixo da chave, nunca a chave.
- Configuração: `YEROON_API_URL` (padrão `https://yeroon.streaming.diegolirio.com`),
  `YEROON_API_KEY` e `YEROON_WEBHOOK_SECRET`.

### Rotas do `-api` (casos de uso no `-core`)

| Rota | Faz |
|---|---|
| `POST /videos/uploads` | init na v1 e grava `video(id)` |
| `POST /videos/{id}/upload/part-urls` | repassa à v1 |
| `GET /videos/{id}/upload/parts` | repassa à v1 (partes já enviadas, para retomar) |
| `POST /videos/{id}/upload/complete` | repassa à v1 |
| `DELETE /videos/{id}/upload` | repassa à v1 |
| `GET /videos` | `list` da v1, filtrado aos ids da tabela `video` (mais nada) |
| `GET /videos/{id}` | `get` da v1 (404 se o id não está na tabela) |
| `POST /videos/{id}/playback` | `playbackToken` da v1; devolve só `embedUrl` e `expiresAt` |
| `POST /webhooks/yeroon` | recebe o webhook (abaixo) |

O status vem sempre da v1, a cada leitura. Por isso o vídeo aparece pronto
sozinho, mesmo sem o webhook alcançar a máquina do dev.

### Webhook

- Guarda de entrada no `-api` (`presenter/`):
  - confere `X-Yeroon-Signature` (`t=<epoch>,v1=<hex>`) com
    `HMAC-SHA256(YEROON_WEBHOOK_SECRET, "<epoch>.<corpo bruto>")`, comparando
    em tempo constante;
  - recusa `epoch` com mais de 5 min de diferença (`401`);
  - sem segredo configurado, recusa sempre.
- Caso de uso: grava `webhook_receipt` (um `delivery_id` repetido não faz
  nada e responde `200`) e loga o evento.
- Nesta parte não há mais efeito, porque o status já é lido da v1. O
  recebimento fica pronto para as próximas partes (por exemplo, "publicar a
  aula quando o vídeo ficar pronto").
- Responde `2xx` rápido; nada pesado dentro da requisição.

### Web (Next)

- **Mesmo visual do yeroon play.** Copiar do `yeroon-stream-poc-web` (commit
  `b90af46`):
  - os tokens de `globals.css`;
  - as fontes (Geist, Geist Mono, Source Serif 4);
  - `AppShell` (cabeçalho com busca e "Enviar vídeo", lista lateral);
  - `VideoList`;
  - `UploadDialog` / `UploadForm`;
  - `lib/videoStatus`;
  - o upload multipart com retomada (`lib/upload.ts`).

  O nome no cabeçalho vira "analizza **tube**", com o mesmo tratamento do
  "yeroon play".
- Os Route Handlers `src/app/api/**` repassam ao `-api` (`ANALIZZA_API_URL`),
  como no yeroon (`lib/repasse.ts`). O navegador só fala com o Next e com o
  R2.
- **Assistir:** em `/videos/[id]`, quando está `PRONTO`, o web chama
  `POST /api/videos/{id}/playback` e coloca
  `<iframe src={embedUrl} allow="autoplay; fullscreen; picture-in-picture" allowFullScreen>`
  no quadro 16:9. O token é pedido a cada abertura e nunca é guardado. Um
  `postMessage` com `type: "error"` mostra a mensagem de erro no quadro.
- O `UploadForm` usa as rotas do analizza-tube, com o mesmo formato de
  resposta do yeroon (`videoId`, `partSizeBytes`, `partCount`, `parts`), então o
  `lib/upload.ts` copiado funciona sem mudança, **com a retomada**.
- *Decisão minha:* a lista mostra todos os status, inclusive `CANCELADO` e
  `ABANDONADO`, com o rótulo no selo, como no yeroon. Esconder fica para a
  parte de cursos.

## Testes

- **Unitários:**
  - assinatura do webhook: válida, corpo alterado, epoch velho, cabeçalho
    malformado;
  - mapeamento dos erros da v1.
- **Integração:** um IT por rota, com o dublê `@Primary` do cliente yeroon,
  cobrindo:
  - id fora da tabela → 404;
  - `422` repassado;
  - webhook válido, repetido e com assinatura errada;
  - a lista só com os ids do analizza-tube.
- **Web:** Vitest para a lista, o modal e a tela de assistir (iframe com o
  `embedUrl`, estado de erro).

## Pronto inclui (CLAUDE.md)

- Runbook `docs/checkpoints/yeroon-play-plugado.md` (`screen-web`), com:
  - os pré-requisitos e o cadastro;
  - enviar, listar e assistir contra o yeroon dev;
  - procurar `yk_` nos bundles;
  - opcional: webhook ponta a ponta com `cloudflared tunnel --url http://localhost:8080`
    e o `webhookUrl` do cliente apontando para o túnel.
- As variáveis novas (`YEROON_*`, `ANALIZZA_API_URL`) na seção *Variáveis de
  ambiente* do `INSTRUCTIONS.md` e no `README.md`, sem valor.

## Deixado para o builder

- Os nomes exatos das classes e dos casos de uso, seguindo o `INSTRUCTIONS.md`.
- Paginação da lista. A v1 não pagina, e o volume é de POC.
- Quanto do visual copiar como está ou adaptar. O critério é parecer o
  yeroon play lado a lado.
