"use client";
import { motion } from "framer-motion";

interface MatchBadgeProps {
  score: number;
}

export const MatchBadge = ({ score }: MatchBadgeProps) => {
  const getColors = () => {
    if (score >= 80) return "text-emerald-400 border-emerald-500/50 bg-emerald-500/10 shadow-[0_0_15px_rgba(52,211,153,0.3)]";
    if (score >= 50) return "text-amber-400 border-amber-500/50 bg-amber-500/10 shadow-[0_0_15px_rgba(251,191,36,0.3)]";
    return "text-slate-400 border-slate-500/50 bg-slate-500/10 shadow-[0_0_15px_rgba(148,163,184,0.3)]";
  };

  return (
    <motion.div
      initial={{ scale: 0.8, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      transition={{ type: "spring", stiffness: 200, damping: 10 }}
      className={`px-3 py-1 rounded-full border text-xs font-bold uppercase tracking-wider backdrop-blur-sm ${getColors()}`}
    >
      {score}% Match
    </motion.div>
  );
};
