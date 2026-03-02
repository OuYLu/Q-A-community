import { request } from "@/api/http";
import { BASE_URL } from "@/utils/constants";

export interface PageInfo<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface AppCategoryVO {
  id: number;
  name: string;
  icon: string;
  questionCount: number;
}

export interface AppCategoryTreeNodeVO {
  id: number;
  name: string;
  label?: string;
  parentId?: number | null;
  icon?: string;
  questionCount?: number;
  children?: AppCategoryTreeNodeVO[];
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

export interface AppQuestionHotItemVO {
  id: number;
  title: string;
  viewCount: number;
  answerCount: number;
  likeCount: number;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  createdAt: string;
}

export interface AppDiscoverQuestionItemVO {
  id: number;
  title: string;
  summary?: string;
  viewCount?: number;
  answerCount?: number;
  likeCount?: number;
  authorName?: string;
  createdAt?: string;
}

export interface AppExpertCardVO {
  userId: number;
  nickname: string;
  avatar: string;
  realName: string;
  organization: string;
  title: string;
  expertise: string;
}

export interface AppHomeVO {
  categories: AppCategoryVO[];
  hotTopics: AppTopicListItemVO[];
  hotQuestions: AppQuestionHotItemVO[];
  experts: AppExpertCardVO[];
}

function toAbsoluteUrl(url?: string) {
  if (!url) return "";
  const text = url.trim();
  if (!text) return "";
  if (text.startsWith("http://") || text.startsWith("https://")) return text;
  if (text.startsWith("/")) return `${BASE_URL}${text}`;
  return `${BASE_URL}/${text}`;
}

function normalizeCategory(item: AppCategoryVO): AppCategoryVO {
  return {
    ...item,
    icon: toAbsoluteUrl(item.icon)
  };
}

function normalizeCategoryTree(node: AppCategoryTreeNodeVO): AppCategoryTreeNodeVO {
  return {
    ...node,
    icon: toAbsoluteUrl(node.icon),
    children: node.children?.map((x) => normalizeCategoryTree(x))
  };
}

function normalizeTopic(item: AppTopicListItemVO): AppTopicListItemVO {
  return {
    ...item,
    coverImg: toAbsoluteUrl(item.coverImg)
  };
}

function normalizeHotQuestion(item: AppQuestionHotItemVO): AppQuestionHotItemVO {
  return {
    ...item,
    authorAvatar: toAbsoluteUrl(item.authorAvatar)
  };
}

function normalizeExpert(item: AppExpertCardVO): AppExpertCardVO {
  return {
    ...item,
    avatar: toAbsoluteUrl(item.avatar)
  };
}

export const discoverApi = {
  getHome() {
    return request<AppHomeVO>({
      url: "/api/customer/discover/home"
    }).then((data) => ({
      ...data,
      categories: (data?.categories || []).map((x) => normalizeCategory(x)),
      hotTopics: (data?.hotTopics || []).map((x) => normalizeTopic(x)),
      hotQuestions: (data?.hotQuestions || []).map((x) => normalizeHotQuestion(x)),
      experts: (data?.experts || []).map((x) => normalizeExpert(x))
    }));
  },
  getCategories() {
    return request<AppCategoryVO[]>({
      url: "/api/customer/discover/categories"
    }).then((data) => (data || []).map((x) => normalizeCategory(x)));
  },
  getCategoryTree(parentId?: number) {
    return request<AppCategoryTreeNodeVO[]>({
      url: "/api/customer/discover/categories/tree",
      params: { parentId }
    }).then((data) => (data || []).map((x) => normalizeCategoryTree(x)));
  },
  getHotTopics(limit = 6) {
    return request<AppTopicListItemVO[]>({
      url: "/api/customer/discover/topics/hot",
      params: { limit }
    }).then((data) => (data || []).map((x) => normalizeTopic(x)));
  },
  getHotQuestions(limit = 10) {
    return request<AppQuestionHotItemVO[]>({
      url: "/api/customer/discover/rank/hot",
      params: { limit }
    }).then((data) => (data || []).map((x) => normalizeHotQuestion(x)));
  },
  getExperts(limit = 6) {
    return request<AppExpertCardVO[]>({
      url: "/api/customer/discover/experts",
      params: { limit }
    }).then((data) => (data || []).map((x) => normalizeExpert(x)));
  },
  getQuestionPage(params: {
    page?: number;
    pageSize?: number;
    keyword?: string;
    categoryId?: number;
    topicId?: number;
    sortBy?: "hot" | "latest";
    onlyUnsolved?: boolean;
  }) {
    return request<PageInfo<AppDiscoverQuestionItemVO>>({
      url: "/api/customer/discover/questions",
      params
    });
  }
};
