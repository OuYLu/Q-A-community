export type User = {
  id: number;
  username: string;
  nickname: string;
  roles: string[];
  perms?: string[];
};
