export function HowItWorks() {
  return (
    <section className="py-16 md:py-24 lg:py-32 bg-background">
      <div className="container mx-auto px-8 md:px-12 lg:px-24 max-w-7xl">
        <div className="flex flex-col lg:flex-row items-center gap-16">
          <div className="flex-1 lg:order-2">
            <h2 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl mb-6">
              Three simple steps to smarter documents.
            </h2>
            <p className="text-lg text-zinc-600 dark:text-zinc-400 mb-10">
              Getting started with Pocket GPT takes less than a minute. No accounts, no subscriptions, no cloud setup.
            </p>
            
            <div className="space-y-8">
              <div className="flex gap-4">
                <div className="flex-shrink-0 flex items-center justify-center h-10 w-10 rounded-full bg-blue-600 text-white font-bold">1</div>
                <div>
                  <h3 className="text-xl font-semibold text-foreground">Install the App</h3>
                  <p className="mt-2 text-zinc-600 dark:text-zinc-400">Download the APK directly or grab it from the Play Store and install it on your Android device.</p>
                </div>
              </div>
              <div className="flex gap-4">
                <div className="flex-shrink-0 flex items-center justify-center h-10 w-10 rounded-full bg-blue-600 text-white font-bold">2</div>
                <div>
                  <h3 className="text-xl font-semibold text-foreground">Import Documents</h3>
                  <p className="mt-2 text-zinc-600 dark:text-zinc-400">Tap the (+) button in the app to import PDFs, text files, or scanned images. The app will immediately chunk and embed them locally.</p>
                </div>
              </div>
              <div className="flex gap-4">
                <div className="flex-shrink-0 flex items-center justify-center h-10 w-10 rounded-full bg-blue-600 text-white font-bold">3</div>
                <div>
                  <h3 className="text-xl font-semibold text-foreground">Start Asking Questions</h3>
                  <p className="mt-2 text-zinc-600 dark:text-zinc-400">Open the AI Chat interface and query your documents. The vector database retrieves the context instantly.</p>
                </div>
              </div>
            </div>
          </div>
          
          <div className="flex-1 lg:order-1 w-full flex justify-center">
             <div className="relative aspect-square w-full max-w-md rounded-3xl bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 p-8 flex items-center justify-center shadow-xl">
               {/* Visual Placeholder for app workflow */}
               <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-blue-100 via-transparent to-transparent dark:from-blue-900/20 opacity-70 rounded-3xl"></div>
               <div className="space-y-4 w-full z-10">
                 <div className="h-12 w-3/4 bg-white dark:bg-black rounded-xl shadow-sm border border-zinc-200 dark:border-zinc-800 p-3 flex items-center opacity-70">
                   <div className="h-4 w-4 rounded-full bg-blue-500 mr-3"></div>
                   <div className="h-3 w-1/2 bg-zinc-200 dark:bg-zinc-800 rounded"></div>
                 </div>
                 <div className="h-24 w-full bg-blue-50 dark:bg-blue-950/30 rounded-xl shadow-sm border border-blue-200 dark:border-blue-900 p-4">
                   <div className="h-3 w-3/4 bg-blue-200 dark:bg-blue-800 rounded mb-3"></div>
                   <div className="h-3 w-full bg-blue-200 dark:bg-blue-800 rounded mb-3"></div>
                   <div className="h-3 w-5/6 bg-blue-200 dark:bg-blue-800 rounded"></div>
                 </div>
                 <div className="h-12 w-2/3 ml-auto bg-white dark:bg-black rounded-xl shadow-sm border border-zinc-200 dark:border-zinc-800 p-3 flex items-center justify-end opacity-70">
                    <div className="h-3 w-1/2 bg-zinc-200 dark:bg-zinc-800 rounded mr-3"></div>
                    <div className="h-4 w-4 rounded-full bg-purple-500"></div>
                 </div>
               </div>
             </div>
          </div>
        </div>
      </div>
    </section>
  );
}
