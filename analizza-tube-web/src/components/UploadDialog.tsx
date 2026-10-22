"use client";

import { useEffect } from "react";
import { CloseIcon } from "./icons";
import { UploadForm } from "./UploadForm";

export function UploadDialog({ onClose }: { onClose: () => void }) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  return (
    <div className="fixed inset-0 z-10 flex items-center justify-center bg-[#0b0a09b3] p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="upload-title"
        className="box-border flex max-h-[calc(100vh-32px)] w-full max-w-[560px] flex-col gap-5 overflow-y-auto rounded-[22px] border border-line-strong bg-surface p-6 shadow-[0_28px_56px_#0000008c,0_10px_20px_#0000004d]"
      >
        <div className="flex items-start justify-between gap-3">
          <UploadHeading />
          <button
            type="button"
            onClick={onClose}
            aria-label="Fechar"
            className="flex size-10 cursor-pointer items-center justify-center rounded-lg border-0 bg-transparent text-muted transition-colors hover:bg-[#ede8df0d]"
          >
            <CloseIcon />
          </button>
        </div>
        <UploadForm />
      </div>
    </div>
  );
}

export function UploadHeading() {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-[11px] font-semibold tracking-[0.08em] text-accent-soft">NOVO VÍDEO</span>
      <h2 id="upload-title" className="m-0 font-serif text-2xl leading-tight font-medium tracking-[-0.02em]">
        Enviar <span className="italic text-accent">vídeo</span>
      </h2>
    </div>
  );
}
