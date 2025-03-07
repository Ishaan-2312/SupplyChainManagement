package com.IshaanBansal.SupplyChainManagement.Repository;


import com.IshaanBansal.SupplyChainManagement.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
}
