package com.example.orderservice.service;

import com.example.orderservice.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final String PRODUCT_SERVICE = "product-service";

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    private final AtomicInteger counter = new AtomicInteger();

    public ServiceInstance findProductServiceInstance() {
        List<ServiceInstance> instances = discoveryClient.getInstances(PRODUCT_SERVICE);

        if (instances.isEmpty()) {
            throw new ServiceUnavailableException("No instances of product-service found");
        }

        // xoay vòng giữa các instance thay vì luôn lấy get(0)
        int index = Math.floorMod(counter.getAndIncrement(), instances.size());
        return instances.get(index);
    }

    public String getProductServices() {
        try {
            return restTemplate.getForObject("http://product-service/api/products", String.class);
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Failed to call product-service: " + e.getMessage());
        }
    }
}