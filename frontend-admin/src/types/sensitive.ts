export type CmsSensitiveWord = {
  id: number;
  word: string;
  level: number;
  category?: string;
  hitActionDesc?: string;
  reasonTemplate?: string;
  enabled: number;
  createdAt?: string;
};

export type CmsSensitiveWordQueryDTO = {
  keyword?: string;
  level?: number | null;
  enabled?: number | null;
  category?: string;
  pageNum?: number;
  pageSize?: number;
};

export type CmsSensitiveWordSaveDTO = {
  word: string;
  level: 1 | 2;
  category?: string;
  hitActionDesc?: string;
  reasonTemplate?: string;
  enabled?: 0 | 1;
};

