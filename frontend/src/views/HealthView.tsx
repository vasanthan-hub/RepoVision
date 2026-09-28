import React, { useState, useEffect } from 'react';
import { RepositoryHealth, Repository } from '../types/api';
import { api } from '../services/api';
import { Activity, ShieldCheck, Info } from 'lucide-react';

interface HealthViewProps {
  repository: Repository;
}

export const HealthView: React.FC<HealthViewProps> = ({ repository }) => {
  const [data, setData] = useState<RepositoryHealth | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadHealth();
  }, [repository.id]);

  const loadHealth = async () => {
    setLoading(true);
    try {
      const res = await api.getHealth(repository.id);
      setData(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-20 flex flex-col items-center justify-center space-y-3">
        <div className="h-8 w-8 border-3 border-sky-500/20 border-t-sky-500 rounded-full animate-spin" />
        <p className="text-xs text-slate-400">Computing 5-dimensional repository health signals...</p>
      </div>
    );
  }

  if (!data) return null;

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'EXCELLENT':
        return 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30';
      case 'GOOD':
        return 'bg-sky-500/10 text-sky-300 border-sky-500/30';
      case 'NEEDS_ATTENTION':
        return 'bg-amber-500/10 text-amber-300 border-amber-500/30';
      default:
        return 'bg-rose-500/10 text-rose-300 border-rose-500/30';
    }
  };

  return (
    <div className="space-y-6">
      {/* Health Overview Banner */}
      <div className="bg-[#0f172a] rounded-2xl border border-slate-800 p-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div className="space-y-2 max-w-xl">
          <div className="flex items-center space-x-2">
            <Activity className="h-5 w-5 text-sky-400" />
            <span className="text-xs font-bold uppercase tracking-wider text-sky-400">
              Repository Health Assessment
            </span>
          </div>
          <h2 className="text-xl font-bold text-white">Overall Health Score: {data.overallScore} / 100</h2>
          <p className="text-xs text-slate-300 leading-relaxed font-normal">{data.summary}</p>
        </div>

        <div className="bg-slate-900/90 p-5 rounded-2xl border border-slate-800 flex items-center space-x-5 shrink-0">
          <div className="text-center">
            <div className="text-4xl font-black text-white font-mono">{data.overallScore}</div>
            <div className={`text-[10px] font-bold px-2 py-0.5 rounded border mt-1 ${getStatusBadge(data.status)}`}>
              {data.status}
            </div>
          </div>
        </div>
      </div>

      {/* Rationale Notice */}
      <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 text-xs text-slate-400 flex items-start space-x-2.5">
        <Info className="h-4 w-4 text-sky-400 shrink-0 mt-0.5" />
        <span>
          <strong className="text-slate-200">Defensible Scoring Model: </strong>
          The overall score is a weighted synthesis of 5 core engineering vectors: Activity (20%), Stability (25%), Branch Health (20%), Ownership & Knowledge (20%), and Release Velocity (15%). Every score is supported by underlying empirical repository evidence.
        </span>
      </div>

      {/* 5 Dimensions Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {data.dimensions.map((dim) => (
          <div key={dim.name} className="bg-[#0f172a] rounded-2xl border border-slate-800 p-5 space-y-3.5 flex flex-col justify-between">
            <div className="space-y-2">
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="font-bold text-slate-100 text-sm">{dim.name}</h3>
                  <p className="text-[11px] text-slate-400">Weight: {Math.round(dim.weight * 100)}% of total score</p>
                </div>
                <div className="flex items-center space-x-2">
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded border ${getStatusBadge(dim.status)}`}>
                    {dim.status}
                  </span>
                  <span className="font-mono text-base font-extrabold text-white">{dim.score}</span>
                </div>
              </div>

              <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
                <div
                  className={`h-2 rounded-full ${dim.score >= 80 ? 'bg-emerald-500' : (dim.score >= 60 ? 'bg-sky-500' : 'bg-amber-500')}`}
                  style={{ width: `${dim.score}%` }}
                />
              </div>

              <p className="text-xs text-slate-300">{dim.description}</p>
            </div>

            <div className="pt-3 border-t border-slate-800/80 space-y-1.5">
              <div className="text-[11px] font-bold text-slate-400">Supporting Evidence:</div>
              <ul className="space-y-1 text-[11px] text-slate-400">
                {dim.evidence.map((ev, i) => (
                  <li key={i} className="flex items-start space-x-1.5">
                    <span className="h-1 w-1 rounded-full bg-sky-400 mt-1.5 shrink-0" />
                    <span>{ev}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
