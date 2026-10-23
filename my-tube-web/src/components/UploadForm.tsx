"use client";

import { useState } from "react";
import { UploadIcon } from "./icons";
import { useUploadJobContext } from "./UploadJob";

/** Escolhe o arquivo e entrega o envio ao AppShell, que mostra o progresso fora do modal. */
export function UploadForm({ onStarted }: { onStarted?: () => void }) {
  const { busy, start } = useUploadJobContext();
  const [file, setFile] = useState<File | null>(null);
  const [dragging, setDragging] = useState(false);

  const send = () => {
    if (!file || busy) return;
    start(file);
    setFile(null);
    onStarted?.();
  };

  return (
    <div className="flex flex-col gap-5">
      <label
        htmlFor="arquivo"
        onDragOver={(e) => {
          e.preventDefault();
          if (!busy) setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={(e) => {
          e.preventDefault();
          setDragging(false);
          if (!busy) setFile(e.dataTransfer.files?.[0] ?? null);
        }}
        className={`flex cursor-pointer flex-col items-center justify-center gap-2 rounded-2xl border-[1.5px] border-dashed bg-deep px-4 py-8 text-center ${dragging ? "border-accent" : "border-line-bold"}`}
      >
        <span className="flex size-12 items-center justify-center rounded-full bg-[#16803c1f] text-accent-soft">
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
        disabled={busy}
        onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        className="sr-only"
      />

      {busy && <p className="m-0 text-sm text-muted">Já há um envio em andamento. Acompanhe na lista de vídeos.</p>}

      <div className="flex justify-end gap-2 pt-1">
        <button
          type="button"
          disabled={!file || busy}
          onClick={send}
          className="h-11 cursor-pointer rounded-lg border-0 bg-accent px-[18px] text-sm font-semibold text-white transition-colors hover:bg-accent-hover disabled:cursor-default disabled:opacity-50"
        >
          Enviar
        </button>
      </div>
    </div>
  );
}
