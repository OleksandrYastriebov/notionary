import axiosInstance from "./axiosConfig";

export const authApi = {
  signUp: (userData) => axiosInstance.post("/sign-up", userData),

  signIn: (credentials) => axiosInstance.post("/sign-in", credentials),

  signOut: () => axiosInstance.post("/sign-out"), // cookie sent automatically

  refreshToken: () => axiosInstance.post("/refresh-token"), // cookie sent automatically

  confirmEmail: (token) => axiosInstance.get(`/confirm-email?token=${token}`),
};
