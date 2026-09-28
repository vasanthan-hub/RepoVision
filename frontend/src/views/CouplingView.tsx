import React, { useState, useEffect } from 'react';
import { ChangeCoupling, Repository } from '../types/api';
import { api } from '../services/api';
import { Network, Link2, Info, ArrowRight } from 'lucide-react';

interface CouplingViewProps {
  repository: Repository;
}

export const CouplingView: React.FC<CouplingViewProps> = ({ repository }) => {
  const [data, setData] = useState<ChangeCoupling | null>(null);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'ALL' | 'STRONG' | 'MODERATE'>('ALL');

  useEffect(() => {
    loadCoupling();
  }, [repository.id]);

  const loadCoupling = async () => {
    setLoading(true);
    try {
      const res = await api.getChangeCoupling(repository.id);
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
        <p className="text-xs text-slate-400">Computing cross-file co-change coupling matrix...</p>
      </div>
    );
  }

  const couplings = data?.couplings || [];
  const filtered = couplings.filter((c) => filter === 'ALL' || c.strength === filter);

  return (
    <div className="space-y-6">
      {/* Explanation Banner */}
      <div className="bg-gradient-to-r from-slate-900 to-indigo-950/30 p-5 rounded-2xl border border-slate-800 space-y-2">
        <div className="flex items-center space-x-2 text-sky-400 font-bold text-xs uppercase tracking-wider">
          <Info className="h-4 w-4" />
          <span>Architectural Change Coupling Signal</span>
        </div>
        <p className="text-xs text-slate-300 leading-relaxed max-w-3xl">
          Change coupling detects files that consistently modify together within identical commits. When components in disparate packages exhibit high co-change rates (&gt;70%), it signals implicit architectural coupling, leaky abstractions, or shotgun surgery anti-patterns.
        </p>
      </div>

      {/* Filter and Table */}
      <div className="bg-[#0f172a] rounded-2xl border border-slate-800 overflow-hidden space-y-4">
        <div className="p-4 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Network className="h-4 w-4 text-sky-400" />
            <h3 className="font-bold text-slate-100 text-sm">
              Coupled File Pairs ({filtered.length})
            </h3>
          </div>

          <div className="flex items-center space-x-1.5 bg-slate-900 p-1 rounded-lg border border-slate-800 text-xs">
            <button
              onClick={() => setFilter('ALL')}
              className={`px-3 py-1 rounded-md font-semibold transition-all ${
                filter === 'ALL' ? 'bg-sky-500 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              All ({couplings.length})
            </button>
            <button
              onClick={() => setFilter('STRONG')}
              className={`px-3 py-1 rounded-md font-semibold transition-all ${
                filter === 'STRONG' ? 'bg-sky-500 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Strong ({data?.strongCouplingCount || 0})
            </button>
            <button
              onClick={() => setFilter('MODERATE')}
              className={`px-3 py-1 rounded-md font-semibold transition-all ${
                filter === 'MODERATE' ? 'bg-sky-500 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Moderate
            </button>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 font-semibold border-b border-slate-800">
              <tr>
                <th className="px-6 py-3">Coupled Files Pair</th>
                <th className="px-6 py-3">Co-Changes</th>
                <th className="px-6 py-3">Coupling %</th>
                <th className="px-6 py-3">Coupling Strength</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {filtered.map((c, i) => (
                <tr key={i} className="hover:bg-slate-800/30 transition-colors">
                  <td className="px-6 py-3.5 max-w-lg">
                    <div className="flex items-center space-x-2 font-mono text-[11px]">
                      <span className="text-slate-200 font-semibold truncate">{c.fileA}</span>
                      <Link2 className="h-3 w-3 text-sky-400 shrink-0" />
                      <span className="text-slate-200 font-semibold truncate">{c.fileB}</span>
                    </div>
                  </td>
                  <td className="px-6 py-3.5 font-mono text-slate-200 font-bold">
                    {c.coChangeCount} commits
                  </td>
                  <td className="px-6 py-3.5">
                    <div className="flex items-center space-x-2">
                      <span className="font-mono text-sky-400 font-bold w-12">{c.couplingPercentage.toFixed(1)}%</span>
                      <div className="w-24 bg-slate-800 rounded-full h-1.5 overflow-hidden">
                        <div
                          className={`h-1.5 rounded-full ${c.strength === 'STRONG' ? 'bg-rose-500' : 'bg-amber-500'}`}
                          style={{ width: `${c.couplingPercentage}%` }}
                        />
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-3.5">
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                        c.strength === 'STRONG'
                          ? 'bg-rose-500/10 text-rose-300 border-rose-500/30'
                          : 'bg-amber-500/10 text-amber-300 border-amber-500/30'
                      }`}
                    >
                      {c.strength}
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
