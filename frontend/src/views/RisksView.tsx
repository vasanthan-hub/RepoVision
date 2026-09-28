import React, { useState, useEffect } from 'react';
import { RiskReport, RiskItem, Repository } from '../types/api';
import { api } from '../services/api';
import { AlertTriangle, ShieldAlert, ShieldCheck, Flame, GitBranch, GitCommit, Network, Users } from 'lucide-react';

interface RisksViewProps {
  repository: Repository;
}

export const RisksView: React.FC<RisksViewProps> = ({ repository }) => {
  const [data, setData] = useState<RiskReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [severityFilter, setSeverityFilter] = useState<'ALL' | 'HIGH' | 'MEDIUM' | 'LOW'>('ALL');

  useEffect(() => {
    loadRisks();
  }, [repository.id]);

  const loadRisks = async () => {
    setLoading(true);
    try {
      const res = await api.getRisks(repository.id);
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
        <p className="text-xs text-slate-400">Evaluating multi-vector architectural and hygiene risks...</p>
      </div>
    );
  }

  const risks = data?.risks || [];
  const filtered = risks.filter((r) => severityFilter === 'ALL' || r.severity === severityFilter);

  return (
    <div className="space-y-6">
      {/* Risk Summary Counters */}
      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1">Total Detected Risks</div>
          <div className="text-2xl font-black text-white font-mono">{data?.totalRisks || 0}</div>
          <p className="text-[11px] text-slate-400 mt-1">Multi-factor engine scan</p>
        </div>

        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1">High Severity</div>
          <div className="text-2xl font-black text-rose-400 font-mono">{data?.highSeverityCount || 0}</div>
          <p className="text-[11px] text-rose-400/80 mt-1">Requires immediate attention</p>
        </div>

        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1">Medium Severity</div>
          <div className="text-2xl font-black text-amber-400 font-mono">{data?.mediumSeverityCount || 0}</div>
          <p className="text-[11px] text-amber-400/80 mt-1">Technical debt indicators</p>
        </div>

        <div className="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1">Low Severity</div>
          <div className="text-2xl font-black text-sky-400 font-mono">{data?.lowSeverityCount || 0}</div>
          <p className="text-[11px] text-slate-400 mt-1">Observational signals</p>
        </div>
      </div>

      {/* Filter and List */}
      <div className="space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <h3 className="font-bold text-slate-100 text-sm flex items-center space-x-2">
            <AlertTriangle className="h-4 w-4 text-amber-400" />
            <span>Identified Risk Signals ({filtered.length})</span>
          </h3>

          <div className="flex items-center space-x-1.5 bg-slate-900 p-1 rounded-lg border border-slate-800 text-xs">
            <button
              onClick={() => setSeverityFilter('ALL')}
              className={`px-3 py-1 rounded-md font-semibold transition-all ${
                severityFilter === 'ALL' ? 'bg-sky-500 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              All ({risks.length})
            </button>
            <button
              onClick={() => setSeverityFilter('HIGH')}
              className={`px-3 py-1 rounded-md font-semibold transition-all ${
                severityFilter === 'HIGH' ? 'bg-rose-500 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              High ({data?.highSeverityCount || 0})
            </button>
            <button
              onClick={() => setSeverityFilter('MEDIUM')}
              className={`px-3 py-1 rounded-md font-semibold transition-all ${
                severityFilter === 'MEDIUM' ? 'bg-amber-500 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Medium ({data?.mediumSeverityCount || 0})
            </button>
            <button
              onClick={() => setSeverityFilter('LOW')}
              className={`px-3 py-1 rounded-md font-semibold transition-all ${
                severityFilter === 'LOW' ? 'bg-sky-500 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Low ({data?.lowSeverityCount || 0})
            </button>
          </div>
        </div>

        {filtered.length === 0 ? (
          <div className="bg-[#0f172a] rounded-2xl border border-slate-800 p-12 text-center space-y-2">
            <ShieldCheck className="h-8 w-8 text-emerald-400 mx-auto" />
            <h4 className="font-bold text-slate-200 text-sm">No Risks Found</h4>
            <p className="text-xs text-slate-400">All risk evaluation parameters remain within nominal thresholds.</p>
          </div>
        ) : (
          <div className="space-y-3.5">
            {filtered.map((risk) => (
              <div
                key={risk.id}
                className="bg-[#0f172a] rounded-2xl border border-slate-800 p-5 space-y-3 shadow-sm hover:border-slate-700 transition-colors"
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div className="flex items-center space-x-2.5">
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                        risk.severity === 'HIGH'
                          ? 'bg-rose-500/10 text-rose-300 border-rose-500/30'
                          : risk.severity === 'MEDIUM'
                          ? 'bg-amber-500/10 text-amber-300 border-amber-500/30'
                          : 'bg-sky-500/10 text-sky-300 border-sky-500/30'
                      }`}
                    >
                      {risk.severity}
                    </span>
                    <span className="text-xs font-mono uppercase font-bold text-slate-400">
                      {risk.type.replace('_', ' ')}
                    </span>
                    <span className="text-slate-600">•</span>
                    <h4 className="font-bold text-slate-100 text-sm">{risk.title}</h4>
                  </div>
                </div>

                <div className="font-mono text-xs text-sky-400 bg-slate-900/80 px-3 py-1.5 rounded-lg border border-slate-800/80 truncate">
                  {risk.entity}
                </div>

                <div className="space-y-1.5 text-xs">
                  <div className="text-slate-300">
                    <span className="font-bold text-slate-400">Evidence: </span>
                    {risk.evidence}
                  </div>
                  <div className="text-slate-400 leading-relaxed">
                    <span className="font-bold text-slate-400">Description: </span>
                    {risk.description}
                  </div>
                </div>

                <div className="pt-2 border-t border-slate-800/80 text-xs flex items-start space-x-2 text-slate-200 font-medium">
                  <span className="font-bold text-indigo-400 shrink-0">Remediation Action:</span>
                  <span>{risk.recommendation}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
