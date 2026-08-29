package com.example.riskmanagementsystem.config;

import com.example.riskmanagementsystem.model.Category;
import com.example.riskmanagementsystem.model.Role;
import com.example.riskmanagementsystem.repo.CategoryRepository;
import com.example.riskmanagementsystem.repo.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepo;
    private final CategoryRepository categoryRepo;

    public DataInitializer(RoleRepository roleRepo, CategoryRepository categoryRepo) {
        this.roleRepo = roleRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public void run(String... args) {

        // Seed roles
        for (String name : List.of("ADMIN", "MANAGER", "USER")) {
            if (roleRepo.findByName(name) == null) {
                Role role = new Role();
                role.setName(name);
                roleRepo.save(role);
            }
        }

        // Seed categories
        List<String[]> categories = List.of(
                new String[]{"Operational",  "Day to day risks arising from internal processes, supplier delays, inventory shortages or equipment failures that affect normal business operations."},
                new String[]{"Financial",    "Risks related to supplier insolvency, cost volatility, currency fluctuations or unexpected financial losses that impact the organisation."},
                new String[]{"Geopolitical", "Risks arising from trade restrictions, political instability, sanctions or regulatory changes in international markets that disrupt global sourcing."},
                new String[]{"Environmental","Risks caused by natural disasters, extreme weather events, climate change or environmental compliance requirements affecting supply chain continuity."},
                new String[]{"Cybersecurity","Risks from data breaches, system outages, ransomware or third-party system vulnerabilities that compromise digital supply chain operations."},
                new String[]{"Compliance",   "Risks arising from failure to meet legal, regulatory or contractual obligations including import/export regulations and industry standards."},
                new String[]{"Reputational", "Risks that could damage the organisation's public image or stakeholder trust, including supplier misconduct, poor quality or public incidents."},
                new String[]{"Other", "Risks that do not fit into any of the predefined categories above. Use this option when the risk you are reporting is unique to your organisation or situation and requires a custom description."}

        );

        for (String[] cat : categories) {
            if (categoryRepo.findByName(cat[0]) == null) {
                Category category = new Category();
                category.setName(cat[0]);
                category.setDescription(cat[1]);
                categoryRepo.save(category);
            }
        }
    }
}
