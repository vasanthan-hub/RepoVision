import React, { useState, useEffect } from 'react';
import { DeveloperAnalytics, Repository } from '../types/api';
import { api } from '../services/api';
import { Users, GitCommit, TrendingUp, BarChart3 } from 'lucide-react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend
} from 'recharts';

interface DevelopersViewProps {
  repository: Repository;
}

const COLORS = ['#38bdf8', '#818cf8', '#34d399', '#f472b6', '#fbbf24', '#a78bfa'];

export const DevelopersView: React.FC<DevelopersViewProps> = ({ repository }) => {
  const [data, setData] = useState<DeveloperAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, [repository.id]);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.getDeveloperAnalytics(repository.id);
      setData(res);
    } catch (err: any) {
      setError(err.message || 'Failed to load developer analytics');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-20 flex flex-col items-center justify-center space-y-3">
        <div className="h-8 w-8 border-3 border-sky-500/20 border-t-sky-500 rounded-full animate-spin" />
        <p className="text-xs text-slate-400">Loading contributor analytics...</p>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="p-8 text-center rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-300">
        <p className="text-sm font-semibold">{error || 'No developer data found'}</p>
      </div>
    );
  }

  const pieData = data.developers.map((d) => ({
    name: d.authorName,
    value: d.churn,
    commits: d.commitCount,
  }));

  return (
    <div className="space-y-6">
      {/* Contributor Distribution Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Metric Card */}
        <div className="bg-[#0f172a] p-5 rounded-2xl border border-slate-800 space-y-4 flex flex-col justify-between">
          <div>
            <div className="text-xs font-bold uppercase tracking-wider text-sky-400 mb-1">
              Engineering Team
            </div>
            <h3 className="text-xl font-bold text-white">Collaboration Metrics</h3>
            <p className="text-xs text-slate-400 mt-2 leading-relaxed">
              Distribution of commit volume and code churn across active engineers. Metrics reflect contribution share to assist in domain knowledge spreading.
            </p>
          </div>

          <div className="grid grid-cols-2 gap-3 pt-2">
            <div className="bg-slate-900/80 p-3 rounded-xl border border-slate-800">
              <div className="text-[11px] text-slate-400">Active Authors</div>
              <div className="text-xl font-extrabold text-white font-mono">{data.totalDevelopers}</div>
            </div>
            <div className="bg-slate-900/80 p-3 rounded-xl border border-slate-800">
              <div className="text-[11px] text-slate-400">Total Commits</div>
              <div className="text-xl font-extrabold text-white font-mono">
                {data.developers.reduce((acc, d) => acc + d.commitCount, 0)}
              </div>
            </div>
          </div>
        </div>

        {/* Churn Share Pie Chart */}
        <div className="lg:col-span-2 bg-[#0f172a] p-5 rounded-2xl border border-slate-800 space-y-2">
          <h4 className="font-bold text-slate-200 text-xs flex items-center space-x-2">
            <BarChart3 className="h-3.5 w-3.5 text-indigo-400" />
            <span>Code Churn Contribution Share</span>
          </h4>
          <div className="h-56 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                  label={({ name, percent }: { name?: string; percent?: number }) => `${name ?? ''} (${((percent ?? 0) * 100).toFixed(0)}%)`}
                >
                  {pieData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(value: any) => [`${Number(value).toLocaleString()} lines churn`, 'Churn']}
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.75rem', fontSize: '11px' }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Contributor Table */}
      <div className="bg-[#0f172a] rounded-2xl border border-slate-800 overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-800">
          <h3 className="font-bold text-slate-100 text-sm flex items-center space-x-2">
            <Users className="h-4 w-4 text-sky-400" />
            <span>Contributor Activity & Volume Breakdown</span>
          </h3>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 font-semibold border-b border-slate-800">
              <tr>
                <th className="px-6 py-3">Contributor</th>
                <th className="px-6 py-3">Commits</th>
                <th className="px-6 py-3">Additions</th>
                <th className="px-6 py-3">Deletions</th>
                <th className="px-6 py-3">Total Churn</th>
                <th className="px-6 py-3">Commit %</th>
                <th className="px-6 py-3">Churn %</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {data.developers.map((dev) => (
                <tr key={dev.authorEmail} className="hover:bg-slate-800/30 transition-colors">
                  <td className="px-6 py-3.5">
                    <div className="font-semibold text-slate-200">{dev.authorName}</div>
                    <div className="text-[11px] text-slate-400 font-mono">{dev.authorEmail}</div>
                  </td>
                  <td className="px-6 py-3.5 font-mono text-slate-200">{dev.commitCount}</td>
                  <td className="px-6 py-3.5 font-mono text-emerald-400">+{dev.additions.toLocaleString()}</td>
                  <td className="px-6 py-3.5 font-mono text-rose-400">-{dev.deletions.toLocaleString()}</td>
                  <td className="px-6 py-3.5 font-mono font-bold text-white">{dev.churn.toLocaleString()}</td>
                  <td className="px-6 py-3.5">
                    <div className="flex items-center space-x-2">
                      <span className="font-mono text-slate-300 w-10">{dev.commitPercentage.toFixed(1)}%</span>
                      <div className="w-16 bg-slate-800 rounded-full h-1.5 overflow-hidden">
                        <div className="bg-sky-500 h-1.5 rounded-full" style={{ width: `${dev.commitPercentage}%` }} />
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-3.5">
                    <div className="flex items-center space-x-2">
                      <span className="font-mono text-slate-300 w-10">{dev.churnPercentage.toFixed(1)}%</span>
                      <div className="w-16 bg-slate-800 rounded-full h-1.5 overflow-hidden">
                        <div className="bg-indigo-500 h-1.5 rounded-full" style={{ width: `${dev.churnPercentage}%` }} />
                      </div>
                    </div>
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
