import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, expect, test, vi } from "vitest";

vi.mock("next/navigation", () => ({
  usePathname: () => "/videos/v1",
  useRouter: () => ({ push: vi.fn() }),
}));

import { AppShell } from "./AppShell";

beforeEach(() => {
  vi.stubGlobal(
    "fetch",
    vi.fn(async () => new Response(JSON.stringify({ videos: [
      { videoId: "v1", fileName: "ferias.mp4", status: "PRONTO", createdAt: "2026-10-20T10:00:00Z", thumbnailUrl: null },
      { videoId: "v2", fileName: "treino.mp4", status: "PRONTO", createdAt: "2026-10-21T10:00:00Z", thumbnailUrl: null },
    ] }), { headers: { "content-type": "application/json" } })),
  );
});
afterEach(() => vi.unstubAllGlobals());

test("oBotaoEnviarVideoAbreEFechaOModal", async () => {
  render(<AppShell><p>conteudo</p></AppShell>);
  expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  fireEvent.click(screen.getByRole("button", { name: "Enviar vídeo" }));
  expect(screen.getByRole("dialog", { name: /enviar vídeo/i })).toBeInTheDocument();
  fireEvent.click(screen.getByRole("button", { name: "Fechar" }));
  expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
});

test("aBuscaDoCabecalhoFiltraALista", async () => {
  render(<AppShell><p>conteudo</p></AppShell>);
  await screen.findByRole("link", { name: /treino\.mp4/ });
  fireEvent.change(screen.getByRole("searchbox", { name: "Pesquisar vídeos" }), { target: { value: "ferias" } });
  expect(screen.queryByRole("link", { name: /treino\.mp4/ })).not.toBeInTheDocument();
  expect(screen.getByRole("link", { name: /ferias\.mp4/ })).toHaveAttribute("aria-current", "page");
});

test("mostraOConteudoDaRota", () => {
  render(<AppShell><p>conteudo</p></AppShell>);
  expect(screen.getByText("conteudo")).toBeInTheDocument();
});
