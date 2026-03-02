import { request } from "@/api/http";
import { BASE_URL } from "@/utils/constants";
import type { AppExpertPostItemVO } from "@/api/expert";

export interface AppUserHomeVO {
  userId: number;
  nickname: string;
  avatar?: string;
  slogan?: string;
  expertStatus?: number;
  questionCount: number;
  answerCount: number;
  expertPostCount: number;
  followerCount: number;
  followingCount: number;
  followed: boolean;
  self: boolean;
}

export interface AppUserAnswerItemVO {
  answerId: number;
  questionId: number;
  questionTitle: string;
  likeCount: number;
  contentPreview: string;
  createdAt: string;
}

export interface PageInfo<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

function toAbsoluteUrl(url?: string) {
  if (!url) return "";
  const text = url.trim();
  if (!text) return "";
  if (text.startsWith("http://") || text.startsWith("https://")) return text;
  if (text.startsWith("/")) return `${BASE_URL}${text}`;
  return `${BASE_URL}/${text}`;
}

function normalizeHome(data?: AppUserHomeVO): AppUserHomeVO {
  return {
    userId: Number(data?.userId || 0),
    nickname: data?.nickname || "用户",
    avatar: toAbsoluteUrl(data?.avatar),
    slogan: data?.slogan || "",
    expertStatus: data?.expertStatus,
    questionCount: Number(data?.questionCount || 0),
    answerCount: Number(data?.answerCount || 0),
    expertPostCount: Number((data as any)?.expertPostCount || 0),
    followerCount: Number(data?.followerCount || 0),
    followingCount: Number(data?.followingCount || 0),
    followed: !!data?.followed,
    self: !!data?.self
  };
}

export const userApi = {
  home(userId: number) {
    return request<AppUserHomeVO>({
      url: `/api/customer/users/${userId}/home`
    }).then((data) => normalizeHome(data));
  },
  follow(userId: number) {
    return request<void>({
      url: `/api/customer/users/${userId}/follow`,
      method: "POST"
    });
  },
  unfollow(userId: number) {
    return request<void>({
      url: `/api/customer/users/${userId}/follow`,
      method: "DELETE"
    });
  },
  answers(userId: number, params?: { page?: number; pageSize?: number }) {
    return request<PageInfo<AppUserAnswerItemVO>>({
      url: `/api/customer/users/${userId}/answers`,
      params
    });
  },
  expertPosts(userId: number, params?: { page?: number; pageSize?: number }) {
    return request<PageInfo<AppExpertPostItemVO>>({
      url: `/api/customer/users/${userId}/expert-posts`,
      params
    });
  }
};