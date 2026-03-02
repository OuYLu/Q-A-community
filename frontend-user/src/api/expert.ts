import { request } from "@/api/http";

export interface ExpertProofFileDTO {
  url: string;
  name: string;
}

export interface ExpertApplyDTO {
  realName: string;
  organization: string;
  title: string;
  expertise: string;
  proofFiles: {
    LICENSE: ExpertProofFileDTO[];
    EMPLOYMENT: ExpertProofFileDTO[];
    TITLE?: ExpertProofFileDTO[];
    EDUCATION?: ExpertProofFileDTO[];
    OTHER?: ExpertProofFileDTO[];
  };
}

export interface ExpertPostCreateDTO {
  categoryId: number;
  title: string;
  summary?: string;
  coverImage?: string;
  content?: string;
  imageUrls?: string[];
  contentBlocks?: Array<{ type: "text" | "image"; text?: string; url?: string }>;
  tagNames?: string[];
}

export interface AppExpertPostItemVO {
  id: number;
  categoryId?: number;
  categoryName?: string;
  authorUserId?: number;
  authorName?: string;
  authorAvatar?: string;
  authorExpertise?: string;
  source?: string | null;
  title?: string;
  summary?: string;
  coverImage?: string;
  imageUrls?: string[];
  tagNames?: string[];
  status?: number;
  viewCount?: number;
  likeCount?: number;
  favoriteCount?: number;
  createdAt?: string;
}

export interface AppExpertPostCategoryVO {
  id: number;
  parentId?: number | null;
  name: string;
}

export interface AppExpertContentBlockVO {
  type: "text" | "image";
  text?: string;
  url?: string;
}

export interface AppExpertPostDetailVO {
  id: number;
  categoryId?: number;
  categoryName?: string;
  authorUserId?: number;
  authorName?: string;
  authorAvatar?: string;
  authorExpertise?: string | null;
  authorTitle?: string | null;
  source?: string | null;
  title: string;
  summary?: string;
  coverImage?: string;
  imageUrls?: string[];
  tagNames?: string[];
  status?: number;
  content?: string;
  contentBlocks?: AppExpertContentBlockVO[];
  viewCount?: number;
  likeCount?: number;
  favoriteCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export const expertApi = {
  apply(data: ExpertApplyDTO) {
    return request<void>({
      url: "/api/expert/apply",
      method: "POST",
      data
    });
  },
  createPost(data: ExpertPostCreateDTO) {
    return request<{ id: number }>({
      url: "/api/expert/posts",
      method: "POST",
      data
    });
  },
  categories() {
    return request<AppExpertPostCategoryVO[]>({
      url: "/api/expert/posts/categories"
    });
  },
  page(params?: { page?: number; pageSize?: number; sortBy?: "hot" | "latest"; categoryId?: number }) {
    return request<PageInfo<AppExpertPostItemVO>>({
      url: "/api/expert/posts",
      params
    });
  },
  myPosts(params?: { page?: number; pageSize?: number; status?: number; keyword?: string }) {
    return request<PageInfo<AppExpertPostItemVO>>({
      url: "/api/expert/posts/my",
      params
    });
  },
  detail(id: number) {
    return request<AppExpertPostDetailVO>({
      url: `/api/expert/posts/${id}`
    });
  }
};