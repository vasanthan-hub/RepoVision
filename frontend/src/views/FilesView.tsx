import React, { useState, useEffect } from 'react';
import { FileAnalytics, FileMetrics, FileDetail, Repository } from '../types/api';
import { api } from '../services/api';
import { FileCode2, Flame, Search, Info, ExternalLink, Users } from 'lucide-react';
import { FileDetailModal } from '../components/FileDetailModal';

interface FilesViewProps {
  repository: Repository;
  selectedFilePath?: string | null;
  onClearSelectedFilePath?: () => void;
}

export const FilesView: React.FC<FilesViewProps> = ({
  repository,
  selectedFilePath,
  onClearSelectedFilePath,
}) => {
  const [data, setData] = useState<FileAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [activeFileDetail, setActiveFileDetail] = useState<FileDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  useEffect(() => {
    loadFiles();
  }, [repository.id]);

  useEffect(() => {
    if (selectedFilePath) {
      handleOpenFileDetail(selectedFilePath);
      if (onClearSelectedFilePath) onClearSelectedFilePath();
    }
  }, [selectedFilePath]);

  const loadFiles = async () => {
    setLoading(true);
    try {
      const res = await api.getFileAnalytics(repository.id);
      setData(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenFileDetail = async (path: string) => {
    setDetailLoading(true);
    try {
      const detail = await api.getFileDetail(repository.id, path);
      setActiveFileDetail(detail);
    } catch (err) {
      console.error(err);
    } finally {
      setDetailLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-20 flex flex-col items-center justify-center space-y-3">
        <div className="h-8 w-8 border-3 border-sky-500/20 border-t-sky-500 rounded-full animate-spin" />
        <p className="text-xs text-slate-400">Computing file metrics and architectural hotspot scores...</p>
      </div>
    );
  }

  const files = data?.files || [];
  const filteredFiles = files.filter((f) =>
    f.filePath.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Hotspot Methodology Info Card */}
      <div className="bg-gradient-to-r from-slate-900 to-indigo-950/30 p-5 rounded-2xl border border-slate-800 space-y-2">
        <div className="flex items-center space-x-2 text-sky-400 font-bold text-xs uppercase tracking-wider">
          <Info className="h-4 w-4" />
          <span>Hotspot Scoring Methodology</span>
        </div>
        <p className="text-xs text-slate-300 leading-relaxed max-w-3xl">
          Hotspot scores (0–100) pinpoint codebase components exhibiting high modification velocity, cumulative churn volatility, and developer coordination friction. The formula uses weighted normalization:
        </p>
        <div className="bg-slate-950/80 p-3 rounded-xl border border-slate-800/80 font-mono text-[11px] text-sky-300 inline-block">
          Score = (Modification Frequency × 40%) + (Code Churn × 35%) + (Recency × 15%) + (Contributors × 10%)
        </div>
      </div>

      {/* Search and Table */}
      <div className="bg-[#0f172a] rounded-2xl border border-slate-800 overflow-hidden space-y-4">
        <div className="p-4 border-b border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center space-x-2">
            <FileCode2 className="h-4 w-4 text-sky-400" />
            <h3 className="font-bold text-slate-100 text-sm">
              Tracked Files ({filteredFiles.length} of {files.length})
            </h3>
          </div>

          <div className="relative w-full sm:w-72">
            <Search className="h-3.5 w-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search file path or extension..."
              className="w-full bg-slate-900 border border-slate-700/80 rounded-lg pl-9 pr-3 py-1.5 text-xs text-slate-200 focus:outline-none focus:ring-2 focus:ring-sky-500 font-mono"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 font-semibold border-b border-slate-800">
              <tr>
                <th className="px-6 py-3">File Path</th>
                <th className="px-6 py-3">Modifications</th>
                <th className="px-6 py-3">Additions</th>
                <th className="px-6 py-3">Deletions</th>
                <th className="px-6 py-3">Total Churn</th>
                <th className="px-6 py-3">Authors</th>
                <th className="px-6 py-3">Hotspot Score</th>
                <th className="px-6 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {filteredFiles.map((f) => (
                <tr
                  key={f.filePath}
                  className="hover:bg-slate-800/30 transition-colors group cursor-pointer"
                  onClick={() => handleOpenFileDetail(f.filePath)}
                >
                  <td className="px-6 py-3.5 font-mono font-medium text-slate-200 max-w-md truncate">
                    {f.filePath}
                  </td>
                  <td className="px-6 py-3.5 font-mono text-slate-300">{f.modificationCount}</td>
                  <td className="px-6 py-3.5 font-mono text-emerald-400">+{f.additions}</td>
                  <td className="px-6 py-3.5 font-mono text-rose-400">-{f.deletions}</td>
                  <td className="px-6 py-3.5 font-mono font-bold text-white">{f.churn.toLocaleString()}</td>
                  <td className="px-6 py-3.5 font-mono text-slate-300">{f.contributorCount}</td>
                  <td className="px-6 py-3.5">
                    <div className="flex items-center space-x-1.5">
                      <Flame className={`h-3.5 w-3.5 ${f.hotspotScore >= 70 ? 'text-rose-400' : (f.hotspotScore >= 50 ? 'text-amber-400' : 'text-slate-500')}`} />
                      <span className="font-mono font-bold text-slate-100">{f.hotspotScore}</span>
                    </div>
                  </td>
                  <td className="px-6 py-3.5 text-right">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleOpenFileDetail(f.filePath);
                      }}
                      className="text-xs text-sky-400 hover:text-sky-300 font-semibold p-1 hover:bg-slate-800 rounded transition-colors inline-flex items-center space-x-1"
                    >
                      <span>Analyze</span>
                      <ExternalLink className="h-3 w-3" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      <FileDetailModal
        file={activeFileDetail}
        onClose={() => setActiveFileDetail(null)}
      />
    </div>
  );
};
