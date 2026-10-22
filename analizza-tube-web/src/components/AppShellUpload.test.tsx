import { act, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, expect, test, vi } from "vitest";

const push = vi.fn();
vi.mock("next/navigation", () => ({
  usePathname: () => "/",
  useRouter: () => ({ push }),
}));

type Opts = { onProgress?: (sent: number, total: number) => void; signal?: AbortSignal };
let pending: { resolve: (v: { videoId: string }) => void; reject: (e: Error) => void; opts: Opts } | null;
const uploadVideo = vi.fn(
  (_file: File, opts: Opts) =>
    new Promise<{ videoId: string }>((resolve, reject) => {
      pending = { resolve, reject, opts };
    }),
);
vi.mock("@/lib/upload", () => ({
  uploadVideo: (file: File, opts: Opts) => uploadVideo(file, opts),
  UploadRejectedError: class extends Error {},
}));

import { AppShell } from "./AppShell";

beforeEach(() => {
  pending = null;
  uploadVideo.mockClear();
  push.mockClear();
  vi.stubGlobal(
    "fetch",
    vi.fn(async () => new Response(JSON.stringify({ videos: [] }), { headers: { "content-type": "application/json" } })),
  );
});
afterEach(() => vi.unstubAllGlobals());

function enviar(nome = "aula.mov") {
  fireEvent.click(screen.getByRole("button", { name: "Enviar vídeo" }));
  const file = new File(["x"], nome, { type: "video/quicktime" });
  fireEvent.change(screen.getByLabelText(/arraste o arquivo/i), { target: { files: [file] } });
  fireEvent.click(screen.getByRole("button", { name: "Enviar" }));
}

test("aoEnviarOModalFechaEOProgressoApareceForaDele", async () => {
  render(<AppShell><p>conteudo</p></AppShell>);
  enviar();

  expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  const status = screen.getByRole("status", { name: "Envio em andamento" });
  expect(status).toHaveTextContent("aula.mov");

  act(() => pending!.opts.onProgress?.(1, 4));
  expect(status).toHaveTextContent("25%");
});

test("enquantoEnviaNaoDaParaAbrirOutroEnvio", () => {
  render(<AppShell><p>conteudo</p></AppShell>);
  enviar();

  const botao = screen.getByRole("button", { name: /enviando/i });
  expect(botao).toBeDisabled();
  fireEvent.click(botao);
  expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  expect(uploadVideo).toHaveBeenCalledTimes(1);
});

test("aoTerminarAbreOVideoELiberaONovoEnvio", async () => {
  render(<AppShell><p>conteudo</p></AppShell>);
  enviar();

  await act(async () => pending!.resolve({ videoId: "v9" }));

  expect(push).toHaveBeenCalledWith("/videos/v9");
  expect(screen.queryByRole("status", { name: "Envio em andamento" })).not.toBeInTheDocument();
  expect(screen.getByRole("button", { name: "Enviar vídeo" })).toBeEnabled();
});

test("cancelarInterrompeOEnvio", async () => {
  render(<AppShell><p>conteudo</p></AppShell>);
  enviar();

  fireEvent.click(screen.getByRole("button", { name: "Cancelar envio" }));
  expect(pending!.opts.signal?.aborted).toBe(true);
  await act(async () => pending!.reject(new DOMException("x", "AbortError")));

  expect(screen.queryByRole("status", { name: "Envio em andamento" })).not.toBeInTheDocument();
  expect(screen.getByRole("button", { name: "Enviar vídeo" })).toBeEnabled();
});

test("aFalhaFicaVisivelETentarDeNovoRetomaOMesmoArquivo", async () => {
  render(<AppShell><p>conteudo</p></AppShell>);
  enviar("treino.mp4");

  await act(async () => pending!.reject(new Error("rede")));

  expect(screen.getByRole("alert")).toHaveTextContent("O envio de treino.mp4 falhou.");
  fireEvent.click(screen.getByRole("button", { name: "Tentar de novo" }));
  expect(uploadVideo).toHaveBeenCalledTimes(2);
  expect(uploadVideo.mock.calls[1][0].name).toBe("treino.mp4");
});
