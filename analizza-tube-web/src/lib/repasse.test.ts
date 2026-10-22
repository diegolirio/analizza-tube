import { afterEach, expect, test, vi } from "vitest";
import { repasse } from "./repasse";

afterEach(() => vi.unstubAllGlobals());

test("preservaStatusCorpoEContentTypeDaPlaylist", async () => {
  const fetchMock = vi.fn(
    async () =>
      new Response("#EXTM3U\n", {
        status: 409,
        headers: {
          "content-type": "application/vnd.apple.mpegurl",
          "cache-control": "no-store",
        },
      }),
  );
  vi.stubGlobal("fetch", fetchMock);
  const res = await repasse(
    new Request("http://localhost:3000/api/videos/v1/hls/master.m3u8"),
    "/videos/v1/hls/master.m3u8",
  );
  expect(res.status).toBe(409);
  expect(await res.text()).toBe("#EXTM3U\n");
  expect(res.headers.get("content-type")).toBe("application/vnd.apple.mpegurl");
  expect(res.headers.get("cache-control")).toBe("no-store");
  const [url, init] = fetchMock.mock.calls[0] as unknown as [string, RequestInit];
  expect(url).toBe("http://localhost:8080/videos/v1/hls/master.m3u8");
  expect(init.method).toBe("GET");
});

test("repassaMetodoECorpoJson", async () => {
  const fetchMock = vi.fn(async () => new Response(null, { status: 204 }));
  vi.stubGlobal("fetch", fetchMock);
  await repasse(
    new Request("http://localhost:3000/x", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: '{"a":1}',
    }),
    "/api/videos/uploads",
  );
  const [, init] = fetchMock.mock.calls[0] as unknown as [string, RequestInit];
  expect(init.method).toBe("POST");
  expect(init.body).toBe('{"a":1}');
  expect(new Headers(init.headers).get("content-type")).toBe("application/json");
});
