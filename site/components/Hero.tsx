import { Smartphone, Download, Sparkles, Shield, Send } from "lucide-react";
import Link from "next/link";

export function Hero() {
  return (
    <section className="relative overflow-hidden bg-background min-h-[calc(100vh-4rem)] flex items-center py-12 md:py-16">
      {/* Background Effects */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)]"></div>
      <div className="absolute top-0 right-0 -mr-40 mt-10 opacity-30 dark:opacity-20 blur-3xl rounded-full bg-blue-400 w-96 h-96 mix-blend-multiply dark:mix-blend-screen pointer-events-none"></div>
      <div className="absolute bottom-0 left-0 -ml-40 mb-10 opacity-30 dark:opacity-20 blur-3xl rounded-full bg-purple-400 w-96 h-96 mix-blend-multiply dark:mix-blend-screen pointer-events-none"></div>

      <div className="container mx-auto px-8 md:px-12 lg:px-24 w-full max-w-7xl relative z-10">
        <div className="flex flex-col lg:flex-row items-center gap-12 lg:gap-24">
          
          {/* Text Content */}
          <div className="flex-1 text-center lg:text-left">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 text-sm font-medium mb-6 ring-1 ring-inset ring-blue-500/20">
              <Shield className="h-4 w-4" />
              100% Offline & Private
            </div>
            <h1 className="text-4xl font-extrabold tracking-tight sm:text-6xl text-foreground leading-[1.1]">
              Your Personal <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600 dark:from-blue-400 dark:to-purple-400">
                AI Assistant
              </span>
            </h1>
            <p className="mt-6 text-lg leading-8 text-zinc-600 dark:text-zinc-400 max-w-2xl mx-auto lg:mx-0">
              Query your documents safely and securely with Pocket GPT. Harness the power of local RAG on your Android device—no cloud required.
            </p>
            <div className="mt-10 flex flex-col sm:flex-row items-center justify-center lg:justify-start gap-4">
              <Link
                href="/app"
                className="flex items-center gap-2 rounded-full bg-blue-600 px-8 py-4 text-sm font-semibold text-white shadow-xl shadow-blue-500/20 hover:bg-blue-500 hover:shadow-blue-500/40 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 transition-all w-full sm:w-auto justify-center hover:-translate-y-0.5"
              >
                <Smartphone className="h-5 w-5" />
                Get it on Google Play
              </Link>
              <a
                href="#"
                className="flex items-center gap-2 rounded-full px-8 py-4 text-sm font-semibold text-foreground ring-1 ring-inset ring-black/10 dark:ring-white/10 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-all w-full sm:w-auto justify-center hover:-translate-y-0.5"
              >
                <Download className="h-5 w-5" />
                Download APK
              </a>
            </div>
          </div>
          
          {/* App Preview Mockup */}
          <div className="flex-1 w-full max-w-lg lg:max-w-none flex justify-center lg:justify-end mt-12 lg:mt-0">
            <div className="relative aspect-[9/19] w-full max-w-[260px] lg:max-w-[300px] rounded-[2.5rem] border-[8px] border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 shadow-2xl overflow-hidden flex flex-col transform lg:rotate-2 hover:rotate-0 transition-transform duration-500">
              
              {/* App Header */}
              <div className="px-6 py-4 border-b border-zinc-100 dark:border-zinc-800 flex justify-between items-center bg-white/80 dark:bg-zinc-950/80 backdrop-blur-md z-10 sticky top-0">
                 <div className="flex items-center gap-2">
                    <Sparkles className="h-5 w-5 text-blue-500" />
                    <span className="font-semibold text-sm">Pocket GPT</span>
                 </div>
                 <div className="h-6 w-6 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center">
                   <div className="h-1 w-1 rounded-full bg-zinc-400"></div>
                 </div>
              </div>

              {/* Chat Area */}
              <div className="flex-1 p-4 flex flex-col gap-4 overflow-hidden relative">
                 <div className="absolute inset-0 bg-gradient-to-b from-blue-50/50 to-transparent dark:from-blue-900/10 pointer-events-none" />
                 
                 <div className="self-end max-w-[85%] bg-blue-600 text-white p-3 rounded-2xl rounded-tr-sm text-xs shadow-sm z-10">
                   What are the key points in the Q2 report?
                 </div>
                 
                 <div className="self-start max-w-[85%] bg-zinc-100 dark:bg-zinc-800 text-foreground p-3 rounded-2xl rounded-tl-sm text-xs shadow-sm z-10">
                   Based on your document <strong>Q2_Financials.pdf</strong>, the key points are:
                   <ul className="list-disc pl-4 mt-2 space-y-1 text-zinc-600 dark:text-zinc-400">
                     <li>Revenue increased by 14%</li>
                     <li>Operating costs decreased</li>
                   </ul>
                 </div>
                 
                 <div className="self-end max-w-[85%] bg-blue-600 text-white p-3 rounded-2xl rounded-tr-sm text-xs shadow-sm z-10 opacity-50">
                   <div className="h-2 w-12 bg-white/50 rounded animate-pulse"></div>
                 </div>
              </div>

              {/* App Input Bar */}
              <div className="p-4 bg-white dark:bg-zinc-950 border-t border-zinc-100 dark:border-zinc-800 z-10">
                <div className="flex items-center bg-zinc-100 dark:bg-zinc-900 rounded-full px-4 py-2">
                  <div className="flex-1 h-4 bg-transparent text-xs text-zinc-400">Ask a question...</div>
                  <div className="h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center -mr-2">
                     <Send className="h-4 w-4 text-white ml-0.5" />
                  </div>
                </div>
              </div>
              
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
