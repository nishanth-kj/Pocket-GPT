export default function PrivacyPage() {
  return (
    <div className="container mx-auto px-4 py-24 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-4xl font-bold tracking-tight mb-8">Privacy Policy</h1>
        <div className="prose prose-zinc dark:prose-invert text-zinc-600 dark:text-zinc-400 space-y-6">
          <p className="text-lg font-medium text-foreground">
            Your privacy is our primary feature.
          </p>
          <p>
            Pocket GPT is designed from the ground up to be a fully local, offline application. We believe that your documents and queries belong to you alone.
          </p>
          <h2 className="text-2xl font-semibold text-foreground mt-8 mb-4">Data Collection</h2>
          <p>
            We do not collect, transmit, or store any of your personal data, documents, or chat history on any external servers. All processing, including vector embeddings and AI generation, occurs entirely on your device's hardware.
          </p>
          <h2 className="text-2xl font-semibold text-foreground mt-8 mb-4">Analytics</h2>
          <p>
            The Pocket GPT app does not contain any third-party tracking, analytics, or telemetry SDKs.
          </p>
        </div>
      </div>
    </div>
  );
}
