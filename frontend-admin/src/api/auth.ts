import http from "./http";
import type { Result } from "../types/api";

type LoginPayload = {
  username: string;
  password: string;
};

type LoginResponse = {
  token: string;
  expiresAt: number;
  userId: number;
};

export const login = (payload: LoginPayload) => {
  return http.post<Result<LoginResponse>>("/auth/login", payload);
};
