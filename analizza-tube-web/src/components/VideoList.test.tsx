import { render, screen, within } from "@testing-library/react";
import { afterEach, beforeEach, expect, test, vi } from "vitest";
import { VideoList } from "./VideoList";

const VIDEOS = [
  { videoId: "v1", fileName: "ferias.mp4", status: "PRONTO", createdAt: "2026-10-20T10:00:00Z", thumbnailUrl: null },
  { videoId: "v2", fileName: "aula-de-guarda.mov", status: "PROCESSANDO", createdAt: "2026-10-21T10:00:00Z", thumbnailUrl: null },
];

beforeEach(() => {
  vi.stubGlobal(
    "fetch",
    vi.fn(async () => new Response(JSON.stringify({ videos: VIDEOS }), { headers: { "content-type": "application/json" } })),
  );
});
afterEach(() => vi.unstubAllGlobals());

test("cadaVideoLinkaParaASuaPaginaEMostraOStatus", async () => {
  render(<VideoList />);
  const link = await screen.findByRole("link", { name: /ferias\.mp4/ });
  expect(link).toHaveAttribute("href", "/videos/v1");
  const outro = screen.getByRole("link", { name: /aula-de-guarda\.mov/ });
  expect(within(outro).getByText("Convertendo")).toBeInTheDocument();
});

test("marcaOVideoQueEstaTocando", async () => {
  render(<VideoList currentId="v2" />);
  const atual = await screen.findByRole("link", { name: /aula-de-guarda\.mov/ });
  expect(atual).toHaveAttribute("aria-current", "page");
  expect(screen.getByRole("link", { name: /ferias\.mp4/ })).not.toHaveAttribute("aria-current");
});

test("aBuscaFiltraPeloNomeDoArquivoSemDiferenciarMaiusculas", async () => {
  render(<VideoList query="FERIAS" />);
  expect(await screen.findByRole("link", { name: /ferias\.mp4/ })).toBeInTheDocument();
  expect(screen.queryByRole("link", { name: /aula-de-guarda/ })).not.toBeInTheDocument();
  expect(screen.getByText("1 de 2")).toBeInTheDocument();
});

test("buscaSemResultadoDizIsso", async () => {
  render(<VideoList query="xyz" />);
  expect(await screen.findByText("Nenhum vídeo com “xyz”.")).toBeInTheDocument();
});
