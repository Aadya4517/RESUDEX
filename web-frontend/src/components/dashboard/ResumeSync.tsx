"use client";
import { useState, useRef } from "react";
import axios from "axios";
import { motion, AnimatePresence } from "framer-motion";
import { UploadCloud, FileText, CheckCircle2, AlertCircle, X, Zap } from "lucide-react";
import { GlassCard } from "@/components/ui/GlassCard";

interface ResumeSyncProps {
  uid: number;
  on_ok: () => void;
}

/**
 * Component for uploading CV.
 * Scans the doc and updates the DB.
 */
export const ResumeSync = ({ uid, on_ok }: ResumeSyncProps) => {
  const [dragging, set_dragging] = useState(false);
  const [doc, set_doc] = useState<File | null>(null);
  const [is_busy, set_is_busy] = useState(false);
  const [load_ptr, set_load_ptr] = useState(0);
  const [rep_state, set_rep_state] = useState<"idle" | "ok" | "err">("idle");
  const input_ref = useRef<HTMLInputElement>(null);

  const drag_logic = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") set_dragging(true);
    else if (e.type === "dragleave") set_dragging(false);
  };

  const drop_logic = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    set_dragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      set_doc(e.dataTransfer.files[0]);
    }
  };

  const send_cv = async () => {
    if (!doc) return;
    set_is_busy(true);
    set_rep_state("idle");
    set_load_ptr(0);

    const data = new FormData();
    data.append("uid", uid.toString());
    data.append("file", doc);

    try {
      // fake progress for aesthetic
      const loop = setInterval(() => {
        set_load_ptr(p => (p < 90 ? p + 10 : p));
      }, 200);

      await axios.post("http://localhost:8080/api/resume/push_cv", data, {
        headers: { "Content-Type": "multipart/form-data" }
      });

      clearInterval(loop);
      set_load_ptr(100);
      set_rep_state("ok");
      setTimeout(() => {
        on_ok();
        set_doc(null);
        set_rep_state("idle");
        set_is_busy(false);
      }, 1500);
    } catch (err) {
      console.error("Upload Fail:", err);
      set_rep_state("err");
      set_is_busy(false);
    }
  };

  return (
    <GlassCard className="relative overflow-hidden border-dashed border-2 border-white/10 hover:border-[#00F0FF]/30 transition-colors">
      <div
        onDragEnter={drag_logic}
        onDragLeave={drag_logic}
        onDragOver={drag_logic}
        onDrop={drop_logic}
        className={`flex flex-col items-center justify-center p-12 text-center ${dragging ? 'bg-[#00F0FF]/5' : ''}`}
      >
        <input 
          ref={input_ref} 
          type="file" 
          className="hidden" 
          onChange={(e) => e.target.files && set_doc(e.target.files[0])}
          accept=".pdf,.docx"
        />

        <AnimatePresence mode="wait">
          {!doc && !is_busy && (
            <motion.div 
              key="idle"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="space-y-4"
            >
              <div className="bg-white/5 p-6 rounded-3xl inline-block float-animation">
                <UploadCloud className="text-[#00F0FF]" size={48} />
              </div>
              <div>
                <h4 className="text-xl font-black mb-1 leading-tight">Sync CV</h4>
                <p className="text-slate-400 text-sm font-medium">Drop PDF/DOCX here</p>
              </div>
              <button 
                onClick={() => input_ref.current?.click()}
                className="neon-button text-xs px-8 py-3"
              >
                SELECT FILE
              </button>
            </motion.div>
          )}

          {doc && !is_busy && (
            <motion.div 
              key="preview"
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              className="space-y-6 w-full"
            >
              <div className="flex items-center gap-4 bg-white/5 p-4 rounded-2xl border border-white/10">
                <div className="bg-[#00F0FF]/10 p-3 rounded-xl">
                  <FileText className="text-[#00F0FF]" size={24} />
                </div>
                <div className="text-left flex-1 min-w-0">
                  <p className="font-bold text-sm truncate">{doc.name}</p>
                  <p className="text-[10px] font-black uppercase text-slate-500">Atomic Analysis Ready</p>
                </div>
                <button onClick={() => set_doc(null)} className="text-slate-500 hover:text-white">
                  <X size={20}/>
                </button>
              </div>

              <div className="flex gap-4">
                <button onClick={send_cv} className="flex-1 neon-button">SYNC NOW</button>
                <button onClick={() => set_doc(null)} className="flex-1 px-6 py-3 border border-white/10 rounded-xl font-bold text-xs uppercase hover:bg-white/5">CANCEL</button>
              </div>
            </motion.div>
          )}

          {is_busy && (
            <motion.div 
              key="uploading"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="space-y-6 w-full"
            >
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-black uppercase tracking-widest text-[#00F0FF]">Syncing...</span>
                <span className="text-xs font-black text-[#00F0FF]">{load_ptr}%</span>
              </div>
              <div className="h-2 bg-white/10 rounded-full overflow-hidden">
                <motion.div 
                  initial={{ width: 0 }}
                  animate={{ width: `${load_ptr}%` }}
                  className="h-full bg-gradient-to-r from-cyan-500 via-blue-500 to-pink-500"
                />
              </div>
              <div className="flex items-center gap-2 justify-center text-slate-400">
                <Zap size={14} className="animate-pulse text-yellow-400"/>
                <p className="text-[10px] font-bold uppercase tracking-tighter">Running human-like analysis...</p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {rep_state === "ok" && (
          <motion.div 
            initial={{ opacity: 0, scale: 0.5 }}
            animate={{ opacity: 1, scale: 1 }}
            className="absolute inset-0 bg-[#0D0221] flex flex-col items-center justify-center z-20"
          >
            <CheckCircle2 className="text-[#00F0FF] mb-4" size={64} />
            <h4 className="text-2xl font-black text-[#00F0FF]">Success</h4>
            <p className="text-slate-400 font-bold uppercase text-[10px] tracking-widest mt-2">Saved to DB</p>
          </motion.div>
        )}

        {rep_state === "err" && (
          <motion.div 
            initial={{ opacity: 0, scale: 0.5 }}
            animate={{ opacity: 1, scale: 1 }}
            className="absolute inset-0 bg-[#0D0221] flex flex-col items-center justify-center z-20"
          >
            <AlertCircle className="text-red-400 mb-4" size={64} />
            <h4 className="text-2xl font-black text-red-400">Fail</h4>
            <button onClick={() => set_rep_state("idle")} className="mt-4 text-[#00F0FF] text-xs font-bold uppercase border-b border-[#00F0FF]">Retry</button>
          </motion.div>
        )}
      </div>
    </GlassCard>
  );
};
