package com.linalg.exchange;

import java.util.*;

public final class OrderBook {

    // TreeMap orders price levels; LinkedHashMap preserves FIFO at a price level
    // while allowing O(1) removal of a known order id from that level.
    private final TreeMap<Long, LinkedHashMap<Long, Order>> bids =
        new TreeMap<>(Comparator.reverseOrder());
    private final TreeMap<Long, LinkedHashMap<Long, Order>> asks =
        new TreeMap<>();

    // Only resting orders are indexed.
    private final Map<Long, OrderLocation> restingOrders = new HashMap<>();

    private record OrderLocation(Side side, long priceTicks) {}

    public List<Trade> submit(Order incoming) {
        Objects.requireNonNull(incoming, "incoming");

        if (restingOrders.containsKey(incoming.id())) {
            throw new IllegalArgumentException("duplicate resting order id: " + incoming.id());
        }

        List<Trade> trades = new ArrayList<>();
        var oppositeBook = incoming.side() == Side.BUY ? asks : bids;

        while (!incoming.isFilled() && !oppositeBook.isEmpty()) {
            var bestLevel = oppositeBook.firstEntry();
            long restingPrice = bestLevel.getKey();

            if (!crosses(incoming, restingPrice)) break;

            var ordersAtLevel = bestLevel.getValue();
            var iterator = ordersAtLevel.entrySet().iterator();

            while (iterator.hasNext() && !incoming.isFilled()) {
                var entry = iterator.next();
                Order resting = entry.getValue();
                long quantity = Math.min(resting.remainingQuantity(), incoming.remainingQuantity());

                incoming.fill(quantity);
                resting.fill(quantity);
                trades.add(makeTrade(incoming, resting, restingPrice, quantity));

                if (resting.isFilled()) {
                    iterator.remove();
                    restingOrders.remove(resting.id());
                }
            }

            if (ordersAtLevel.isEmpty()) {
                oppositeBook.pollFirstEntry();
            }
        }

        if (!incoming.isFilled()) {
            rest(incoming);
        }

        return trades;
    }

    public boolean cancel(long orderId) {
        OrderLocation location = restingOrders.remove(orderId);
        if (location == null) return false;

        var book = location.side() == Side.BUY ? bids : asks;
        var level = book.get(location.priceTicks());
        if (level == null) return false; // defensive: index/book should never diverge

        Order removed = level.remove(orderId);
        if (level.isEmpty()) {
            book.remove(location.priceTicks());
        }
        return removed != null;
    }

    private boolean crosses(Order incoming, long restingPrice) {
        return incoming.side() == Side.BUY
            ? incoming.priceTicks() >= restingPrice
            : incoming.priceTicks() <= restingPrice;
    }

    private Trade makeTrade(Order incoming, Order resting, long price, long quantity) {
        return incoming.side() == Side.BUY
            ? new Trade(incoming.id(), resting.id(), price, quantity, incoming.timestamp())
            : new Trade(resting.id(), incoming.id(), price, quantity, incoming.timestamp());
    }

    private void rest(Order order) {
        if (restingOrders.containsKey(order.id())) {
            throw new IllegalArgumentException("duplicate resting order id: " + order.id());
        }

        var book = order.side() == Side.BUY ? bids : asks;
        var level = book.computeIfAbsent(order.priceTicks(), ignored -> new LinkedHashMap<>());
        level.put(order.id(), order);
        restingOrders.put(order.id(), new OrderLocation(order.side(), order.priceTicks()));
    }

    public Optional<Long> bestBid() {
        return bids.isEmpty() ? Optional.empty() : Optional.of(bids.firstKey());
    }

    public Optional<Long> bestAsk() {
        return asks.isEmpty() ? Optional.empty() : Optional.of(asks.firstKey());
    }

    public int restingOrderCount() {
        return restingOrders.size();
    }
}
