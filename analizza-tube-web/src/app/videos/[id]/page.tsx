import { Suspense } from "react";
import { VideoDetail } from "@/components/VideoDetail";

export default function VideoPage({ params }: PageProps<"/videos/[id]">) {
  return (
    <Suspense fallback={<p className="text-muted">Carregando…</p>}>
      <VideoDetailFromParams params={params} />
    </Suspense>
  );
}

async function VideoDetailFromParams({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <VideoDetail id={id} />;
}
