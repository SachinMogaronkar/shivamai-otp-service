import api from "./axios";

export const requestOtpApi = (clientId, clientSecret, data) =>
  api.post("/otp/request", data, {
    headers: {
      "X-Client-Id": clientId,
      "X-Client-Secret": clientSecret,
    },
  });

export const verifyOtpApi = (clientId, clientSecret, data) =>
  api.post("/otp/verify", data, {
    headers: {
      "X-Client-Id": clientId,
      "X-Client-Secret": clientSecret,
    },
  });