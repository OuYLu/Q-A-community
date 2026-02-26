import { request } from "@/api/http";

export interface LoginDTO {
  username: string;
  password: string;
}

export interface LoginVO {
  token: string;
  expiresAt: number;
  userId: number;
}

export interface CustomerRegisterDTO {
  username: string;
  password: string;
  nickname?: string;
  phone?: string;
  email?: string;
  avatar?: string;
}

export interface UserVO {
  id?: number;
  username?: string;
  nickname?: string;
  phone?: string;
  email?: string;
  avatar?: string;
  enabled?: boolean;
}

export interface WechatLoginDTO {
  code: string;
  nickname?: string;
  avatar?: string;
}

export interface WechatLoginVO {
  token: string | null;
  expiresAt: number;
  userId: number;
  username?: string;
  newUser: boolean;
  needPhoneBind: boolean;
  bindTicket: string | null;
}

export interface WechatBindPhoneDTO {
  bindTicket: string;
  phoneCode?: string;
  phone?: string;
  nickname?: string;
  avatar?: string;
}

export const authApi = {
  login(data: LoginDTO) {
    return request<LoginVO>({
      url: "/api/auth/login",
      method: "POST",
      data,
      withAuth: false
    });
  },
  register(data: CustomerRegisterDTO) {
    return request<UserVO>({
      url: "/api/customer/register",
      method: "POST",
      data,
      withAuth: false
    });
  },
  wechatLogin(data: WechatLoginDTO) {
    return request<WechatLoginVO>({
      url: "/api/auth/wechat/login",
      method: "POST",
      data,
      withAuth: false
    });
  },
  wechatBindPhone(data: WechatBindPhoneDTO) {
    return request<WechatLoginVO>({
      url: "/api/auth/wechat/bind-phone",
      method: "POST",
      data,
      withAuth: false
    });
  }
};
