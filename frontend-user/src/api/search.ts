import { request } from "@/api/http";

export interface AppSearchQuestionVO {
  id: number;
  title: string;
  summary?: string;
  titleHighlight?: string;
  summaryHighlight?: string;
  answerCount?: number;
  viewCount?: number;
  likeCount?: number;
  createdAt?: string;
}

export interface AppSearchTopicVO {
  id: number;
  title: string;
  subtitle: string;
  followCount: number;
}

export interface AppSearchTagVO {
  id: number;
  name: string;
  useCount: number;
}

export interface AppSearchKbVO {
  id: number;
  title: string;
  summary?: string;
  titleHighlight?: string;
  summaryHighlight?: string;
  viewCount?: number;
  likeCount?: number;
  source?: string | null;
  createdAt?: string;
}

export interface AppSearchAnswerVO {
  answerId: number;
  questionId: number;
  questionTitle: string;
  contentPreview?: string;
  questionTitleHighlight?: string;
  contentPreviewHighlight?: string;
  likeCount?: number;
  createdAt?: string;
}

export interface AppSearchSimilarQuestionVO {
  id: number;
  title: string;
  titleHighlight?: string;
}

export interface AppSearchResultVO {
  query: string;
  questions: AppSearchQuestionVO[];
  answers: AppSearchAnswerVO[];
  topics: AppSearchTopicVO[];
  tags: AppSearchTagVO[];
  kbEntries: AppSearchKbVO[];
  similarQuestions?: AppSearchSimilarQuestionVO[];
}

export interface AppSearchHotVO {
  queryText: string;
  hitCount: number;
}

export interface AppSearchHistoryVO {
  queryText: string;
  lastTime: string;
}

export const searchApi = {
  search(params: {
    query: string;
    type?: "all" | "question" | "answer" | "topic" | "tag" | "kb";
    sortBy?: "comprehensive" | "latest" | "hot";
    categoryId?: number;
    topicId?: number;
    onlyUnsolved?: boolean;
    page?: number;
    pageSize?: number;
  }) {
    return request<AppSearchResultVO>({
      url: "/api/customer/search",
      params
    });
  },
  hot(limit = 10) {
    return request<AppSearchHotVO[]>({
      url: "/api/customer/search/hot",
      params: { limit }
    });
  },
  history(limit = 10) {
    return request<AppSearchHistoryVO[]>({
      url: "/api/customer/search/history",
      params: { limit }
    });
  },
  clearHistory() {
    return request<void>({
      url: "/api/customer/search/history",
      method: "DELETE"
    });
  },
  logSearch(data: { queryText: string; searchType?: number; hitCount?: number }) {
    return request<void>({
      url: "/api/customer/search/log",
      method: "POST",
      data
    });
  }
};
