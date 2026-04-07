"use client";
import { useEffect, useState } from "react";
import axios from "axios";
import { motion, AnimatePresence } from "framer-motion";
import { 
  ShieldCheck, 
  Users, 
  Briefcase, 
  Plus, 
  Search, 
  ChevronRight, 
  TrendingUp,
  Mail,
  Zap,
  CheckCircle2,
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

interface Applicant {
  id: number;
  username: string;
  full_name: string;
  score: number;
  matchedSkills: string[];
  missingSkills: string[];
  techScore: number;
  application_id: number;
  status: string;
}

export default function AdminDashboard() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [selectedJob, setSelectedJob] = useState<Job | null>(null);
  const [applicants, setApplicants] = useState<Applicant[]>([]);
  const [loading, setLoading] = useState(true);
  const [view, setView] = useState<"jobs" | "applicants">("jobs");
  const [activeNoteApp, setActiveNoteApp] = useState<number | null>(null);
  const [noteText, setNoteText] = useState("");
  const [candidateNotes, setCandidateNotes] = useState<any[]>([]);

  const KANBAN_STAGES = ["APPLIED", "SCREENING", "INTERVIEWING", "OFFERED", "REJECTED"];

  useEffect(() => {
    fetchJobs();
  }, []);

  const fetchJobs = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/jobs");
      setJobs(res.data);
      setLoading(false);
    } catch (e) {}
  };

  const fetchApplicants = async (jobId: number) => {
    try {
      const res = await axios.get(`http://localhost:8080/api/jobs/${jobId}/applicants`);
      setApplicants(res.data);
      setView("applicants");
    } catch (e) {}
  };

  const selectCandidate = async (appId: number) => {
    try {
      await axios.post(`http://localhost:8080/api/applicants/${appId}/select`);
      alert("Candidate Shortlisted! Notification sent.");
      if (selectedJob) fetchApplicants(selectedJob.id);
    } catch (e) {}
  };

  const handleDragStart = (e: React.DragEvent, appId: number) => {
    e.dataTransfer.setData("appId", appId.toString());
  };

  const handleDrop = async (e: React.DragEvent, newStatus: string) => {
    e.preventDefault();
    const appIdStr = e.dataTransfer.getData("appId");
    if (!appIdStr) return;
    const appId = parseInt(appIdStr);
    
    setApplicants(prev => prev.map(app => app.application_id === appId ? { ...app, status: newStatus } : app));
    
    try {
      await axios.put(`http://localhost:8080/api/applications/${appId}/status`, { status: newStatus });
    } catch (err) {}
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const viewNotes = async (userId: number, appId: number) => {
    if (activeNoteApp === appId) {
      setActiveNoteApp(null);
      return;
    }
    setActiveNoteApp(appId);
    try {
      const res = await axios.get(`http://localhost:8080/api/users/${userId}/notes`);
      setCandidateNotes(res.data);
    } catch (e) {}
  };

  const submitNote = async (userId: number) => {
    if (!noteText.trim()) return;
    try {
      await axios.post(`http://localhost:8080/api/users/${userId}/notes`, { note: noteText });
      setNoteText("");
      // Refresh notes
      const res = await axios.get(`http://localhost:8080/api/users/${userId}/notes`);
      setCandidateNotes(res.data);
    } catch (e) {}
  };


  return (
    <div className="flex min-h-screen bg-[#060012] text-white">
      {/* --- Sidebar --- */}
      <aside className="w-64 border-r border-indigo-500/10 bg-[#060012]/80 backdrop-blur-3xl p-6 flex flex-col fixed h-full z-20">
        <div className="flex items-center gap-3 mb-10 px-2 cursor-pointer" onClick={() => { setView("jobs"); setSelectedJob(null); }}>
          <ShieldCheck className="text-indigo-400 w-8 h-8 fill-indigo-400/20" />
          <h1 className="text-2xl font-black tracking-tighter">RESUDEX <span className="text-indigo-400">ADM</span></h1>
        </div>

        <nav className="flex-1 space-y-2">
          <SidebarItem icon={<Briefcase size={20}/>} label="Job Listings" active={view === "jobs"} onClick={() => setView("jobs")} />
          <SidebarItem icon={<Users size={20}/>} label="Talent Pool" />
          <SidebarItem icon={<TrendingUp size={20}/>} label="Analytics" />
        </nav>

        <div className="pt-6 border-t border-white/5">
          <SidebarItem icon={<Mail size={20}/>} label="Recruiter Support" />
        </div>
      </aside>

      {/* --- Main Content --- */}
      <main className="flex-1 pl-64 overflow-y-auto p-12 min-h-screen">
        <AnimatePresence mode="wait">
          {view === "jobs" ? (
            <motion.section 
              key="jobs"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
            >
              <div className="flex justify-between items-center mb-12">
                <div>
                  <h2 className="text-3xl font-black mb-2 flex items-center gap-2">
                    Active Listings <Zap className="text-indigo-400 fill-indigo-400/20" size={24}/>
                  </h2>
                  <p className="text-slate-400 font-medium">Manage job postings and screen top-tier talent.</p>
                </div>
                <button className="bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-3 px-6 rounded-xl flex items-center gap-2 transition-all shadow-[0_0_20px_rgba(79,70,229,0.2)]">
                  <Plus size={20}/> CREATE JOB
                </button>
              </div>

              <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                {jobs.map((job, idx) => (
                  <GlassCard key={job.id} className="group relative border-indigo-500/5 hover:border-indigo-500/30">
                    <h3 className="text-xl font-bold mb-2 group-hover:text-indigo-400 transition-colors uppercase tracking-tight">{job.title}</h3>
                    <p className="text-slate-400 text-sm mb-6 line-clamp-2 h-10">{job.description}</p>
                    <button 
                      onClick={() => { setSelectedJob(job); fetchApplicants(job.id); }}
                      className="w-full bg-white/5 hover:bg-indigo-500/10 border border-white/5 hover:border-indigo-500/30 rounded-xl py-3 font-black text-xs tracking-widest flex items-center justify-center gap-2 transition-all"
                    >
                      RANK APPLICANTS <ChevronRight size={16}/>
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
                <button onClick={() => setView("jobs")} className="p-3 bg-white/5 rounded-xl hover:bg-white/10 text-indigo-400">
                  <ArrowBackIcon size={20}/>
                </button>
                <div>
                  <h2 className="text-3xl font-black">{selectedJob?.title}</h2>
                  <p className="text-slate-400 font-bold uppercase tracking-widest text-xs">AI Ranked Talent Pool</p>
                </div>
              </div>

              <div className="flex gap-6 overflow-x-auto pb-8 h-[calc(100vh-250px)]">
                {KANBAN_STAGES.map((stage) => (
                  <div 
                    key={stage} 
                    className="flex-shrink-0 w-[400px] bg-white/5 border border-white/5 rounded-2xl flex flex-col"
                    onDragOver={handleDragOver}
                    onDrop={(e) => handleDrop(e, stage)}
                  >
                    <div className="p-4 border-b border-white/10 flex justify-between items-center bg-black/20 rounded-t-2xl">
                      <h4 className="font-bold text-slate-300 tracking-wider text-sm">{stage}</h4>
                      <span className="text-xs bg-white/10 px-2 py-0.5 rounded-full font-black text-slate-400">
                        {applicants.filter(a => (a.status || 'APPLIED').toUpperCase() === stage).length}
                      </span>
                    </div>

                    <div className="flex-1 p-4 space-y-4 overflow-y-auto">
                      {applicants
                        .filter(a => (a.status || 'APPLIED').toUpperCase() === stage)
                        .map(app => (
                        <div 
                          key={app.id} 
                          draggable 
                          onDragStart={(e) => handleDragStart(e, app.application_id)}
                          className="bg-[#0D0221] border border-indigo-500/10 p-5 rounded-xl cursor-grab active:cursor-grabbing hover:border-indigo-500/40 transition-colors relative"
                        >
                          <div className="absolute top-4 right-4">
                            <MatchBadge score={app.score} />
                          </div>
                          
                          <h5 className="font-black text-lg mb-1">{app.full_name || app.username}</h5>
                          {app.techScore > 70 && <span className="bg-yellow-500/10 text-yellow-500 text-[10px] font-black px-2 py-0.5 rounded-md border border-yellow-500/20 mb-2 inline-block">TOP TECH SCORE</span>}
                          
                          <div className="flex flex-wrap gap-1 mt-2 mb-4">
                            {app.matchedSkills.slice(0, 3).map(skill => (
                              <span key={skill} className="text-[9px] font-bold px-2 py-0.5 bg-indigo-500/10 text-indigo-300 rounded uppercase">{skill}</span>
                            ))}
                          </div>

                          <div className="flex gap-2 mt-4 pt-4 border-t border-white/5">
                             <button
                               onClick={() => window.open(`http://localhost:8080/api/resume/export/${app.id}`)}
                               className="flex-1 py-2 bg-indigo-600/20 hover:bg-indigo-600/40 text-indigo-300 rounded text-[10px] font-black tracking-widest transition-colors"
                             >
                               PDF EXPORT
                             </button>
                             <button 
                               onClick={() => viewNotes(app.id, app.application_id)}
                               className="flex-1 py-2 bg-white/5 hover:bg-white/10 text-slate-300 rounded text-[10px] font-black tracking-widest transition-colors"
                             >
                               {activeNoteApp === app.application_id ? "HIDE NOTES" : "NOTES"}
                             </button>
                          </div>

                          {/* Recruiter Notes Inline Expand */}
                          <AnimatePresence>
                            {activeNoteApp === app.application_id && (
                              <motion.div 
                                initial={{ height: 0, opacity: 0 }}
                                animate={{ height: 'auto', opacity: 1 }}
                                exit={{ height: 0, opacity: 0 }}
                                className="mt-4 pt-4 border-t border-white/10 overflow-hidden"
                              >
                                <div className="space-y-2 mb-4 max-h-32 overflow-y-auto pr-1">
                                  {candidateNotes.length === 0 ? (
                                    <p className="text-xs text-slate-500 italic">No notes yet.</p>
                                  ) : (
                                    candidateNotes.map(n => (
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
                                    placeholder="Add a private note..."
                                    value={noteText}
                                    onChange={(e) => setNoteText(e.target.value)}
                                  />
                                  <button onClick={() => submitNote(app.id)} className="bg-indigo-600 px-3 rounded font-black text-[10px]">ADD</button>
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

function ArrowBackIcon({ size }: { size: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 12H5M11 18l-6-6 6-6"/>
    </svg>
  );
}
