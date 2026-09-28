import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { ReleaseAnalytics, ReleaseMetrics } from '../types/api';
import { 
  Tag, 
  Calendar, 
  Clock, 
  GitCommit, 
  Layers, 
  Activity, 
  Info,
  ExternalLink,
  Terminal,
  TrendingDown,
  RefreshCw
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  LineChart,
  Line
} from 'recharts';

interface ReleasesViewProps {
  repositoryId: string;
}

export const ReleasesView: React.FC<ReleasesViewProps> = ({ repositoryId }) => {
  const [data, setData] = useState<ReleaseAnalytics | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await api.getReleases(repositoryId);
      setData(res);
    } catch (err: any) {
      setError(err.message || 'Failed to load release analytics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (repositoryId) {
      loadData();
    }
  }, [repositoryId]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
        <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
        <p className="text-slate-400 text-sm font-medium">Inspecting Git tags, release cadences, and tag intervals...</p>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-6 text-center text-red-400">
        <p className="font-semibold text-lg mb-2">Error Loading Releases</p>
        <p className="text-sm text-red-300/80 mb-4">{error}</p>
        <button 
          onClick={loadData}
          className="px-4 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-300 rounded-lg text-sm font-medium"
        >
          Retry
        </button>
      </div>
    );
  }

  const hasReleases = data.totalReleases > 0;

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Tag className="w-5 h-5 text-indigo-400" />
              <h2 className="text-xl font-bold text-white tracking-tight">Release Cadence & Tag Analytics</h2>
            </div>
            <p className="text-slate-400 text-sm">
              Discover release velocity, deployment rhythms, and scope changes between Git tags.
            </p>
          </div>
          <button
            onClick={loadData}
            className="flex items-center gap-2 px-3 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium rounded-lg border border-slate-700/50 transition-colors self-start sm:self-auto"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            Refresh Releases
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-medium">Total Releases / Tags</p>
            <p className="text-2xl font-bold text-white mt-1 font-mono">{data.totalReleases}</p>
          </div>
          <div className="p-2.5 bg-indigo-500/10 rounded-xl border border-indigo-500/20">
            <Tag className="w-5 h-5 text-indigo-400" />
          </div>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-medium">Average Release Interval</p>
            <p className="text-2xl font-bold text-sky-400 mt-1 font-mono">
              {data.averageReleaseIntervalDays > 0 ? `${data.averageReleaseIntervalDays.toFixed(1)} days` : 'N/A'}
            </p>
          </div>
          <div className="p-2.5 bg-sky-500/10 rounded-xl border border-sky-500/20">
            <Clock className="w-5 h-5 text-sky-400" />
          </div>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-medium">Deployment Rhythm</p>
            <p className="text-sm font-semibold text-emerald-400 mt-2">
              {data.totalReleases >= 3 ? 'Predictable Cadence' : data.totalReleases > 0 ? 'Nascent Releases' : 'No Tags Detected'}
            </p>
          </div>
          <div className="p-2.5 bg-emerald-500/10 rounded-xl border border-emerald-500/20">
            <Activity className="w-5 h-5 text-emerald-400" />
          </div>
        </div>
      </div>

      {!hasReleases ? (
        /* Graceful Empty State with Git tag guidance */
        <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-8 text-center max-w-2xl mx-auto space-y-4">
          <div className="w-12 h-12 bg-slate-800/80 rounded-full flex items-center justify-center mx-auto text-slate-400">
            <Tag className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-base font-semibold text-white mb-1">No Git Tags Found</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              This repository currently does not contain any Git tags (annotated or lightweight). 
              RepoVision extracts release intervals, commits per release, and tag churn deterministically from Git references.
            </p>
          </div>

          <div className="bg-slate-950/80 border border-slate-800 rounded-xl p-4 text-left font-mono text-xs text-slate-300">
            <div className="flex items-center gap-2 text-slate-400 mb-2 font-sans font-semibold text-[11px] uppercase tracking-wider">
              <Terminal className="w-3.5 h-3.5 text-indigo-400" />
              How to create tags in your repository:
            </div>
            <p className="text-slate-400 mb-1"># Create an annotated release tag</p>
            <p className="text-indigo-300 mb-2">git tag -a v1.0.0 -m "Release version 1.0.0"</p>
            <p className="text-slate-400 mb-1"># Push tags to remote</p>
            <p className="text-indigo-300">git push origin --tags</p>
          </div>
        </div>
      ) : (
        <>
          {/* Release Interval & Churn Chart */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl p-6">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-400 mb-4">
              Days Between Releases
            </h3>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={data.releases}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.5} />
                  <XAxis dataKey="tagName" stroke="#94a3b8" fontSize={11} />
                  <YAxis stroke="#94a3b8" fontSize={11} unit="d" />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.75rem', fontSize: '12px' }}
                    itemStyle={{ color: '#e2e8f0' }}
                  />
                  <Bar dataKey="daysSincePreviousRelease" name="Days Since Previous" fill="#6366f1" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Releases Table */}
          <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl overflow-hidden">
            <div className="p-4 border-b border-slate-800 flex items-center justify-between">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-400">
                Release History ({data.releases.length})
              </h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-950/60 text-slate-400 border-b border-slate-800">
                  <tr>
                    <th className="py-3 px-4 font-semibold">Tag</th>
                    <th className="py-3 px-4 font-semibold">Release Date</th>
                    <th className="py-3 px-4 font-semibold">Commit</th>
                    <th className="py-3 px-4 font-semibold">Days Since Prev</th>
                    <th className="py-3 px-4 font-semibold">Commits</th>
                    <th className="py-3 px-4 font-semibold">Churn</th>
                    <th className="py-3 px-4 font-semibold">Release Note</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {data.releases.map((rel, idx) => (
                    <tr key={idx} className="hover:bg-slate-800/30 transition-colors">
                      <td className="py-3 px-4">
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md font-mono text-xs font-semibold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                          <Tag className="w-3 h-3" />
                          {rel.tagName}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-slate-300 font-mono">
                        {rel.releaseDate ? new Date(rel.releaseDate).toLocaleDateString() : 'N/A'}
                      </td>
                      <td className="py-3 px-4">
                        <span className="font-mono text-slate-400 bg-slate-800 px-2 py-0.5 rounded">
                          {rel.commitHash ? rel.commitHash.substring(0, 7) : 'N/A'}
                        </span>
                      </td>
                      <td className="py-3 px-4 font-mono text-slate-300">
                        {rel.daysSincePreviousRelease > 0 ? `${rel.daysSincePreviousRelease}d` : '—'}
                      </td>
                      <td className="py-3 px-4 font-mono text-indigo-300">
                        {rel.commitsCount}
                      </td>
                      <td className="py-3 px-4 font-mono text-slate-300">
                        {rel.churn.toLocaleString()}
                      </td>
                      <td className="py-3 px-4 text-slate-400 max-w-xs truncate" title={rel.message}>
                        {rel.message || '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  );
};
