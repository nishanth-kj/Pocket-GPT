import { Smartphone, Download } from "lucide-react";
import Link from "next/link";

export function Hero() {
  return (
    <section className="relative overflow-hidden bg-background min-h-[calc(100vh-4rem)] flex items-center py-12 sm:py-24">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 w-full">
        <div className="flex flex-col lg:flex-row items-center gap-12 lg:gap-24">
          <div className="flex-1 text-center lg:text-left">
            <h1 className="text-4xl font-extrabold tracking-tight sm:text-6xl text-foreground">
              Your Personal <br />
              <span className="text-blue-600 dark:text-blue-400">Offline AI Assistant</span>
            </h1>
            <p className="mt-6 text-lg leading-8 text-zinc-600 dark:text-zinc-400 max-w-2xl mx-auto lg:mx-0">
              Query your documents safely and securely with Pocket GPT. Fully local, completely offline, and always ready.
            </p>
            <div className="mt-10 flex flex-col sm:flex-row items-center justify-center lg:justify-start gap-4">
              <Link
                href="/app"
                className="flex items-center gap-2 rounded-full bg-blue-600 px-6 py-3.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 transition-colors w-full sm:w-auto justify-center"
              >
                <Smartphone className="h-5 w-5" />
                Get it on Google Play
              </Link>
              <a
                href="#"
                className="flex items-center gap-2 rounded-full px-6 py-3.5 text-sm font-semibold text-foreground ring-1 ring-inset ring-black/10 dark:ring-white/10 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors w-full sm:w-auto justify-center"
              >
                <Download className="h-5 w-5" />
                Download APK
              </a>
            </div>
          </div>
          <div className="flex-1 w-full max-w-lg lg:max-w-none">
            <div className="relative aspect-[9/16] w-full max-w-[300px] mx-auto rounded-3xl border-[8px] border-zinc-200 dark:border-zinc-800 bg-zinc-100 dark:bg-zinc-900 shadow-2xl overflow-hidden flex items-center justify-center">
              <div className="absolute inset-0 bg-gradient-to-br from-blue-100 to-purple-100 dark:from-blue-900/20 dark:to-purple-900/20" />
              <div className="z-10 text-center p-6">
                <p className="font-semibold text-zinc-400 dark:text-zinc-500">Android App Preview</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
