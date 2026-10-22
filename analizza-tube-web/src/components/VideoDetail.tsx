"use client";

import { useEffect, useState } from "react";
import { statusLabel } from "@/lib/videoStatus";

// Estados finais: nada mais muda, entao a pesquisa para.
const TERMINAL = ["PRONTO", "FALHOU", "CANCELADO", "ABANDONADO"];

type Detail = { status: string; fileName: string; failureReason: string | null };

export function VideoDetail({ id }: { id: string }) {
  const [detail, setDetail] = useState<Detail | null>(null);
  const [error, setError] = useState(false);
  const [embedUrl, setEmbedUrl] = useState<string | null>(null);
  const [playerError, setPlayerError] = useState(false);
  const finished = detail !== null && TERMINAL.includes(detail.status);
  const safeId = encodeURIComponent(id);
  const ready = detail?.status === "PRONTO";

  useEffect(() => {
    if (finished) return;
    let cancelled = false;
    const load = async () => {
      try {
        const res = await fetch(`/api/videos/${safeId}`);
        if (!res.ok) throw new Error(String(res.status));
        const body = (await res.json()) as Detail;
        if (!cancelled) {
          setDetail(body);
          setError(false);
        }
      } catch {
        if (!cancelled) setError(true);
      }
    };
    void load();
    const timer = setInterval(load, 5000);
    return () => {
      cancelled = true;
      clearInterval(timer);
    };
  }, [safeId, finished]);

  useEffect(() => {
    if (!ready) return;
    let cancelled = false;
    (async () => {
      try {
        const res = await fetch(`/api/videos/${safeId}/playback`, { method: "POST" });
        if (!res.ok) throw new Error(String(res.status));
        const body = (await res.json()) as { embedUrl: string };
        if (!cancelled) setEmbedUrl(body.embedUrl);
      } catch {
        if (!cancelled) setPlayerError(true);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [safeId, ready]);

  useEffect(() => {
    if (!embedUrl) return;
    let origin: string;
    try {
      origin = new URL(embedUrl).origin;
    } catch {
      return;
    }
    const onMessage = (event: MessageEvent) => {
      if (event.origin !== origin) return;
      const data = event.data as { source?: string; type?: string } | null;
      if (data?.source === "yeroon-player" && data.type === "error") setPlayerError(true);
    };
    window.addEventListener("message", onMessage);
    return () => window.removeEventListener("message", onMessage);
  }, [embedUrl]);

  if (!detail) {
    return <p className="text-muted">{error ? "Vídeo não encontrado." : "Carregando…"}</p>;
  }
  return (
    <section aria-label="Vídeo em reprodução" className="flex flex-col gap-4">
      {ready && embedUrl && !playerError ? (
        <iframe
          title="Player"
          src={embedUrl}
          allow="autoplay; fullscreen; picture-in-picture"
          allowFullScreen
          className="aspect-video w-full rounded-2xl border border-line"
        />
      ) : (
        <div className="flex aspect-video w-full flex-col items-center justify-center gap-2 rounded-2xl border border-line bg-deep p-6 text-center">
          {detail.status === "FALHOU" ? (
            <p role="alert" className="m-0 text-sm text-[#f3b4b4]">
              Falha no processamento: {detail.failureReason}
            </p>
          ) : playerError ? (
            <p role="alert" className="m-0 text-sm text-[#f3b4b4]">
              Não foi possível tocar o vídeo.
            </p>
          ) : (
            <>
              <span className="font-mono text-xs tracking-[0.08em] text-accent-soft">
                {statusLabel(detail.status).toUpperCase()}
              </span>
              <p className="m-0 text-sm text-muted">O vídeo aparece aqui assim que a conversão terminar.</p>
            </>
          )}
        </div>
      )}
      <h1 className="m-0 font-serif text-2xl leading-tight font-medium tracking-[-0.02em] break-words">
        {detail.fileName}
      </h1>
      <div className="flex flex-col gap-1.5 rounded-[10px] border border-line bg-surface p-4">
        <span className="font-mono text-xs text-muted">Status: {statusLabel(detail.status)}</span>
      </div>
    </section>
  );
}
