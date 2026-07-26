import Link from "next/link";

export function Footer() {
  return (
    <footer className="border-t border-black/10 dark:border-white/10 bg-zinc-50 dark:bg-zinc-950 mt-auto">
      <div className="container mx-auto px-8 md:px-12 lg:px-24 max-w-7xl py-6 md:py-8">
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
          </div>
        </div>
      </div>
    </footer>
  );
}
