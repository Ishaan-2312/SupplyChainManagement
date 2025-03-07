package com.IshaanBansal.SupplyChainManagement.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Web3jOrderService {

    @Autowired
    private Web3jService web3jService;

    public String placeOrder(Long orderId, String productName, int quantity, double totalPrice, String supplierAddress) {
        try {
            return web3jService.placeOrder(orderId, productName, quantity, totalPrice, supplierAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error placing order";
        }
    }

    public String markAsDispatched(Long orderId) {
        try {
            return web3jService.markAsDispatched(orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error marking order as dispatched";
        }
    }

    public String markAsReceived(Long orderId) {
        try {
            return web3jService.markAsReceived(orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error marking order as received";
        }
    }
}


