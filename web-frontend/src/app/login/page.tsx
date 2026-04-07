"use client";
import { useState } from "react";
import axios from "axios";
import { motion, AnimatePresence } from "framer-motion";
import { Zap, Mail, Lock, User, ArrowRight, CheckCircle2, AlertCircle } from "lucide-react";
import { GlassCard } from "@/components/ui/GlassCard";

export default function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  // Form States
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");

  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      if (isLogin) {
        const res = await axios.post("http://localhost:8080/api/auth/login", { username, password });
        // In a real app, save JWT. Here we just redirect.
        window.location.href = "/dashboard";
      } else {
        await axios.post("http://localhost:8080/api/users/register", {
          username,
          password,
          email,
          fullName
        });
        setSuccess(true);
        setTimeout(() => {
          setIsLogin(true);
          setSuccess(false);
        }, 2000);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || "Authentication failed. Please check your credentials.");
      console.error("Auth Error:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#060012] flex flex-col items-center justify-center p-6 relative overflow-hidden">
      {/* Background Glows (Auth Style) */}
      <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-[#00F0FF]/10 blur-[150px] rounded-full" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-[#F72585]/10 blur-[150px] rounded-full" />

      <motion.div 
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex items-center gap-2 mb-12 cursor-pointer"
        onClick={() => window.location.href = "/"}
      >
        <Zap className="text-[#00F0FF] w-12 h-12 fill-[#00F0FF]/20" />
        <h1 className="text-4xl font-black tracking-tighter">RESUDEX</h1>
      </motion.div>

      <div className="relative w-full max-w-md">
        <AnimatePresence mode="wait">
          {success ? (
            <motion.div 
              key="success"
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 1.1 }}
              className="flex flex-col items-center justify-center text-center p-12 bg-[#00F0FF]/5 rounded-[3rem] border border-[#00F0FF]/20 backdrop-blur-3xl"
            >
              <CheckCircle2 size={64} className="text-[#00F0FF] mb-6 animate-bounce" />
              <h2 className="text-3xl font-black mb-2">You're In!</h2>
              <p className="text-slate-400 font-medium">Account created. Redirecting to sign in...</p>
            </motion.div>
          ) : (
            <motion.div
              key={isLogin ? "login" : "register"}
              initial={{ x: isLogin ? -20 : 20, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              exit={{ x: isLogin ? 20 : -20, opacity: 0 }}
              transition={{ type: "spring", damping: 20, stiffness: 300 }}
            >
              <GlassCard className="p-10 border-[#00F0FF]/5" hoverGlow={false}>
                <div className="mb-10 text-center">
                  <h2 className="text-3xl font-black mb-2 lowercase">{isLogin ? "Welcome Back" : "New Account"}</h2>
                  <p className="text-slate-400 text-sm font-medium tracking-tight">
                  {isLogin ? "Sign in to your account." : "Join RESUDEX."}
                  </p>
                </div>

                <form onSubmit={handleAuth} className="space-y-5">
                  <AnimatePresence>
                    {!isLogin && (
                      <>
                        <InputField label="Full Name" icon={<User size={18}/>} placeholder="Aadya Pratap" value={fullName} onChange={setFullName} />
                        <InputField label="Email" icon={<Mail size={18}/>} placeholder="aadya@example.com" value={email} onChange={setEmail} />
                      </>
                    )}
                  </AnimatePresence>
                  
                  <InputField label="Username" icon={<Zap size={18}/>} placeholder="aadya4517" value={username} onChange={setUsername} />
                  <InputField label="Password" icon={<Lock size={18}/>} placeholder="••••••••" type="password" value={password} onChange={setPassword} />

                  <AnimatePresence>
                    {error && (
                      <motion.div 
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: "auto", opacity: 1 }}
                        className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl flex items-center gap-2 text-red-500 text-[10px] font-black uppercase tracking-widest"
                      >
                        <AlertCircle size={14}/> {error}
                      </motion.div>
                    )}
                  </AnimatePresence>

                  <button 
                    disabled={loading}
                    className={`w-full neon-button py-4 mt-4 flex items-center justify-center gap-2 group ${loading ? 'opacity-50 cursor-wait' : ''}`}
                  >
                    {loading ? "Signing in..." : isLogin ? "SIGN IN" : "CREATE ACCOUNT"} 
                    {!loading && <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />}
                  </button>
                </form>

                <div className="mt-10 pt-8 border-t border-white/5 text-center">
                  <p className="text-slate-500 text-xs font-black uppercase tracking-[0.2em]">
                    {isLogin ? "No account?" : "Already a member?"}
                    <button 
                      onClick={() => setIsLogin(!isLogin)}
                      className="ml-2 text-[#00F0FF] hover:underline cursor-pointer"
                    >
                      {isLogin ? "CREATE ACCOUNT" : "SIGN IN"}
                    </button>
                  </p>
                </div>
              </GlassCard>
            </motion.div>
          )}
        </AnimatePresence>
      </div>


    </div>
  );
}

function InputField({ label, icon, placeholder, type = "text", value, onChange }: { 
  label: string, icon: any, placeholder: string, type?: string, value: string, onChange: (v: string) => void 
}) {
  return (
    <motion.div 
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-2 text-white"
    >
      <label className="text-[10px] font-black uppercase tracking-[0.3em] text-[#00F0FF]/60 ml-1">{label}</label>
      <div className="relative group">
        <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-[#00F0FF] transition-colors">{icon}</div>
        <input 
          type={type} 
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder} 
          className="w-full bg-white/5 border border-white/10 rounded-2xl py-4 pl-12 pr-4 text-sm focus:outline-none focus:border-[#00F0FF]/50 transition-all placeholder:text-slate-700 font-medium"
          required
        />
      </div>
    </motion.div>
  );
}

function StatItem({ label, val, color }: { label: string, val: string, color: string }) {
  return (
    <div className="flex items-center gap-2 px-4 py-1">
      <span className="text-[9px] font-black uppercase tracking-widest text-slate-600">{label}</span>
      <span className={`text-[9px] font-black uppercase ${color} tracking-tighter`}>{val}</span>
    </div>
  );
}
