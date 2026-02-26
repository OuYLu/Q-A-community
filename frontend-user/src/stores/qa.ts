import { defineStore } from "pinia";
import type { AppQuestionHotItemVO } from "@/api/discover";
import type { AppSearchQuestionVO } from "@/api/search";
import type { AppTopicQuestionItemVO } from "@/api/topic";

export interface QaQuestion {
  id: number;
  title: string;
  content: string;
  tags: string[];
  authorName: string;
  createdAt: string;
  answerCount: number;
  viewCount: number;
  likeCount: number;
  isSolved: boolean;
}

export interface QaAnswer {
  id: number;
  questionId: number;
  authorName: string;
  content: string;
  createdAt: string;
  likeCount: number;
}

function nowText() {
  return new Date().toLocaleString();
}

export const useQaStore = defineStore("qa", {
  state: () => ({
    questions: [] as QaQuestion[],
    answersByQuestion: {} as Record<number, QaAnswer[]>
  }),
  getters: {
    getQuestionById: (state) => (id: number) => state.questions.find((q) => q.id === id),
    getAnswers: (state) => (questionId: number) => state.answersByQuestion[questionId] || []
  },
  actions: {
    upsertQuestion(partial: Partial<QaQuestion> & { id: number; title: string }) {
      const index = this.questions.findIndex((q) => q.id === partial.id);
      const next: QaQuestion = {
        id: partial.id,
        title: partial.title,
        content: partial.content || "暂无详细描述",
        tags: partial.tags || ["健康", "养生"],
        authorName: partial.authorName || "匿名用户",
        createdAt: partial.createdAt || nowText(),
        answerCount: partial.answerCount || 0,
        viewCount: partial.viewCount || 0,
        likeCount: partial.likeCount || 0,
        isSolved: partial.isSolved || false
      };
      if (index < 0) {
        this.questions.unshift(next);
      } else {
        this.questions[index] = {
          ...this.questions[index],
          ...next
        };
      }
    },
    upsertFromHotRank(items: AppQuestionHotItemVO[]) {
      items.forEach((it) => {
        this.upsertQuestion({
          id: it.id,
          title: it.title,
          content: "来自热榜的问题，点击查看详情后可继续补充。",
          answerCount: it.answerCount,
          viewCount: it.viewCount,
          likeCount: it.likeCount,
          authorName: it.authorName,
          createdAt: it.createdAt,
          tags: ["热榜"]
        });
      });
    },
    upsertFromSearch(items: AppSearchQuestionVO[]) {
      items.forEach((it) => {
        this.upsertQuestion({
          id: it.id,
          title: it.title,
          content: it.summary || "暂无详细描述",
          answerCount: it.answerCount || 0,
          viewCount: it.viewCount || 0,
          authorName: it.authorName || "匿名用户",
          createdAt: it.createdAt,
          tags: ["搜索"]
        });
      });
    },
    upsertFromTopicQuestions(items: AppTopicQuestionItemVO[]) {
      items.forEach((it) => {
        this.upsertQuestion({
          id: it.id,
          title: it.title,
          content: it.summary || "专题问题",
          answerCount: it.answerCount,
          viewCount: it.viewCount,
          likeCount: it.likeCount || 0,
          authorName: it.authorName || "匿名用户",
          createdAt: it.createdAt,
          tags: ["专题"]
        });
      });
    },
    askQuestion(payload: { title: string; content: string; tags: string[]; authorName?: string }) {
      const question: QaQuestion = {
        id: Date.now(),
        title: payload.title,
        content: payload.content,
        tags: payload.tags,
        authorName: payload.authorName || "我",
        createdAt: nowText(),
        answerCount: 0,
        viewCount: 0,
        likeCount: 0,
        isSolved: false
      };
      this.questions.unshift(question);
      this.answersByQuestion[question.id] = [];
      return question.id;
    },
    answerQuestion(payload: { questionId: number; content: string; authorName?: string }) {
      const answer: QaAnswer = {
        id: Date.now(),
        questionId: payload.questionId,
        authorName: payload.authorName || "我",
        content: payload.content,
        createdAt: nowText(),
        likeCount: 0
      };
      if (!this.answersByQuestion[payload.questionId]) {
        this.answersByQuestion[payload.questionId] = [];
      }
      this.answersByQuestion[payload.questionId].unshift(answer);
      const question = this.questions.find((q) => q.id === payload.questionId);
      if (question) {
        question.answerCount += 1;
      }
    },
    ensureMockAnswer(questionId: number) {
      const answers = this.answersByQuestion[questionId];
      if (answers && answers.length > 0) return;
      this.answersByQuestion[questionId] = [
        {
          id: Date.now(),
          questionId,
          authorName: "养生达人李姐",
          content:
            "建议先固定作息、晚饭后减少刺激饮品、睡前做放松训练。若长期失眠，请及时线下就医评估。",
          createdAt: nowText(),
          likeCount: 32
        }
      ];
    }
  }
});
