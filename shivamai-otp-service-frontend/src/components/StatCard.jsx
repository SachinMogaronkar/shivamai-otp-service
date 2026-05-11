import { useEffect, useState } from "react";

export default function StatCard({ title, value, color }) {

  const isPercentage = title.toLowerCase().includes("rate");
  const target = Number(value) || 0;

  const [display, setDisplay] = useState(0);

  useEffect(() => {
    let start = 0;
    const duration = 800;
    const stepTime = 16;
    const increment = target / (duration / stepTime);

    const interval = setInterval(() => {
      start += increment;

      if (start >= target) {
        setDisplay(target);
        clearInterval(interval);
      } else {
        setDisplay(start);
      }
    }, stepTime);

    return () => clearInterval(interval);
  }, [target]);

  return (
    <div className={`card ${color || ""}`}>
      <h4>{title}</h4>

      <p>
        {isPercentage ? display.toFixed(1) : Math.floor(display)}
        {isPercentage && "%"}
      </p>
    </div>
  );
}