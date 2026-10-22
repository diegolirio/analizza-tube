---
type: screen-web
---

# Vídeos hospedados pela yeroon play

Upload pelo modal (com retomada), lista lateral, reprodução no iframe da
yeroon play e webhook `video.ready`. Contrato:
`docs/superpowers/specs/2026-10-21-yeroon-play-plugado-design.md`.

## Preparação

Subir é preparação; a conferência é na tela do `-web`.

1. **Pré-requisitos no yeroon play** (spec, "Pré-requisitos"; bloqueiam só
   esta conferência, os testes rodam contra o dublê):
   - a v1 no ar (`/v1/videos` não pode responder 404) e a rota
     `GET /v1/videos/{id}/upload/parts` deployada;
   - os secrets `STREAM_PLATFORM_ADMIN_TOKEN` e
     `STREAM_PLATFORM_PLAYBACK_TOKEN_SECRET` e a variável
     `STREAM_PLATFORM_PLAYER_BASE_URL` no environment `development` do
     `diegolirio/yeroon-streaming-poc`;
   - o CORS do R2 liberando `http://localhost:3000` (`deploy/r2-cors.json`).
2. **Cadastrar o cliente** (quem tem o token de administrador). A resposta traz
   `apiKey` e `webhookSecret` uma única vez:

   ```bash
   curl -sS -X POST "https://yeroon.streaming.diegolirio.com/admin/tenants" \
     -H "X-Admin-Token: $STREAM_PLATFORM_ADMIN_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"name":"analizza-tube","allowedOrigins":["http://localhost:3000"]}'
   ```

3. **Variáveis no `.env` local** (fora do git; o `.env` está no `.gitignore`).
   O Makefile não carrega o arquivo: exporte no shell antes do `make run`
   (`set -a; . ./.env; set +a`). Os valores vêm da resposta do passo 2:

   ```
   YEROON_API_KEY=<apiKey>
   YEROON_WEBHOOK_SECRET=<webhookSecret>
   ```

4. `make up` e `make run` (api em `:8080`, web em `:3000`).

## O que precisa bater

1. Abra `http://localhost:3000`, clique em enviar e escolha um vídeo pelo
   modal. No DevTools > Network, os `PUT` das partes vão para
   `*.r2.cloudflarestorage.com` (não para `localhost:8080`) e respondem 200.
2. O vídeo aparece na lista lateral e passa de em processamento para
   **Pronto** (recarregue a lista se preciso; sem webhook configurado o estado
   vem da consulta à yeroon).
3. Clique no vídeo pronto: o iframe da yeroon play abre e o vídeo toca.
4. Nenhum `yk_` aparece no navegador: DevTools > Network (busque `yk_` em
   cabeçalhos e respostas, com "Search" do painel) e Sources (busca global
   por `yk_`). Zero ocorrências.

## O que tentar para ver se quebra

- **Troca de vídeo:** abra o vídeo A e, sem recarregar, clique no vídeo B na
  lista lateral. O quadro tem de passar para B, não ficar em A.
- **Fechar a aba no meio do upload** (com algumas partes já enviadas), abrir
  de novo e escolher **o mesmo arquivo**: o envio retoma, só as partes que
  faltam vão (confira no Network) e termina em Pronto. Escolher um arquivo
  diferente não pode misturar partes.
- **Id inventado:** abra a URL de um vídeo com um id que não existe. A tela
  mostra "Vídeo não encontrado." e não quebra.
- **Yeroon fora do ar:** suba a api com `YEROON_API_URL` errada (por exemplo
  `http://localhost:9`). No Network, `/api/videos` responde 502. Esperado na
  tela, sem tela em branco e sem erro não tratado no console:
  - lista lateral: aviso "Não foi possível carregar os vídeos."
    (`VideoList.tsx:67`, `role="alert"`);
  - modal de envio, ao escolher um arquivo: "O envio falhou. Escolha o mesmo
    arquivo para retomar." (`UploadForm.tsx:32`, `role="alert"`);
  - detalhe de um vídeo: "Vídeo não encontrado." (`VideoDetail.tsx:68`; a tela
    não distingue 502 de 404, qualquer resposta não-ok cai aí).
  A mensagem "Serviço de vídeo indisponível." é o corpo do 502 da api (visível
  só na resposta no Network); a tela não a exibe.
- Enviar arquivo que não é vídeo ou vazio; clicar em enviar duas vezes.

## O que não está na lista

Estranhe qualquer coisa que apareça sem ter sido pedida: um segundo vídeo na
lista depois de retomar um upload, um número de partes diferente do esperado,
uma requisição para outro host, um aviso no console, um estado que não muda
nunca, um valor de chave ou segredo em qualquer lugar visível. A lista acima
é feita do que já se pensou.

## Opcional: webhook ponta a ponta

1. `cloudflared tunnel --url http://localhost:8080` e anote a URL pública.
2. Cadastre (ou atualize) o cliente com
   `webhookUrl` = URL do túnel + `/webhooks/yeroon` (mesmo `curl` acima, com
   o campo `webhookUrl`), e atualize o `YEROON_WEBHOOK_SECRET` com o novo
   `webhookSecret`.
3. Envie um vídeo. Quando a transcodificação terminar, o log da api registra
   o `video.ready` aceito e a lista vira Pronto sem consulta manual.
4. Reenvie a mesma entrega (mesmo `X-Yeroon-Delivery`): a api responde sem
   reprocessar. Assinatura errada: 401.

## O que este checkpoint já pegou

Ainda não conferido. Preencher, com data e achado real, depois da primeira
execução.
