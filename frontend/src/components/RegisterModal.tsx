import React, { useState } from 'react';
import { X, FolderGit2, AlertCircle } from 'lucide-react';
import { api } from '../services/api';
import { Repository } from '../types/api';

interface RegisterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onRegistered: (repo: Repository) => void;
}

export const RegisterModal: React.FC<RegisterModalProps> = ({
  isOpen,
  onClose,
  onRegistered,
}) => {
  const [name, setName] = useState('');
  const [localPath, setLocalPath] = useState('');
  const [defaultBranch, setDefaultBranch] = useState('main');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !localPath.trim()) {
      setError('Repository name and local path are required.');
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const repo = await api.registerRepository(name.trim(), localPath.trim(), defaultBranch.trim() || 'main');
      await api.syncCommits(repo.id);
      onRegistered(repo);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Failed to register repository. Verify that the path points to a valid Git repository.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div className="bg-[#0f172a] border border-slate-800 rounded-2xl w-full max-w-md shadow-2xl overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <FolderGit2 className="h-5 w-5 text-sky-400" />
            <h3 className="font-bold text-slate-100 text-sm">Register Git Repository</h3>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-200 p-1 rounded-lg hover:bg-slate-800/60 transition-colors"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 text-xs">
          {error && (
            <div className="flex items-start space-x-2.5 p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-300">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5 text-rose-400" />
              <span>{error}</span>
            </div>
          )}

          <div>
            <label className="block font-semibold text-slate-300 mb-1.5">Repository Display Name</label>
            <input
              type="text"
              placeholder="e.g. enterprise-platform"
              className="w-full bg-slate-900 border border-slate-700/80 rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:ring-2 focus:ring-sky-500"
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={isLoading}
            />
          </div>

          <div>
            <label className="block font-semibold text-slate-300 mb-1.5">Absolute Local Filepath</label>
            <input
              type="text"
              placeholder="e.g. C:\enterprise-platform or /home/user/project"
              className="w-full bg-slate-900 border border-slate-700/80 rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:ring-2 focus:ring-sky-500 font-mono text-[11px]"
              value={localPath}
              onChange={(e) => setLocalPath(e.target.value)}
              disabled={isLoading}
            />
            <p className="text-[11px] text-slate-400 mt-1">Must be an existing local folder containing a .git directory.</p>
          </div>

          <div>
            <label className="block font-semibold text-slate-300 mb-1.5">Default Branch</label>
            <input
              type="text"
              placeholder="main"
              className="w-full bg-slate-900 border border-slate-700/80 rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:ring-2 focus:ring-sky-500"
              value={defaultBranch}
              onChange={(e) => setDefaultBranch(e.target.value)}
              disabled={isLoading}
            />
          </div>

          <div className="pt-2 flex justify-end space-x-2.5">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold transition-colors"
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-4 py-2 rounded-lg bg-sky-600 hover:bg-sky-500 text-white font-semibold transition-colors shadow-sm shadow-sky-600/30 flex items-center space-x-1.5 disabled:opacity-50"
            >
              {isLoading ? (
                <>
                  <span className="h-3 w-3 border-2 border-white/30 border-t-white rounded-full animate-spin mr-1" />
                  <span>Validating...</span>
                </>
              ) : (
                <span>Analyze Repository</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
