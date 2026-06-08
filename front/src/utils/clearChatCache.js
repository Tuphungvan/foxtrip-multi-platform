const CHAT_STORAGE_HINT = 'chat';

export const clearChatCache = () => {
  if (typeof window === 'undefined' || !window.localStorage) {
    return;
  }

  const keys = Object.keys(window.localStorage);
  keys.forEach((key) => {
    if (String(key).toLowerCase().includes(CHAT_STORAGE_HINT)) {
      window.localStorage.removeItem(key);
    }
  });
};
