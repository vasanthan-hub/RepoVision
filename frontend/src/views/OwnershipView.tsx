import React, { useState, useEffect } from 'react';
import { OwnershipAnalytics, Repository } from '../types/api';
import { api } from '../services/api';
import { UserCheck, ShieldAlert, Users, Layers, AlertCircle, CheckCircle } from 'lucide-react';

interface OwnershipViewProps {
  repository: Repository;
}

export const OwnershipView: React.FC<OwnershipViewProps> = ({ repository }) => {
  const [data, setData] = useState<OwnershipAnalytics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOwnership();
  }, [repository.id]);

  const loadOwnership = async () => {
    setLoading(true);
    try {
      const res = await api.getOwnershipAnalytics(repository.id);
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
        <p className="text-xs text-slate-400">Analyzing domain ownership concentration and bus factor...</p>
      </div>
    );
  }

  const busFactor = data?.busFactor;
  const files = data?.fileOwnerships || [];

  return (
    <div className="space-y-6">
      {/* Bus Factor Alert Hero */}
      <div className="bg-[#0f172a] rounded-2xl border border-slate-800 p-6 space-y-5">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2 max-w-xl">
            <div className="flex items-center space-x-2">
              <ShieldAlert className="h-5 w-5 text-amber-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-amber-400">
                Knowledge Concentration & Bus Factor
              </span>
            </div>
            <h2 className="text-xl font-bold text-white">Repository Bus Factor: {busFactor?.busFactor || 1}</h2>
            <p className="text-xs text-slate-300 leading-relaxed font-normal">
              Bus Factor evaluates the minimum number of core engineers whose sudden absence would leave &gt;50% of the repository's modules without domain expertise.
            </p>
          </div>

          <div className="bg-slate-900/90 p-4 rounded-xl border border-slate-800 flex items-center space-x-6 shrink-0">
            <div>
              <div className="text-[11px] text-slate-400 mb-0.5">Estimated Factor</div>
              <div className="text-3xl font-black text-amber-400 font-mono">
                {busFactor?.busFactor}
              </div>
            </div>
            <div className="h-10 w-px bg-slate-800" />
            <div>
              <div className="text-[11px] text-slate-400 mb-0.5">Single-Author Files</div>
              <div className="text-xl font-bold text-slate-100 font-mono">
                {busFactor?.singleContributorFilesCount} / {busFactor?.totalFiles}
              </div>
            </div>
          </div>
        </div>

        {/* Supporting Evidence List */}
        <div className="bg-slate-950/60 p-4 rounded-xl border border-slate-800/80 space-y-2">
          <div className="text-xs font-bold text-slate-300">Underlying Analytical Evidence:</div>
          <ul className="space-y-1.5 text-xs text-slate-400">
            {busFactor?.evidence.map((ev, i) => (
              <li key={i} className="flex items-start space-x-2">
                <span className="h-1.5 w-1.5 rounded-full bg-sky-400 mt-1.5 shrink-0" />
                <span>{ev}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {/* Key Developers Ownership Share */}
      <div className="bg-[#0f172a] rounded-2xl border border-slate-800 p-5 space-y-3">
        <h3 className="font-bold text-slate-100 text-sm flex items-center space-x-2">
          <Users className="h-4 w-4 text-sky-400" />
          <span>Primary Codebase Ownership by Contributor</span>
        </h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {busFactor?.keyDevelopers.map((dev) => (
            <div key={dev.developerEmail} className="bg-slate-900/60 p-4 rounded-xl border border-slate-800/80 space-y-2">
              <div className="flex justify-between items-start">
                <div>
                  <div className="font-bold text-slate-100 text-xs">{dev.developerName}</div>
                  <div className="text-[11px] text-slate-400 font-mono">{dev.developerEmail}</div>
                </div>
                <span className="text-xs font-mono font-bold text-sky-400">{dev.percentageOfCodebase}%</span>
              </div>
              <div className="text-[11px] text-slate-400">
                Primary maintainer of <span className="font-bold text-slate-200">{dev.primarilyOwnedFilesCount}</span> files
              </div>
              <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                <div className="bg-sky-500 h-1.5 rounded-full" style={{ width: `${dev.percentageOfCodebase}%` }} />
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* File Ownership Table */}
      <div className="bg-[#0f172a] rounded-2xl border border-slate-800 overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-800">
          <h3 className="font-bold text-slate-100 text-sm flex items-center space-x-2">
            <UserCheck className="h-4 w-4 text-sky-400" />
            <span>Component Ownership & Knowledge Concentration</span>
          </h3>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 font-semibold border-b border-slate-800">
              <tr>
                <th className="px-6 py-3">File Path</th>
                <th className="px-6 py-3">Module</th>
                <th className="px-6 py-3">Primary Contributor</th>
                <th className="px-6 py-3">Ownership %</th>
                <th className="px-6 py-3">Authors</th>
                <th className="px-6 py-3">Concentration</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {files.map((fo) => (
                <tr key={fo.filePath} className="hover:bg-slate-800/30 transition-colors">
                  <td className="px-6 py-3 font-mono font-medium text-slate-200 max-w-sm truncate">
                    {fo.filePath}
                  </td>
                  <td className="px-6 py-3 font-mono text-[11px] text-slate-400 max-w-xs truncate">
                    {fo.module}
                  </td>
                  <td className="px-6 py-3 text-slate-200">
                    <span className="font-semibold">{fo.primaryContributorName}</span>
                  </td>
                  <td className="px-6 py-3 font-mono font-bold text-sky-400">
                    {fo.primaryContributorPercentage.toFixed(1)}%
                  </td>
                  <td className="px-6 py-3 font-mono text-slate-300">{fo.contributorCount}</td>
                  <td className="px-6 py-3">
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                        fo.concentrationLevel === 'HIGH'
                          ? 'bg-rose-500/10 text-rose-300 border-rose-500/30'
                          : fo.concentrationLevel === 'MODERATE'
                          ? 'bg-amber-500/10 text-amber-300 border-amber-500/30'
                          : 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30'
                      }`}
                    >
                      {fo.concentrationLevel}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
