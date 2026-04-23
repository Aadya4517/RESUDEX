"use client";
import { useEffect, useState } from "react";
import axios from "axios";
import { motion, AnimatePresence } from "framer-motion";
import { 
  Briefcase, 
  User, 
  BarChart3, 
  Settings, 
  LogOut, 
  Search,
  UploadCloud,
  Zap,
  CheckCircle2,
  X,
  Download
} from "lucide-react";
import { GlassCard } from "@/components/ui/GlassCard";
import { MatchBadge } from "@/components/ui/MatchBadge";
import { ResumeSync } from "@/components/dashboard/ResumeSync";

// --- Types ---
interface Job {
  id: number;
  title: string;
  description: string;
  sc: number;
  hits: string[];
  miss: string[];
  roadmap: string[];
}

interface Analytics {
  domain_fit: Record<string, number>;
  exp_yrs: number;
  topDomains: string[];
}

interface Notification {
  app_id: number; // matched with backend field
  message: string;
}

/**
 * Main User Dashboard.
 * Where the magic happens - recruiters find you, you apply to jobs.
 */
export default function Dashboard() {
  const [job_list, set_job_list] = useState<Job[]>([]);
  const [stats, set_stats] = useState<Analytics | null>(null);
  const [notifs, set_notifs] = useState<any[]>([]);
  const [sync_modal, set_sync_modal] = useState(false);
  const [magic_job, set_magic_job] = useState<Job | null>(null);
  const [is_busy, set_is_busy] = useState(true);
  
  // get id from local storage
  const [uid, set_uid] = useState<number | null>(null);

  useEffect(() => {
    const saved_id = localStorage.getItem("uid");
    if (saved_id) {
        set_uid(parseInt(saved_id));
    }
  }, []);

  useEffect(() => {
    if (!uid) return;
    load_data();
    // check for new messages every few seconds
    const loop = setInterval(poll_notifs, 10000);
    return () => clearInterval(loop);
  }, [uid]);

  const load_data = async () => {
    if (!uid) return;
    try {
      set_is_busy(true);
      // new endpoints
      const [res_jobs, res_stats] = await Promise.all([
        axios.get(`http://localhost:8080/api/jobs/matched_for/${uid}`),
        axios.get(`http://localhost:8080/api/usr/cv_stats/${uid}`)
      ]);
      set_job_list(res_jobs.data);
      set_stats(res_stats.data);
    } catch (err) {
      console.error("Dashboard Load Err:", err);
    } finally {
      set_is_busy(false);
    }
  };

  const poll_notifs = async () => {
    if (!uid) return;
    try {
      const res = await axios.get(`http://localhost:8080/api/notifications/new_for/${uid}`);
      if (res.data.length !== notifs.length) {
        set_notifs(res.data);
      }
    } catch (e) {}
  };

  const kill_notif = async (nid: number) => {
    try {
      await axios.post(`http://localhost:8080/api/notifications/dismiss/${nid}`);
      set_notifs(prev => prev.filter(n => n.id !== nid));
    } catch (e) {}
  };

  return (
    <div className="flex min-h-screen bg-[#0D0221] text-white">
      {/* --- Sidebar --- */}
      <aside className="w-64 border-r border-white/5 bg-[#0D0221]/50 backdrop-blur-xl p-6 hidden lg:flex flex-col">
        <div className="flex items-center gap-3 mb-10 px-2">
          <Zap className="text-[#00F0FF] w-8 h-8 fill-[#00F0FF]/20" />
          <h1 className="text-2xl font-black tracking-tighter text-[#00F0FF]">RESUDEX</h1>
        </div>

        <nav className="flex-1 space-y-2">
          <SidebarItem icon={<Briefcase size={20}/>} label="Opportunities" active />
          <SidebarItem icon={<BarChart3 size={20}/>} label="Analytics" />
          <SidebarItem icon={<User size={20}/>} label="Profile" />
          <SidebarItem icon={<Settings size={20}/>} label="Settings" />
        </nav>

        <div className="pt-6 border-t border-white/5">
          <SidebarItem 
            icon={<LogOut size={20}/>} 
            label="Logout" 
            danger 
            onClick={() => { localStorage.clear(); window.location.href = "/login"; }}
          />
        </div>
      </aside>

      {/* --- Page Body --- */}
      <main className="flex-1 overflow-y-auto p-8 lg:p-12">
        <header className="flex justify-between items-center mb-12">
          <div>
            <h2 className="text-3xl font-black mb-2 flex items-center gap-2">
              Welcome Back <Zap className="text-yellow-400 fill-yellow-400/20" size={24}/>
            </h2>
            <p className="text-slate-400 font-medium">Profile is lookin' good. Keep it updated.</p>
          </div>
          <div className="flex gap-4">
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => window.open(`http://localhost:8080/api/resume/get_pdf/${uid}`)}
              className="px-4 py-3 border border-white/10 rounded-xl font-bold uppercase tracking-wider text-xs flex items-center gap-2 hover:bg-white/5 transition-all"
            >
              <Download size={18}/> GET PDF
            </motion.button>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => set_sync_modal(true)}
              className="neon-button flex items-center gap-2"
            >
              <UploadCloud size={18}/> SYNC PROFILE
            </motion.button>
          </div>
        </header>

        {/* --- Notifs --- */}
        <div className="fixed top-8 right-8 z-50 space-y-4">
          <AnimatePresence>
            {notifs.slice(0, 3).map((n) => (
              <motion.div
                key={n.id}
                initial={{ x: 100, opacity: 0 }}
                animate={{ x: 0, opacity: 1 }}
                exit={{ x: 100, opacity: 0 }}
                className="bg-[#00F0FF]/10 backdrop-blur-xl border border-[#00F0FF]/30 p-4 rounded-xl shadow-[0_0_20px_rgba(0,240,255,0.1)] flex gap-4 max-w-sm pointer-events-auto"
              >
                <div className="bg-[#00F0FF]/20 p-2 rounded-lg float-animation">
                  <Zap className="text-[#00F0FF]" size={20} />
                </div>
                <div className="flex-1">
                  <p className="text-xs font-bold leading-relaxed">{n.message}</p>
                  <button onClick={() => kill_notif(n.id)} className="text-[10px] font-black uppercase text-[#00F0FF] mt-2 hover:underline">Dismiss</button>
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>

        <AnimatePresence>
          {magic_job && (
            <div className="fixed inset-0 z-[60] flex items-center justify-center p-6">
              <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => set_magic_job(null)} className="absolute inset-0 bg-black/80 backdrop-blur-md" />
              <motion.div initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.9, opacity: 0 }} className="w-full max-w-2xl relative bg-[#0D0221] border border-[#00F0FF]/20 rounded-3xl p-8 overflow-hidden shadow-[0_0_50px_rgba(0,240,255,0.1)]">
                 <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-[#00F0FF] to-transparent" />
                 
                 <h2 className="text-3xl font-black mb-6 flex items-center gap-3">
                   LEVEL UP: <span className="text-[#00F0FF]">{magic_job.title}</span>
                 </h2>

                 <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div>
                      <h4 className="text-xs font-bold text-slate-500 uppercase tracking-widest mb-4">Magic Match Scorecard</h4>
                      <div className="grid grid-cols-4 gap-2">
                        {magic_job.hits.map(h => (
                          <div key={h} className="h-10 bg-[#00F0FF]/20 border border-[#00F0FF]/30 rounded-lg flex items-center justify-center" title={h}>
                            <Zap className="text-[#00F0FF]" size={14} />
                          </div>
                        ))}
                        {magic_job.miss.map(m => (
                          <div key={m} className="h-10 bg-white/5 border border-white/10 rounded-lg flex items-center justify-center opacity-40" title={m}>
                            <X className="text-slate-500" size={14} />
                          </div>
                        ))}
                      </div>
                      <p className="mt-4 text-xs text-slate-400 italic">Neon blocks show hits, dimmed show gaps.</p>
                    </div>

                    <div>
                      <h4 className="text-xs font-bold text-[#F72585] uppercase tracking-widest mb-4">The Roadmap</h4>
                      <div className="space-y-3">
                        {magic_job.roadmap?.map((step, idx) => (
                          <div key={idx} className="flex gap-3 bg-white/5 p-3 rounded-xl border border-white/5 hover:border-[#F72585]/30 transition-all">
                            <span className="text-[#F72585] font-black">{idx + 1}</span>
                            <p className="text-sm font-medium text-slate-200">{step}</p>
                          </div>
                        )) || <p className="text-sm text-slate-500">Perfect fit. No steps needed.</p>}
                      </div>
                    </div>
                 </div>

                 <button onClick={() => set_magic_job(null)} className="mt-10 w-full py-4 bg-white/5 hover:bg-white/10 rounded-xl font-black uppercase tracking-tighter text-sm transition-all">
                   I GOT THIS
                 </button>
              </motion.div>
            </div>
          )}
        </AnimatePresence>

        {/* --- Sync Popup --- */}
        <AnimatePresence>
          {sync_modal && (
            <div className="fixed inset-0 z-[60] flex items-center justify-center p-6">
              <motion.div 
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                onClick={() => set_sync_modal(false)}
                className="absolute inset-0 bg-black/60 backdrop-blur-sm"
              />
              <motion.div
                initial={{ scale: 0.9, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.9, opacity: 0 }}
                className="w-full max-w-lg relative"
              >
                <ResumeSync userId={uid!} onSuccess={() => { load_data(); set_sync_modal(false); }} />
                <button onClick={() => set_sync_modal(false)} className="absolute top-6 right-6 text-slate-500 hover:text-white">
                  <X className="w-6 h-6" />
                </button>
              </motion.div>
            </div>
          )}
        </AnimatePresence>

        {/* --- Stats Row --- */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          <GlassCard className="cyber-gradient border-none">
            <h4 className="text-sm font-bold text-slate-400 uppercase tracking-widest mb-4">Top Expertise</h4>
            <div className="space-y-4">
              {stats?.topDomains.slice(0, 3).map((domain) => (
                <div key={domain}>
                  <div className="flex justify-between text-xs font-bold mb-1 p-1">
                    <span>{domain}</span>
                    <span className="text-[#00F0FF]">{stats.domain_fit[domain]}%</span>
                  </div>
                  <div className="h-1.5 bg-white/5 rounded-full overflow-hidden">
                    <motion.div 
                      initial={{ width: 0 }}
                      animate={{ width: `${stats.domain_fit[domain]}%` }}
                      className="h-full bg-gradient-to-r from-cyan-500 to-blue-500 rounded-full"
                    />
                  </div>
                </div>
              ))}
            </div>
          </GlassCard>

          <GlassCard className="flex flex-col justify-center items-center text-center">
            <div className="bg-cyan-500/10 p-4 rounded-full mb-4">
              <Zap className="text-[#00F0FF]" size={32} />
            </div>
            <h3 className="text-4xl font-black text-[#00F0FF]">{stats?.exp_yrs || 0}+</h3>
            <p className="text-slate-400 font-bold uppercase text-xs tracking-widest mt-1">Years on Job</p>
          </GlassCard>

          <GlassCard className="flex flex-col justify-center items-center text-center">
             <div className="bg-pink-500/10 p-4 rounded-full mb-4">
               <CheckCircle2 className="text-[#F72585]" size={32} />
             </div>
             <h3 className="text-4xl font-black text-[#F72585]">{job_list.length}</h3>
             <p className="text-slate-400 font-bold uppercase text-xs tracking-widest mt-1">Job Matches</p>
          </GlassCard>
        </div>

        {/* --- Job Feed --- */}
        <section>
          <div className="flex items-center justify-between mb-8">
            <h3 className="text-xl font-black flex items-center gap-2">
              <Zap className="text-[#00F0FF] fill-[#00F0FF]/10" size={20}/> RECSS FOR YOU
            </h3>
            <div className="flex gap-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" size={16}/>
                <input 
                  type="text" 
                  placeholder="Filter..." 
                  className="bg-white/5 border border-white/10 rounded-xl py-2 pl-10 pr-4 text-sm focus:outline-none focus:border-[#00F0FF]/50 transition-all"
                />
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
            <AnimatePresence>
              {job_list.map((j, i) => (
                <motion.div
                  key={j.id}
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: i * 0.1 }}
                >
                  <GlassCard className="h-full flex flex-col group relative overflow-hidden">
                    <div className="absolute top-0 right-0 p-6">
                      <MatchBadge score={j.sc} />
                    </div>
                    
                    <h4 className="text-xl font-black pr-24 mb-2 group-hover:text-[#00F0FF] transition-colors">
                      {j.title}
                    </h4>
                    <p className="text-slate-400 text-sm leading-relaxed mb-6 line-clamp-2">
                      {j.description}
                    </p>

                    <div className="flex flex-wrap gap-2 mb-8">
                      {j.hits?.slice(0, 5).map(s => (
                        <span key={s} className="bg-white/5 border border-white/10 px-3 py-1 rounded-lg text-[10px] font-bold text-slate-300 uppercase tracking-wider">
                          # {s}
                        </span>
                      ))}
                    </div>

                    <div className="mt-auto flex gap-4">
                      <button className="flex-[2] neon-button">SEND CV</button>
                      <button 
                        onClick={() => set_magic_job(j)}
                        className="flex-1 px-4 py-3 border border-[#00F0FF]/20 bg-[#00F0FF]/5 hover:bg-[#00F0FF]/10 rounded-xl font-bold text-[10px] uppercase text-[#00F0FF] transition-all flex items-center justify-center gap-2"
                      >
                        <Zap size={14} /> INSIGHT
                      </button>
                    </div>
                  </GlassCard>
                </motion.div>
              ))}
            </AnimatePresence>
          </div>
        </section>
      </main>
    </div>
  );
}

function SidebarItem({ icon, label, active = false, danger = false, onClick }: { icon: any, label: string, active?: boolean, danger?: boolean, onClick?: () => void }) {
  return (
    <div 
      onClick={onClick}
      className={`
      group flex items-center gap-4 px-4 py-3 rounded-xl cursor-pointer transition-all duration-300
      ${active ? 'bg-[#00F0FF]/10 text-[#00F0FF] border border-[#00F0FF]/20 shadow-[0_0_15px_rgba(0,240,255,0.1)]' : 'text-slate-400 hover:text-white hover:bg-white/5'}
      ${danger ? 'hover:text-red-400 hover:bg-red-400/10' : ''}
    `}>
      <span className={active ? 'text-[#00F0FF]' : 'group-hover:scale-110 transition-transform'}>{icon}</span>
      <span className="font-bold text-sm tracking-tight">{label}</span>
    </div>
  );
}
