import { Smartphone, Download, Shield } from "lucide-react";
import Link from "next/link";
import { GithubIcon } from "./GithubIcon";

export function Hero() {
  return (
    <section className="relative overflow-hidden bg-background min-h-[calc(100vh-4rem)] lg:h-[calc(100vh-4rem)] flex items-center py-8 lg:py-0">
      {/* Background Effects */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)]"></div>
      <div className="absolute top-0 right-0 -mr-40 mt-10 opacity-30 dark:opacity-20 blur-3xl rounded-full bg-blue-400 w-96 h-96 mix-blend-multiply dark:mix-blend-screen pointer-events-none"></div>
      <div className="absolute bottom-0 left-0 -ml-40 mb-10 opacity-30 dark:opacity-20 blur-3xl rounded-full bg-purple-400 w-96 h-96 mix-blend-multiply dark:mix-blend-screen pointer-events-none"></div>

      <div className="container mx-auto px-8 md:px-12 lg:px-24 w-full max-w-7xl relative z-10">
        <div className="flex flex-col items-center justify-center">
          
          {/* Text Content */}
          <div className="text-center w-full max-w-4xl mx-auto flex flex-col items-center">
           
            <h1 className="text-4xl font-extrabold tracking-tight sm:text-6xl text-foreground leading-[1.1]">
              Your Personal <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600 dark:from-blue-400 dark:to-purple-400">
                AI Assistant
              </span>
            </h1>
            <p className="mt-6 text-lg leading-8 text-zinc-600 dark:text-zinc-400 max-w-2xl mx-auto">
              Query your documents safely and securely with Pocket GPT. Harness the power of local RAG on your Android device—no cloud required.
            </p>
            <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4 w-full">
              <Link
                href="/app"
                className="flex items-center gap-2 rounded-full bg-blue-600 px-8 py-4 text-sm font-semibold text-white shadow-xl shadow-blue-500/20 hover:bg-blue-500 hover:shadow-blue-500/40 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 transition-all w-full sm:w-auto justify-center hover:-translate-y-0.5"
              >
                <Smartphone className="h-5 w-5" />
                Google Play
              </Link>
              <a
                href="#"
                className="flex items-center gap-2 rounded-full px-8 py-4 text-sm font-semibold text-foreground ring-1 ring-inset ring-black/10 dark:ring-white/10 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-all w-full sm:w-auto justify-center hover:-translate-y-0.5"
              >
                <Download className="h-5 w-5" />
                Download APK
              </a>
              <a
                href="https://github.com/nishanth-kj/Pocket-GPT"
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-2 rounded-full px-8 py-4 text-sm font-semibold text-foreground ring-1 ring-inset ring-black/10 dark:ring-white/10 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-all w-full sm:w-auto justify-center hover:-translate-y-0.5"
              >
                <GithubIcon className="h-5 w-5" />
                GitHub
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
