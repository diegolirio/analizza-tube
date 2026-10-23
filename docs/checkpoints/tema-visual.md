---
type: screen-web
---

# Tema visual do web

Um tema só, claro: fundo branco predominante, texto quase preto e acento
verde. As cores moram nos tokens `@theme` de
`my-tube-web/src/app/globals.css`. Os poucos tons fixos que sobraram nos
componentes (sobreposição no hover, selo de status, alerta de erro) foram
acertados para o fundo claro.

## Preparação

`make run` na raiz (com o `.env` exportado, ver README) e abrir
http://localhost:3000. Para ver a lista e o player, é preciso ter pelo menos um
vídeo enviado.

## 1. O que precisa bater

- **Nome:** o logo diz "my **tube**" e a aba do navegador diz "my tube".
- **Início:** fundo branco, logo com o quadrado verde-escuro e "tube" em verde,
  botão **Enviar vídeo** verde com texto branco, área "Escolha um vídeo" em
  cinza bem claro.
- **Modal Enviar vídeo:** fundo escurecido atrás do modal, título com "vídeo"
  em verde, área de arrastar com borda tracejada cinza e ícone verde.
  Arrastando um arquivo por cima, a borda fica verde.
- **Foco pelo teclado:** com Tab, botões e campos ganham um anel verde.
- **Lista lateral:** passar o mouse num item escurece o fundo de leve, e o
  vídeo aberto fica com fundo verde-claro.
- **Selo de status** (vídeo ainda processando): texto escuro sobre fundo
  branco translúcido, legível em cima da miniatura.

## 2. O que tentar para ver se quebra

- Provocar um erro de envio (derrubar a api no meio do upload, ou enviar com a
  chave da yeroon errada): o alerta tem que sair em vermelho escuro, legível
  no fundo claro, e não no rosa-claro do tema antigo.
- Um vídeo com `FALHOU`: a mensagem de falha no lugar do player tem que estar
  legível.
- Procurar texto cinza-claro demais: placeholder da busca e legendas
  pequenas. Se precisar apertar os olhos, a cor `faint` está fraca.
- Sistema operacional em modo escuro: a página continua clara (o tema é um
  só), e as barras de rolagem e os campos nativos não ficam escuros.

## 3. O que não está na lista

Estranhe qualquer resto do tema escuro: um violeta, um creme ou um fundo
marrom-escuro em algum canto que esta lista não citou. Cores fixas nos
componentes (`bg-[#…]`, `text-[#…]`) escapam dos tokens, e alguma pode ter
ficado para trás. Estranhe também qualquer "analizza" que ainda apareça na
tela, na aba ou numa mensagem: o app se chama my-tube.

## 4. O que este checkpoint já pegou

_Nada ainda._
