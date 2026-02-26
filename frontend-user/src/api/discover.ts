import { request } from "@/api/http";

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

export const discoverApi = {
  getHome() {
    return request<AppHomeVO>({
      url: "/api/customer/discover/home"
    });
  },
  getCategories() {
    return request<AppCategoryVO[]>({
      url: "/api/customer/discover/categories"
    });
  },
  getCategoryTree(parentId?: number) {
    return request<AppCategoryTreeNodeVO[]>({
      url: "/api/customer/discover/categories/tree",
      params: { parentId }
    });
  },
  getHotTopics(limit = 6) {
    return request<AppTopicListItemVO[]>({
      url: "/api/customer/discover/topics/hot",
      params: { limit }
    });
  },
  getHotQuestions(limit = 10) {
    return request<AppQuestionHotItemVO[]>({
      url: "/api/customer/discover/rank/hot",
      params: { limit }
    });
  },
  getExperts(limit = 6) {
    return request<AppExpertCardVO[]>({
      url: "/api/customer/discover/experts",
      params: { limit }
    });
  }
};
