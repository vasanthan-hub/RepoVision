export interface Repository {
  id: string;
  name: string;
  localPath: string;
  defaultBranch: string;
  analysisStatus: 'NOT_ANALYZED' | 'ANALYZING' | 'COMPLETED' | 'FAILED';
  createdAt: string;
  updatedAt: string;
}

export interface CommitSummary {
  hash: string;
  subject: string;
  additions: number;
  deletions: number;
  churn: number;
}

export interface CommitsByDate {
  date: string;
  count: number;
}

export interface CommitAnalytics {
  totalCommits: number;
  totalAdditions: number;
  totalDeletions: number;
  totalChurn: number;
  averageCommitSize: number;
  averageAdditionsPerCommit: number;
  averageDeletionsPerCommit: number;
  largestCommit: CommitSummary | null;
  averageCommitsPerActiveDay: number;
  commitsByDate: CommitsByDate[];
}

export interface DeveloperActivity {
  date: string;
  commitCount: number;
}

export interface DeveloperMetrics {
  authorName: string;
  authorEmail: string;
  commitCount: number;
  additions: number;
  deletions: number;
  churn: number;
  commitPercentage: number;
  churnPercentage: number;
  activityByDate: DeveloperActivity[];
}

export interface DeveloperAnalytics {
  totalDevelopers: number;
  developers: DeveloperMetrics[];
}

export interface FileMetrics {
  filePath: string;
  modificationCount: number;
  additions: number;
  deletions: number;
  churn: number;
  contributorCount: number;
  firstModified: string | null;
  lastModified: string | null;
  averageChangeSize: number;
  hotspotScore: number;
}

export interface FileAnalytics {
  totalFiles: number;
  files: FileMetrics[];
}

export interface FileContributor {
  name: string;
  email: string;
  commitsCount: number;
  linesChanged: number;
  percentage: number;
}

export interface FileCommitSummary {
  hash: string;
  authorName: string;
  authoredAt: string;
  subject: string;
  additions: number;
  deletions: number;
}

export interface FileDetail {
  filePath: string;
  modificationCount: number;
  additions: number;
  deletions: number;
  churn: number;
  contributorCount: number;
  firstModified: string | null;
  lastModified: string | null;
  averageChangeSize: number;
  hotspotScore: number;
  contributors: FileContributor[];
  recentCommits: FileCommitSummary[];
}

export interface BranchMetrics {
  name: string;
  latestCommitHash: string;
  latestCommitSubject: string;
  latestCommitAuthor: string;
  lastActivity: string;
  commitCount: number;
  isStale: boolean;
  isMerged: boolean;
  isDefault: boolean;
  daysInactive: number;
}

export interface BranchAnalytics {
  totalBranches: number;
  activeBranches: number;
  staleBranches: number;
  branches: BranchMetrics[];
}

export interface FileOwnership {
  filePath: string;
  module: string;
  primaryContributorName: string;
  primaryContributorEmail: string;
  primaryContributorPercentage: number;
  totalChurn: number;
  contributorCount: number;
  concentrationLevel: 'HIGH' | 'MODERATE' | 'BALANCED';
  contributors: FileContributor[];
}

export interface DeveloperFileCount {
  developerName: string;
  developerEmail: string;
  primarilyOwnedFilesCount: number;
  percentageOfCodebase: number;
  totalContributionsChurn: number;
}

export interface BusFactor {
  busFactor: number;
  singleContributorFilesCount: number;
  highConcentrationFilesCount: number;
  totalFiles: number;
  keyDevelopers: DeveloperFileCount[];
  evidence: string[];
}

export interface OwnershipAnalytics {
  totalFiles: number;
  busFactor: BusFactor;
  fileOwnerships: FileOwnership[];
}

export interface FileCoupling {
  fileA: string;
  fileB: string;
  coChangeCount: number;
  fileAChanges: number;
  fileBChanges: number;
  couplingPercentage: number;
  strength: 'STRONG' | 'MODERATE' | 'WEAK';
}

export interface ChangeCoupling {
  totalCoupledPairs: number;
  strongCouplingCount: number;
  couplings: FileCoupling[];
}

export interface RiskItem {
  id: string;
  type: 'HOTSPOT' | 'HIGH_CHURN' | 'OWNERSHIP_CONCENTRATION' | 'STALE_BRANCH' | 'LARGE_COMMIT' | 'DECLINING_ACTIVITY' | 'STRONG_CHANGE_COUPLING';
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
  title: string;
  entity: string;
  evidence: string;
  description: string;
  recommendation: string;
}

export interface RiskReport {
  totalRisks: number;
  highSeverityCount: number;
  mediumSeverityCount: number;
  lowSeverityCount: number;
  risks: RiskItem[];
}

export interface HealthDimension {
  name: string;
  score: number;
  weight: number;
  status: string;
  description: string;
  evidence: string[];
}

export interface RepositoryHealth {
  overallScore: number;
  status: 'EXCELLENT' | 'GOOD' | 'NEEDS_ATTENTION' | 'CRITICAL';
  activityScore: number;
  stabilityScore: number;
  branchHealthScore: number;
  ownershipScore: number;
  releaseVelocityScore: number;
  dimensions: HealthDimension[];
  summary: string;
}

export interface InsightItem {
  id: string;
  category: 'ARCHITECTURE' | 'COLLABORATION' | 'HYGIENE' | 'VELOCITY';
  type: 'POSITIVE' | 'WARNING' | 'CRITICAL' | 'INFO';
  title: string;
  finding: string;
  interpretation: string;
  action: string;
}

export interface InsightReport {
  totalInsights: number;
  insights: InsightItem[];
}

export interface MetricDelta {
  metricName: string;
  currentValue: number;
  previousValue: number;
  absoluteChange: number;
  percentageChange: number;
  unit: string;
  trend: 'UP' | 'DOWN' | 'FLAT';
}

export interface PeriodComparison {
  days: number;
  currentPeriodStart: string;
  currentPeriodEnd: string;
  previousPeriodStart: string;
  previousPeriodEnd: string;
  metrics: Record<string, MetricDelta>;
}

export interface WhatChanged {
  days: number;
  periodLabel: string;
  highlights: string[];
  detailedChanges: string[];
}

export interface ReleaseMetrics {
  tagName: string;
  releaseDate: string;
  commitHash: string;
  message: string;
  daysSincePreviousRelease: number;
  commitsCount: number;
  churn: number;
}

export interface ReleaseAnalytics {
  totalReleases: number;
  averageReleaseIntervalDays: number;
  releases: ReleaseMetrics[];
}

export interface RecentActivitySummary {
  commitsLast7Days: number;
  commitsLast30Days: number;
  commitsLast90Days: number;
  activeDaysCount: number;
}

export interface RepositoryOverview {
  repository: Repository;
  health: RepositoryHealth;
  totalCommits: number;
  totalDevelopers: number;
  totalFiles: number;
  totalBranches: number;
  activeBranches: number;
  staleBranches: number;
  totalChurn: number;
  recentActivity: RecentActivitySummary;
  topHotspots: FileMetrics[];
  risks: RiskReport;
  insights: InsightItem[];
}
