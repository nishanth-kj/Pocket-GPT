import Link from "next/link";
import { GithubIcon } from "./GithubIcon";

export function Footer() {
  return (
    <footer className="border-t border-black/10 dark:border-white/10 bg-zinc-50 dark:bg-zinc-950 mt-auto">
      <div className="container mx-auto px-8 md:px-12 lg:px-24 max-w-7xl py-4">
        <div className="flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-sm text-zinc-500 dark:text-zinc-400">
            &copy; {new Date().getFullYear()} Pocket GPT. All rights reserved.
          </p>
          <div className="flex items-center gap-6">
            <Link href="/app" className="text-sm text-zinc-500 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors">
              App Features
            </Link>
            <Link href="/docs" className="text-sm text-zinc-500 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors">
              Documentation
            </Link>
            <Link href="/privacy" className="text-sm text-zinc-500 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors">
              Privacy Policy
            </Link>
            <div className="pl-2 ml-2 border-l border-black/10 dark:border-white/10 flex items-center">
              <a href="https://github.com/nishanth-kj/Pocket-GPT" target="_blank" rel="noopener noreferrer" className="text-zinc-500 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors" aria-label="GitHub Repository">
                <GithubIcon className="h-5 w-5" />
              </a>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
