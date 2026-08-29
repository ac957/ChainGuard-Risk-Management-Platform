package com.example.riskmanagementsystem.repo;

import com.example.riskmanagementsystem.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
}