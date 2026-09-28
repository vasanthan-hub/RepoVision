import React from 'react';
import {
  LayoutDashboard,
  GitCommit,
  Users,
  FileCode2,
  GitBranch,
  UserCheck,
  Network,
  AlertTriangle,
  Activity,
  Lightbulb,
  ArrowLeftRight,
  Tag,
  ExternalLink
} from 'lucide-react';

export type ActiveTab =
  | 'overview'
  | 'commits'
  | 'developers'
  | 'files'
  | 'branches'
  | 'ownership'
  | 'coupling'
  | 'risks'
  | 'health'
  | 'insights'
  | 'comparison'
  | 'releases';

interface SidebarProps {
  activeTab: ActiveTab;
  onTabChange: (tab: ActiveTab) => void;
  riskCount?: number;
  healthScore?: number;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab,
  onTabChange,
  riskCount,
  healthScore,
}) => {
  const navItems: { id: ActiveTab; label: string; icon: React.ReactNode; badge?: string | number; badgeColor?: string }[] = [
    {
      id: 'overview',
      label: 'Overview',
      icon: <LayoutDashboard className="h-4 w-4" />,
    },
    {
      id: 'commits',
      label: 'Commits',
      icon: <GitCommit className="h-4 w-4" />,
    },
    {
      id: 'developers',
      label: 'Developers',
      icon: <Users className="h-4 w-4" />,
    },
    {
      id: 'files',
      label: 'Files & Hotspots',
      icon: <FileCode2 className="h-4 w-4" />,
    },
    {
      id: 'branches',
      label: 'Branches',
      icon: <GitBranch className="h-4 w-4" />,
    },
    {
      id: 'ownership',
      label: 'Ownership & Bus Factor',
      icon: <UserCheck className="h-4 w-4" />,
    },
    {
      id: 'coupling',
      label: 'Change Coupling',
      icon: <Network className="h-4 w-4" />,
    },
    {
      id: 'risks',
      label: 'Risk Engine',
      icon: <AlertTriangle className="h-4 w-4 text-amber-400" />,
      badge: riskCount !== undefined && riskCount > 0 ? riskCount : undefined,
      badgeColor: 'bg-rose-500/20 text-rose-300 border-rose-500/30',
    },
    {
      id: 'health',
      label: 'Health Score',
      icon: <Activity className="h-4 w-4 text-emerald-400" />,
      badge: healthScore !== undefined ? `${healthScore}` : undefined,
      badgeColor: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30',
    },
    {
      id: 'insights',
      label: 'Insights & Actions',
      icon: <Lightbulb className="h-4 w-4 text-sky-400" />,
    },
    {
      id: 'comparison',
      label: 'What Changed',
      icon: <ArrowLeftRight className="h-4 w-4" />,
    },
    {
      id: 'releases',
      label: 'Releases & Tags',
      icon: <Tag className="h-4 w-4" />,
    },
  ];

  return (
    <aside className="w-64 bg-[#0a0f1d] border-r border-slate-800/80 flex flex-col justify-between py-5 shrink-0 select-none">
      <div className="space-y-1 px-3">
        <div className="px-3 pb-2 text-[10px] font-bold uppercase tracking-wider text-slate-400">
          Analytics & Intelligence
        </div>
        {navItems.map((item) => {
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onTabChange(item.id)}
              className={`w-full flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold transition-all group ${
                isActive
                  ? 'bg-sky-500/10 text-sky-400 border border-sky-500/20 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40 border border-transparent'
              }`}
            >
              <div className="flex items-center space-x-2.5">
                <span className={`${isActive ? 'text-sky-400' : 'text-slate-400 group-hover:text-slate-200'}`}>
                  {item.icon}
                </span>
                <span>{item.label}</span>
              </div>
              {item.badge && (
                <span className={`text-[10px] font-mono font-bold px-1.5 py-0.5 rounded border ${item.badgeColor}`}>
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {/* Philosophy banner & Creator Info */}
      <div className="px-4 pt-3 border-t border-slate-800/80 mx-2 space-y-2.5">
        <div className="p-2.5 rounded-xl bg-slate-900/60 border border-slate-800 text-[11px] text-slate-400 leading-relaxed">
          <p className="font-semibold text-slate-300 mb-0.5">Explainable Signals</p>
          Deterministic metrics computed directly from Git facts.
        </div>

        {/* Creator Attribution */}
        <a
          href="https://github.com/vasanthan-hub/RepoVision"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center justify-between p-2.5 rounded-xl bg-gradient-to-r from-slate-900/80 to-indigo-950/40 border border-slate-800 hover:border-sky-500/40 transition-all group"
        >
          <div className="flex items-center space-x-2">
            <div className="h-6 w-6 rounded-full bg-gradient-to-tr from-sky-500 to-indigo-500 flex items-center justify-center text-[10px] font-bold text-white shadow-sm ring-1 ring-white/20">
              VR
            </div>
            <div>
              <p className="text-[11px] font-semibold text-slate-200 group-hover:text-sky-300 transition-colors">
                Vasanthan R
              </p>
              <p className="text-[9px] text-slate-400">@vasanthan-hub</p>
            </div>
          </div>
          <ExternalLink className="h-3 w-3 text-slate-400 group-hover:text-sky-400 transition-colors" />
        </a>
      </div>
    </aside>
  );
};
