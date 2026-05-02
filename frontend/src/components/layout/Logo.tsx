import { ChevronDown } from "lucide-react";

export function Logo({ size = 40 }: { size?: number }) {
  return (
    <div
      className="flex shrink-0 items-center justify-center rounded-full bg-primary text-white"
      style={{ width: size, height: size }}
    >
      <ChevronDown className="h-1/2 w-1/2" strokeWidth={3} />
    </div>
  );
}
