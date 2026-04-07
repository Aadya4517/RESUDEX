"use client";
import { motion } from "framer-motion";
import { ReactNode } from "react";

interface GlassCardProps {
  children: ReactNode;
  className?: string;
  hoverGlow?: boolean;
}

export const GlassCard = ({ children, className = "", hoverGlow = true }: GlassCardProps) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      whileHover={hoverGlow ? { 
        y: -5, 
        borderColor: "rgba(0, 240, 255, 0.4)",
        boxShadow: "0 0 30px rgba(0, 240, 255, 0.1)"
      } : {}}
      className={`glass-card p-6 border border-white/10 ${className}`}
    >
      {children}
    </motion.div>
  );
};
