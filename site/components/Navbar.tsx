import Link from "next/link";
import { ThemeToggle } from "./ThemeToggle";
import { BrainCircuit } from "lucide-react";

export function Navbar() {
  return (
    <nav className="sticky top-0 z-50 w-full border-b border-black/10 dark:border-white/10 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center gap-2">
            <Link href="/" className="flex items-center gap-2 group">
              <BrainCircuit className="h-6 w-6 text-blue-600 dark:text-blue-400 group-hover:scale-110 transition-transform" />
              <span className="text-lg font-bold tracking-tight">Pocket GPT</span>
            </Link>
          </div>
          
          <div className="flex items-center gap-6">
            <Link href="/app" className="text-sm font-medium text-zinc-600 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors">
              App
            </Link>
            <Link href="/docs" className="text-sm font-medium text-zinc-600 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors">
              Docs
            </Link>
            <Link href="/privacy" className="text-sm font-medium text-zinc-600 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors">
              Privacy
            </Link>
            <div className="pl-2 border-l border-black/10 dark:border-white/10">
              <ThemeToggle />
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
}
