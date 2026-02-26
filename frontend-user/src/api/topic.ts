import { request } from "@/api/http";

export interface PageInfo<T> {
  list: T[];
  pageNum: number;
  pageSize: number;
  total: number;
}

export interface AppTopicListItemVO {
  id: number;
  title: string;
  subtitle: string;
  coverImg: string;
  followCount: number;
  questionCount: number;
  todayNewCount: number;
}

export interface AppTopicDetailVO extends AppTopicListItemVO {
  intro: string;
  followed: boolean;
}

export interface AppTopicQuestionItemVO {
  id: number;
  title: string;
  summary?: string;
  answerCount: number;
  viewCount: number;
  likeCount?: number;
  createdAt?: string;
  authorName?: string;
}

export const topicApi = {
  list(params: {
    keyword?: string;
    sortBy?: "followCount" | "questionCount" | "todayNewCount" | "createdAt";
    sortOrder?: "asc" | "desc";
    page?: number;
    pageSize?: number;
  }) {
    return request<PageInfo<AppTopicListItemVO>>({
      url: "/api/customer/topics",
      params
    });
  },
  detail(id: number) {
    return request<AppTopicDetailVO>({
      url: `/api/customer/topics/${id}`
    });
  },
  questions(id: number, params: { sortBy?: "hot" | "latest" | "unsolved"; page?: number; pageSize?: number }) {
    return request<PageInfo<AppTopicQuestionItemVO>>({
      url: `/api/customer/topics/${id}/questions`,
      params
    });
  },
  follow(id: number) {
    return request<void>({
      url: `/api/customer/topics/${id}/follow`,
      method: "POST"
    });
  },
  unfollow(id: number) {
    return request<void>({
      url: `/api/customer/topics/${id}/follow`,
      method: "DELETE"
    });
  }
};
