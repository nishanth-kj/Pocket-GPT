import { ShieldCheck, Database, LayoutTemplate } from "lucide-react";

const features = [
  {
    name: "100% Offline Privacy",
    description: "Your documents never leave your device. All embeddings and AI processing happen completely offline, ensuring maximum data security.",
    icon: ShieldCheck,
  },
  {
    name: "Lightning Fast Vector Search",
    description: "Powered by a highly optimized SQLite database under the hood, enabling instant retrieval augmented generation (RAG).",
    icon: Database,
  },
  {
    name: "Native Android Interface",
    description: "Built with standard Android UI paradigms, offering a smooth, responsive, and familiar experience perfectly tailored for mobile.",
    icon: LayoutTemplate,
  },
];

export function Features() {
  return (
    <section className="py-16 md:py-24 lg:py-32 bg-zinc-50 dark:bg-zinc-950">
      <div className="container mx-auto px-8 md:px-12 lg:px-24 max-w-7xl">
        <div className="text-center max-w-2xl mx-auto mb-16">
          <h2 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
            Everything you need, nothing you don't.
          </h2>
          <p className="mt-4 text-lg text-zinc-600 dark:text-zinc-400">
            Pocket GPT is stripped of cloud telemetry and bloated frameworks to give you a pure, private, and incredibly fast document assistant.
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 lg:gap-12">
          {features.map((feature) => (
            <div key={feature.name} className="flex flex-col items-center text-center p-6 bg-white dark:bg-zinc-900 rounded-3xl shadow-sm border border-zinc-200 dark:border-zinc-800">
              <div className="h-16 w-16 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center mb-6">
                <feature.icon className="h-8 w-8 text-blue-600 dark:text-blue-400" aria-hidden="true" />
              </div>
              <h3 className="text-xl font-semibold text-foreground mb-3">{feature.name}</h3>
              <p className="text-zinc-600 dark:text-zinc-400 leading-relaxed">
                {feature.description}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
