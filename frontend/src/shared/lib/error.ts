export const getErrorMessage = (error: unknown, fallback = "Có lỗi xảy ra") =>
  error instanceof Error && error.message ? error.message : fallback;
