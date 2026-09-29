package com.empire.hll;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class HyperLogLogTest {
    
    @Test
    public void testAccuracy_1000Elements() {
        HyperLogLog hll = new HyperLogLog(14); // 16,384 buckets
        
        // Add 1,000 unique elements
        for (int i = 0; i < 1000; i++) {
            hll.add("user_" + i);
        }
        
        long estimate = hll.estimate();
        
        // Should be within 5% of actual (950-1050)
        assertTrue(estimate >= 950 && estimate <= 1050, 
            "Expected ~1000, got " + estimate);
    }
    
    @Test
    public void testAccuracy_100000Elements() {
        HyperLogLog hll = new HyperLogLog(14);
        
        // Add 100,000 unique elements
        for (int i = 0; i < 100_000; i++) {
            hll.add("user_" + i);
        }
        
        long estimate = hll.estimate();
        
        // Should be within 2% of actual (98,000-102,000)
        assertTrue(estimate >= 98_000 && estimate <= 102_000,
            "Expected ~100,000, got " + estimate);
    }
    
    @Test
    public void testDuplicates() {
        HyperLogLog hll = new HyperLogLog(14);
        
        // Add same element 10,000 times
        for (int i = 0; i < 10_000; i++) {
            hll.add("same_user");
        }
        
        long estimate = hll.estimate();
        
        // Should estimate ~1 (not 10,000)
        assertTrue(estimate <= 2, 
            "Duplicates should not inflate count. Got " + estimate);
    }
    
    @Test
    public void testMerge() {
        HyperLogLog hll1 = new HyperLogLog(14);
        HyperLogLog hll2 = new HyperLogLog(14);
        
        // Add 50,000 to each (no overlap)
        for (int i = 0; i < 50_000; i++) {
            hll1.add("user_a_" + i);
            hll2.add("user_b_" + i);
        }
        
        // Merge
        hll1.merge(hll2);
        
        long estimate = hll1.estimate();
        
        // Should be ~100,000 (50k + 50k)
        assertTrue(estimate >= 98_000 && estimate <= 102_000,
            "Merged estimate should be ~100,000, got " + estimate);
    }
    
    @Test
    public void testMemoryEfficiency() {
        HyperLogLog hll = new HyperLogLog(14);
        
        // Should use ~12KB (16,384 buckets * 4 bytes)
        int memoryBytes = hll.getMemoryBytes();
        assertTrue(memoryBytes <= 65_536, // 64KB max
            "Memory usage should be <= 64KB, got " + memoryBytes + " bytes");
    }
    
    @Test
    public void testPropertyBased_AccuracyBounds() {
        // Property: For any cardinality N, estimate should be within 2% for N > 10,000
        HyperLogLog hll = new HyperLogLog(14);
        
        int[] testSizes = {10_000, 50_000, 100_000, 500_000};
        
        for (int targetSize : testSizes) {
            hll = new HyperLogLog(14); // Fresh HLL for each test
            
            for (int i = 0; i < targetSize; i++) {
                hll.add("element_" + i);
            }
            
            long estimate = hll.estimate();
            double errorRate = Math.abs(estimate - targetSize) / (double) targetSize;
            
            assertTrue(errorRate <= 0.05, // 5% error tolerance
                "For N=" + targetSize + ", estimate=" + estimate + 
                ", error=" + (errorRate * 100) + "%");
        }
    }
    
    @Test
    public void testThreadSafety() throws InterruptedException {
        HyperLogLog hll = new HyperLogLog(14);
        
        // Launch 10 threads, each adding 10,000 unique elements
        Thread[] threads = new Thread[10];
        for (int t = 0; t < 10; t++) {
            final int threadId = t;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < 10_000; i++) {
                    hll.add("thread_" + threadId + "_user_" + i);
                }
            });
            threads[t].start();
        }
        
        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }
        
        long estimate = hll.estimate();
        
        // Should be ~100,000 (10 threads * 10,000 users)
        assertTrue(estimate >= 95_000 && estimate <= 105_000,
            "Thread-safe estimate should be ~100,000, got " + estimate);
    }
}
