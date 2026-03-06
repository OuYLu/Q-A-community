"use strict";
const utils_constants = require("./constants.js");
function baseOrigin() {
  try {
    return new URL(utils_constants.BASE_URL).origin;
  } catch {
    return "";
  }
}
function toMediaUrl(raw) {
  const text = String(raw || "").trim();
  if (!text)
    return "";
  if (text.startsWith("/"))
    return `${utils_constants.BASE_URL}${text}`;
  if (!/^https?:\/\//i.test(text))
    return `${utils_constants.BASE_URL}/${text}`;
  if (/^http:\/\//i.test(text)) {
    const origin = baseOrigin();
    try {
      const parsed = new URL(text);
      if (origin)
        return `${origin}${parsed.pathname}${parsed.search}${parsed.hash}`;
      return text.replace(/^http:\/\//i, "https://");
    } catch {
      return text.replace(/^http:\/\//i, "https://");
    }
  }
  return text;
}
exports.toMediaUrl = toMediaUrl;
