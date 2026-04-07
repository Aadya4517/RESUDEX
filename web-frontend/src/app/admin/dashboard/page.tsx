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
}

export default function AdminDashboard() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [selectedJob, setSelectedJob] = useState<Job | null>(null);
  const [applicants, setApplicants] = useState<Applicant[]>([]);
  const [loading, setLoading] = useState(true);
  const [view, setView] = useState<"jobs" | "applicants">("jobs");

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
      // Refresh list
      if (selectedJob) fetchApplicants(selectedJob.id);
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

              <div className="space-y-4">
                {applicants.map((app, idx) => (
                  <GlassCard key={app.id} className="flex items-center gap-8 relative p-8 border-indigo-500/5 hover:border-indigo-500/30">
                    <div className="absolute top-0 right-0 p-6">
                      <MatchBadge score={app.score} />
                    </div>

                    <div className="w-16 h-16 bg-gradient-to-br from-indigo-600 to-purple-600 rounded-2xl flex items-center justify-center text-2xl font-black border border-white/20">
                      {app.full_name?.charAt(0) || app.username.charAt(0)}
                    </div>

                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-1">
                        <h4 className="text-xl font-black tracking-tight">{app.full_name || app.username}</h4>
                        {app.techScore > 70 && <span className="bg-yellow-500/10 text-yellow-500 text-[10px] font-black px-2 py-0.5 rounded-md border border-yellow-500/20 shadow-[0_0_10px_rgba(234,179,8,0.2)]">TOP SCORE</span>}
                      </div>
                      <div className="flex flex-wrap gap-2 mt-3">
                        {app.matchedSkills.slice(0, 4).map(skill => (
                          <span key={skill} className="text-[10px] font-black px-2 py-1 bg-emerald-500/10 text-emerald-400 rounded-lg border border-emerald-500/20 uppercase tracking-widest">{skill}</span>
                        ))}
                      </div>
                    </div>

                    <div className="flex gap-4">
                       <button 
                        onClick={() => selectCandidate(app.application_id)}
                        className="bg-indigo-600 hover:bg-indigo-500 px-8 py-3 rounded-xl font-black text-xs tracking-widest transition-all shadow-[0_0_15px_rgba(79,70,229,0.3)]"
                       >
                         SHORTLIST
                       </button>
                       <button className="p-3 border border-indigo-500/20 rounded-xl hover:bg-white/5 text-slate-400">
                         <Mail size={20}/>
                       </button>
                    </div>
                  </GlassCard>
                ))}

                {applicants.length === 0 && (
                  <GlassCard className="flex flex-col items-center justify-center p-20 text-center">
                    <Users className="text-slate-700 mb-4" size={64}/>
                    <h3 className="text-2xl font-black text-slate-600">No Applicants Yet</h3>
                  </GlassCard>
                )}
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
