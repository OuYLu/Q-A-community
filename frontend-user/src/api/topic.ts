import { request } from "@/api/http";
import { BASE_URL } from "@/utils/constants";

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
  tags?: string[];
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

function toAbsoluteUrl(url?: string) {
  if (!url) return "";
  const text = url.trim();
  if (!text) return "";
  if (text.startsWith("http://") || text.startsWith("https://")) return text;
  if (text.startsWith("/")) return `${BASE_URL}${text}`;
  return `${BASE_URL}/${text}`;
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
    }).then((data) => ({
      ...data,
      coverImg: toAbsoluteUrl(data?.coverImg)
    }));
  },
  questions(id: number, params: { sortBy?: "hot" | "latest" | "unsolved"; page?: number; pageSize?: number }) {
    return request<PageInfo<AppTopicQuestionItemVO>>({
      url: `/api/customer/topics/${id}/questions`,
      params
    });
  },
  createTopicQuestion(id: number, data: {
    title: string;
    content?: string;
    categoryId?: number;
    tagIds?: number[];
    tagNames?: string[];
    imageUrls?: string[];
  }) {
    return request<number>({
      url: `/api/customer/topics/${id}/questions`,
      method: "POST",
      data
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
