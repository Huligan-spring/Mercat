package com.curso.mercat.repository;

import com.curso.mercat.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByUserIdOrderByPurchasedAtDesc(Long userId);

}
