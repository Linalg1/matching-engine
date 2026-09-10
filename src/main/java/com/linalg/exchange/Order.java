package com.linalg.exchange;

import java.util.Objects;

public final class Order {
    private final long id;
    private final Side side;
    private final long priceTicks;
    private long remainingQuantity;
    private final long timestamp;

    public Order(long id, Side side, long priceTicks, long quantity, long timestamp) {
        if (id < 0) throw new IllegalArgumentException("order id must be non-negative");
        if (priceTicks <= 0) throw new IllegalArgumentException("price must be positive");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");

        this.id = id;
        this.side = Objects.requireNonNull(side, "side");
        this.priceTicks = priceTicks;
        this.remainingQuantity = quantity;
        this.timestamp = timestamp;
    }

    public long id() { return id; }
    public Side side() { return side; }
    public long priceTicks() { return priceTicks; }
    public long remainingQuantity() { return remainingQuantity; }
    public long timestamp() { return timestamp; }

    public boolean isFilled() {
        return remainingQuantity == 0;
    }

    public void fill(long quantity) {
        if (quantity <= 0 || quantity > remainingQuantity) {
            throw new IllegalArgumentException("invalid fill quantity: " + quantity);
        }
        remainingQuantity -= quantity;
    }

    @Override
    public String toString() {
        return "%s %d @ %d (id=%d)".formatted(side, remainingQuantity, priceTicks, id);
    }
}
