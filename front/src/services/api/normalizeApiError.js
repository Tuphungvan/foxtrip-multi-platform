const DEFAULT_ERROR_MESSAGE = 'Có lỗi ngoài ý muốn. Vui lòng thử lại sau.';

/**
 * Normalize axios errors to a predictable shape for UI and mutations.
 */
export const normalizeApiError = (error) => {
  const status = error?.response?.status ?? 0;
  const body = error?.response?.data;

  let message = error?.message ?? DEFAULT_ERROR_MESSAGE;
  if (message === 'Network Error') {
    message = 'Lỗi kết nối mạng. Vui lòng kiểm tra lại đường truyền của bạn.';
  }
  let errors = [];
  let data = null;

  if (body) {
    if (typeof body === 'string') {
      message = body;
    } else if (typeof body === 'object') {
      // 1. Phác thảo thông điệp chính từ ApiResponse (ưu tiên cao nhất)
      if (body.message && typeof body.message === 'string') {
          message = body.message;
      } else if (body.error && typeof body.error === 'string') {
          message = body.error; // Fallback cho Spring
      }
      
      // 2. Chắp nối mảng ApiError ({field, message}) từ MethodArgumentNotValidException
      if (Array.isArray(body.errors) && body.errors.length > 0) {
          errors = body.errors;
          // Kết nối các message nhỏ lại với nhau
          const validationMessages = errors
              .filter(e => e && e.message)
              .map(e => e.message);
              
          if (validationMessages.length > 0) {
              // Nếu đã có message chính (ví dụ: "Dữ liệu không hợp lệ"), ta nối thêm chi tiết
              message = message ? `${message} (${validationMessages.join('; ')})` : validationMessages.join('; ');
          }
      }

      data = body.data ?? null;
    }
  }

  return {
    status,
    message,
    errors,
    data,
    raw: error,
  };
};
