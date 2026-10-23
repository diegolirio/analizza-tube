import { UploadHeading } from "@/components/UploadDialog";
import { UploadForm } from "@/components/UploadForm";

export default function UploadPage() {
  return (
    <section className="flex w-full max-w-[560px] flex-col gap-5 rounded-[22px] border border-line-strong bg-surface p-6">
      <UploadHeading />
      <UploadForm />
    </section>
  );
}
