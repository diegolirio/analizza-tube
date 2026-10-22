import { act, render, screen, waitFor } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";
import { VideoDetail } from "./VideoDetail";

afterEach(() => vi.unstubAllGlobals());

function stubFetch(status: string) {
  const fetchMock = vi.fn(async (url: string, init?: RequestInit) => {
    void init;
    if (url === "/api/videos/v1/playback") {
      return Response.json({ embedUrl: "https://p/embed/x", expiresAt: "2030-01-01T00:00:00Z" });
    }
    return Response.json({ status, fileName: "aula.mp4", failureReason: null });
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

test("prontoMostraOIframeComOEmbedUrl", async () => {
  const fetchMock = stubFetch("PRONTO");
  render(<VideoDetail id="v1" />);
  const iframe = await screen.findByTitle("Player");
  expect(iframe).toHaveAttribute("src", "https://p/embed/x");
  const playback = fetchMock.mock.calls.find(([u]) => u === "/api/videos/v1/playback");
  expect((playback?.[1] as RequestInit).method).toBe("POST");
});

test("processandoMostraStatusSemIframe", async () => {
  stubFetch("CONVERTENDO");
  render(<VideoDetail id="v1" />);
  expect(await screen.findByText("CONVERTENDO")).toBeInTheDocument();
  expect(screen.queryByTitle("Player")).toBeNull();
});

test("erroDoPlayerMostraMensagem", async () => {
  stubFetch("PRONTO");
  render(<VideoDetail id="v1" />);
  await screen.findByTitle("Player");
  act(() => {
    window.dispatchEvent(
      new MessageEvent("message", { data: { source: "yeroon-player", type: "error" } }),
    );
  });
  await waitFor(() =>
    expect(screen.getByText("Não foi possível tocar o vídeo.")).toBeInTheDocument(),
  );
});
