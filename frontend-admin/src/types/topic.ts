export type TopicCategoryVO = {
  id: number;
  name: string;
  parentId?: number | null;
};

export type TopicRecentQuestionVO = {
  id: number;
  title: string;
  status?: number;
  createdAt?: string;
  userId?: number;
  authorName?: string;
};

export type TopicTrendPointVO = {
  date: string;
  cnt: number;
};

export type TopicStatsVO = {
  followCount?: number;
  questionCount?: number;
  todayNewCount?: number;
};

export type AdminTopicListItemVO = {
  id: number;
  title: string;
  subtitle?: string;
  coverImg?: string;
  status: number;
  followCount?: number;
  questionCount?: number;
  todayNewCount?: number;
  categoryCount?: number;
  createdBy?: number;
  createdAt?: string;
  updatedAt?: string;
};

export type AdminTopicDetailVO = {
  id: number;
  title: string;
  subtitle?: string;
  coverImg?: string;
  intro?: string;
  status: number;
  followCount?: number;
  questionCount?: number;
  todayNewCount?: number;
  createdBy?: number;
  createdAt?: string;
  updatedAt?: string;
  categories?: TopicCategoryVO[];
  recentQuestions?: TopicRecentQuestionVO[];
};

export type QaTopicPageQueryDTO = {
  title?: string;
  status?: number | null;
  createdBy?: number | null;
  dateStart?: string;
  dateEnd?: string;
  sortBy?: string;
  sortOrder?: string;
  page?: number;
  pageSize?: number;
};

export type QaTopicSaveDTO = {
  title: string;
  subtitle?: string;
  coverImg?: string;
  intro?: string;
  status?: number;
  categoryIds?: number[];
};

export type QaTopicStatusUpdateDTO = {
  status: number;
};
