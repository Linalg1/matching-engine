package com.linalg.exchange;

public record Trade (
    long buyOrderId,
    long sellOrderId,
    long priceTicks,
    long quantity,
    long timestamp

    
) {}
