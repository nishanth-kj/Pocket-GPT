"use client";

import Link from "next/link";
import { useState } from "react";
import { ThemeToggle } from "./ThemeToggle";
import { BrainCircuit, Menu, X } from "lucide-react";
import { GithubIcon } from "./GithubIcon";

export function Navbar() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  return (
    <nav className="sticky top-0 z-50 w-full border-b border-black/10 dark:border-white/10 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto px-8 md:px-12 lg:px-24 max-w-7xl">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center gap-2">
            <Link href="/" className="flex items-center gap-2 group" onClick={() => setIsMobileMenuOpen(false)}>
              <BrainCircuit className="h-6 w-6 text-blue-600 dark:text-blue-400 group-hover:scale-110 transition-transform" />
              <span className="text-lg font-bold tracking-tight">Pocket GPT</span>
            </Link>
          </div>
          
          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-6">
            <Link href="/app" className="text-sm font-medium text-zinc-600 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors">
              App
            </Link>
            <Link href="/docs" className="text-sm font-medium text-zinc-600 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors">
              Docs
            </Link>
            <Link href="/privacy" className="text-sm font-medium text-zinc-600 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors">
              Privacy
            </Link>
            <div className="pl-4 border-l border-black/10 dark:border-white/10 flex items-center gap-4">
              <a href="https://github.com/nishanth-kj/Pocket-GPT" target="_blank" rel="noopener noreferrer" className="text-zinc-600 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors" aria-label="GitHub Repository">
                <GithubIcon className="h-5 w-5" />
              </a>
              <ThemeToggle />
            </div>
          </div>

          {/* Mobile Menu Toggle */}
          <div className="flex items-center md:hidden gap-3">
            <a href="https://github.com/nishanth-kj/Pocket-GPT" target="_blank" rel="noopener noreferrer" className="text-zinc-600 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors" aria-label="GitHub Repository">
              <GithubIcon className="h-5 w-5" />
            </a>
            <ThemeToggle />
            <button
              type="button"
              className="p-2 -mr-2 rounded-md text-zinc-600 hover:text-black dark:text-zinc-400 dark:hover:text-white"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              aria-label="Toggle menu"
            >
              {isMobileMenuOpen ? (
                <X className="h-6 w-6" aria-hidden="true" />
              ) : (
                <Menu className="h-6 w-6" aria-hidden="true" />
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Navigation */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-t border-black/5 dark:border-white/5 bg-background">
          <div className="space-y-1 px-4 pb-3 pt-2">
            <Link
              href="/app"
              onClick={() => setIsMobileMenuOpen(false)}
              className="block rounded-md px-3 py-2 text-base font-medium text-zinc-600 hover:bg-zinc-50 hover:text-black dark:text-zinc-400 dark:hover:bg-zinc-900 dark:hover:text-white"
            >
              App
            </Link>
            <Link
              href="/docs"
              onClick={() => setIsMobileMenuOpen(false)}
              className="block rounded-md px-3 py-2 text-base font-medium text-zinc-600 hover:bg-zinc-50 hover:text-black dark:text-zinc-400 dark:hover:bg-zinc-900 dark:hover:text-white"
            >
              Docs
            </Link>
            <Link
              href="/privacy"
              onClick={() => setIsMobileMenuOpen(false)}
              className="block rounded-md px-3 py-2 text-base font-medium text-zinc-600 hover:bg-zinc-50 hover:text-black dark:text-zinc-400 dark:hover:bg-zinc-900 dark:hover:text-white"
            >
              Privacy
            </Link>
          </div>
        </div>
      )}
    </nav>
  );
}
