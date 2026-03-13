/**
 * Validation utilities matching backend constraints
 */

export const validators = {
  email: (value) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!value) return "Email is required";
    if (value.length > 100) return "Email must not exceed 100 characters";
    if (!emailRegex.test(value)) return "Invalid email format";
    return true;
  },

  password: (value) => {
    if (!value) return "Password is required";
    if (value.length < 8) return "Password must be at least 8 characters";
    if (value.length > 100) return "Password must not exceed 100 characters";
    return true;
  },

  firstName: (value) => {
    if (!value) return "First name is required";
    if (value.length > 50) return "First name must not exceed 50 characters";
    return true;
  },

  lastName: (value) => {
    if (!value) return "Last name is required";
    if (value.length > 50) return "Last name must not exceed 50 characters";
    return true;
  },

  title: (value, maxLength = 100) => {
    if (!value) return "Title is required";
    if (value.length > maxLength)
      return `Title must not exceed ${maxLength} characters`;
    return true;
  },

  url: (value) => {
    if (!value) return true; // URL is optional
    if (value.length > 2048) return "URL is too long";
    try {
      new URL(value);
      return true;
    } catch {
      return "Invalid URL format";
    }
  },

  price: (value) => {
    if (value === null || value === undefined) return true; // Optional
    if (value < 0) return "Price must be positive";
    if (!/^\d+(\.\d{1,2})?$/.test(value.toString()))
      return "Invalid price format (e.g., 12345678.99)";
    return true;
  },

  description: (value, maxLength = 1000) => {
    if (!value) return true; // Optional
    if (value.length > maxLength)
      return `Description must not exceed ${maxLength} characters`;
    return true;
  },
};
