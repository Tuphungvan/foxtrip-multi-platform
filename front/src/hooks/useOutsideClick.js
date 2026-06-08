import { useEffect } from 'react';

/**
 * Gọi callback khi người dùng click ra ngoài element được ref.
 * @param {React.RefObject} ref - ref của element cần detect outside click
 * @param {Function} callback - hàm được gọi khi click outside
 */
const useOutsideClick = (ref, callback) => {
    useEffect(() => {
        const handleClick = (e) => {
            if (ref.current && !ref.current.contains(e.target)) {
                callback();
            }
        };

        document.addEventListener('mousedown', handleClick);
        return () => document.removeEventListener('mousedown', handleClick);
    }, [ref, callback]);
};

export default useOutsideClick;
