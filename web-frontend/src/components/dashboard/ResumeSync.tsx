"use client";
import { useState, useRef } from "react";
import axios from "axios";
import { motion, AnimatePresence } from "framer-motion";
import { UploadCloud, FileText, CheckCircle2, AlertCircle, X, Zap } from "lucide-react";
import { GlassCard } from "@/components/ui/GlassCard";

interface ResumeSyncProps {
  userId: number;
  onSuccess: () => void;
}

export const ResumeSync = ({ userId, onSuccess }: ResumeSyncProps) => {
  const [dragActive, setDragActive] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [status, setStatus] = useState<"idle" | "success" | "error">("idle");
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") setDragActive(true);
    else if (e.type === "dragleave") setDragActive(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setFile(e.dataTransfer.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    setStatus("idle");
    setProgress(0);

    const formData = new FormData();
    formData.append("userId", userId.toString());
    formData.append("file", file);

    try {
      // Simulate progress for smooth UI UX
      const interval = setInterval(() => {
        setProgress(p => (p < 90 ? p + 10 : p));
      }, 200);

      await axios.post("http://localhost:8080/api/resume/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" }
      });

      clearInterval(interval);
      setProgress(100);
      setStatus("success");
      setTimeout(() => {
        onSuccess();
        setFile(null);
        setStatus("idle");
        setUploading(false);
      }, 1500);
    } catch (error) {
      console.error("Upload Error:", error);
      setStatus("error");
      setUploading(false);
    }
  };

  return (
    <GlassCard className="relative overflow-hidden border-dashed border-2 border-white/10 hover:border-[#00F0FF]/30 transition-colors">
      <div
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
        className={`flex flex-col items-center justify-center p-12 text-center ${dragActive ? 'bg-[#00F0FF]/5' : ''}`}
      >
        <input 
          ref={fileInputRef} 
          type="file" 
          className="hidden" 
          onChange={(e) => e.target.files && setFile(e.target.files[0])}
          accept=".pdf,.docx"
        />

        <AnimatePresence mode="wait">
          {!file && !uploading && (
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
                <h4 className="text-xl font-black mb-1 leading-tight">Sync Your Expert Profile</h4>
                <p className="text-slate-400 text-sm font-medium">Drag and drop your latest resume (PDF/DOCX)</p>
              </div>
              <button 
                onClick={() => fileInputRef.current?.click()}
                className="neon-button text-xs px-8 py-3"
              >
                BROWSE FILES
              </button>
            </motion.div>
          )}

          {file && !uploading && (
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
                  <p className="font-bold text-sm truncate">{file.name}</p>
                  <p className="text-[10px] font-black uppercase text-slate-500">Ready for AI Analysis</p>
                </div>
                <button onClick={() => setFile(null)} className="text-slate-500 hover:text-white">
                  <X size={20}/>
                </button>
              </div>

              <div className="flex gap-4">
                <button onClick={handleUpload} className="flex-1 neon-button">BEGIN SYNC</button>
                <button onClick={() => setFile(null)} className="flex-1 px-6 py-3 border border-white/10 rounded-xl font-bold text-xs uppercase hover:bg-white/5">CANCEL</button>
              </div>
            </motion.div>
          )}

          {uploading && (
            <motion.div 
              key="uploading"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="space-y-6 w-full"
            >
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-black uppercase tracking-widest text-[#00F0FF]">Analyzing Content...</span>
                <span className="text-xs font-black text-[#00F0FF]">{progress}%</span>
              </div>
              <div className="h-2 bg-white/10 rounded-full overflow-hidden">
                <motion.div 
                  initial={{ width: 0 }}
                  animate={{ width: `${progress}%` }}
                  className="h-full bg-gradient-to-r from-cyan-500 via-blue-500 to-pink-500"
                />
              </div>
              <div className="flex items-center gap-2 justify-center text-slate-400">
                <Zap size={14} className="animate-pulse text-yellow-400"/>
                <p className="text-[10px] font-bold uppercase tracking-tighter">AI matching engines are synchronizing...</p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {status === "success" && (
          <motion.div 
            initial={{ opacity: 0, scale: 0.5 }}
            animate={{ opacity: 1, scale: 1 }}
            className="absolute inset-0 bg-[#0D0221] flex flex-col items-center justify-center z-20"
          >
            <CheckCircle2 className="text-[#00F0FF] mb-4" size={64} />
            <h4 className="text-2xl font-black text-[#00F0FF]">Sync Successful!</h4>
            <p className="text-slate-400 font-bold uppercase text-[10px] tracking-widest mt-2">Dashboard Updating...</p>
          </motion.div>
        )}

        {status === "error" && (
          <motion.div 
            initial={{ opacity: 0, scale: 0.5 }}
            animate={{ opacity: 1, scale: 1 }}
            className="absolute inset-0 bg-[#0D0221] flex flex-col items-center justify-center z-20"
          >
            <AlertCircle className="text-red-400 mb-4" size={64} />
            <h4 className="text-2xl font-black text-red-400">Sync Failed</h4>
            <button onClick={() => setStatus("idle")} className="mt-4 text-[#00F0FF] text-xs font-bold uppercase border-b border-[#00F0FF]">Try Again</button>
          </motion.div>
        )}
      </div>
    </GlassCard>
  );
};
