"use client";
import { useState } from "react";
import { motion } from "framer-motion";
import { ShieldCheck, Lock, User, ArrowRight } from "lucide-react";
import { GlassCard } from "@/components/ui/GlassCard";

export default function AdminLogin() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (username === "admin" && password === "admin123") {
      window.location.href = "/admin/dashboard";
    } else {
      alert("Invalid Admin Credentials");
    }
  };

  return (
    <div className="min-h-screen bg-[#060012] flex flex-col items-center justify-center p-6 relative overflow-hidden">
      {/* Background Glows (Deep Purple for Admin) */}
      <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-purple-900/20 blur-[150px] rounded-full text-white" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-indigo-900/20 blur-[150px] rounded-full" />

      <motion.div 
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex items-center gap-3 mb-8"
      >
        <ShieldCheck className="text-indigo-400 w-12 h-12 fill-indigo-400/20" />
        <h1 className="text-4xl font-black tracking-tighter text-white">RESUDEX <span className="text-indigo-400">ADMIN</span></h1>
      </motion.div>

      <GlassCard className="w-full max-w-md p-10 border-indigo-500/10 relative z-10" hoverGlow={false}>
        <div className="mb-8">
          <h2 className="text-2xl font-black mb-2">Recruiter Access</h2>
          <p className="text-slate-400 font-medium text-sm">Secure portal for candidate selection and job management.</p>
        </div>

        <form onSubmit={handleLogin} className="space-y-6">
          <div className="space-y-2">
            <label className="text-[10px] font-black uppercase tracking-widest text-indigo-400">Credential ID</label>
            <div className="relative">
              <User className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" size={18}/>
              <input 
                type="text" 
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="admin" 
                className="w-full bg-white/5 border border-white/10 rounded-xl py-4 pl-12 pr-4 text-sm focus:outline-none focus:border-indigo-400/50 transition-all text-white"
                required
              />
            </div>
          </div>

          <div className="space-y-2 text-white">
            <label className="text-[10px] font-black uppercase tracking-widest text-indigo-400">Security Key</label>
            <div className="relative">
              <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" size={18}/>
              <input 
                type="password" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••" 
                className="w-full bg-white/5 border border-white/10 rounded-xl py-4 pl-12 pr-4 text-sm focus:outline-none focus:border-indigo-400/50 transition-all text-white"
                required
              />
            </div>
          </div>

          <button className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-4 rounded-xl flex items-center justify-center gap-2 group transition-all shadow-[0_0_20px_rgba(79,70,229,0.3)]">
            AUTHORIZE <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />
          </button>
        </form>

        <div className="mt-8 pt-8 border-t border-white/5 text-center">
          <p className="text-slate-500 text-xs font-bold tracking-widest uppercase">
            Restricted System Access
          </p>
        </div>
      </GlassCard>
    </div>
  );
}
