import React, { useState, useRef, useEffect } from 'react';
import { FaRobot, FaPaperPlane, FaTimes, FaChevronDown } from 'react-icons/fa';
import { chatbotService } from '../../services/api/chatbotService';
import { useUIStore } from '../../store/useUIStore';
import { useAuthStore } from '../../store/useAuthStore';

const Chatbot = () => {
    const { isChatbotOpen: isOpen, closeChatbot, toggleChatbot } = useUIStore();
    const { isAuthenticated } = useAuthStore();
    const [messages, setMessages] = useState([
        { role: 'assistant', content: 'Xin chào! Tôi là FoxBot, trợ lý du lịch ảo của bạn. Tôi có thể giúp gì cho bạn hôm nay?' }
    ]);
    const [inputValue, setInputValue] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        if (isOpen) {
            scrollToBottom();
        }
    }, [messages, isOpen]);

    const handleSend = async (e) => {
        e.preventDefault();
        if (!inputValue.trim() || isLoading) return;

        const userMessage = { role: 'user', content: inputValue };
        setMessages(prev => [...prev, userMessage]);
        setInputValue('');
        setIsLoading(true);

        if (!isAuthenticated) {
            setTimeout(() => {
                const aiMessage = {
                    role: 'assistant',
                    content: 'Vui lòng đăng nhập để kích hoạt chức năng.\n\nMọi thắc mắc vui lòng liên hệ:\nSĐT: 0859605024\nEmail: foxtripgroup@gmail.com'
                };
                setMessages(prev => [...prev, aiMessage]);
                setIsLoading(false);
            }, 800);
            return;
        }

        try {
            // Prepare history for backend (excluding the last message we just added)
            const history = messages.map(m => ({ role: m.role, content: m.content }));

            const res = await chatbotService.chat(inputValue, history);
            console.log('Chatbot API Response:', res);
            const aiMessage = {
                role: 'assistant',
                content: res.message || 'Xin lỗi, tôi gặp sự cố khi xử lý yêu cầu của bạn.'
            };
            setMessages(prev => [...prev, aiMessage]);
        } catch (error) {
            console.error('Chat error:', error);
            setMessages(prev => [...prev, { role: 'assistant', content: 'Hiện tại tôi đang bận, vui lòng thử lại sau nhé!' }]);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="fixed bottom-6 right-6 z-[100] flex flex-col items-end font-sans">
            {/* Chat Window */}
            {isOpen && (
                <div className="w-[340px] h-[480px] bg-white rounded-3xl shadow-xl border border-slate-100 flex flex-col overflow-hidden mb-4 animate-zoom-in">
                    {/* Header */}
                    <div className="bg-[#129AF2] p-4 text-white flex items-center justify-between shrink-0">
                        <div className="flex items-center gap-2.5">
                            <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">
                                <FaRobot className="text-base" />
                            </div>
                            <div>
                                <h4 className="font-bold text-sm tracking-tight">FoxBot</h4>
                                <div className="flex items-center gap-1">
                                    <span className="w-1.5 h-1.5 rounded-full bg-green-400"></span>
                                    <span className="text-[10px] font-medium opacity-80 uppercase tracking-wider">Trực tuyến</span>
                                </div>
                            </div>
                        </div>
                        <button onClick={closeChatbot} className="text-white/60 hover:text-white transition-colors">
                            <FaChevronDown />
                        </button>
                    </div>

                    {/* Messages Area */}
                    <div className="flex-grow overflow-y-auto p-4 space-y-4 custom-scrollbar bg-white">
                        {messages.map((msg, idx) => (
                            <div key={idx} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                                <div className={`max-w-[80%] p-3 rounded-2xl text-[13px] font-medium whitespace-pre-wrap ${msg.role === 'user'
                                        ? 'bg-[#129AF2] text-white rounded-tr-none'
                                        : 'bg-slate-100 text-slate-700 rounded-tl-none'
                                    }`}>
                                    {msg.content}
                                </div>
                            </div>
                        ))}
                        {isLoading && (
                            <div className="flex justify-start">
                                <div className="bg-slate-100 p-3 rounded-2xl rounded-tl-none flex gap-1">
                                    <span className="w-1 h-1 rounded-full bg-slate-400 animate-bounce"></span>
                                    <span className="w-1 h-1 rounded-full bg-slate-400 animate-bounce delay-100"></span>
                                    <span className="w-1 h-1 rounded-full bg-slate-400 animate-bounce delay-200"></span>
                                </div>
                            </div>
                        )}
                        <div ref={messagesEndRef} />
                    </div>

                    {/* Input Area */}
                    <form onSubmit={handleSend} className="p-3 bg-white border-t border-slate-50 shrink-0">
                        <div className="relative">
                            <input
                                type="text"
                                value={inputValue}
                                onChange={(e) => setInputValue(e.target.value)}
                                placeholder="Hỏi tôi bất cứ điều gì..."
                                className="w-full pl-4 pr-10 py-2.5 rounded-xl bg-slate-50 border-none focus:ring-1 focus:ring-[#129AF2]/20 transition-all font-medium text-xs"
                            />
                            <button
                                type="submit"
                                disabled={!inputValue.trim() || isLoading}
                                className={`absolute right-1.5 top-1/2 -translate-y-1/2 w-7 h-7 rounded-lg flex items-center justify-center transition-all ${inputValue.trim() && !isLoading
                                        ? 'bg-[#129AF2] text-white'
                                        : 'bg-slate-100 text-slate-300'
                                    }`}
                            >
                                <FaPaperPlane className="text-[10px]" />
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Toggle Button */}
            <button
                onClick={toggleChatbot}
                className={`w-12 h-12 rounded-full flex items-center justify-center shadow-lg transition-all duration-300 hover:scale-105 active:scale-95 ${isOpen ? 'bg-slate-800 text-white' : 'bg-[#129AF2] text-white'
                    }`}
            >
                {isOpen ? <FaTimes className="text-lg" /> : <FaRobot className="text-xl" />}
            </button>
        </div>

    );
};

export default Chatbot;

