import React, { useState } from 'react';
import { Repository } from '../types/api';
import {
  FolderGit2,
  RefreshCw,
  PlusCircle,
  FileDown,
  GitBranch,
  ShieldAlert,
  ChevronDown
} from 'lucide-react';
import { api } from '../services/api';

interface HeaderProps {
  repositories: Repository[];
  selectedRepo: Repository | null;
  onSelectRepo: (repo: Repository) => void;
  onOpenRegister: () => void;
  onRefresh: () => void;
  isRefreshing: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  repositories,
  selectedRepo,
  onSelectRepo,
  onOpenRegister,
  onRefresh,
  isRefreshing,
}) => {
  const [showExports, setShowExports] = useState(false);

  return (
    <header className="sticky top-0 z-30 bg-[#0d1424]/90 backdrop-blur-md border-b border-slate-800/80 px-6 py-3.5 flex items-center justify-between">
      {/* Brand Identity */}
      <div className="flex items-center space-x-3.5">
        <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-sky-500/20 ring-1 ring-white/20">
          <FolderGit2 className="h-5 w-5 text-white" />
        </div>
        <div>
          <div className="flex items-center space-x-2">
            <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
              RepoVision
            </span>
            <span className="text-[10px] font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded bg-sky-500/10 text-sky-400 border border-sky-500/20">
              v1.0
            </span>
          </div>
          <p className="text-xs text-slate-400 font-medium hidden sm:block">
            Git tells you what happened. RepoVision tells you what it means.
          </p>
        </div>
      </div>

      {/* Repository Selector & Action Controls */}
      <div className="flex items-center space-x-3">
        {/* Repo Switcher Dropdown */}
        <div className="relative">
          <select
            className="bg-slate-900 border border-slate-700/80 text-slate-200 text-xs font-medium rounded-lg px-3 py-2 pr-8 focus:outline-none focus:ring-2 focus:ring-sky-500 cursor-pointer appearance-none shadow-sm"
            value={selectedRepo?.id || ''}
            onChange={(e) => {
              const repo = repositories.find((r) => r.id === e.target.value);
              if (repo) onSelectRepo(repo);
            }}
          >
            {repositories.map((repo) => (
              <option key={repo.id} value={repo.id}>
                {repo.name} ({repo.defaultBranch})
              </option>
            ))}
          </select>
          <ChevronDown className="h-3.5 w-3.5 text-slate-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
        </div>

        {/* Sync Button */}
        <button
          onClick={onRefresh}
          disabled={isRefreshing || !selectedRepo}
          className="flex items-center space-x-1.5 bg-slate-800 hover:bg-slate-700/80 text-slate-200 text-xs font-semibold px-3 py-2 rounded-lg border border-slate-700/80 transition-all shadow-sm active:scale-95 disabled:opacity-50"
          title="Re-analyze Git repository and sync facts"
        >
          <RefreshCw className={`h-3.5 w-3.5 text-sky-400 ${isRefreshing ? 'animate-spin' : ''}`} />
          <span className="hidden md:inline">Sync Facts</span>
        </button>

        {/* Register Repo Button */}
        <button
          onClick={onOpenRegister}
          className="flex items-center space-x-1.5 bg-sky-600 hover:bg-sky-500 text-white text-xs font-semibold px-3 py-2 rounded-lg shadow-sm shadow-sky-600/30 transition-all active:scale-95"
        >
          <PlusCircle className="h-3.5 w-3.5" />
          <span>New Repo</span>
        </button>

        {/* Export Dropdown */}
        {selectedRepo && (
          <div className="relative">
            <button
              onClick={() => setShowExports(!showExports)}
              className="flex items-center space-x-1.5 bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-300 border border-indigo-500/30 text-xs font-semibold px-3 py-2 rounded-lg transition-all active:scale-95"
            >
              <FileDown className="h-3.5 w-3.5" />
              <span>Export</span>
              <ChevronDown className="h-3 w-3" />
            </button>

            {showExports && (
              <div
                className="absolute right-0 mt-2 w-48 bg-slate-900 border border-slate-700 rounded-xl shadow-2xl py-1 z-50 text-xs font-medium"
                onMouseLeave={() => setShowExports(false)}
              >
                <div className="px-3 py-1.5 text-[11px] font-semibold uppercase tracking-wider text-slate-400 border-b border-slate-800">
                  Reports
                </div>
                <a
                  href={api.getPdfUrl(selectedRepo.id)}
                  download
                  className="flex items-center px-3 py-2 hover:bg-slate-800/80 text-slate-200 transition-colors"
                >
                  <span className="h-2 w-2 rounded-full bg-rose-500 mr-2" />
                  Health Report (PDF)
                </a>
                <div className="px-3 py-1.5 text-[11px] font-semibold uppercase tracking-wider text-slate-400 border-t border-slate-800">
                  Data Extracts (CSV)
                </div>
                <a
                  href={api.getCsvUrl(selectedRepo.id, 'commits')}
                  download
                  className="block px-3 py-1.5 hover:bg-slate-800/80 text-slate-300"
                >
                  Commits Log CSV
                </a>
                <a
                  href={api.getCsvUrl(selectedRepo.id, 'files')}
                  download
                  className="block px-3 py-1.5 hover:bg-slate-800/80 text-slate-300"
                >
                  Files & Hotspots CSV
                </a>
                <a
                  href={api.getCsvUrl(selectedRepo.id, 'developers')}
                  download
                  className="block px-3 py-1.5 hover:bg-slate-800/80 text-slate-300"
                >
                  Developers CSV
                </a>
                <a
                  href={api.getCsvUrl(selectedRepo.id, 'risks')}
                  download
                  className="block px-3 py-1.5 hover:bg-slate-800/80 text-slate-300"
                >
                  Risk Engine CSV
                </a>
              </div>
            )}
          </div>
        )}

        {/* Creator & GitHub Link */}
        <a
          href="https://github.com/vasanthan-hub/RepoVision"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center space-x-1.5 bg-slate-800/80 hover:bg-slate-700/80 text-slate-300 hover:text-white border border-slate-700/80 text-xs font-semibold px-2.5 py-2 rounded-lg transition-all active:scale-95 shadow-sm"
          title="Created by Vasanthan R (@vasanthan-hub)"
        >
          <svg className="h-3.5 w-3.5 fill-current text-slate-400 group-hover:text-white" viewBox="0 0 24 24">
            <path fillRule="evenodd" clipRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" />
          </svg>
          <span className="hidden lg:inline text-slate-300">Vasanthan R</span>
        </a>
      </div>
    </header>
  );
};
