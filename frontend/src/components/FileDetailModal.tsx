import React from 'react';
import { X, FileCode2, GitCommit, Users, Flame, Calendar } from 'lucide-react';
import { FileDetail } from '../types/api';

interface FileDetailModalProps {
  file: FileDetail | null;
  onClose: () => void;
}

export const FileDetailModal: React.FC<FileDetailModalProps> = ({ file, onClose }) => {
  if (!file) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div className="bg-[#0f172a] border border-slate-800 rounded-2xl w-full max-w-3xl max-h-[85vh] shadow-2xl flex flex-col overflow-hidden">
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between shrink-0">
          <div className="flex items-center space-x-3 overflow-hidden">
            <div className="p-2 rounded-lg bg-sky-500/10 text-sky-400">
              <FileCode2 className="h-5 w-5" />
            </div>
            <div className="truncate">
              <h3 className="font-bold text-slate-100 text-sm truncate font-mono">{file.filePath}</h3>
              <p className="text-xs text-slate-400">Deep Component Analytics & Contribution Breakdown</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-200 p-1.5 rounded-lg hover:bg-slate-800 transition-colors shrink-0"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 overflow-y-auto space-y-6 text-xs">
          {/* Top Metric Strip */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div className="bg-slate-900/80 p-3 rounded-xl border border-slate-800">
              <div className="text-slate-400 text-[11px] mb-1">Hotspot Score</div>
              <div className="flex items-center space-x-1.5">
                <Flame className={`h-4 w-4 ${file.hotspotScore >= 70 ? 'text-rose-400' : 'text-amber-400'}`} />
                <span className="text-base font-extrabold text-slate-100 font-mono">{file.hotspotScore}</span>
                <span className="text-[10px] text-slate-400">/100</span>
              </div>
            </div>

            <div className="bg-slate-900/80 p-3 rounded-xl border border-slate-800">
              <div className="text-slate-400 text-[11px] mb-1">Total Churn</div>
              <div className="text-base font-extrabold text-slate-100 font-mono">
                {file.churn.toLocaleString()} <span className="text-[10px] font-normal text-slate-400">lines</span>
              </div>
              <div className="text-[10px] text-emerald-400">
                +{file.additions} <span className="text-rose-400">-{file.deletions}</span>
              </div>
            </div>

            <div className="bg-slate-900/80 p-3 rounded-xl border border-slate-800">
              <div className="text-slate-400 text-[11px] mb-1">Modifications</div>
              <div className="text-base font-extrabold text-slate-100 font-mono">
                {file.modificationCount} <span className="text-[10px] font-normal text-slate-400">commits</span>
              </div>
              <div className="text-[10px] text-slate-400">Avg {file.averageChangeSize} lines/edit</div>
            </div>

            <div className="bg-slate-900/80 p-3 rounded-xl border border-slate-800">
              <div className="text-slate-400 text-[11px] mb-1">Contributors</div>
              <div className="flex items-center space-x-1.5">
                <Users className="h-4 w-4 text-sky-400" />
                <span className="text-base font-extrabold text-slate-100 font-mono">{file.contributorCount}</span>
              </div>
              <div className="text-[10px] text-slate-400">
                {file.contributorCount === 1 ? 'Single author' : 'Collaborative'}
              </div>
            </div>
          </div>

          {/* Contributors Distribution */}
          <div>
            <h4 className="font-bold text-slate-200 mb-3 flex items-center space-x-2">
              <Users className="h-3.5 w-3.5 text-sky-400" />
              <span>Authorship & Ownership Breakdown</span>
            </h4>
            <div className="space-y-2">
              {file.contributors.map((c) => (
                <div key={c.email} className="bg-slate-900/60 p-3 rounded-xl border border-slate-800/80">
                  <div className="flex justify-between items-center mb-1.5">
                    <div>
                      <span className="font-semibold text-slate-200">{c.name}</span>
                      <span className="text-slate-400 text-[11px] ml-2">({c.email})</span>
                    </div>
                    <div className="font-mono text-xs font-bold text-sky-400">{c.percentage}%</div>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                    <div className="bg-sky-500 h-1.5 rounded-full" style={{ width: `${c.percentage}%` }} />
                  </div>
                  <div className="flex justify-between text-[10px] text-slate-400 mt-1">
                    <span>{c.commitsCount} commit(s)</span>
                    <span>{c.linesChanged.toLocaleString()} lines churn</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Recent Commits Touching this file */}
          <div>
            <h4 className="font-bold text-slate-200 mb-3 flex items-center space-x-2">
              <GitCommit className="h-3.5 w-3.5 text-indigo-400" />
              <span>Recent Commit History ({file.recentCommits.length})</span>
            </h4>
            <div className="space-y-2">
              {file.recentCommits.map((c) => (
                <div key={c.hash} className="bg-slate-900/60 p-3 rounded-xl border border-slate-800/80 flex items-center justify-between">
                  <div className="space-y-0.5 truncate pr-3">
                    <div className="flex items-center space-x-2">
                      <span className="font-mono text-[11px] text-sky-400 font-bold">{c.hash.substring(0, 7)}</span>
                      <span className="text-slate-200 font-medium truncate">{c.subject}</span>
                    </div>
                    <div className="text-[10px] text-slate-400">
                      by <span className="text-slate-300 font-medium">{c.authorName}</span> on {new Date(c.authoredAt).toLocaleDateString()}
                    </div>
                  </div>
                  <div className="text-right shrink-0 font-mono text-[11px]">
                    <span className="text-emerald-400">+{c.additions}</span>{' '}
                    <span className="text-rose-400">-{c.deletions}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
