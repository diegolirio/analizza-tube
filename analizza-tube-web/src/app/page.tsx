import { PlayIcon } from "@/components/icons";

export default function Home() {
  return (
    <section
      aria-labelledby="inicio"
      className="flex aspect-video w-full flex-col items-center justify-center gap-3 rounded-2xl border border-line bg-deep p-6 text-center"
    >
      <span className="flex size-[72px] items-center justify-center rounded-full border border-line-bold bg-[#1a1815cc]">
        <PlayIcon size={26} />
      </span>
      <h1 id="inicio" className="m-0 font-serif text-2xl leading-tight font-medium tracking-[-0.02em]">
        Escolha um vídeo
      </h1>
      <p className="m-0 max-w-sm text-sm text-muted">
        Toque num vídeo da lista para assistir, ou use “Enviar vídeo” para subir um novo.
      </p>
    </section>
  );
}
