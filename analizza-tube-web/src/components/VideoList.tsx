"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { statusLabel } from "@/lib/videoStatus";

type VideoItem = {
  videoId: string;
  fileName: string;
  status: string;
  createdAt: string;
  thumbnailUrl: string | null;
};

const ACTIVE = ["ENVIADO", "PROCESSANDO"];
const POLL_MS = 5000;

export function VideoList({ currentId, query = "" }: { currentId?: string; query?: string }) {
  const [videos, setVideos] = useState<VideoItem[] | null>(null);
  const [error, setError] = useState(false);
  const polling = videos === null || videos.some((v) => ACTIVE.includes(v.status));

  useEffect(() => {
    if (!polling) return;
    let cancelled = false;
    const load = async () => {
      try {
        const res = await fetch("/api/videos");
        if (!res.ok) throw new Error(String(res.status));
        const body = (await res.json()) as { videos: VideoItem[] };
        if (!cancelled) {
          setVideos(body.videos);
          setError(false);
        }
      } catch {
        if (!cancelled) {
          setError(true);
          setVideos((v) => v ?? []);
        }
      }
    };
    void load();
    const timer = setInterval(load, POLL_MS);
    return () => {
      cancelled = true;
      clearInterval(timer);
    };
  }, [polling]);

  const term = query.trim().toLowerCase();
  const shown = (videos ?? []).filter((v) => v.fileName.toLowerCase().includes(term));
  const total = videos?.length ?? 0;

  return (
    <>
      <div className="flex flex-col gap-0.5 border-b border-line px-4 pt-4 pb-3">
        <span className="text-[11px] font-semibold tracking-[0.08em] text-accent-soft">BIBLIOTECA</span>
        <h2 className="m-0 font-serif text-xl font-medium leading-tight tracking-[-0.02em]">Vídeos</h2>
        <span className="font-mono text-xs text-muted">
          {term ? `${shown.length} de ${total}` : `${total} ${total === 1 ? "vídeo" : "vídeos"}`}
        </span>
      </div>

      {videos === null && <p className="m-0 p-4 text-sm text-muted">Carregando…</p>}
      {error && (
        <p role="alert" className="m-0 p-4 text-sm text-muted">
          Não foi possível carregar os vídeos.
        </p>
      )}
      {videos !== null && total === 0 && !error && (
        <p className="m-0 p-4 text-sm text-muted">Nenhum vídeo ainda.</p>
      )}
      {term && total > 0 && shown.length === 0 && (
        <p className="m-0 p-4 text-sm text-muted">Nenhum vídeo com “{query.trim()}”.</p>
      )}

      <ol className="m-0 flex max-h-[760px] list-none flex-col gap-0.5 overflow-y-auto p-2">
        {shown.map((v) => {
          const current = v.videoId === currentId;
          return (
            <li key={v.videoId}>
              <Link
                href={`/videos/${v.videoId}`}
                aria-current={current ? "page" : undefined}
                className={`flex items-start gap-2.5 rounded-[10px] p-2 text-ink no-underline transition-colors hover:bg-[#ede8df0d] ${current ? "bg-[#7a52e01f]" : ""}`}
              >
                <span className="relative block h-[72px] w-32 flex-none overflow-hidden rounded-lg border border-line bg-deep">
                  {v.thumbnailUrl && (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={v.thumbnailUrl} alt="" className="size-full object-cover" />
                  )}
                  {v.status !== "PRONTO" && (
                    <span className="absolute right-1 bottom-1 rounded bg-[#14110fe6] px-1.5 font-mono text-[11px] leading-[18px]">
                      {statusLabel(v.status)}
                    </span>
                  )}
                </span>
                <span className="flex min-w-0 flex-1 flex-col gap-0.5">
                  <span className="line-clamp-2 text-sm leading-snug font-semibold break-words">{v.fileName}</span>
                  <span className="font-mono text-xs text-muted">
                    {new Date(v.createdAt).toLocaleDateString("pt-BR")}
                  </span>
                </span>
              </Link>
            </li>
          );
        })}
      </ol>
    </>
  );
}
