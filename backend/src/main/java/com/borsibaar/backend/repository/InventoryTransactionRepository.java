package com.borsibaar.backend.repository;

import com.borsibaar.backend.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByInventoryIdOrderByCreatedAtDesc(Long inventoryId);

    List<InventoryTransaction> findByReferenceId(String referenceId);

    @Query("""
        SELECT it FROM InventoryTransaction it
        JOIN Inventory i ON it.inventoryId = i.id
        WHERE i.organizationId = :organizationId
        AND it.transactionType = 'SALE'
        ORDER BY it.createdAt DESC
        """)
    List<InventoryTransaction> findSaleTransactionsByOrganizationId(@Param("organizationId") Long organizationId);
}
