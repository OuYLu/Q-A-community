import http from "./http";
import type { Result } from "../types/api";
import type { PageInfo } from "../types/adminUser";
import type {
  AdminTopicDetailVO,
  AdminTopicListItemVO,
  QaTopicPageQueryDTO,
  QaTopicSaveDTO,
  QaTopicStatusUpdateDTO,
  TopicStatsVO,
  TopicTrendPointVO
} from "../types/topic";

export const pageTopics = (query: QaTopicPageQueryDTO) => {
  return http.get<Result<PageInfo<AdminTopicListItemVO>>>("/admin/qa/topic/page", { params: query });
};

export const getTopicDetail = (id: number) => {
  return http.get<Result<AdminTopicDetailVO>>(`/admin/qa/topic/${id}`);
};

export const createTopic = (payload: QaTopicSaveDTO) => {
  return http.post<Result<Record<string, number>>>("/admin/qa/topic", payload);
};

export const updateTopic = (id: number, payload: QaTopicSaveDTO) => {
  return http.put<Result<null>>(`/admin/qa/topic/${id}`, payload);
};

export const deleteTopic = (id: number) => {
  return http.delete<Result<null>>(`/admin/qa/topic/${id}`);
};

export const updateTopicStatus = (id: number, payload: QaTopicStatusUpdateDTO) => {
  return http.put<Result<null>>(`/admin/qa/topic/${id}/status`, payload);
};

export const getTopicTrend = (id: number, days = 7) => {
  return http.get<Result<TopicTrendPointVO[]>>(`/admin/qa/topic/${id}/trend`, { params: { days } });
};

export const getTopicStats = (id: number) => {
  return http.get<Result<TopicStatsVO>>(`/admin/qa/topic/${id}/stats`);
};
