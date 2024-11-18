package com.ykhaleq.airlineticketservice.service;

import java.util.HashMap;
import java.util.Map;

public class PricingService {

    private final Map<String, double[]> pricingRules;

    public PricingService() {
        this.pricingRules = new HashMap<>();

        // Define pricing tiers for each level
        pricingRules.put("First Class", new double[]{500.0, 1000.0, 1600.0}); // Tiers based on reserved count
        pricingRules.put("Business", new double[]{350.0, 450.0});
        pricingRules.put("Premium Economy", new double[]{250.0, 150.0, 300.0});
        pricingRules.put("Economy", new double[]{200.0}); // Flat pricing
    }

    // Calculate price dynamically based on reserved count
    public double calculatePrice(String levelName, int reservedCount) {
        double[] tiers = pricingRules.get(levelName);

        if (levelName.equals("First Class")) {
            if (reservedCount <= 10) {
                return tiers[0]; // $500
            } else if (reservedCount <= 30) {
                return tiers[1]; // $1000
            } else {
                return tiers[2]; // $1600
            }
        } else if (levelName.equals("Business")) {
            return reservedCount <= 45 ? tiers[0] : tiers[1]; // $350 or $450
        } else if (levelName.equals("Premium Economy")) {
            if (reservedCount <= 40) {
                return tiers[0]; // $250
            } else if (reservedCount <= 60) {
                return tiers[1]; // $150
            } else {
                return tiers[2]; // $300
            }
        } else {
            return tiers[0]; // Economy: $200
        }
    }
}