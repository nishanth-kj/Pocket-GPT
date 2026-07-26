export default function DocsPage() {
  return (
    <div className="container mx-auto px-4 py-24 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-4xl font-bold tracking-tight mb-8">Documentation</h1>
        <div className="prose prose-zinc dark:prose-invert">
          <p className="text-lg leading-8 text-zinc-600 dark:text-zinc-400 mb-8">
            Learn how to use Pocket GPT to search and query your local documents securely.
          </p>
          <div className="space-y-8">
            <section>
              <h2 className="text-2xl font-semibold mb-4 border-b border-black/10 dark:border-white/10 pb-2">Getting Started</h2>
              <p className="text-zinc-600 dark:text-zinc-400">
                1. Download and install the Pocket GPT APK on your Android device.<br/>
                2. Open the app and grant necessary storage permissions.<br/>
                3. Import a PDF or text document using the FAB (+) button.<br/>
                4. Start querying the document via the AI Chat interface.
              </p>
            </section>
          </div>
        </div>
      </div>
    </div>
  );
}
