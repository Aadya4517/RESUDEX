"use client";
import { useState } from "react";
import axios from "axios";
import { motion, AnimatePresence } from "framer-motion";
import { Zap, Mail, Lock, User, ArrowRight, CheckCircle2, AlertCircle } from "lucide-react";
import { GlassCard } from "@/components/ui/GlassCard";

/**
 * Authentication Page.
 * Handles the login/signup logic for the app.
 */
export default function AuthPage() {
  const [is_login, set_is_login] = useState(true);
  const [is_busy, set_is_busy] = useState(false);
  const [err_msg, set_err_msg] = useState<string | null>(null);
  const [is_done, set_is_done] = useState(false);

  // local form data
  const [usr, set_usr] = useState("");
  const [pass, set_pass] = useState("");
  const [mail, set_mail] = useState("");
  const [fname, set_fname] = useState("");

  const do_auth = async (e: React.FormEvent) => {
    e.preventDefault();
    set_is_busy(true);
    set_err_msg(null);

    try {
      if (is_login) {
        // use the new snake_case endpoint
        const res = await axios.post("http://localhost:8080/api/auth/log_in", { 
          usr: usr, 
          pwd: pass 
        });
        
        // save the user metadata in sync
        localStorage.setItem("uid", res.data.uid);
        localStorage.setItem("usr", res.data.usr);
        
        window.location.href = "/dashboard";
      } else {
        // registration flow
        await axios.post("http://localhost:8080/api/auth/register_usr", {
          usr: usr,
          pwd: pass,
          email: mail,
          f_name: fname
        });
        set_is_done(true);
        setTimeout(() => {
          set_is_login(true);
          set_is_done(false);
        }, 2000);
      }
    } catch (err: any) {
      set_err_msg(err.response?.data?.error || "Auth fails. Check logic.");
      console.error("Auth Err:", err);
    } finally {
      set_is_busy(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#060012] flex flex-col items-center justify-center p-6 relative overflow-hidden">
      {/* lights in the back */}
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
          {is_done ? (
            <motion.div 
              key="success"
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 1.1 }}
              className="flex flex-col items-center justify-center text-center p-12 bg-[#00F0FF]/5 rounded-[3rem] border border-[#00F0FF]/20 backdrop-blur-3xl"
            >
              <CheckCircle2 size={64} className="text-[#00F0FF] mb-6 animate-bounce" />
              <h2 className="text-3xl font-black mb-2">You're In!</h2>
              <p className="text-slate-400 font-medium">Redirecting to sign in...</p>
            </motion.div>
          ) : (
            <motion.div
              key={is_login ? "login" : "register"}
              initial={{ x: is_login ? -20 : 20, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              exit={{ x: is_login ? 20 : -20, opacity: 0 }}
              transition={{ type: "spring", damping: 20, stiffness: 300 }}
            >
              <GlassCard className="p-10 border-[#00F0FF]/5" hoverGlow={false}>
                <div className="mb-10 text-center">
                  <h2 className="text-3xl font-black mb-2 lowercase">{is_login ? "Go sign in" : "Make new account"}</h2>
                  <p className="text-slate-400 text-sm font-medium tracking-tight">
                  {is_login ? "Use your creds." : "Fill the stuff below."}
                  </p>
                </div>

                <form onSubmit={do_auth} className="space-y-5">
                  <AnimatePresence>
                    {!is_login && (
                      <>
                        <InputField label="Full Name" icon={<User size={18}/>} placeholder="Name here" value={fname} onChange={set_fname} />
                        <InputField label="Email" icon={<Mail size={18}/>} placeholder="mail@here.com" value={mail} onChange={set_mail} />
                      </>
                    )}
                  </AnimatePresence>
                  
                  <InputField label="Username" icon={<Zap size={18}/>} placeholder="usr4517" value={usr} onChange={set_usr} />
                  <InputField label="Password" icon={<Lock size={18}/>} placeholder="••••••••" type="password" value={pass} onChange={set_pass} />

                  <AnimatePresence>
                    {err_msg && (
                      <motion.div 
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: "auto", opacity: 1 }}
                        className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl flex items-center gap-2 text-red-500 text-[10px] font-black uppercase tracking-widest"
                      >
                        <AlertCircle size={14}/> {err_msg}
                      </motion.div>
                    )}
                  </AnimatePresence>

                  <button 
                    disabled={is_busy}
                    className={`w-full neon-button py-4 mt-4 flex items-center justify-center gap-2 group ${is_busy ? 'opacity-50 cursor-wait' : ''}`}
                  >
                    {is_busy ? "Busy..." : is_login ? "SIGN IN" : "CREATE"} 
                    {!is_busy && <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />}
                  </button>
                </form>

                <div className="mt-10 pt-8 border-t border-white/5 text-center">
                  <p className="text-slate-500 text-xs font-black uppercase tracking-[0.2em]">
                    {is_login ? "No account?" : "Already in?"}
                    <button 
                      onClick={() => set_is_login(!is_login)}
                      className="ml-2 text-[#00F0FF] hover:underline cursor-pointer"
                    >
                      {is_login ? "CREATE" : "LOG IN"}
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
