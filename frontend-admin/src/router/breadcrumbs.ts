import { computed } from "vue";
import { useRoute } from "vue-router";
import { useMenuStore } from "../store/menu";
import { resolveMenuPath } from "../router/dynamic";

export const useBreadcrumbs = () => {
  const route = useRoute();
  const menuStore = useMenuStore();

  const breadcrumbs = computed(() => {
    const result: { title: string; path?: string }[] = [];
    const walk = (items: typeof menuStore.menuTree, parents: string[]) => {
      items.forEach((item) => {
        const currentParents = [...parents, item.name];
        const currentPath = item.path ?? item.pathOrApi ?? resolveMenuPath(item.code);
        if (currentPath === route.path) {
          currentParents.forEach((title, index) => {
            result.push({ title, path: index === currentParents.length - 1 ? route.path : undefined });
          });
        }
        if (item.children && item.children.length > 0) {
          walk(item.children, currentParents);
        }
      });
    };
    walk(menuStore.menuTree, []);
    return result.length > 0 ? result : [{ title: route.meta.title as string }];
  });

  return { breadcrumbs };
};
