package com.shopsphere.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopsphere.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}