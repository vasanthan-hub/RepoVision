import React, { useState, useEffect } from 'react';
import { CommitAnalytics, Repository } from '../types/api';
import { api } from '../services/api';
import {
  GitCommit,
  TrendingUp,
  Plus,
  Minus,
  Calendar,
  Layers,
  Search,
  ExternalLink
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid
} from 'recharts';

interface CommitsViewProps {
  repository: Repository;
}

export const CommitsView: React.FC<CommitsViewProps> = ({ repository }) => {
  const [data, setData] = useState<CommitAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, [repository.id]);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.getCommitAnalytics(repository.id);
      setData(res);
    } catch (err: any) {
      setError(err.message || 'Failed to load commit analytics');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-20 flex flex-col items-center justify-center space-y-3">
        <div className="h-8 w-8 border-3 border-sky-500/20 border-t-sky-500 rounded-full animate-spin" />
        <p className="text-xs text-slate-400">Loading commit analytics...</p>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="p-8 text-center rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-300">
        <p className="text-sm font-semibold">{error || 'No commit data found'}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1 flex items-center justify-between">
            <span>Total Commits</span>
            <GitCommit className="h-4 w-4 text-sky-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">{data.totalCommits.toLocaleString()}</div>
          <div className="text-[11px] text-slate-400 mt-1">
            Avg {data.averageCommitsPerActiveDay.toFixed(1)} commits / active day
          </div>
        </div>

        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1 flex items-center justify-between">
            <span>Total Code Churn</span>
            <TrendingUp className="h-4 w-4 text-indigo-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">{data.totalChurn.toLocaleString()}</div>
          <div className="text-[11px] text-slate-400 mt-1">
            Avg {Math.round(data.averageCommitSize)} lines / commit
          </div>
        </div>

        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1 flex items-center justify-between">
            <span>Additions</span>
            <Plus className="h-4 w-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-black text-emerald-400 font-mono">+{data.totalAdditions.toLocaleString()}</div>
          <div className="text-[11px] text-slate-400 mt-1">
            Avg +{Math.round(data.averageAdditionsPerCommit)} lines / commit
          </div>
        </div>

        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1 flex items-center justify-between">
            <span>Deletions</span>
            <Minus className="h-4 w-4 text-rose-400" />
          </div>
          <div className="text-2xl font-black text-rose-400 font-mono">-{data.totalDeletions.toLocaleString()}</div>
          <div className="text-[11px] text-slate-400 mt-1">
            Avg -{Math.round(data.averageDeletionsPerCommit)} lines / commit
          </div>
        </div>
      </div>

      {/* Largest Commit Card */}
      {data.largestCommit && (
        <div className="bg-[#0f172a] p-5 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded bg-indigo-500/10 text-indigo-300 border border-indigo-500/30">
                Largest Single Commit
              </span>
              <span className="font-mono text-xs text-sky-400 font-semibold">{data.largestCommit.hash.substring(0, 8)}</span>
            </div>
            <p className="text-sm font-semibold text-slate-200">{data.largestCommit.subject}</p>
          </div>
          <div className="text-right shrink-0 font-mono text-xs">
            <div className="text-base font-extrabold text-white">{data.largestCommit.churn.toLocaleString()} lines churn</div>
            <div className="text-emerald-400">+{data.largestCommit.additions} <span className="text-rose-400">-{data.largestCommit.deletions}</span></div>
          </div>
        </div>
      )}

      {/* Commit Cadence Chart */}
      <div className="bg-[#0f172a] p-5 rounded-2xl border border-slate-800 space-y-4">
        <h3 className="font-bold text-slate-100 text-sm flex items-center space-x-2">
          <Calendar className="h-4 w-4 text-sky-400" />
          <span>Commit Frequency Timeline</span>
        </h3>

        {data.commitsByDate.length > 0 ? (
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={data.commitsByDate} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="commitGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#38bdf8" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#38bdf8" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="date" stroke="#64748b" tick={{ fontSize: 10 }} />
                <YAxis stroke="#64748b" tick={{ fontSize: 10 }} allowDecimals={false} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.75rem', fontSize: '11px' }}
                />
                <Area type="monotone" dataKey="count" name="Commits" stroke="#38bdf8" strokeWidth={2} fillOpacity={1} fill="url(#commitGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div className="py-12 text-center text-xs text-slate-400">No date-indexed commit data available.</div>
        )}
      </div>
    </div>
  );
};
