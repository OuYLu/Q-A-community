export type QaCategory = {
  id: number;
  name: string;
  parentId?: number | null;
  icon?: string;
  description?: string;
  status: number;
  sort: number;
  questionCount?: number;
  createdAt?: string;
  updatedAt?: string;
};

export type QaCategoryQueryDTO = {
  name?: string;
  parentId?: number | null;
  status?: number | null;
  pageNum?: number;
  pageSize?: number;
};

export type QaCategorySaveDTO = {
  name: string;
  parentId?: number | null;
  icon?: string;
  description?: string;
  status?: number;
  sort?: number;
};

export type CategoryTreeVO = {
  id: number;
  parentId?: number | null;
  name: string;
  label?: string;
  status?: number;
  sort?: number;
  hasChildren?: boolean;
  leaf?: boolean;
};
