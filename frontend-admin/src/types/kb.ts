export type KbCategoryTreeVO = {
  id: number;
  parentId?: number | null;
  name: string;
  description?: string;
  icon?: string;
  sort?: number;
  status?: number;
  children?: KbCategoryTreeVO[];
};

export type KbCategorySaveDTO = {
  parentId?: number | null;
  name: string;
  description?: string;
  icon?: string;
  sort?: number;
  status?: number;
};

export type KbCategoryStatusDTO = {
  status: 0 | 1;
};

export type KbEntryPageQueryDTO = {
  keyword?: string;
  status?: number | null;
  categoryId?: number | null;
  tagId?: number | null;
  startTime?: string;
  endTime?: string;
  sortBy?: string;
  sortOrder?: string;
  page?: number;
  pageSize?: number;
};

export type KbEntryPageItemVO = {
  id: number;
  title: string;
  categoryId?: number;
  categoryName?: string;
  status?: number;
  createdAt?: string;
  updatedAt?: string;
  viewCount?: number;
  likeCount?: number;
  favoriteCount?: number;
};

export type KbTagSimpleVO = {
  id: number;
  name: string;
};

export type KbEntryDetailVO = {
  id: number;
  categoryId: number;
  title: string;
  summary?: string;
  content: string;
  source?: string;
  status?: number;
  viewCount?: number;
  likeCount?: number;
  favoriteCount?: number;
  createdAt?: string;
  updatedAt?: string;
  tags?: KbTagSimpleVO[];
};

export type KbEntrySaveDTO = {
  categoryId: number;
  title: string;
  summary?: string;
  content: string;
  source?: string;
  tagIds?: number[];
};

export type KbEntryStatusDTO = {
  status: 1 | 4;
};
