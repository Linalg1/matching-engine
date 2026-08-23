package com.linalg.exchange;

import java.util.*;

public final class Simulation {

    private final OrderBook book = new OrderBook();
    private final List<Strategy> strategies;
    private final Map<String, Position> positions = new HashMap<>();
    private final Map<Long, String> orderOwner = new HashMap<>();

    private long tick = 0;
    private long lastTradePrice;

    public Simulation(List<Strategy> strategies, long initialPrice) {
        this.strategies = strategies;
        this.lastTradePrice = initialPrice;
        for (var s : strategies) {
            positions.put(s.name(), new Position());
        }
    }

    public void run(int ticks) {
        for (int i = 0; i < ticks; i++) {
            step();
        }
    }

    private void step() {
        tick++;
        var view = new BookView(book.bestbid(), book.bestask(), lastTradePrice, tick);

        for (var strategy : strategies) {
            var pos = positions.get(strategy.name());

            for (var order : strategy.onTick(view, pos)) {
                orderOwner.put(order.id, strategy.name());

                for (var t : book.submit(order)) {
                    lastTradePrice = t.priceTicks();
                    attribute(t);
                }
            }
        }
    }

    private void attribute(Trade t) {
        var buyer  = orderOwner.get(t.buyOrderId());
        var seller = orderOwner.get(t.sellOrderId());

        if (buyer != null)  positions.get(buyer).apply(Side.BUY,  t.priceTicks(), t.quantity());
        if (seller != null) positions.get(seller).apply(Side.SELL, t.priceTicks(), t.quantity());
    }

    public void printResults() {
        System.out.println("=== Résultats après " + tick + " ticks ===");
        System.out.printf("%-15s %12s %12s %12s%n", "Stratégie", "Inventaire", "Cash", "PnL");

        for (var e : positions.entrySet()) {
            var p = e.getValue();
            System.out.printf("%-15s %12d %12d %12d%n",
                e.getKey(), p.inventory(), p.cash(), p.pnl(lastTradePrice));
        }
    }
}