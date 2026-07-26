import Link from "next/link";
import { Download } from "lucide-react";

export function CallToAction() {
  return (
    <section className="py-16 md:py-24 lg:py-32 bg-zinc-50 dark:bg-zinc-950 relative overflow-hidden">
      <div className="absolute inset-0 bg-blue-600/5 dark:bg-blue-600/10 [mask-image:linear-gradient(to_bottom,transparent,black,transparent)]" />
      <div className="container mx-auto px-8 md:px-12 lg:px-24 max-w-7xl relative z-10 text-center">
        <h2 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl mb-6">
          Ready to take back control of your data?
        </h2>
        <p className="text-xl text-zinc-600 dark:text-zinc-400 mb-10 max-w-2xl mx-auto">
          Experience the power of local LLMs and vector search on your Android device today.
        </p>
        <div className="flex justify-center">
          <Link
            href="/app"
            className="flex items-center gap-2 rounded-full bg-blue-600 px-8 py-4 text-base font-semibold text-white shadow-lg hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 transition-all hover:scale-105"
          >
            <Download className="h-5 w-5" />
            Download Pocket GPT
          </Link>
        </div>
      </div>
    </section>
  );
}
