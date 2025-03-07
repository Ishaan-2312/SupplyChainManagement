package com.IshaanBansal.SupplyChainManagement.Service;


import com.IshaanBansal.SupplyChainManagement.Model.Order;
import com.IshaanBansal.SupplyChainManagement.Repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {


    //Create Order
    //Get Order By Id
    //Get All Orders
    //Mark the Orders for Dispatched And Received

    Logger logger= LoggerFactory.getLogger(OrderService.class);
    @Autowired
    private OrderRepository orderRepository;
//    public Order createOrder(Order order) {
//        order.setStatus("PENDING");
//        return orderRepository.save(order);
//
//    }

    public Optional<Order> getOrderById(Long orderId) {
        if(orderRepository.existsById(orderId)) {

            return orderRepository.findById(orderId);
        }

        return null;
    }

    public List<Order> getAllOrders() {
        List<Order> orders= orderRepository.findAll();
        if(orders.isEmpty()){
            return null;
        }
        return orders;
    }

//    public Order markAsDispatched(Long orderId) {
//        Optional<Order> order=getOrderById(orderId);
//        if(order.isPresent()){
//            Order order1=order.get();
//            order1.setStatus("DISPATCHED");
//            orderRepository.save(order1);
//            return order1;
//        }
//        return null;
//    }
//
//    public Order markAsReceived(Long orderId) {
//        Optional<Order> order=getOrderById(orderId);
//        if(order.isPresent()){
//            Order order1=order.get();
//            order1.setStatus("RECEIVED");
//            orderRepository.save(order1);
//            return order1;
//        }
//        return null;
//    }
}

