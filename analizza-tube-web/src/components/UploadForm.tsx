"use client";

import { useRouter } from "next/navigation";
import { useRef, useState } from "react";
import { UploadRejectedError, uploadVideo } from "@/lib/upload";
import { UploadIcon } from "./icons";

export function UploadForm() {
  const router = useRouter();
  const [file, setFile] = useState<File | null>(null);
  const [dragging, setDragging] = useState(false);
  const [progress, setProgress] = useState<[number, number] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [cancelled, setCancelled] = useState(false);
  const controller = useRef<AbortController | null>(null);

  const start = async () => {
    if (!file) return;
    setError(null);
    setCancelled(false);
    const ctrl = new AbortController();
    controller.current = ctrl;
    try {
      const { videoId } = await uploadVideo(file, {
        signal: ctrl.signal,
        onProgress: (sent, total) => setProgress([sent, total]),
      });
      router.push(`/videos/${videoId}`);
    } catch (e) {
      if ((e as Error).name === "AbortError") setCancelled(true);
      else if (e instanceof UploadRejectedError) setError(e.message);
      else setError("O envio falhou. Escolha o mesmo arquivo para retomar.");
    } finally {
      controller.current = null;
      setProgress(null);
    }
  };

  const uploading = progress !== null;
  const percent = progress ? Math.round((progress[0] / Math.max(progress[1], 1)) * 100) : 0;
  return (
    <div className="flex flex-col gap-5">
      <label
        htmlFor="arquivo"
        onDragOver={(e) => {
          e.preventDefault();
          if (!uploading) setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={(e) => {
          e.preventDefault();
          setDragging(false);
          if (!uploading) setFile(e.dataTransfer.files?.[0] ?? null);
        }}
        className={`flex cursor-pointer flex-col items-center justify-center gap-2 rounded-2xl border-[1.5px] border-dashed bg-deep px-4 py-8 text-center ${dragging ? "border-accent" : "border-line-bold"}`}
      >
        <span className="flex size-12 items-center justify-center rounded-full bg-[#7a52e02e] text-accent-soft">
          <UploadIcon size={22} />
        </span>
        <span className="font-semibold">Arraste o arquivo ou clique para selecionar</span>
        <span className="text-[13px] font-medium text-muted">MP4, MOV ou WEBM</span>
        <span className="font-mono text-xs break-all text-accent-soft">
          {file ? file.name : "Nenhum arquivo selecionado"}
        </span>
      </label>
      <input
        id="arquivo"
        type="file"
        accept="video/*"
        disabled={uploading}
        onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        className="sr-only"
      />

      {progress && (
        <div className="flex flex-col gap-1.5">
          <progress value={progress[0]} max={progress[1]} aria-label="Progresso do envio" className="sr-only" />
          <div className="h-1 overflow-hidden rounded-full bg-line-strong" aria-hidden="true">
            <div className="h-full rounded-full bg-accent transition-[width]" style={{ width: `${percent}%` }} />
          </div>
          <span className="font-mono text-xs text-muted">
            {progress[0]} / {progress[1]} partes · {percent}%
          </span>
        </div>
      )}
      {cancelled && <p className="m-0 text-sm text-muted">Envio cancelado.</p>}
      {error && (
        <p role="alert" className="m-0 rounded-lg border border-[#e0525240] bg-[#e052521a] px-3 py-2 text-sm text-[#f3b4b4]">
          {error}
        </p>
      )}

      <div className="flex justify-end gap-2 pt-1">
        <button
          type="button"
          disabled={!uploading}
          onClick={() => controller.current?.abort()}
          className="h-11 cursor-pointer rounded-lg border border-line-strong bg-transparent px-[18px] text-sm font-semibold text-ink transition-colors hover:bg-[#ede8df0d] disabled:cursor-default disabled:opacity-50"
        >
          Cancelar
        </button>
        <button
          type="button"
          disabled={!file || uploading}
          onClick={start}
          className="h-11 cursor-pointer rounded-lg border-0 bg-accent px-[18px] text-sm font-semibold text-white transition-colors hover:bg-accent-hover disabled:cursor-default disabled:opacity-50"
        >
          {uploading ? "Enviando…" : "Enviar"}
        </button>
      </div>
    </div>
  );
}
