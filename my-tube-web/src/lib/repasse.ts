const PASSTHROUGH_HEADERS = ["content-type", "cache-control"];

/** Repassa a requisição ao -api e devolve status, corpo, content-type e cache-control. */
export async function repasse(request: Request, path: string): Promise<Response> {
  const base = process.env.MY_TUBE_API_URL ?? "http://localhost:8080";
  const method = request.method;
  const hasBody = method !== "GET" && method !== "HEAD";
  const headers: Record<string, string> = {};
  const contentType = request.headers.get("content-type");
  if (hasBody && contentType) headers["content-type"] = contentType;

  const upstream = await fetch(`${base}${path}`, {
    method,
    headers,
    body: hasBody ? await request.text() : undefined,
    cache: "no-store",
  });

  const out = new Headers();
  for (const name of PASSTHROUGH_HEADERS) {
    const value = upstream.headers.get(name);
    if (value) out.set(name, value);
  }
  const noBody = upstream.status === 204 || upstream.status === 304;
  return new Response(noBody ? null : await upstream.arrayBuffer(), {
    status: upstream.status,
    headers: out,
  });
}
