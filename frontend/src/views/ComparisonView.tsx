import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { PeriodComparison, WhatChanged, MetricDelta } from '../types/api';
import { 
  GitCompare, 
  Calendar, 
  ArrowUpRight, 
  ArrowDownRight, 
  Minus, 
  Sparkles, 
  FileText, 
  Layers, 
  Users, 
  GitCommit, 
  Activity,
  CheckCircle,
  RefreshCw
} from 'lucide-react';

interface ComparisonViewProps {
  repositoryId: string;
}

export const ComparisonView: React.FC<ComparisonViewProps> = ({ repositoryId }) => {
  const [days, setDays] = useState<number>(30);
  const [comparison, setComparison] = useState<PeriodComparison | null>(null);
  const [whatChanged, setWhatChanged] = useState<WhatChanged | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = async (selectedDays: number) => {
    try {
      setLoading(true);
      setError(null);
      const [compRes, wcRes] = await Promise.all([
        api.getComparison(repositoryId, selectedDays),
        api.getWhatChanged(repositoryId, selectedDays)
      ]);
      setComparison(compRes);
      setWhatChanged(wcRes);
    } catch (err: any) {
      setError(err.message || 'Failed to load comparison data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (repositoryId) {
      loadData(days);
    }
  }, [repositoryId, days]);

  const handleDaysChange = (newDays: number) => {
    setDays(newDays);
  };

  const formatDate = (isoStr: string) => {
    if (!isoStr) return 'N/A';
    try {
      return new Date(isoStr).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });
    } catch {
      return isoStr;
    }
  };

  const renderTrendIcon = (trend: string, percentage: number) => {
    if (trend === 'UP') {
      return <ArrowUpRight className="w-4 h-4 text-emerald-400" />;
    }
    if (trend === 'DOWN') {
      return <ArrowDownRight className="w-4 h-4 text-rose-400" />;
    }
    return <Minus className="w-4 h-4 text-slate-400" />;
  };

  const getDeltaBadgeClass = (trend: string, isInverseGood: boolean = false) => {
    if (trend === 'FLAT') return 'bg-slate-800 text-slate-400 border-slate-700';
    const isPositive = trend === 'UP';
    const isGood = isInverseGood ? !isPositive : isPositive;
    return isGood 
      ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' 
      : 'bg-rose-500/10 text-rose-400 border-rose-500/20';
  };

  const metricIconMap: Record<string, React.ReactNode> = {
    totalCommits: <GitCommit className="w-4 h-4 text-indigo-400" />,
    totalChurn: <Activity className="w-4 h-4 text-sky-400" />,
    additions: <ArrowUpRight className="w-4 h-4 text-emerald-400" />,
    deletions: <ArrowDownRight className="w-4 h-4 text-rose-400" />,
    activeContributors: <Users className="w-4 h-4 text-purple-400" />,
    filesModified: <FileText className="w-4 h-4 text-amber-400" />,
    avgCommitSize: <Layers className="w-4 h-4 text-blue-400" />
  };

  return (
    <div className="space-y-6">
      {/* Header and Period Selector */}
      <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <GitCompare className="w-5 h-5 text-indigo-400" />
              <h2 className="text-xl font-bold text-white tracking-tight">Period Comparison & "What Changed?"</h2>
            </div>
            <p className="text-slate-400 text-sm">
              Evaluate software engineering velocity, churn patterns, and contributor momentum across equivalent time windows.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-400 font-medium mr-1 flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5" />
              Window:
            </span>
            {[7, 14, 30, 60, 90].map(d => (
              <button
                key={d}
                onClick={() => handleDaysChange(d)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  days === d
                    ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30'
                    : 'bg-slate-800/80 text-slate-400 hover:text-slate-200 hover:bg-slate-800'
                }`}
              >
                {d}d
              </button>
            ))}
            <button
              onClick={() => loadData(days)}
              className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white rounded-lg transition-colors ml-1"
              title="Refresh"
            >
              <RefreshCw className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Date Ranges Sub-banner */}
        {comparison && (
          <div className="mt-5 pt-4 border-t border-slate-800/80 grid grid-cols-1 md:grid-cols-2 gap-4 text-xs font-mono">
            <div className="flex items-center gap-2 text-indigo-300 bg-indigo-950/20 px-3 py-2 rounded-lg border border-indigo-500/20">
              <span className="w-2 h-2 rounded-full bg-indigo-400"></span>
              <span className="font-semibold text-slate-300">Current Window ({days}d):</span>
              <span>{formatDate(comparison.currentPeriodStart)} → {formatDate(comparison.currentPeriodEnd)}</span>
            </div>
            <div className="flex items-center gap-2 text-slate-400 bg-slate-950/40 px-3 py-2 rounded-lg border border-slate-800">
              <span className="w-2 h-2 rounded-full bg-slate-500"></span>
              <span className="font-semibold text-slate-300">Previous Window ({days}d):</span>
              <span>{formatDate(comparison.previousPeriodStart)} → {formatDate(comparison.previousPeriodEnd)}</span>
            </div>
          </div>
        )}
      </div>

      {loading ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] space-y-4">
          <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
          <p className="text-slate-400 text-sm font-medium">Computing historical delta metrics & synthesising changes...</p>
        </div>
      ) : error ? (
        <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-6 text-center text-red-400">
          <p className="font-semibold text-lg mb-2">Error Loading Period Comparison</p>
          <p className="text-sm text-red-300/80 mb-4">{error}</p>
          <button 
            onClick={() => loadData(days)}
            className="px-4 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-300 rounded-lg text-sm font-medium"
          >
            Retry
          </button>
        </div>
      ) : (
        <>
          {/* "What Changed?" Executive Summary */}
          {whatChanged && (
            <div className="bg-gradient-to-br from-slate-900/90 via-slate-900 to-indigo-950/30 border border-slate-800 rounded-2xl p-6">
              <div className="flex items-center gap-2.5 mb-4">
                <div className="p-2 bg-indigo-500/10 rounded-xl border border-indigo-500/20">
                  <Sparkles className="w-5 h-5 text-indigo-400" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white tracking-tight">
                    What Changed in {whatChanged.periodLabel}?
                  </h3>
                  <p className="text-xs text-slate-400">
                    Deterministic engineering highlights derived from period-over-period commit and file analysis.
                  </p>
                </div>
              </div>

              {/* Highlights Cards */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 mb-4">
                {whatChanged.highlights.map((highlight, idx) => (
                  <div 
                    key={idx} 
                    className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3.5 flex items-start gap-3 hover:border-slate-700 transition-colors"
                  >
                    <CheckCircle className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" />
                    <p className="text-xs text-slate-200 leading-relaxed font-sans">{highlight}</p>
                  </div>
                ))}
              </div>

              {/* Detailed Breakdown Accordion / List */}
              {whatChanged.detailedChanges.length > 0 && (
                <div className="bg-slate-950/40 border border-slate-800/60 rounded-xl p-4">
                  <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Detailed Signal Log ({whatChanged.detailedChanges.length} events)
                  </p>
                  <ul className="space-y-1.5 text-xs text-slate-300 font-sans">
                    {whatChanged.detailedChanges.map((change, idx) => (
                      <li key={idx} className="flex items-center gap-2">
                        <span className="w-1.5 h-1.5 rounded-full bg-slate-500 shrink-0" />
                        <span>{change}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}

          {/* Metric Deltas Grid */}
          {comparison && comparison.metrics && (
            <div>
              <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-400 mb-3">
                Period-over-Period Metric Deltas
              </h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {Object.entries(comparison.metrics).map(([key, delta]: [string, MetricDelta]) => {
                  const isInverse = key === 'deletions' || key === 'staleBranches';
                  const badgeClass = getDeltaBadgeClass(delta.trend, isInverse);

                  return (
                    <div 
                      key={key}
                      className="bg-slate-900/70 border border-slate-800/80 rounded-xl p-5 hover:border-slate-700 transition-all flex flex-col justify-between"
                    >
                      <div>
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-xs font-medium text-slate-400 capitalize">
                            {delta.metricName.replace(/([A-Z])/g, ' $1').trim()}
                          </span>
                          <div className="p-1.5 bg-slate-800/60 rounded-lg">
                            {metricIconMap[key] || <Activity className="w-3.5 h-3.5 text-slate-400" />}
                          </div>
                        </div>

                        <div className="flex items-baseline gap-2">
                          <span className="text-2xl font-bold text-white font-mono">
                            {delta.currentValue.toLocaleString()}
                          </span>
                          <span className="text-xs text-slate-500 font-sans">
                            {delta.unit}
                          </span>
                        </div>
                      </div>

                      <div className="mt-4 pt-3 border-t border-slate-800/70 flex items-center justify-between text-xs">
                        <span className="text-slate-500">
                          Prev: <span className="font-mono text-slate-400">{delta.previousValue.toLocaleString()}</span>
                        </span>

                        <span className={`inline-flex items-center gap-0.5 px-2 py-0.5 rounded-md font-mono text-[11px] font-semibold border ${badgeClass}`}>
                          {renderTrendIcon(delta.trend, delta.percentageChange)}
                          {delta.percentageChange > 0 ? `+${delta.percentageChange.toFixed(1)}%` : `${delta.percentageChange.toFixed(1)}%`}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};
