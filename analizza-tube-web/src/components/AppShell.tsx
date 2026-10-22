"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useCallback, useState, type ReactNode } from "react";
import { PlayIcon, SearchIcon, UploadIcon } from "./icons";
import { UploadDialog } from "./UploadDialog";
import { UploadJobProvider, UploadStatus, percentOf, useUploadJob } from "./UploadJob";
import { VideoList } from "./VideoList";

/** Cabecalho, lista lateral e modal de envio, comuns a todas as telas. */
export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const currentId = pathname.match(/^\/videos\/([^/]+)/)?.[1];
  const [query, setQuery] = useState("");
  const [uploading, setUploading] = useState(false);
  const [listVersion, setListVersion] = useState(0);
  const upload = useUploadJob(useCallback(() => setListVersion((n) => n + 1), []));

  return (
    <UploadJobProvider value={upload}>
      <div className="min-h-screen">
        <header className="flex flex-wrap items-center gap-4 border-b border-line px-6 py-3">
          <Link href="/" className="flex items-center gap-2.5 text-ink no-underline">
            <span className="flex size-7 items-center justify-center rounded-[7px] bg-accent-deep text-white">
              <PlayIcon />
            </span>
            <span className="font-serif text-xl font-medium tracking-[-0.02em]">
              analizza <span className="italic text-accent">tube</span>
            </span>
          </Link>

          <div className="flex min-w-0 flex-[1_1_280px] justify-center">
            <div className="flex h-11 w-full max-w-[620px] items-center gap-2 rounded-full border border-line-strong bg-field px-3.5 text-muted">
              <SearchIcon />
              <label htmlFor="busca" className="sr-only">
                Pesquisar vídeos
              </label>
              <input
                id="busca"
                type="search"
                placeholder="Pesquisar"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                className="h-10 min-w-0 flex-1 border-0 bg-transparent text-sm text-ink outline-none"
              />
            </div>
          </div>

          <button
            type="button"
            disabled={upload.busy}
            onClick={() => setUploading(true)}
            className="flex h-11 cursor-pointer items-center gap-2 rounded-lg bg-accent px-[18px] text-sm font-semibold text-white transition-colors hover:bg-accent-hover disabled:cursor-default disabled:opacity-60 disabled:hover:bg-accent"
          >
            <UploadIcon />
            {upload.busy ? `Enviando… ${percentOf(upload.job?.progress ?? null)}%` : "Enviar vídeo"}
          </button>
        </header>

        <div className="mx-auto box-border flex max-w-[1760px] flex-wrap items-start gap-6 p-6">
          <main className="flex min-w-0 flex-[999_1_560px] flex-col gap-4">{children}</main>
          <aside
            aria-label="Lista de vídeos"
            className="min-w-0 max-w-[440px] flex-[1_1_360px] overflow-hidden rounded-2xl border border-line bg-surface"
          >
            <UploadStatus />
            <VideoList currentId={currentId} query={query} refreshKey={listVersion} />
          </aside>
        </div>

        {uploading && <UploadDialog onClose={() => setUploading(false)} />}
      </div>
    </UploadJobProvider>
  );
}
