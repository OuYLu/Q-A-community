export type QaTag = {
  id: number;
  name: string;
  source: number;
  status: number;
  useCount?: number;
  createdAt?: string;
};

export type QaTagQueryDTO = {
  name?: string;
  source?: number | null;
  status?: number | null;
  useCountMin?: number | null;
  useCountMax?: number | null;
  pageNum?: number;
  pageSize?: number;
};

export type QaTagSaveDTO = {
  name: string;
  source: 1 | 2;
  status?: 0 | 1;
};

export type TagRecentQuestion = {
  id: number;
  title: string;
  status?: number;
  createdAt?: string;
};

export type TagDetailExtra = {
  recentQuestions: TagRecentQuestion[];
  questionManagePath?: string;
  questionManageQueryKey?: string;
  questionManageQueryValue?: string;
  questionManageUrl?: string;
};

export type TagUsageTrendPoint = {
  date?: string;
  count?: number;
  statDate?: string;
  refCount?: number;
};
