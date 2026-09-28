import React, { useState, useEffect } from 'react';
import { BranchAnalytics, Repository } from '../types/api';
import { api } from '../services/api';
import { GitBranch, Clock, CheckCircle2, AlertOctagon, GitMerge } from 'lucide-react';

interface BranchesViewProps {
  repository: Repository;
}

export const BranchesView: React.FC<BranchesViewProps> = ({ repository }) => {
  const [data, setData] = useState<BranchAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [thresholdDays, setThresholdDays] = useState(30);

  useEffect(() => {
    loadBranches(thresholdDays);
  }, [repository.id, thresholdDays]);

  const loadBranches = async (days: number) => {
    setLoading(true);
    try {
      const res = await api.getBranchAnalytics(repository.id, days);
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
        <p className="text-xs text-slate-400">Discovering Git branch topology and lifecycle signals...</p>
      </div>
    );
  }

  const branches = data?.branches || [];

  return (
    <div className="space-y-6">
      {/* Branch Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1 flex items-center justify-between">
            <span>Total Branches</span>
            <GitBranch className="h-4 w-4 text-sky-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">{data?.totalBranches || 0}</div>
          <p className="text-[11px] text-slate-400 mt-1">Local & remote tracking</p>
        </div>

        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1 flex items-center justify-between">
            <span>Active Branches</span>
            <CheckCircle2 className="h-4 w-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-black text-emerald-400 font-mono">{data?.activeBranches || 0}</div>
          <p className="text-[11px] text-slate-400 mt-1">Activity within {thresholdDays} days</p>
        </div>

        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1 flex items-center justify-between">
            <span>Stale Branches</span>
            <AlertOctagon className="h-4 w-4 text-amber-400" />
          </div>
          <div className="text-2xl font-black text-amber-400 font-mono">{data?.staleBranches || 0}</div>
          <p className="text-[11px] text-slate-400 mt-1">&gt; {thresholdDays} days dormant</p>
        </div>
      </div>

      {/* Threshold Selector & Table */}
      <div className="bg-[#0f172a] rounded-2xl border border-slate-800 overflow-hidden">
        <div className="p-4 border-b border-slate-800 flex items-center justify-between">
          <h3 className="font-bold text-slate-100 text-sm flex items-center space-x-2">
            <GitBranch className="h-4 w-4 text-sky-400" />
            <span>Branch Lifecycle Inventory</span>
          </h3>

          <div className="flex items-center space-x-2 text-xs">
            <span className="text-slate-400">Stale Threshold:</span>
            <select
              value={thresholdDays}
              onChange={(e) => setThresholdDays(Number(e.target.value))}
              className="bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1 text-slate-200 font-medium"
            >
              <option value={14}>14 days</option>
              <option value={30}>30 days</option>
              <option value={60}>60 days</option>
              <option value={90}>90 days</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 font-semibold border-b border-slate-800">
              <tr>
                <th className="px-6 py-3">Branch Ref</th>
                <th className="px-6 py-3">Status</th>
                <th className="px-6 py-3">Latest Commit</th>
                <th className="px-6 py-3">Author</th>
                <th className="px-6 py-3">Last Active</th>
                <th className="px-6 py-3">Commit Count</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {branches.map((b) => (
                <tr key={b.name} className="hover:bg-slate-800/30 transition-colors">
                  <td className="px-6 py-3.5">
                    <div className="flex items-center space-x-2">
                      <span className="font-mono font-bold text-slate-200">{b.name}</span>
                      {b.isDefault && (
                        <span className="text-[10px] font-bold px-1.5 py-0.2 rounded bg-sky-500/10 text-sky-400 border border-sky-500/20">
                          DEFAULT
                        </span>
                      )}
                      {b.isMerged && (
                        <span className="text-[10px] font-bold px-1.5 py-0.2 rounded bg-indigo-500/10 text-indigo-300 border border-indigo-500/20 flex items-center space-x-1">
                          <GitMerge className="h-2.5 w-2.5" />
                          <span>MERGED</span>
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-3.5">
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                        b.isStale
                          ? 'bg-amber-500/10 text-amber-300 border-amber-500/30'
                          : 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30'
                      }`}
                    >
                      {b.isStale ? `STALE (${b.daysInactive}d)` : 'ACTIVE'}
                    </span>
                  </td>
                  <td className="px-6 py-3.5 max-w-xs truncate">
                    <div className="font-mono text-[11px] text-sky-400 font-semibold">{b.latestCommitHash.substring(0, 7)}</div>
                    <div className="text-slate-300 truncate">{b.latestCommitSubject}</div>
                  </td>
                  <td className="px-6 py-3.5 text-slate-300">{b.latestCommitAuthor}</td>
                  <td className="px-6 py-3.5 text-slate-400">
                    <div className="flex items-center space-x-1">
                      <Clock className="h-3 w-3" />
                      <span>{new Date(b.lastActivity).toLocaleDateString()}</span>
                    </div>
                  </td>
                  <td className="px-6 py-3.5 font-mono text-slate-200">{b.commitCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
