import http from "./http";
import type { Result } from "../types/api";
import type { PageInfo } from "../types/adminUser";
import type {
  AdminQaAnswerPageItemVO,
  AdminQaQuestionPageItemVO,
  QaManageAnswerPageQueryDTO,
  QaManageQuestionPageQueryDTO,
  QaManageStatusUpdateDTO
} from "../types/qa";

export const pageQaManageQuestions = (query: QaManageQuestionPageQueryDTO) => {
  return http.get<Result<PageInfo<AdminQaQuestionPageItemVO>>>("/admin/qa/manage/question/page", { params: query });
};

export const pageQaManageAnswers = (query: QaManageAnswerPageQueryDTO) => {
  return http.get<Result<PageInfo<AdminQaAnswerPageItemVO>>>("/admin/qa/manage/answer/page", { params: query });
};

export const updateQaManageQuestionStatus = (id: number, payload: QaManageStatusUpdateDTO) => {
  return http.put<Result<null>>(`/admin/qa/manage/question/${id}/status`, payload);
};

export const updateQaManageAnswerStatus = (id: number, payload: QaManageStatusUpdateDTO) => {
  return http.put<Result<null>>(`/admin/qa/manage/answer/${id}/status`, payload);
};

export const deleteQaManageQuestion = (id: number) => {
  return http.delete<Result<null>>(`/admin/qa/manage/question/${id}`);
};

export const deleteQaManageAnswer = (id: number) => {
  return http.delete<Result<null>>(`/admin/qa/manage/answer/${id}`);
};

