import { request } from "@/api/http";

export interface PageInfo<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface AppMeOverviewVO {
  userId: number;
  username?: string | null;
  nickname: string | null;
  avatar: string | null;
  slogan: string;
  passwordSet?: number;
  email?: string | null;
  joinedAt: string;
  questionCount: number;
  answerCount: number;
  likeReceivedCount: number;
  followerCount: number;
  followingCount: number;
  favoriteCount: number;
  historyCount: number;
}

export interface AppMyFavoriteItemVO {
  questionId: number;
  title: string;
  answerCount: number;
  likeCount: number;
  favoriteCount: number;
  favoriteAt: string;
}

export interface AppMyHistoryItemVO {
  bizType: number;
  bizId: number;
  title: string;
  subTitle: string;
  viewedAt: string;
}

export interface AppMyQuestionItemVO {
  id: number;
  title: string;
  status: number;
  answerCount: number;
  likeCount: number;
  createdAt: string;
}

export interface AppMyAnswerItemVO {
  answerId: number;
  questionId: number;
  questionTitle: string;
  likeCount: number;
  contentPreview: string;
  createdAt: string;
}

export interface AppFollowUserItemVO {
  userId: number;
  nickname: string | null;
  avatar: string | null;
  expertStatus: number | null;
  followedAt: string;
}

export interface AppDocVO {
  type: string;
  title: string;
  content: string;
}

export type AppDocType = "settings" | "help" | "user-agreement" | "privacy-policy";

export interface AppProfileUpdateDTO {
  username?: string;
  nickname?: string;
  avatar?: string;
  slogan?: string;
  email?: string;
}

export interface AppSetFirstPasswordDTO {
  newPassword: string;
  confirmPassword: string;
}

export interface AppChangePasswordDTO {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

interface PageQuery {
  page?: number;
  pageSize?: number;
}

export const meApi = {
  overview() {
    return request<AppMeOverviewVO>({
      url: "/api/customer/me/overview"
    });
  },
  updateProfile(data: AppProfileUpdateDTO) {
    return request<void>({
      url: "/api/customer/me/profile",
      method: "PUT",
      data
    });
  },
  setFirstPassword(data: AppSetFirstPasswordDTO) {
    return request<void>({
      url: "/api/customer/me/password/set-first",
      method: "POST",
      data
    });
  },
  changePassword(data: AppChangePasswordDTO) {
    return request<void>({
      url: "/api/customer/me/password/change",
      method: "POST",
      data
    });
  },
  favorites(params: PageQuery) {
    return request<PageInfo<AppMyFavoriteItemVO>>({
      url: "/api/customer/me/favorites",
      params
    });
  },
  history(params: PageQuery) {
    return request<PageInfo<AppMyHistoryItemVO>>({
      url: "/api/customer/me/history",
      params
    });
  },
  questions(params: PageQuery) {
    return request<PageInfo<AppMyQuestionItemVO>>({
      url: "/api/customer/me/questions",
      params
    });
  },
  answers(params: PageQuery) {
    return request<PageInfo<AppMyAnswerItemVO>>({
      url: "/api/customer/me/answers",
      params
    });
  },
  following(params: PageQuery) {
    return request<PageInfo<AppFollowUserItemVO>>({
      url: "/api/customer/me/following",
      params
    });
  },
  followers(params: PageQuery) {
    return request<PageInfo<AppFollowUserItemVO>>({
      url: "/api/customer/me/followers",
      params
    });
  },
  doc(type: AppDocType) {
    return request<AppDocVO>({
      url: `/api/customer/me/docs/${type}`
    });
  }
};
