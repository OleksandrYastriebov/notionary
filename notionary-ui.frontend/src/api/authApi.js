import axiosInstance from "./axiosConfig";

export const authApi = {
  signUp: (userData) => axiosInstance.post("/sign-up", userData),

  signIn: (credentials) => axiosInstance.post("/sign-in", credentials),

  signOut: (data) => axiosInstance.post("/sign-out", data),

  refreshToken: (data) => axiosInstance.post("/refresh-token", data),

  confirmEmail: (token) => axiosInstance.get(`/confirm-email?token=${token}`),
};
