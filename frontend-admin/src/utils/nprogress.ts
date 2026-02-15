import NProgress from "nprogress";
import "nprogress/nprogress.css";

NProgress.configure({ showSpinner: false, trickleSpeed: 120 });

export const startProgress = (): void => {
  NProgress.start();
};

export const stopProgress = (): void => {
  NProgress.done();
};
