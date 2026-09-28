import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { InsightReport, InsightItem } from '../types/api';
import { 
  Lightbulb, 
  Layers, 
  Users, 
  Sparkles, 
  Gauge, 
  AlertTriangle, 
  AlertCircle, 
  CheckCircle2, 
  Info,
  Search,
  ArrowRight,
  TrendingUp,
  ShieldCheck,
  RefreshCw
} from 'lucide-react';

interface InsightsViewProps {
  repositoryId: string;
}

export const InsightsView: React.FC<InsightsViewProps> = ({ repositoryId }) => {
  const [data, setData] = useState<InsightReport | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [selectedType, setSelectedType] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await api.getInsights(repositoryId);
      setData(res);
    } catch (err: any) {
      setError(err.message || 'Failed to load insights engine data');
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
        <p className="text-slate-400 text-sm font-medium">Running Insight Engine (Data → Metric → Interpretation → Action)...</p>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-6 text-center text-red-400">
        <p className="font-semibold text-lg mb-2">Error Loading Insights</p>
        <p className="text-sm text-red-300/80 mb-4">{error}</p>
        <button 
          onClick={loadData}
          className="px-4 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-300 rounded-lg text-sm font-medium transition-colors"
        >
          Try Again
        </button>
      </div>
    );
  }

  const categoryIcons: Record<string, React.ReactNode> = {
    ARCHITECTURE: <Layers className="w-4 h-4 text-purple-400" />,
    COLLABORATION: <Users className="w-4 h-4 text-sky-400" />,
    HYGIENE: <Sparkles className="w-4 h-4 text-emerald-400" />,
    VELOCITY: <Gauge className="w-4 h-4 text-amber-400" />
  };

  const typeConfig: Record<string, { badge: string; icon: React.ReactNode; border: string }> = {
    POSITIVE: {
      badge: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
      icon: <CheckCircle2 className="w-4 h-4 text-emerald-400" />,
      border: 'border-l-emerald-500'
    },
    WARNING: {
      badge: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
      icon: <AlertTriangle className="w-4 h-4 text-amber-400" />,
      border: 'border-l-amber-500'
    },
    CRITICAL: {
      badge: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
      icon: <AlertCircle className="w-4 h-4 text-rose-400" />,
      border: 'border-l-rose-500'
    },
    INFO: {
      badge: 'bg-sky-500/10 text-sky-400 border-sky-500/20',
      icon: <Info className="w-4 h-4 text-sky-400" />,
      border: 'border-l-sky-500'
    }
  };

  const filteredInsights = data.insights.filter(item => {
    if (selectedCategory !== 'ALL' && item.category !== selectedCategory) return false;
    if (selectedType !== 'ALL' && item.type !== selectedType) return false;
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const matchTitle = item.title.toLowerCase().includes(q);
      const matchFinding = item.finding.toLowerCase().includes(q);
      const matchAction = item.action.toLowerCase().includes(q);
      const matchInterp = item.interpretation.toLowerCase().includes(q);
      return matchTitle || matchFinding || matchAction || matchInterp;
    }
    return true;
  });

  const criticalCount = data.insights.filter(i => i.type === 'CRITICAL').length;
  const warningCount = data.insights.filter(i => i.type === 'WARNING').length;
  const positiveCount = data.insights.filter(i => i.type === 'POSITIVE').length;
  const infoCount = data.insights.filter(i => i.type === 'INFO').length;

  return (
    <div className="space-y-6">
      {/* Top Banner / Philosophy */}
      <div className="bg-gradient-to-r from-indigo-950/40 via-purple-950/20 to-slate-900 border border-indigo-500/20 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Lightbulb className="w-5 h-5 text-amber-400" />
              <h2 className="text-xl font-bold text-white tracking-tight">Engineering Intelligence & Insight Engine</h2>
            </div>
            <p className="text-slate-400 text-sm max-w-2xl">
              Transforming raw repository facts into explainable, structured engineering guidance via the deterministic model:
              <span className="text-indigo-300 font-mono text-xs ml-1">Data → Metric → Interpretation → Action</span>.
            </p>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={loadData}
              className="flex items-center gap-2 px-3 py-2 bg-slate-800/80 hover:bg-slate-700/80 text-slate-300 text-xs font-medium rounded-lg border border-slate-700/50 transition-colors"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              Re-run Engine
            </button>
          </div>
        </div>
      </div>

      {/* Summary KPI Badges */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-medium">Critical Action Items</p>
            <p className="text-2xl font-bold text-rose-400 mt-1">{criticalCount}</p>
          </div>
          <div className="p-2.5 bg-rose-500/10 rounded-xl border border-rose-500/20">
            <AlertCircle className="w-5 h-5 text-rose-400" />
          </div>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-medium">Warnings & Hygiene</p>
            <p className="text-2xl font-bold text-amber-400 mt-1">{warningCount}</p>
          </div>
          <div className="p-2.5 bg-amber-500/10 rounded-xl border border-amber-500/20">
            <AlertTriangle className="w-5 h-5 text-amber-400" />
          </div>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-medium">Positive Signals</p>
            <p className="text-2xl font-bold text-emerald-400 mt-1">{positiveCount}</p>
          </div>
          <div className="p-2.5 bg-emerald-500/10 rounded-xl border border-emerald-500/20">
            <CheckCircle2 className="w-5 h-5 text-emerald-400" />
          </div>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-medium">Informational</p>
            <p className="text-2xl font-bold text-sky-400 mt-1">{infoCount}</p>
          </div>
          <div className="p-2.5 bg-sky-500/10 rounded-xl border border-sky-500/20">
            <Info className="w-5 h-5 text-sky-400" />
          </div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-4 flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4">
        {/* Category tabs */}
        <div className="flex flex-wrap items-center gap-1.5">
          <button
            onClick={() => setSelectedCategory('ALL')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              selectedCategory === 'ALL'
                ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30'
                : 'bg-slate-800/60 text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            All Categories ({data.insights.length})
          </button>
          {['ARCHITECTURE', 'COLLABORATION', 'HYGIENE', 'VELOCITY'].map(cat => {
            const count = data.insights.filter(i => i.category === cat).length;
            return (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                  selectedCategory === cat
                    ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30'
                    : 'bg-slate-800/60 text-slate-400 hover:text-slate-200 hover:bg-slate-800'
                }`}
              >
                {categoryIcons[cat]}
                <span className="capitalize">{cat.toLowerCase()}</span>
                <span className="text-[10px] opacity-75">({count})</span>
              </button>
            );
          })}
        </div>

        {/* Search */}
        <div className="relative min-w-[240px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search insights & actions..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            className="w-full bg-slate-950/60 border border-slate-700/60 rounded-lg pl-9 pr-3 py-1.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500"
          />
        </div>
      </div>

      {/* Insights Cards List */}
      <div className="space-y-4">
        {filteredInsights.length === 0 ? (
          <div className="bg-slate-900/40 border border-slate-800 rounded-xl p-12 text-center">
            <ShieldCheck className="w-10 h-10 text-slate-600 mx-auto mb-3" />
            <p className="text-slate-300 font-medium text-base mb-1">No Insights Found</p>
            <p className="text-slate-500 text-xs">No repository insights match the selected filter criteria.</p>
          </div>
        ) : (
          filteredInsights.map(insight => {
            const typeInfo = typeConfig[insight.type] || typeConfig['INFO'];
            return (
              <div
                key={insight.id}
                className={`bg-slate-900/80 border border-slate-800/90 rounded-xl p-5 hover:border-slate-700 transition-all border-l-4 ${typeInfo.border}`}
              >
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
                  <div className="flex items-center gap-2.5">
                    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold border ${typeInfo.badge}`}>
                      {typeInfo.icon}
                      {insight.type}
                    </span>
                    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium bg-slate-800/80 text-slate-300 border border-slate-700/50">
                      {categoryIcons[insight.category]}
                      {insight.category}
                    </span>
                    <h3 className="text-sm font-semibold text-white tracking-wide ml-1">{insight.title}</h3>
                  </div>
                </div>

                {/* 3-Stage Explainable Model */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {/* Finding */}
                  <div className="bg-slate-950/50 border border-slate-800/60 rounded-lg p-3.5 flex flex-col justify-between">
                    <div>
                      <div className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-1.5">
                        <span className="w-1.5 h-1.5 rounded-full bg-sky-400"></span>
                        1. Repository Finding
                      </div>
                      <p className="text-xs text-slate-200 leading-relaxed font-sans">{insight.finding}</p>
                    </div>
                  </div>

                  {/* Interpretation */}
                  <div className="bg-slate-950/50 border border-slate-800/60 rounded-lg p-3.5 flex flex-col justify-between">
                    <div>
                      <div className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-wider text-purple-400 mb-1.5">
                        <span className="w-1.5 h-1.5 rounded-full bg-purple-400"></span>
                        2. Engineering Interpretation
                      </div>
                      <p className="text-xs text-slate-300 leading-relaxed font-sans">{insight.interpretation}</p>
                    </div>
                  </div>

                  {/* Action */}
                  <div className="bg-indigo-950/20 border border-indigo-500/20 rounded-lg p-3.5 flex flex-col justify-between">
                    <div>
                      <div className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-wider text-indigo-300 mb-1.5">
                        <ArrowRight className="w-3 h-3 text-indigo-400" />
                        3. Recommended Action
                      </div>
                      <p className="text-xs text-indigo-100 font-medium leading-relaxed font-sans">{insight.action}</p>
                    </div>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
