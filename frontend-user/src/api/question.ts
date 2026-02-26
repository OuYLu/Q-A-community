import { request } from "@/api/http";
import { BASE_URL } from "@/utils/constants";

export interface AppQuestionCreateDTO {
  title: string;
  content?: string;
  categoryId?: number;
  topicId?: number;
  tagIds?: number[];
  tagNames?: string[];
  imageUrls?: string[];
}

export interface AppAnswerCreateDTO {
  content: string;
  isAnonymous?: number;
  imageUrls?: string[];
}

export interface AppQuestionAnswerVO {
  id: number;
  questionId: number;
  content: string;
  authorId: number;
  authorName: string;
  authorAvatar?: string;
  imageUrls?: string[];
  isAnonymous?: number;
  likeCount?: number;
  commentCount?: number;
  favoriteCount?: number;
  liked?: boolean;
  favorited?: boolean;
  bestAnswer?: boolean;
  canRecommend?: boolean;
  createdAt?: string;
  canEdit?: boolean;
}

export interface AppAnswerCommentVO {
  id: number;
  answerId: number;
  parentId?: number;
  authorId: number;
  authorName?: string;
  authorAvatar?: string;
  content: string;
  createdAt?: string;
}

export interface AppAnswerDetailVO {
  answer: AppQuestionAnswerVO;
  questionId: number;
  questionTitle: string;
  comments: AppAnswerCommentVO[];
}

export interface AppQuestionDetailVO {
  id: number;
  title: string;
  content?: string;
  imageUrls?: string[];
  categoryId?: number;
  categoryName?: string;
  topicId?: number;
  topicTitle?: string;
  authorId?: number;
  authorName?: string;
  authorAvatar?: string;
  viewCount?: number;
  answerCount?: number;
  likeCount?: number;
  favoriteCount?: number;
  liked?: boolean;
  favorited?: boolean;
  createdAt?: string;
  lastActiveAt?: string;
  tags?: string[];
  answers?: AppQuestionAnswerVO[];
}

export interface PageInfo<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface AppQuestionListItemVO {
  id: number;
  title: string;
  summary?: string;
  imageUrls?: string[];
  categoryId?: number;
  categoryName?: string;
  topicId?: number;
  topicTitle?: string;
  authorId?: number;
  authorName?: string;
  authorAvatar?: string;
  viewCount?: number;
  answerCount?: number;
  likeCount?: number;
  createdAt?: string;
  lastActiveAt?: string;
}

export interface AppMyQuestionItemVO {
  id: number;
  title: string;
  categoryName?: string;
  tags?: string[];
  imageUrls?: string[];
  status: number;
  answerCount: number;
  likeCount: number;
  viewCount?: number;
  acceptedAnswerId?: number;
  createdAt: string;
}

function toAbsoluteUrl(url?: string) {
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/")) return `${BASE_URL}${url}`;
  return `${BASE_URL}/${url}`;
}

function normalizeImageUrls(urls?: string[]) {
  if (!urls || !urls.length) return [];
  return urls.map((u) => toAbsoluteUrl(u)).filter(Boolean);
}

function normalizeAvatar(url?: string) {
  return toAbsoluteUrl(url);
}

function normalizeAnswerItem(item?: AppQuestionAnswerVO): AppQuestionAnswerVO {
  return {
    ...(item || ({} as AppQuestionAnswerVO)),
    authorAvatar: normalizeAvatar(item?.authorAvatar),
    imageUrls: normalizeImageUrls(item?.imageUrls)
  };
}

function normalizeAnswerDetail(data?: AppAnswerDetailVO): AppAnswerDetailVO {
  return {
    ...(data || ({} as AppAnswerDetailVO)),
    answer: normalizeAnswerItem(data?.answer),
    comments: (data?.comments || []).map((item) => ({
      ...item,
      authorAvatar: normalizeAvatar(item?.authorAvatar)
    }))
  };
}

export const questionApi = {
  createQuestion(data: AppQuestionCreateDTO) {
    return request<number>({
      url: "/api/customer/questions",
      method: "POST",
      data
    });
  },
  detail(id: number) {
    return request<AppQuestionDetailVO>({
      url: `/api/customer/questions/${id}`
    }).then((data) => ({
      ...data,
      imageUrls: normalizeImageUrls(data?.imageUrls),
      authorAvatar: normalizeAvatar(data?.authorAvatar),
      answers: (data?.answers || []).map((item) => normalizeAnswerItem(item))
    }));
  },
  page(params: {
    page?: number;
    pageSize?: number;
    keyword?: string;
    categoryId?: number;
    topicId?: number;
    sortBy?: "hot" | "latest";
    onlyUnsolved?: boolean;
  }) {
    return request<PageInfo<AppQuestionListItemVO>>({
      url: "/api/customer/questions",
      params
    }).then((data) => ({
      ...data,
      list: (data?.list || []).map((item) => ({
        ...item,
        authorAvatar: normalizeAvatar(item?.authorAvatar),
        imageUrls: normalizeImageUrls(item?.imageUrls)
      }))
    }));
  },
  myQuestions(params: { page?: number; pageSize?: number }) {
    return request<PageInfo<AppMyQuestionItemVO>>({
      url: "/api/customer/questions/my",
      params
    }).then((data) => ({
      ...data,
      list: (data?.list || []).map((item) => ({
        ...item,
        imageUrls: normalizeImageUrls(item?.imageUrls)
      }))
    }));
  },
  createAnswer(questionId: number, data: AppAnswerCreateDTO) {
    return request<number>({
      url: `/api/customer/questions/${questionId}/answers`,
      method: "POST",
      data
    });
  },
  toggleLike(questionId: number) {
    return request<AppQuestionDetailVO>({
      url: `/api/customer/questions/${questionId}/like`,
      method: "POST"
    });
  },
  toggleFavorite(questionId: number) {
    return request<AppQuestionDetailVO>({
      url: `/api/customer/questions/${questionId}/favorite`,
      method: "POST"
    });
  },
  answerDetail(answerId: number) {
    return request<AppAnswerDetailVO>({
      url: `/api/customer/answers/${answerId}`
    }).then((data) => normalizeAnswerDetail(data));
  },
  toggleAnswerLike(answerId: number) {
    return request<AppAnswerDetailVO>({
      url: `/api/customer/answers/${answerId}/like`,
      method: "POST"
    }).then((data) => normalizeAnswerDetail(data));
  },
  toggleAnswerFavorite(answerId: number) {
    return request<AppAnswerDetailVO>({
      url: `/api/customer/answers/${answerId}/favorite`,
      method: "POST"
    }).then((data) => normalizeAnswerDetail(data));
  },
  recommendBest(questionId: number, answerId: number) {
    return request<void>({
      url: `/api/customer/questions/${questionId}/answers/${answerId}/recommend`,
      method: "POST"
    });
  },
  answerComments(answerId: number) {
    return request<AppAnswerCommentVO[]>({
      url: `/api/customer/answers/${answerId}/comments`
    });
  },
  createAnswerComment(answerId: number, content: string, parentId?: number) {
    return request<number>({
      url: `/api/customer/answers/${answerId}/comments`,
      method: "POST",
      data: { content, parentId }
    });
  }
};
