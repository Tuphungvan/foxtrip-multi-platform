import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { cartService } from '../services/api/cartService';

export const useCartStore = create(
    persist(
        (set, get) => ({
            items: [], // Array of { tourId, tour, quantity, addons }
            isLoading: false,

            // Lấy giỏ hàng từ Backend
            fetchCart: async () => {
                set({ isLoading: true });
                try {
                    const res = await cartService.getCart();
                    if (res?.data?.items) {
                        // Map lại format từ backend về format của store
                        const items = res.data.items.map(item => ({
                            tourId: item.tourId,
                            tour: item.tour,
                            quantity: item.quantity,
                            addons: item.tour.addons || []
                        }));
                        set({ items });
                    }
                } catch (error) {
                    console.error('Failed to fetch cart:', error);
                } finally {
                    set({ isLoading: false });
                }
            },
            
            addItem: async (tour, quantity = 1, addons = [], isAuthenticated = false) => {
                const currentItems = get().items;
                const existingIndex = currentItems.findIndex(item => item.tourId === tour.id);
                let newQuantity = quantity;
                
                if (existingIndex > -1) {
                    newQuantity = currentItems[existingIndex].quantity + quantity;
                }

                if (isAuthenticated) {
                    try {
                        const res = await cartService.upsertItem(tour.id, newQuantity);
                        if (res?.data?.items) {
                            const items = res.data.items.map(item => ({
                                tourId: item.tourId,
                                tour: item.tour,
                                quantity: item.quantity,
                                addons: item.tour.addons || []
                            }));
                            set({ items });
                            return;
                        }
                    } catch (error) {
                        console.error('Failed to sync addItem to backend:', error);
                    }
                }

                // Fallback for non-auth or error
                if (existingIndex > -1) {
                    const newItems = [...currentItems];
                    newItems[existingIndex].quantity = newQuantity;
                    newItems[existingIndex].addons = addons;
                    set({ items: newItems });
                } else {
                    set({ items: [...currentItems, { tourId: tour.id, tour, quantity, addons }] });
                }
            },
            
            removeItem: async (tourId, isAuthenticated = false) => {
                if (isAuthenticated) {
                    try {
                        await cartService.upsertItem(tourId, 0);
                    } catch (error) {
                        console.error('Failed to sync removeItem to backend:', error);
                    }
                }
                set({ items: get().items.filter(item => item.tourId !== tourId) });
            },
            
            updateQuantity: async (tourId, quantity, isAuthenticated = false) => {
                if (isAuthenticated) {
                    try {
                        const res = await cartService.upsertItem(tourId, quantity);
                        if (res?.data?.items) {
                            const items = res.data.items.map(item => ({
                                tourId: item.tourId,
                                tour: item.tour,
                                quantity: item.quantity,
                                addons: item.tour.addons || []
                            }));
                            set({ items });
                            return;
                        }
                    } catch (error) {
                        console.error('Failed to sync updateQuantity to backend:', error);
                    }
                }
                const newItems = get().items.map(item => 
                    item.tourId === tourId ? { ...item, quantity: Math.max(1, quantity) } : item
                );
                set({ items: newItems });
            },
            
            clearCart: async (isAuthenticated = false) => {
                if (isAuthenticated) {
                    try {
                        await cartService.clearCart();
                    } catch (error) {
                        console.error('Failed to sync clearCart to backend:', error);
                    }
                }
                set({ items: [] });
            },
            
            getTotalItems: () => get().items.reduce((sum, item) => sum + item.quantity, 0),
            
            getTotalAmount: () => get().items.reduce((sum, item) => {
                const tourPrice = (item.tour.finalPrice || item.tour.price || 0) * item.quantity;
                const addonsPrice = (item.addons || []).reduce((aSum, addon) => aSum + (addon.price || 0), 0);
                return sum + tourPrice + addonsPrice;
            }, 0)
        }),
        {
            name: 'foxtrip-cart', // Sẽ được clear khi logout
        }
    )
);
