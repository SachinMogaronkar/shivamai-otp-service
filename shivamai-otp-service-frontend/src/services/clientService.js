import {
  requestOtpApi,
  verifyOtpApi,
} from "../api/clientApi";

export const requestOtp = (clientId, clientSecret, data) =>
  requestOtpApi(clientId, clientSecret, data);

export const verifyOtp = (clientId, clientSecret, data) =>
  verifyOtpApi(clientId, clientSecret, data);