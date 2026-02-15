export type MenuItem = {
  code: string;
  name: string;
  path?: string;
  pathOrApi?: string;
  component?: string;
  type?: string;
  visible?: number;
  children?: MenuItem[];
};
