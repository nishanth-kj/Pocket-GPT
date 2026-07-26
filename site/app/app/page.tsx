export default function AppPage() {
  return (
    <div className="container mx-auto px-4 py-24 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-4xl font-bold tracking-tight mb-8">The Pocket GPT Android App</h1>
        <div className="prose prose-zinc dark:prose-invert">
          <p className="text-lg leading-8 text-zinc-600 dark:text-zinc-400 mb-8">
            Pocket GPT brings the power of Retrieval-Augmented Generation (RAG) directly to your Android device, entirely offline.
          </p>
          <div className="bg-zinc-100 dark:bg-zinc-900 rounded-2xl p-8 mb-8 border border-zinc-200 dark:border-zinc-800">
            <h2 className="text-2xl font-semibold mb-4">Key Features</h2>
            <ul className="space-y-3 text-zinc-600 dark:text-zinc-400 list-disc pl-5">
              <li>100% Offline processing - no data leaves your device</li>
              <li>Local vector database using SQLite (Room)</li>
              <li>Fast document chunking and embeddings</li>
              <li>Native integration with Android UI</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
