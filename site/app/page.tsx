import { Hero } from "@/components/Hero";
import { Features } from "@/components/Features";
import { HowItWorks } from "@/components/HowItWorks";
import { CallToAction } from "@/components/CallToAction";

export default function Home() {
  return (
    <div className="flex flex-col flex-1 w-full">
      <Hero />
      <Features />
      <HowItWorks />
      <CallToAction />
    </div>
  );
}
