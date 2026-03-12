export type QaManageQuestionPageQueryDTO = {
  keyword?: string;
  status?: number | null;
  deleteFlag?: number | null;
  categoryId?: number | null;
  topicId?: number | null;
  userId?: number | null;
  startTime?: string;
  endTime?: string;
  sortBy?: "createdAt" | "updatedAt" | "answerCount" | "viewCount" | "likeCount" | "";
  sortOrder?: "asc" | "desc" | "";
  page: number;
  pageSize: number;
};

export type QaManageAnswerPageQueryDTO = {
  keyword?: string;
  status?: number | null;
  deleteFlag?: number | null;
  questionId?: number | null;
  userId?: number | null;
  startTime?: string;
  endTime?: string;
  sortBy?: "createdAt" | "updatedAt" | "likeCount" | "";
  sortOrder?: "asc" | "desc" | "";
  page: number;
  pageSize: number;
};

export type QaManageStatusUpdateDTO = {
  status: 1 | 4;
};

export type AdminQaQuestionPageItemVO = {
  id: number;
  title?: string;
  summary?: string;
  status?: number;
  deleteFlag?: number;
  categoryId?: number;
  categoryName?: string;
  topicId?: number;
  topicTitle?: string;
  authorId?: number;
  authorName?: string;
  answerCount?: number;
  viewCount?: number;
  likeCount?: number;
  favoriteCount?: number;
  createdAt?: string;
  updatedAt?: string;
};

export type AdminQaAnswerPageItemVO = {
  id: number;
  questionId?: number;
  questionTitle?: string;
  contentPreview?: string;
  status?: number;
  deleteFlag?: number;
  authorId?: number;
  authorName?: string;
  questionAuthorId?: number;
  questionAuthorName?: string;
  likeCount?: number;
  createdAt?: string;
  updatedAt?: string;
};

