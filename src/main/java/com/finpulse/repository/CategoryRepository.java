package com.finpulse.repository;

import com.finpulse.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user.id = :userId ORDER BY c.type, c.name")
    List<Category> findAllAvailableForUser(Long userId);
    Optional<Category> findFirstByNameIgnoreCase(String name);
}
