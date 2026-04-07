"use client";
import Link from "next/link";
import { motion } from "framer-motion";
import { Zap, ShieldCheck, Target, ArrowRight, UserPlus, LogIn } from "lucide-react";
import { GlassCard } from "@/components/ui/GlassCard";

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-[#0D0221] text-white selection:bg-[#00F0FF]/30 selection:text-white overflow-hidden relative">
      {/* Background Glows (Hero Style) */}
      <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-[#00F0FF]/10 blur-[150px] rounded-full" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-[#F72585]/10 blur-[150px] rounded-full" />

      {/* --- Navbar --- */}
      <nav className="fixed top-0 left-0 right-0 z-50 bg-[#0D0221]/80 backdrop-blur-xl border-b border-white/5 px-8 py-4 flex justify-between items-center">
        <div className="flex items-center gap-2 group cursor-pointer">
          <Zap className="text-[#00F0FF] w-8 h-8 fill-[#00F0FF]/20 group-hover:scale-110 transition-transform" />
          <h1 className="text-2xl font-black tracking-tighter">RESUDEX</h1>
        </div>
        <div className="flex items-center gap-6">
          <Link href="/login" className="text-sm font-black uppercase tracking-widest text-slate-400 hover:text-[#00F0FF] transition-colors">Sign In</Link>
          <Link href="/login" className="neon-button text-xs px-6 py-2.5">JOIN ENGINE</Link>
        </div>
      </nav>

      {/* --- Hero Section --- */}
      <section className="relative z-10 pt-48 pb-32 px-6 flex flex-col items-center text-center max-w-6xl mx-auto">


        <motion.h1 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="text-6xl md:text-8xl font-black tracking-tighter leading-[0.9] mb-8"
        >
          LAUNCH YOUR <br />
          <span className="bg-clip-text text-transparent bg-gradient-to-r from-cyan-400 via-blue-500 to-pink-500">EXPERT CAREER</span>
        </motion.h1>

        <motion.p 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="text-slate-400 text-lg md:text-xl max-w-2xl font-medium mb-12"
        >
          Precision resume matching for roles that fit your skills — not just keywords.
        </motion.p>

        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="flex flex-wrap justify-center gap-4"
        >
          <Link href="/login" className="neon-button text-base px-10 py-5 flex items-center gap-3">
            START YOUR JOURNEY <ArrowRight size={20} />
          </Link>
          <Link href="/admin/login" className="px-10 py-5 border border-white/10 rounded-2xl font-black text-sm tracking-widest hover:bg-white/5 transition-all text-slate-400 hover:text-white">
            RECRUITER ACCESS
          </Link>
        </motion.div>

        {/* --- Value Props --- */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-48 w-full">
           <ValueCard 
              icon={<Target className="text-[#00F0FF]" />}
              title="80%+ MATCH ACCURACY"
              desc="Domain-aware matching ensures your real expertise is weighted correctly."
           />
           <ValueCard 
              icon={<ShieldCheck className="text-emerald-400" />}
              title="VERIFIED PROFILES"
              desc="AI-verified skill mapping for credible, recruiter-ready profiles."
           />
           <ValueCard 
              icon={<UserPlus className="text-pink-500" />}
              title="RECRUITER DIRECT"
              desc="Get shortlisted by recruiters and receive instant dashboard notifications."
           />
        </div>
      </section>

      {/* Footer Branding */}
      <footer className="footer-glow text-center pb-12 pt-24 border-t border-white/5 relative z-10 w-full">
         <p className="text-[10px] font-black uppercase tracking-[0.5em] text-slate-600">RESUDEX · Resume Intelligence Platform</p>
      </footer>
    </div>
  );
}

function ValueCard({ icon, title, desc }: { icon: any, title: string, desc: string }) {
  return (
    <GlassCard className="text-left group border-white/5 hover:border-[#00F0FF]/30 h-full p-8">
      <div className="bg-white/5 p-4 rounded-2xl inline-block mb-6 group-hover:scale-110 transition-transform">{icon}</div>
      <h3 className="text-lg font-black tracking-tight mb-3 uppercase">{title}</h3>
      <p className="text-slate-500 text-sm font-medium leading-relaxed">{desc}</p>
    </GlassCard>
  );
}
