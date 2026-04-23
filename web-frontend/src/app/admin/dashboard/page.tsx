"use client";
import { useEffect, useState } from "react";
import axios from "axios";
import { motion, AnimatePresence } from "framer-motion";
import { 
  ShieldCheck, 
  Users, 
  Briefcase, 
  Plus, 
  ChevronRight, 
  TrendingUp,
  Mail,
  Zap,
  X
} from "lucide-react";
import { GlassCard } from "@/components/ui/GlassCard";
import { MatchBadge } from "@/components/ui/MatchBadge";

// --- Types ---
interface Job {
  id: number;
  title: string;
  description: string;
}

interface AppEntry {
  id: number; // user id
  username: string;
  full_name: string;
  sc: number;
  hits: string[];
  miss: string[];
  tech_sc: number;
  app_id: number; // matching backend key
  status: string;
  vibes: string;
}

/**
 * Admin Panel.
 * For recruiters to manage jobs and move candidates through the pipe.
 */
export default function AdminDashboard() {
  const [job_data, set_job_data] = useState<Job[]>([]);
  const [cur_job, set_cur_job] = useState<Job | null>(null);
  const [app_list, set_app_list] = useState<AppEntry[]>([]);
  const [wait, set_wait] = useState(true);
  const [mode, set_mode] = useState<"jobs" | "apps">("jobs");
  const [expanded_aid, set_expanded_aid] = useState<number | null>(null);
  const [msg_text, set_msg_text] = useState("");
  const [user_notes, set_user_notes] = useState<any[]>([]);

  const PIPE_STAGES = ["APPLIED", "SCREENING", "INTERVIEWING", "OFFERED", "REJECTED"];
  const VIBES = ["Wizard", "Grit", "Cultural Fit", "Fast Learner", "Problem Solver"];

  useEffect(() => {
    load_jobs();
  }, []);

  const toggle_vibe = async (aid: number, v: string) => {
    const app = app_list.find(a => a.app_id === aid);
    if (!app) return;
    
    let cur = app.vibes ? app.vibes.split(",") : [];
    if (cur.includes(v)) cur = cur.filter(x => x !== v);
    else cur.push(v);
    
    const next = cur.join(",");
    set_app_list(prev => prev.map(a => a.app_id === aid ? { ...a, vibes: next } : a));
    
    try {
      await axios.post("http://localhost:8080/api/applications/set_vibes", { aid: aid, v: next });
    } catch (e) {}
  };

  const load_jobs = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/jobs/list");
      set_job_data(res.data);
      set_wait(false);
    } catch (e) {}
  };

  const load_apps = async (jid: number) => {
    try {
      const res = await axios.get(`http://localhost:8080/api/jobs/apps_for/${jid}`);
      set_app_list(res.data);
      set_mode("apps");
    } catch (e) {}
  };

  const shortlist = async (aid: number) => {
    try {
      await axios.post(`http://localhost:8080/api/applicants/ok/${aid}`);
      if (cur_job) load_apps(cur_job.id);
    } catch (e) {}
  };

  const start_drag = (e: React.DragEvent, aid: number) => {
    e.dataTransfer.setData("aid", aid.toString());
  };

  const drop_it = async (e: React.DragEvent, s: string) => {
    e.preventDefault();
    const id_str = e.dataTransfer.getData("aid");
    if (!id_str) return;
    const aid = parseInt(id_str);
    
    set_app_list(prev => prev.map(a => a.app_id === aid ? { ...a, status: s } : a));
    
    try {
      await axios.put(`http://localhost:8080/api/applications/set_state/${aid}`, { status: s });
    } catch (err) {}
  };

  const over_it = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const show_notes = async (uid: number, aid: number) => {
    if (expanded_aid === aid) {
      set_expanded_aid(null);
      return;
    }
    set_expanded_aid(aid);
    try {
      const res = await axios.get(`http://localhost:8080/api/usr/notes/${uid}`);
      set_user_notes(res.data);
    } catch (e) {}
  };

  const add_note = async (uid: number) => {
    if (!msg_text.trim()) return;
    try {
      await axios.post(`http://localhost:8080/api/usr/notes/${uid}`, { note: msg_text });
      set_msg_text("");
      const res = await axios.get(`http://localhost:8080/api/usr/notes/${uid}`);
      set_user_notes(res.data);
    } catch (e) {}
  };

  return (
    <div className="flex min-h-screen bg-[#060012] text-white">
      {/* --- Nav --- */}
      <aside className="w-64 border-r border-indigo-500/10 bg-[#060012]/80 backdrop-blur-3xl p-6 flex flex-col fixed h-full z-20">
        <div className="flex items-center gap-3 mb-10 px-2 cursor-pointer" onClick={() => { set_mode("jobs"); set_cur_job(null); }}>
          <ShieldCheck className="text-indigo-400 w-8 h-8 fill-indigo-400/20" />
          <h1 className="text-2xl font-black tracking-tighter text-indigo-400">ADM CENTER</h1>
        </div>

        <nav className="flex-1 space-y-2">
          <SidebarItem icon={<Briefcase size={20}/>} label="Listings" active={mode === "jobs"} onClick={() => set_mode("jobs")} />
          <SidebarItem icon={<Users size={20}/>} label="Pool" />
          <SidebarItem icon={<TrendingUp size={20}/>} label="Stats" />
        </nav>

        <div className="pt-6 border-t border-white/5">
          <SidebarItem icon={<Mail size={20}/>} label="Help" />
        </div>
      </aside>

      {/* --- Main Section --- */}
      <main className="flex-1 pl-64 overflow-y-auto p-12 min-h-screen">
        <AnimatePresence mode="wait">
          {mode === "jobs" ? (
            <motion.section 
              key="jobs"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
            >
              <div className="flex justify-between items-center mb-12">
                <div>
                  <h2 className="text-3xl font-black mb-2 flex items-center gap-2">
                    Openings <Zap className="text-indigo-400 fill-indigo-400/20" size={24}/>
                  </h2>
                  <p className="text-slate-400 font-medium">Screen candidates for your active jobs.</p>
                </div>
                <button className="bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-3 px-6 rounded-xl flex items-center gap-2 transition-all">
                  <Plus size={20}/> NEW JOB
                </button>
              </div>

              <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                {job_data.map((j) => (
                  <GlassCard key={j.id} className="group relative border-indigo-500/5 hover:border-indigo-500/30">
                    <h3 className="text-xl font-bold mb-2 group-hover:text-indigo-400 transition-colors uppercase tracking-tight">{j.title}</h3>
                    <p className="text-slate-400 text-sm mb-6 line-clamp-2 h-10">{j.description}</p>
                    <button 
                      onClick={() => { set_cur_job(j); load_apps(j.id); }}
                      className="w-full bg-white/5 hover:bg-indigo-500/10 border border-white/5 hover:border-indigo-500/30 rounded-xl py-3 font-black text-xs tracking-widest flex items-center justify-center gap-2 transition-all"
                    >
                      SEE CANDIDATES <ChevronRight size={16}/>
                    </button>
                  </GlassCard>
                ))}
              </div>
            </motion.section>
          ) : (
            <motion.section 
              key="applicants"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
            >
              <div className="flex items-center gap-4 mb-8">
                <button onClick={() => set_mode("jobs")} className="p-3 bg-white/5 rounded-xl hover:bg-white/10 text-indigo-400">
                  <BackIcon size={20}/>
                </button>
                <div>
                  <h2 className="text-3xl font-black">{cur_job?.title}</h2>
                  <p className="text-slate-400 font-bold uppercase tracking-widest text-xs">Candidates Feed</p>
                </div>
              </div>

              <div className="flex gap-6 overflow-x-auto pb-8 h-[calc(100vh-250px)]">
                {PIPE_STAGES.map((s) => (
                  <div 
                    key={s} 
                    className="flex-shrink-0 w-[400px] bg-white/5 border border-white/5 rounded-2xl flex flex-col"
                    onDragOver={over_it}
                    onDrop={(e) => drop_it(e, s)}
                  >
                    <div className="p-4 border-b border-white/10 flex justify-between items-center bg-black/20 rounded-t-2xl">
                      <h4 className="font-bold text-slate-300 tracking-wider text-sm">{s}</h4>
                      <span className="text-xs bg-white/10 px-2 py-0.5 rounded-full font-black text-slate-400">
                        {app_list.filter(a => (a.status || 'APPLIED').toUpperCase() === s).length}
                      </span>
                    </div>

                    <div className="flex-1 p-4 space-y-4 overflow-y-auto">
                      {app_list
                        .filter(a => (a.status || 'APPLIED').toUpperCase() === s)
                        .map(app => (
                        <div 
                          key={app.app_id} 
                          draggable 
                          onDragStart={(e) => start_drag(e, app.app_id)}
                          className="bg-[#0D0221] border border-indigo-500/10 p-5 rounded-xl cursor-grab active:cursor-grabbing hover:border-indigo-500/40 transition-colors relative"
                        >
                          <div className="absolute top-4 right-4">
                            <MatchBadge score={app.sc} />
                          </div>
                          
                          <h5 className="font-black text-lg mb-1">{app.full_name || app.username}</h5>
                          {app.tech_sc > 70 && <span className="bg-yellow-500/10 text-yellow-500 text-[10px] font-black px-2 py-0.5 rounded-md border border-yellow-500/20 mb-2 inline-block">PRO SCORE</span>}
                          
                          <div className="flex flex-wrap gap-1 mt-2 mb-3">
                            {app.hits.slice(0, 3).map(skill => (
                              <span key={skill} className="text-[9px] font-bold px-2 py-0.5 bg-indigo-500/10 text-indigo-300 rounded uppercase">{skill}</span>
                            ))}
                          </div>

                          <div className="flex flex-wrap gap-1 mb-4 border-t border-white/5 pt-3">
                            {VIBES.map(v => {
                              const active = app.vibes?.split(",").includes(v);
                              return (
                                <button 
                                  key={v}
                                  onClick={() => toggle_vibe(app.app_id, v)}
                                  className={`text-[8px] font-black px-2 py-1 rounded-md transition-all ${active ? 'bg-indigo-500 text-white' : 'bg-white/5 text-slate-500 hover:text-slate-300'}`}
                                >
                                  {v.toUpperCase()}
                                </button>
                              );
                            })}
                          </div>

                          <div className="flex gap-2 mt-4 pt-4 border-t border-white/5">
                             <button
                               onClick={() => window.open(`http://localhost:8080/api/resume/get_pdf/${app.id}`)}
                               className="flex-1 py-2 bg-indigo-600/20 hover:bg-indigo-600/40 text-indigo-300 rounded text-[10px] font-black tracking-widest transition-colors"
                             >
                               PDF
                             </button>
                             <button 
                               onClick={() => show_notes(app.id, app.app_id)}
                               className="flex-1 py-2 bg-white/5 hover:bg-white/10 text-slate-300 rounded text-[10px] font-black tracking-widest transition-colors"
                             >
                               {expanded_aid === app.app_id ? "HIDE" : "NOTES"}
                             </button>
                          </div>

                          <AnimatePresence>
                            {expanded_aid === app.app_id && (
                              <motion.div 
                                initial={{ height: 0, opacity: 0 }}
                                animate={{ height: 'auto', opacity: 1 }}
                                exit={{ height: 0, opacity: 0 }}
                                className="mt-4 pt-4 border-t border-white/10 overflow-hidden"
                              >
                                <div className="space-y-2 mb-4 max-h-32 overflow-y-auto pr-1">
                                  {user_notes.length === 0 ? (
                                    <p className="text-xs text-slate-500 italic">Fresh slate.</p>
                                  ) : (
                                    user_notes.map(n => (
                                      <div key={n.id} className="bg-white/5 p-2 rounded border border-white/5">
                                        <p className="text-xs text-slate-300">{n.note}</p>
                                        <p className="text-[10px] text-slate-500 mt-1">{new Date(n.created_at).toLocaleDateString()}</p>
                                      </div>
                                    ))
                                  )}
                                </div>
                                <div className="flex gap-2">
                                  <input 
                                    className="flex-1 bg-black/30 border border-white/10 rounded px-2 py-1 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500"
                                    placeholder="Type here..."
                                    value={msg_text}
                                    onChange={(e) => set_msg_text(e.target.value)}
                                  />
                                  <button onClick={() => add_note(app.id)} className="bg-indigo-600 px-3 rounded font-black text-[10px]">ADD</button>
                                </div>
                              </motion.div>
                            )}
                          </AnimatePresence>
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </motion.section>
          )}
        </AnimatePresence>
      </main>
    </div>
  );
}

function SidebarItem({ icon, label, active = false, onClick = () => {} }: { icon: any, label: string, active?: boolean, onClick?: () => void }) {
  return (
    <div onClick={onClick} className={`
      group flex items-center gap-4 px-4 py-3 rounded-xl cursor-pointer transition-all duration-300
      ${active ? 'bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 shadow-[0_0_15px_rgba(79,70,229,0.1)]' : 'text-slate-400 hover:text-white hover:bg-white/5'}
    `}>
      <span className={active ? 'text-indigo-400' : 'group-hover:scale-110 transition-transform'}>{icon}</span>
      <span className="font-bold text-sm tracking-tight">{label}</span>
    </div>
  );
}

function BackIcon({ size }: { size: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 12H5M11 18l-6-6 6-6"/>
    </svg>
  );
}
