import api from './api';
import { getAllProducts } from './productService';

// GET /api/v1/inventory/product/{productId} - Get product inventory
export const getInventory = (productId) => api.get(`/inventory/product/${productId}`);

// GET /api/v1/inventory/product/{productId}/in-stock - Check if product is in stock
export const isProductInStock = (productId) => api.get(`/inventory/product/${productId}/in-stock`);

// GET /api/v1/inventory/product/{productId}/available-stock?quantity={quantity} - Check stock availability
export const hasAvailableStock = (productId, quantity) =>
    api.get(`/inventory/product/${productId}/available-stock`, { params: { quantity } });

// PUT /api/v1/inventory/product/{productId}?newQuantity={quantity} - Update stock quantity
export const updateStock = (productId, newQuantity) =>
    api.put(`/inventory/product/${productId}`, null, { params: { newQuantity } });

// PATCH /api/v1/inventory/product/{productId}/add?quantity={quantity} - Add stock
export const addStock = (productId, quantity) =>
    api.patch(`/inventory/product/${productId}/add`, null, { params: { quantity } });

// PATCH /api/v1/inventory/product/{productId}/remove?quantity={quantity} - Remove stock
export const removeStock = (productId, quantity) =>
    api.patch(`/inventory/product/${productId}/remove`, null, { params: { quantity } });

// PATCH /api/v1/inventory/product/{productId}/reserve?quantity={quantity} - Reserve stock
export const reserveStock = (productId, quantity) =>
    api.patch(`/inventory/product/${productId}/reserve`, null, { params: { quantity } });

// PATCH /api/v1/inventory/product/{productId}/release?quantity={quantity} - Release reserved stock
export const releaseStock = (productId, quantity) =>
    api.patch(`/inventory/product/${productId}/release`, null, { params: { quantity } });

// GET /api/v1/inventory/low-stock - Get low stock products
export const getLowStockProducts = () => api.get('/inventory/low-stock');

// GET /api/v1/inventory/out-of-stock - Get out of stock products
export const getOutOfStockProducts = () => api.get('/inventory/out-of-stock');

// PATCH /api/v1/inventory/product/{productId}/reorder-level?reorderLevel={level} - Update reorder level
export const updateReorderLevel = (productId, reorderLevel) =>
    api.patch(`/inventory/product/${productId}/reorder-level`, null, { params: { reorderLevel } });

// GET /api/v1/inventory/products/batch?productIds={ids} - Get inventory for multiple products
export const getInventoryBatch = (productIds) => 
    api.get('/inventory/products/batch', { params: { productIds: productIds.join(',') } });

// GET all inventory items (combines with product service)
export const getAllInventoryItems = async () => {
    const products = await getAllProducts();
    const productIds = products.data.map(p => p.product_id || p.productId || p.id);
    
    let inventoryMap = {};
    try {
        const inventoryResponse = await getInventoryBatch(productIds);
        inventoryMap = (inventoryResponse.data || []).reduce((acc, inv) => {
            acc[inv.productId] = inv;
            return acc;
        }, {});
    } catch (error) {
        console.error('Error fetching batch inventory:', error);
    }

    const inventoryItems = products.data.map(product => {
        const productId = product.product_id || product.productId || product.id;
        const inventory = inventoryMap[productId];
        return {
            ...product,
            productId,
            productName: product.product_name || product.productName || product.name,
            stockQuantity: inventory?.stockQuantity || inventory?.quantityInStock || 0,
            reorderLevel: inventory?.reorderLevel || 10,
            reservedQuantity: inventory?.reservedQuantity || 0
        };
    });
    return { data: inventoryItems };
};
