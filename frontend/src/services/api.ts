import {
  Repository,
  RepositoryOverview,
  CommitAnalytics,
  DeveloperAnalytics,
  FileAnalytics,
  FileMetrics,
  FileDetail,
  BranchAnalytics,
  OwnershipAnalytics,
  BusFactor,
  ChangeCoupling,
  RiskReport,
  RepositoryHealth,
  InsightReport,
  PeriodComparison,
  WhatChanged,
  ReleaseAnalytics,
} from '../types/api';

const BASE_URL = '/api';

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let errorMsg = `HTTP Error ${res.status}`;
    try {
      const err = await res.json();
      if (err.message) errorMsg = err.message;
    } catch {
      // ignore
    }
    throw new Error(errorMsg);
  }
  return res.json();
}

export const api = {
  async getRepositories(): Promise<Repository[]> {
    const res = await fetch(`${BASE_URL}/repositories`);
    return handleResponse<Repository[]>(res);
  },

  async registerRepository(name: string, localPath: string, defaultBranch = 'main'): Promise<Repository> {
    const res = await fetch(`${BASE_URL}/repositories`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, localPath, defaultBranch }),
    });
    return handleResponse<Repository>(res);
  },

  async syncCommits(repositoryId: string): Promise<void> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/commits`);
    await handleResponse(res);
  },

  async analyzeRepository(repositoryId: string): Promise<void> {
    await this.syncCommits(repositoryId);
  },

  async getOverview(repositoryId: string): Promise<RepositoryOverview> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/overview`);
    return handleResponse<RepositoryOverview>(res);
  },

  async getCommitAnalytics(repositoryId: string): Promise<CommitAnalytics> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/commits`);
    return handleResponse<CommitAnalytics>(res);
  },

  async getDeveloperAnalytics(repositoryId: string): Promise<DeveloperAnalytics> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/developers`);
    return handleResponse<DeveloperAnalytics>(res);
  },

  async getFileAnalytics(repositoryId: string): Promise<FileAnalytics> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/files`);
    return handleResponse<FileAnalytics>(res);
  },

  async getHotspots(repositoryId: string, limit = 10): Promise<FileMetrics[]> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/files/hotspots?limit=${limit}`);
    return handleResponse<FileMetrics[]>(res);
  },

  async getFileDetail(repositoryId: string, path: string): Promise<FileDetail> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/files/detail?path=${encodeURIComponent(path)}`);
    return handleResponse<FileDetail>(res);
  },

  async getBranchAnalytics(repositoryId: string, staleThresholdDays = 30): Promise<BranchAnalytics> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/branches?staleThresholdDays=${staleThresholdDays}`);
    return handleResponse<BranchAnalytics>(res);
  },

  async getOwnershipAnalytics(repositoryId: string): Promise<OwnershipAnalytics> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/ownership`);
    return handleResponse<OwnershipAnalytics>(res);
  },

  async getBusFactor(repositoryId: string): Promise<BusFactor> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/ownership/bus-factor`);
    return handleResponse<BusFactor>(res);
  },

  async getChangeCoupling(repositoryId: string): Promise<ChangeCoupling> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/coupling`);
    return handleResponse<ChangeCoupling>(res);
  },

  async getRisks(repositoryId: string): Promise<RiskReport> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/intelligence/risks`);
    return handleResponse<RiskReport>(res);
  },

  async getHealth(repositoryId: string): Promise<RepositoryHealth> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/intelligence/health`);
    return handleResponse<RepositoryHealth>(res);
  },

  async getInsights(repositoryId: string): Promise<InsightReport> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/intelligence/insights`);
    return handleResponse<InsightReport>(res);
  },

  async getComparison(repositoryId: string, days = 30): Promise<PeriodComparison> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/comparison?days=${days}`);
    return handleResponse<PeriodComparison>(res);
  },

  async getWhatChanged(repositoryId: string, days = 30): Promise<WhatChanged> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/intelligence/what-changed?days=${days}`);
    return handleResponse<WhatChanged>(res);
  },

  async getReleases(repositoryId: string): Promise<ReleaseAnalytics> {
    const res = await fetch(`${BASE_URL}/repositories/${repositoryId}/analytics/releases`);
    return handleResponse<ReleaseAnalytics>(res);
  },

  getPdfUrl(repositoryId: string): string {
    return `${BASE_URL}/repositories/${repositoryId}/reports/pdf`;
  },

  getCsvUrl(repositoryId: string, type: 'commits' | 'files' | 'developers' | 'risks'): string {
    return `${BASE_URL}/repositories/${repositoryId}/reports/csv/${type}`;
  },
};
