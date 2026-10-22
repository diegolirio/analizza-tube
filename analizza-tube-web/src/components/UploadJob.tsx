"use client";

import { useRouter } from "next/navigation";
import { createContext, useCallback, useContext, useRef, useState } from "react";
import { UploadRejectedError, uploadVideo } from "@/lib/upload";

/** Um envio por vez, acima do modal: fechar o modal não perde o progresso nem abre espaço para um segundo envio. */
export type UploadJob = {
  file: File;
  progress: [number, number] | null;
  error: string | null;
  retriable: boolean;
};

type UploadJobApi = {
  job: UploadJob | null;
  busy: boolean;
  start: (file: File) => void;
  cancel: () => void;
  dismiss: () => void;
};

const UploadJobContext = createContext<UploadJobApi | null>(null);

export const UploadJobProvider = UploadJobContext.Provider;

export function useUploadJobContext(): UploadJobApi {
  const api = useContext(UploadJobContext);
  if (!api) throw new Error("useUploadJobContext fora do AppShell");
  return api;
}

export function useUploadJob(onDone: (videoId: string) => void): UploadJobApi {
  const router = useRouter();
  const [job, setJob] = useState<UploadJob | null>(null);
  const controller = useRef<AbortController | null>(null);
  const busy = job !== null && job.error === null;

  const start = useCallback(
    async (file: File) => {
      if (controller.current) return;
      const ctrl = new AbortController();
      controller.current = ctrl;
      setJob({ file, progress: null, error: null, retriable: false });
      try {
        const { videoId } = await uploadVideo(file, {
          signal: ctrl.signal,
          onProgress: (sent, total) =>
            setJob((j) => (j && j.file === file ? { ...j, progress: [sent, total] } : j)),
        });
        setJob(null);
        onDone(videoId);
        router.push(`/videos/${videoId}`);
      } catch (e) {
        if ((e as Error).name === "AbortError") setJob(null);
        else if (e instanceof UploadRejectedError)
          setJob({ file, progress: null, error: e.message, retriable: false });
        else
          setJob({ file, progress: null, error: `O envio de ${file.name} falhou.`, retriable: true });
      } finally {
        controller.current = null;
      }
    },
    [onDone, router],
  );

  const cancel = useCallback(() => controller.current?.abort(), []);
  const dismiss = useCallback(() => setJob(null), []);

  return { job, busy, start: (file) => void start(file), cancel, dismiss };
}

export function percentOf(progress: [number, number] | null): number {
  return progress ? Math.round((progress[0] / Math.max(progress[1], 1)) * 100) : 0;
}

/** Cartão do envio em andamento (ou que falhou), no topo da lista lateral. */
export function UploadStatus() {
  const { job, start, cancel, dismiss } = useUploadJobContext();
  if (!job) return null;

  if (job.error) {
    return (
      <div className="flex flex-col gap-2 border-b border-line p-4">
        <p role="alert" className="m-0 rounded-lg border border-[#b4232340] bg-[#b423230f] px-3 py-2 text-sm text-[#b42323]">
          {job.error}
        </p>
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={dismiss}
            className="h-9 cursor-pointer rounded-lg border border-line-strong bg-transparent px-3 text-sm font-semibold text-ink transition-colors hover:bg-[#1a1d1a0d]"
          >
            Descartar
          </button>
          {job.retriable && (
            <button
              type="button"
              onClick={() => start(job.file)}
              className="h-9 cursor-pointer rounded-lg border-0 bg-accent px-3 text-sm font-semibold text-white transition-colors hover:bg-accent-hover"
            >
              Tentar de novo
            </button>
          )}
        </div>
      </div>
    );
  }

  const percent = percentOf(job.progress);
  return (
    <div role="status" aria-label="Envio em andamento" className="flex flex-col gap-2 border-b border-line p-4">
      <div className="flex items-center justify-between gap-3">
        <span className="text-[11px] font-semibold tracking-[0.08em] text-accent-soft">ENVIANDO</span>
        <button
          type="button"
          onClick={cancel}
          aria-label="Cancelar envio"
          className="h-8 cursor-pointer rounded-lg border border-line-strong bg-transparent px-2.5 text-xs font-semibold text-ink transition-colors hover:bg-[#1a1d1a0d]"
        >
          Cancelar
        </button>
      </div>
      <span className="truncate text-sm font-semibold">{job.file.name}</span>
      {job.progress && (
        <progress value={job.progress[0]} max={job.progress[1]} aria-label="Progresso do envio" className="sr-only" />
      )}
      <div className="h-1 overflow-hidden rounded-full bg-line-strong" aria-hidden="true">
        <div className="h-full rounded-full bg-accent transition-[width]" style={{ width: `${percent}%` }} />
      </div>
      <span className="font-mono text-xs text-muted">
        {job.progress ? `${job.progress[0]} / ${job.progress[1]} partes · ${percent}%` : "Preparando…"}
      </span>
    </div>
  );
}
