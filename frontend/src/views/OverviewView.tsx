import React from 'react';
import {
  RepositoryOverview,
  FileMetrics
} from '../types/api';
import {
  Activity,
  GitCommit,
  Users,
  FileCode2,
  GitBranch,
  Flame,
  AlertTriangle,
  Lightbulb,
  ArrowRight,
  TrendingUp
} from 'lucide-react';
import { ActiveTab } from '../components/Sidebar';

interface OverviewViewProps {
  overview: RepositoryOverview;
  onNavigate: (tab: ActiveTab) => void;
  onSelectFile: (filePath: string) => void;
}

export const OverviewView: React.FC<OverviewViewProps> = ({
  overview,
  onNavigate,
  onSelectFile,
}) => {
  const { repository, health, risks, insights, topHotspots, recentActivity } = overview;

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'EXCELLENT':
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
      case 'GOOD':
        return 'text-sky-400 bg-sky-500/10 border-sky-500/30';
      case 'NEEDS_ATTENTION':
        return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
      default:
        return 'text-rose-400 bg-rose-500/10 border-rose-500/30';
    }
  };

  return (
    <div className="space-y-6">
      {/* Health Score Hero Card */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-slate-900 via-slate-900 to-indigo-950/40 border border-slate-800 p-6 shadow-xl">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2 max-w-2xl">
            <div className="flex items-center space-x-2.5">
              <span className="text-xs font-bold uppercase tracking-wider text-sky-400">
                Repository Health Overview
              </span>
              <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${getStatusColor(health.status)}`}>
                {health.status}
              </span>
            </div>
            <h1 className="text-2xl font-black tracking-tight text-white m-0">
              {repository.name}
            </h1>
            <p className="text-xs text-slate-300 leading-relaxed font-normal">
              {health.summary}
            </p>
            <div className="flex items-center space-x-4 pt-1 text-[11px] text-slate-400 font-mono">
              <span>Trunk: <span className="text-slate-200">{repository.defaultBranch}</span></span>
              <span>•</span>
              <span>Local: <span className="text-slate-200">{repository.localPath}</span></span>
            </div>
          </div>

          {/* Overall Health Score Meter */}
          <div className="flex items-center gap-5 self-start md:self-auto bg-slate-950/60 p-4 rounded-2xl border border-slate-800/80">
            <div className="relative h-20 w-20 flex items-center justify-center">
              <svg className="h-full w-full -rotate-90" viewBox="0 0 36 36">
                <path
                  className="text-slate-800"
                  strokeWidth="3.5"
                  stroke="currentColor"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
                <path
                  className={`${health.overallScore >= 70 ? 'text-sky-400' : 'text-amber-400'}`}
                  strokeDasharray={`${health.overallScore}, 100`}
                  strokeWidth="3.5"
                  strokeLinecap="round"
                  stroke="currentColor"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
                <span className="text-2xl font-black text-white font-mono tracking-tight">{health.overallScore}</span>
                <span className="text-[9px] uppercase tracking-wider font-semibold text-slate-400">Health</span>
              </div>
            </div>

            <div className="space-y-1 text-xs">
              <div className="flex justify-between gap-4 text-slate-400 text-[11px]">
                <span>Activity:</span>
                <span className="font-mono text-slate-200 font-semibold">{health.activityScore}</span>
              </div>
              <div className="flex justify-between gap-4 text-slate-400 text-[11px]">
                <span>Stability:</span>
                <span className="font-mono text-slate-200 font-semibold">{health.stabilityScore}</span>
              </div>
              <div className="flex justify-between gap-4 text-slate-400 text-[11px]">
                <span>Branching:</span>
                <span className="font-mono text-slate-200 font-semibold">{health.branchHealthScore}</span>
              </div>
              <div className="flex justify-between gap-4 text-slate-400 text-[11px]">
                <span>Ownership:</span>
                <span className="font-mono text-slate-200 font-semibold">{health.ownershipScore}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Primary KPI Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-6 gap-3.5">
        <div
          onClick={() => onNavigate('commits')}
          className="bg-[#0f172a] hover:bg-[#131d36] transition-all cursor-pointer p-4 rounded-xl border border-slate-800 shadow-sm group"
        >
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold">Commits</span>
            <GitCommit className="h-4 w-4 text-sky-400 group-hover:scale-110 transition-transform" />
          </div>
          <div className="text-xl font-extrabold text-white font-mono tracking-tight">
            {overview.totalCommits.toLocaleString()}
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            {recentActivity.activeDaysCount} active days
          </p>
        </div>

        <div
          onClick={() => onNavigate('developers')}
          className="bg-[#0f172a] hover:bg-[#131d36] transition-all cursor-pointer p-4 rounded-xl border border-slate-800 shadow-sm group"
        >
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold">Developers</span>
            <Users className="h-4 w-4 text-indigo-400 group-hover:scale-110 transition-transform" />
          </div>
          <div className="text-xl font-extrabold text-white font-mono tracking-tight">
            {overview.totalDevelopers}
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            Active contributors
          </p>
        </div>

        <div
          onClick={() => onNavigate('files')}
          className="bg-[#0f172a] hover:bg-[#131d36] transition-all cursor-pointer p-4 rounded-xl border border-slate-800 shadow-sm group"
        >
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold">Files</span>
            <FileCode2 className="h-4 w-4 text-cyan-400 group-hover:scale-110 transition-transform" />
          </div>
          <div className="text-xl font-extrabold text-white font-mono tracking-tight">
            {overview.totalFiles}
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            Tracked in Git
          </p>
        </div>

        <div
          onClick={() => onNavigate('branches')}
          className="bg-[#0f172a] hover:bg-[#131d36] transition-all cursor-pointer p-4 rounded-xl border border-slate-800 shadow-sm group"
        >
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold">Branches</span>
            <GitBranch className="h-4 w-4 text-emerald-400 group-hover:scale-110 transition-transform" />
          </div>
          <div className="text-xl font-extrabold text-white font-mono tracking-tight">
            {overview.totalBranches}
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            <span className="text-emerald-400">{overview.activeBranches} active</span>
            {overview.staleBranches > 0 && <span className="text-amber-400 ml-1.5">• {overview.staleBranches} stale</span>}
          </p>
        </div>

        <div
          onClick={() => onNavigate('commits')}
          className="bg-[#0f172a] hover:bg-[#131d36] transition-all cursor-pointer p-4 rounded-xl border border-slate-800 shadow-sm group"
        >
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold">Total Churn</span>
            <TrendingUp className="h-4 w-4 text-amber-400 group-hover:scale-110 transition-transform" />
          </div>
          <div className="text-xl font-extrabold text-white font-mono tracking-tight">
            {overview.totalChurn.toLocaleString()}
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            Lines touched
          </p>
        </div>

        <div
          onClick={() => onNavigate('risks')}
          className="bg-[#0f172a] hover:bg-[#131d36] transition-all cursor-pointer p-4 rounded-xl border border-slate-800 shadow-sm group"
        >
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold">Active Risks</span>
            <AlertTriangle className="h-4 w-4 text-rose-400 group-hover:scale-110 transition-transform" />
          </div>
          <div className="text-xl font-extrabold text-white font-mono tracking-tight">
            {risks.totalRisks}
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            <span className="text-rose-400 font-bold">{risks.highSeverityCount} high</span>
            <span className="text-slate-400 ml-1.5">• {risks.mediumSeverityCount} med</span>
          </p>
        </div>
      </div>

      {/* Two Column Section: Health Dimensions & Top Hotspots */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Health Dimensions Detailed Breakdown */}
        <div className="bg-[#0f172a] rounded-2xl border border-slate-800 p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-slate-100 text-sm flex items-center space-x-2">
              <Activity className="h-4 w-4 text-sky-400" />
              <span>Health Score Dimensions (0-100)</span>
            </h3>
            <button
              onClick={() => onNavigate('health')}
              className="text-xs text-sky-400 hover:text-sky-300 font-medium flex items-center space-x-1"
            >
              <span>Deep dive</span>
              <ArrowRight className="h-3 w-3" />
            </button>
          </div>

          <div className="space-y-3.5">
            {health.dimensions.map((dim) => (
              <div key={dim.name} className="space-y-1.5">
                <div className="flex justify-between items-center text-xs">
                  <div className="flex items-center space-x-2">
                    <span className="font-semibold text-slate-200">{dim.name}</span>
                    <span className="text-[10px] text-slate-400">({Math.round(dim.weight * 100)}% weight)</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <span className={`text-[10px] font-bold px-1.5 py-0.2 rounded border ${getStatusColor(dim.status)}`}>
                      {dim.status}
                    </span>
                    <span className="font-mono font-bold text-slate-100">{dim.score}</span>
                  </div>
                </div>
                <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
                  <div
                    className={`h-2 rounded-full transition-all duration-500 ${
                      dim.score >= 80 ? 'bg-emerald-500' : dim.score >= 60 ? 'bg-sky-500' : 'bg-amber-500'
                    }`}
                    style={{ width: `${dim.score}%` }}
                  />
                </div>
                <p className="text-[11px] text-slate-400">{dim.description}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Top Hotspots Preview Table */}
        <div className="bg-[#0f172a] rounded-2xl border border-slate-800 p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-slate-100 text-sm flex items-center space-x-2">
              <Flame className="h-4 w-4 text-rose-400" />
              <span>Top Architectural Hotspots</span>
            </h3>
            <button
              onClick={() => onNavigate('files')}
              className="text-xs text-sky-400 hover:text-sky-300 font-medium flex items-center space-x-1"
            >
              <span>View all files</span>
              <ArrowRight className="h-3 w-3" />
            </button>
          </div>

          <div className="divide-y divide-slate-800/80">
            {topHotspots.map((file) => (
              <div
                key={file.filePath}
                onClick={() => onSelectFile(file.filePath)}
                className="py-2.5 flex items-center justify-between hover:bg-slate-800/40 rounded-lg px-2 -mx-2 cursor-pointer transition-colors"
              >
                <div className="truncate pr-3 space-y-0.5">
                  <div className="font-mono text-xs font-semibold text-slate-200 truncate">
                    {file.filePath}
                  </div>
                  <div className="text-[11px] text-slate-400 flex items-center space-x-2">
                    <span>{file.modificationCount} modifications</span>
                    <span>•</span>
                    <span>{file.churn.toLocaleString()} lines churn</span>
                    <span>•</span>
                    <span>{file.contributorCount} author(s)</span>
                  </div>
                </div>

                <div className="text-right shrink-0">
                  <div className="flex items-center space-x-1 justify-end">
                    <Flame className={`h-3.5 w-3.5 ${file.hotspotScore >= 70 ? 'text-rose-400' : 'text-amber-400'}`} />
                    <span className="font-mono font-bold text-xs text-slate-100">{file.hotspotScore}</span>
                  </div>
                  <span className="text-[10px] text-slate-400">Score</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Critical Risks & Actionable Insights Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Identified Risks */}
        <div className="bg-[#0f172a] rounded-2xl border border-slate-800 p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-slate-100 text-sm flex items-center space-x-2">
              <AlertTriangle className="h-4 w-4 text-amber-400" />
              <span>Prioritized Engineering Risks</span>
            </h3>
            <button
              onClick={() => onNavigate('risks')}
              className="text-xs text-sky-400 hover:text-sky-300 font-medium flex items-center space-x-1"
            >
              <span>Risk console</span>
              <ArrowRight className="h-3 w-3" />
            </button>
          </div>

          <div className="space-y-3">
            {risks.risks.slice(0, 3).map((risk) => (
              <div
                key={risk.id}
                className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800/80 space-y-1.5"
              >
                <div className="flex items-center justify-between">
                  <span className="font-bold text-slate-200 text-xs">{risk.title}</span>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                    risk.severity === 'HIGH'
                      ? 'bg-rose-500/10 text-rose-300 border-rose-500/30'
                      : 'bg-amber-500/10 text-amber-300 border-amber-500/30'
                  }`}>
                    {risk.severity}
                  </span>
                </div>
                <div className="text-[11px] font-mono text-sky-400 truncate">{risk.entity}</div>
                <p className="text-[11px] text-slate-400">{risk.evidence}</p>
                <p className="text-[11px] text-slate-300 font-medium pt-1 border-t border-slate-800/60">
                  <span className="text-slate-400">Action: </span>{risk.recommendation}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* Actionable Insights */}
        <div className="bg-[#0f172a] rounded-2xl border border-slate-800 p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-slate-100 text-sm flex items-center space-x-2">
              <Lightbulb className="h-4 w-4 text-sky-400" />
              <span>Actionable Engineering Insights</span>
            </h3>
            <button
              onClick={() => onNavigate('insights')}
              className="text-xs text-sky-400 hover:text-sky-300 font-medium flex items-center space-x-1"
            >
              <span>All insights</span>
              <ArrowRight className="h-3 w-3" />
            </button>
          </div>

          <div className="space-y-3">
            {insights.slice(0, 3).map((insight) => (
              <div
                key={insight.id}
                className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800/80 space-y-1.5"
              >
                <div className="flex items-center justify-between">
                  <span className="font-bold text-slate-200 text-xs">{insight.title}</span>
                  <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded bg-indigo-500/10 text-indigo-300 border border-indigo-500/30">
                    {insight.category}
                  </span>
                </div>
                <p className="text-[11px] text-slate-400">{insight.finding}</p>
                <p className="text-[11px] text-slate-300 font-medium pt-1 border-t border-slate-800/60">
                  <span className="text-slate-400">Recommendation: </span>{insight.action}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
