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
  AlertCircle,
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
  score: number;
  matchedSkills: string[];
}

interface Analytics {
  domainFit: Record<string, number>;
  yearsOfExperience: number;
  topDomains: string[];
}

interface Notification {
  id: number;
  message: string;
}

export default function Dashboard() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [analytics, setAnalytics] = useState<Analytics | null>(null);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [showSync, setShowSync] = useState(false);
  const [loading, setLoading] = useState(true);
  const [userId] = useState(2); // Mocked for now, normally from Auth context

  useEffect(() => {
    fetchData();
    // Start notification polling every 10 seconds
    const interval = setInterval(fetchNotifications, 10000);
    return () => clearInterval(interval);
  }, [userId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [jobsRes, analyticsRes] = await Promise.all([
        axios.get(`http://localhost:8080/api/jobs/matched/${userId}`),
        axios.get(`http://localhost:8080/api/users/${userId}/resume/analytics`)
      ]);
      setJobs(jobsRes.data);
      setAnalytics(analyticsRes.data);
    } catch (error) {
      console.error("Dashboard Fetch Error:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchNotifications = async () => {
    try {
      const res = await axios.get(`http://localhost:8080/api/notifications/unread/${userId}`);
      if (res.data.length > notifications.length) {
        setNotifications(res.data);
      }
    } catch (e) {}
  };

  const markRead = async (id: number) => {
    try {
      await axios.post(`http://localhost:8080/api/notifications/read/${id}`);
      setNotifications(notifications.filter(n => n.id !== id));
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
          <SidebarItem icon={<LogOut size={20}/>} label="Logout" danger />
        </div>
      </aside>

      {/* --- Main Content --- */}
      <main className="flex-1 overflow-y-auto p-8 lg:p-12">
        <header className="flex justify-between items-center mb-12">
          <div>
            <h2 className="text-3xl font-black mb-2 flex items-center gap-2">
              Welcome Back, Developer <Zap className="text-yellow-400 fill-yellow-400/20" size={24}/>
            </h2>
            <p className="text-slate-400 font-medium">Your profile is 85% optimized for top-tier roles.</p>
          </div>
          <div className="flex gap-4">
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => window.open(`http://localhost:8080/api/resume/export/${userId}`)}
              className="px-4 py-3 border border-white/10 rounded-xl font-bold uppercase tracking-wider text-xs flex items-center gap-2 hover:bg-white/5 transition-all"
            >
              <Download size={18}/> Get ATS PDF
            </motion.button>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => setShowSync(true)}
              className="neon-button flex items-center gap-2"
            >
              <UploadCloud size={18}/> SYNC PROFILE
            </motion.button>
          </div>
        </header>

        {/* --- Notification Toast Container --- */}
        <div className="fixed top-8 right-8 z-50 space-y-4">
          <AnimatePresence>
            {notifications.slice(0, 3).map((n) => (
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
                  <button onClick={() => markRead(n.id)} className="text-[10px] font-black uppercase text-[#00F0FF] mt-2 hover:underline">Dismiss</button>
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>

        {/* --- Resume Sync Modal --- */}
        <AnimatePresence>
          {showSync && (
            <div className="fixed inset-0 z-[60] flex items-center justify-center p-6">
              <motion.div 
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                onClick={() => setShowSync(false)}
                className="absolute inset-0 bg-black/60 backdrop-blur-sm"
              />
              <motion.div
                initial={{ scale: 0.9, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.9, opacity: 0 }}
                className="w-full max-w-lg relative"
              >
                <ResumeSync userId={userId} onSuccess={() => { fetchData(); setShowSync(false); }} />
                <button onClick={() => setShowSync(false)} className="absolute top-6 right-6 text-slate-500 hover:text-white">
                  <X className="w-6 h-6" />
                </button>
              </motion.div>
            </div>
          )}
        </AnimatePresence>

        {/* --- Analytics Summary --- */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          <GlassCard className="cyber-gradient border-none">
            <h4 className="text-sm font-bold text-slate-400 uppercase tracking-widest mb-4">Top Expertise</h4>
            <div className="space-y-4">
              {analytics?.topDomains.slice(0, 3).map((domain) => (
                <div key={domain}>
                  <div className="flex justify-between text-xs font-bold mb-1 p-1">
                    <span>{domain}</span>
                    <span className="text-[#00F0FF]">{analytics.domainFit[domain]}%</span>
                  </div>
                  <div className="h-1.5 bg-white/5 rounded-full overflow-hidden">
                    <motion.div 
                      initial={{ width: 0 }}
                      animate={{ width: `${analytics.domainFit[domain]}%` }}
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
            <h3 className="text-4xl font-black text-[#00F0FF]">{analytics?.yearsOfExperience || 0}+</h3>
            <p className="text-slate-400 font-bold uppercase text-xs tracking-widest mt-1">Years Experience</p>
          </GlassCard>

          <GlassCard className="flex flex-col justify-center items-center text-center">
             <div className="bg-pink-500/10 p-4 rounded-full mb-4">
               <CheckCircle2 className="text-[#F72585]" size={32} />
             </div>
             <h3 className="text-4xl font-black text-[#F72585]">{jobs.length}</h3>
             <p className="text-slate-400 font-bold uppercase text-xs tracking-widest mt-1">Live Opportunities</p>
          </GlassCard>
        </div>

        {/* --- Jobs Grid --- */}
        <section>
          <div className="flex items-center justify-between mb-8">
            <h3 className="text-xl font-black flex items-center gap-2">
              <Zap className="text-[#00F0FF] fill-[#00F0FF]/10" size={20}/> RECOMMENDATIONS
            </h3>
            <div className="flex gap-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" size={16}/>
                <input 
                  type="text" 
                  placeholder="Filter skills..." 
                  className="bg-white/5 border border-white/10 rounded-xl py-2 pl-10 pr-4 text-sm focus:outline-none focus:border-[#00F0FF]/50 transition-all"
                />
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
            <AnimatePresence>
              {jobs.map((job, idx) => (
                <motion.div
                  key={job.id}
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: idx * 0.1 }}
                >
                  <GlassCard className="h-full flex flex-col group relative overflow-hidden">
                    <div className="absolute top-0 right-0 p-6">
                      <MatchBadge score={job.score} />
                    </div>
                    
                    <h4 className="text-xl font-black pr-24 mb-2 group-hover:text-[#00F0FF] transition-colors">
                      {job.title}
                    </h4>
                    <p className="text-slate-400 text-sm leading-relaxed mb-6 line-clamp-2">
                      {job.description}
                    </p>

                    <div className="flex flex-wrap gap-2 mb-8">
                      {job.matchedSkills?.slice(0, 5).map(skill => (
                        <span key={skill} className="bg-white/5 border border-white/10 px-3 py-1 rounded-lg text-[10px] font-bold text-slate-300 uppercase tracking-wider">
                          # {skill}
                        </span>
                      ))}
                    </div>

                    <div className="mt-auto flex gap-4">
                      <button className="flex-1 neon-button">QUICK APPLY</button>
                      <button className="px-6 py-3 border border-white/10 rounded-xl font-bold text-xs uppercase hover:bg-white/5 transition-all">
                        COVER LETTER
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

function SidebarItem({ icon, label, active = false, danger = false }: { icon: any, label: string, active?: boolean, danger?: boolean }) {
  return (
    <div className={`
      group flex items-center gap-4 px-4 py-3 rounded-xl cursor-pointer transition-all duration-300
      ${active ? 'bg-[#00F0FF]/10 text-[#00F0FF] border border-[#00F0FF]/20 shadow-[0_0_15px_rgba(0,240,255,0.1)]' : 'text-slate-400 hover:text-white hover:bg-white/5'}
      ${danger ? 'hover:text-red-400 hover:bg-red-400/10' : ''}
    `}>
      <span className={active ? 'text-[#00F0FF]' : 'group-hover:scale-110 transition-transform'}>{icon}</span>
      <span className="font-bold text-sm tracking-tight">{label}</span>
    </div>
  );
}
