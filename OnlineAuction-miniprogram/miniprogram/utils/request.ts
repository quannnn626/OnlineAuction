type HttpMethod = "GET" | "POST";

interface RequestOptions {
  url: string;
  method?: HttpMethod;
  data?: Record<string, unknown>;
}

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

function getSetCookie(headers: WechatMiniprogram.IAnyObject): string {
  const h = headers || {};
  const value = (h["Set-Cookie"] || h["set-cookie"] || "") as string;
  if (!value) return "";
  const m = value.match(/JSESSIONID=[^;]+/);
  return m ? m[0] : "";
}

export function request<T>(options: RequestOptions): Promise<T> {
  const app = getApp<IAppOption>();
  const baseUrl = app.globalData.baseUrl;
  const sessionId = app.globalData.sessionId || wx.getStorageSync("sessionId") || "";

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl}${options.url}`,
      method: options.method || "GET",
      data: options.data || {},
      header: sessionId ? { Cookie: sessionId } : {},
      success: (res) => {
        const newSessionId = getSetCookie(res.header || {});
        if (newSessionId) {
          app.globalData.sessionId = newSessionId;
          wx.setStorageSync("sessionId", newSessionId);
        }

        const body = res.data as ApiResult<T>;
        if (body && body.code === 200) {
          resolve(body.data);
          return;
        }
        reject(new Error((body && body.message) || "请求失败"));
      },
      fail: (err) => reject(err),
    });
  });
}
