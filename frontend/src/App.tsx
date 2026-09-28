import React, { useState, useEffect, useCallback } from 'react';
import { Repository, RepositoryOverview, FileDetail } from './types/api';
import { api } from './services/api';
import { Header } from './components/Header';
import { Sidebar, ActiveTab } from './components/Sidebar';
import { RegisterModal } from './components/RegisterModal';
import { FileDetailModal } from './components/FileDetailModal';

// Views
import { OverviewView } from './views/OverviewView';
import { CommitsView } from './views/CommitsView';
import { DevelopersView } from './views/DevelopersView';
import { FilesView } from './views/FilesView';
import { BranchesView } from './views/BranchesView';
import { OwnershipView } from './views/OwnershipView';
import { CouplingView } from './views/CouplingView';
import { RisksView } from './views/RisksView';
import { HealthView } from './views/HealthView';
import { InsightsView } from './views/InsightsView';
import { ComparisonView } from './views/ComparisonView';
import { ReleasesView } from './views/ReleasesView';

import { FolderGit2, AlertTriangle, PlusCircle, RefreshCw } from 'lucide-react';

export const App: React.FC = () => {
  const [repositories, setRepositories] = useState<Repository[]>([]);
  const [selectedRepo, setSelectedRepo] = useState<Repository | null>(null);
  const [overview, setOverview] = useState<RepositoryOverview | null>(null);
  const [activeTab, setActiveTab] = useState<ActiveTab>('overview');

  const [isLoadingRepos, setIsLoadingRepos] = useState<boolean>(true);
  const [isLoadingOverview, setIsLoadingOverview] = useState<boolean>(false);
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Modals & Drawers
  const [isRegisterOpen, setIsRegisterOpen] = useState<boolean>(false);
  const [selectedFileDetail, setSelectedFileDetail] = useState<FileDetail | null>(null);
  const [selectedFilePath, setSelectedFilePath] = useState<string | null>(null);

  // Initial Load of Repositories
  const loadRepositories = useCallback(async (preferredRepoId?: string) => {
    try {
      setIsLoadingRepos(true);
      setError(null);
      const repos = await api.getRepositories();
      setRepositories(repos);

      if (repos.length > 0) {
        let repoToSelect = repos[0];
        if (preferredRepoId) {
          const found = repos.find(r => r.id === preferredRepoId);
          if (found) repoToSelect = found;
        } else {
          // Check for canonical enterprise-platform default repo
          const defaultRepo = repos.find(r => r.id === '9d13d996-144a-4cb7-a3f0-ebbd0c616221');
          if (defaultRepo) repoToSelect = defaultRepo;
        }
        setSelectedRepo(repoToSelect);
      } else {
        setSelectedRepo(null);
        setOverview(null);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to connect to RepoVision backend');
    } finally {
      setIsLoadingRepos(false);
    }
  }, []);

  useEffect(() => {
    loadRepositories();
  }, [loadRepositories]);

  // Load Overview whenever selectedRepo changes
  const loadOverview = useCallback(async (repoId: string) => {
    try {
      setIsLoadingOverview(true);
      setOverview(null);
      const ov = await api.getOverview(repoId);
      setOverview(ov);
    } catch (err: any) {
      console.error('Error fetching repository overview:', err);
      setOverview(null);
    } finally {
      setIsLoadingOverview(false);
    }
  }, []);

  useEffect(() => {
    if (selectedRepo?.id) {
      loadOverview(selectedRepo.id);
    } else {
      setOverview(null);
    }
  }, [selectedRepo?.id, loadOverview]);

  // Handle Sync / Ingestion
  const handleRefresh = async () => {
    if (!selectedRepo) return;
    try {
      setIsRefreshing(true);
      await api.analyzeRepository(selectedRepo.id);
      await loadOverview(selectedRepo.id);
    } catch (err: any) {
      console.error('Failed to analyze repository:', err);
    } finally {
      setIsRefreshing(false);
    }
  };

  // Handle File Selection for Modal / Drill-down
  const handleSelectFile = async (filePath: string) => {
    if (!selectedRepo) return;
    try {
      const detail = await api.getFileDetail(selectedRepo.id, filePath);
      setSelectedFileDetail(detail);
    } catch (err) {
      console.error('Failed to load file details:', err);
      // Fallback: switch to files tab with filter
      setSelectedFilePath(filePath);
      setActiveTab('files');
    }
  };

  // Handle Repo Registered
  const handleRepoRegistered = (newRepo: Repository) => {
    setRepositories(prev => [newRepo, ...prev]);
    setSelectedRepo(newRepo);
    setIsRegisterOpen(false);
    setActiveTab('overview');
  };

  if (isLoadingRepos) {
    return (
      <div className="min-h-screen bg-[#090d16] flex flex-col items-center justify-center p-6 text-slate-300">
        <div className="w-12 h-12 border-4 border-sky-500 border-t-transparent rounded-full animate-spin mb-4" />
        <h2 className="text-xl font-bold text-white tracking-tight">RepoVision</h2>
        <p className="text-xs text-slate-400 mt-1 font-mono">Initializing Engineering Intelligence Platform...</p>
      </div>
    );
  }

  if (error && repositories.length === 0) {
    return (
      <div className="min-h-screen bg-[#090d16] flex flex-col items-center justify-center p-6 text-slate-300">
        <div className="bg-red-500/10 border border-red-500/20 rounded-2xl p-8 max-w-lg text-center">
          <AlertTriangle className="w-12 h-12 text-rose-400 mx-auto mb-4" />
          <h2 className="text-xl font-bold text-white mb-2">Backend Connection Unavailable</h2>
          <p className="text-sm text-slate-400 mb-6 font-sans">
            Unable to reach the RepoVision Spring Boot API on <code className="text-sky-300 bg-slate-900 px-2 py-0.5 rounded">http://localhost:8080</code>.
            Ensure the backend application is running.
          </p>
          <button
            onClick={() => loadRepositories()}
            className="px-5 py-2.5 bg-sky-600 hover:bg-sky-500 text-white text-sm font-semibold rounded-xl transition-all shadow-lg shadow-sky-600/30"
          >
            Retry Connection
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#090d16] flex flex-col text-slate-100 font-sans selection:bg-sky-500/30 selection:text-white">
      {/* Top Header */}
      <Header
        repositories={repositories}
        selectedRepo={selectedRepo}
        onSelectRepo={(repo) => setSelectedRepo(repo)}
        onOpenRegister={() => setIsRegisterOpen(true)}
        onRefresh={handleRefresh}
        isRefreshing={isRefreshing}
      />

      {/* Main Workspace Layout */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Modular Sidebar */}
        <Sidebar
          activeTab={activeTab}
          onTabChange={(tab) => setActiveTab(tab)}
          riskCount={overview?.risks?.totalRisks}
          healthScore={overview?.health?.overallScore}
        />

        {/* Content Area */}
        <main className="flex-1 overflow-y-auto p-6 md:p-8 space-y-6">
          {!selectedRepo ? (
            <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-12 text-center max-w-lg mx-auto mt-12">
              <FolderGit2 className="w-12 h-12 text-sky-400 mx-auto mb-4" />
              <h3 className="text-lg font-bold text-white mb-2">No Repository Registered</h3>
              <p className="text-sm text-slate-400 mb-6">
                Connect a local Git repository path to extract commits, analyze churn, measure coupling, and generate engineering intelligence.
              </p>
              <button
                onClick={() => setIsRegisterOpen(true)}
                className="inline-flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white font-semibold text-sm rounded-xl transition-all shadow-lg shadow-sky-500/25"
              >
                <PlusCircle className="w-4 h-4" />
                Register Local Repository
              </button>
            </div>
          ) : (
            <>
              {activeTab === 'overview' && (
                isLoadingOverview && !overview ? (
                  <div className="flex flex-col items-center justify-center min-h-[400px] space-y-3">
                    <div className="w-8 h-8 border-3 border-sky-500 border-t-transparent rounded-full animate-spin" />
                    <p className="text-slate-400 text-xs font-mono">Synthesizing Repository Overview...</p>
                  </div>
                ) : overview ? (
                  <OverviewView
                    overview={overview}
                    onNavigate={(tab) => setActiveTab(tab)}
                    onSelectFile={handleSelectFile}
                  />
                ) : (
                  <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-8 text-center text-slate-400">
                    <p className="text-sm mb-3">Repository registered, ready for initial ingestion.</p>
                    <button
                      onClick={handleRefresh}
                      className="px-4 py-2 bg-sky-600 hover:bg-sky-500 text-white text-xs font-semibold rounded-lg"
                    >
                      Run Analysis Now
                    </button>
                  </div>
                )
              )}

              {activeTab === 'commits' && <CommitsView key={selectedRepo.id} repository={selectedRepo} />}
              {activeTab === 'developers' && <DevelopersView key={selectedRepo.id} repository={selectedRepo} />}
              {activeTab === 'files' && (
                <FilesView
                  key={selectedRepo.id}
                  repository={selectedRepo}
                  selectedFilePath={selectedFilePath}
                  onClearSelectedFilePath={() => setSelectedFilePath(null)}
                />
              )}
              {activeTab === 'branches' && <BranchesView key={selectedRepo.id} repository={selectedRepo} />}
              {activeTab === 'ownership' && <OwnershipView key={selectedRepo.id} repository={selectedRepo} />}
              {activeTab === 'coupling' && <CouplingView key={selectedRepo.id} repository={selectedRepo} />}
              {activeTab === 'risks' && <RisksView key={selectedRepo.id} repository={selectedRepo} />}
              {activeTab === 'health' && <HealthView key={selectedRepo.id} repository={selectedRepo} />}
              {activeTab === 'insights' && <InsightsView key={selectedRepo.id} repositoryId={selectedRepo.id} />}
              {activeTab === 'comparison' && <ComparisonView key={selectedRepo.id} repositoryId={selectedRepo.id} />}
              {activeTab === 'releases' && <ReleasesView key={selectedRepo.id} repositoryId={selectedRepo.id} />}
            </>
          )}
        </main>
      </div>

      {/* Global Modals */}
      <RegisterModal
        isOpen={isRegisterOpen}
        onClose={() => setIsRegisterOpen(false)}
        onRegistered={handleRepoRegistered}
      />

      <FileDetailModal
        file={selectedFileDetail}
        onClose={() => setSelectedFileDetail(null)}
      />
    </div>
  );
};

export default App;
