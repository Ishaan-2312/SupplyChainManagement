package com.IshaanBansal.SupplyChainManagement.Controller;


//import com.IshaanBansal.SupplyChain.Service.OrderService;

import com.IshaanBansal.SupplyChainManagement.Model.Order;
import com.IshaanBansal.SupplyChainManagement.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/Order")
public class OrderController {
    @Autowired
    private OrderService orderService;


    @GetMapping("/getAllOrders")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }


    @GetMapping("/getOrderById")
    public ResponseEntity<Optional<Order>> getOrderById(@RequestParam Long orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        if (order.isPresent()) {
            return ResponseEntity.ok(order);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


}

