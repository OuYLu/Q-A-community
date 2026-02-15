import http from "./http";
import type { Result } from "../types/api";
import type { MenuItem } from "../types/menu";

const normalizeMenu = (items: MenuItem[]): MenuItem[] => {
  return items.map((item) => {
    const path = item.path ?? (item.pathOrApi?.startsWith("/") ? item.pathOrApi : undefined);
    return {
      ...item,
      path,
      children: item.children ? normalizeMenu(item.children) : []
    };
  });
};

export const fetchMenu = async () => {
  const res = await http.get<Result<MenuItem[]>>("/admin/menu/list");
  return {
    ...res,
    data: normalizeMenu(res.data)
  };
};
