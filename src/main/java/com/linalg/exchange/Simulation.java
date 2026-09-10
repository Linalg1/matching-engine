package com.linalg.exchange;

import java.util.*;

public final class Simulation {
    private final OrderBook book = new OrderBook();
    private final List<Strategy> strategies;
    private final Map<String, Position> positions = new LinkedHashMap<>();
    private final Map<Long, String> orderOwner = new HashMap<>();
    private final Map<String, Long> fillQuantity = new LinkedHashMap<>();
    private final Map<String, Long> maxAbsInventory = new LinkedHashMap<>();
    private final Random schedulingRng;

    private long tick = 0;
    private long lastTradePrice;
    private long tradeCount = 0;

    public Simulation(List<Strategy> strategies, long initialPrice) {
        this(strategies, initialPrice, 12345L);
    }

    public Simulation(List<Strategy> strategies, long initialPrice, long schedulingSeed) {
        this.strategies = List.copyOf(strategies);
        this.lastTradePrice = initialPrice;
        this.schedulingRng = new Random(schedulingSeed);

        for (var strategy : strategies) {
            if (positions.containsKey(strategy.name())) {
                throw new IllegalArgumentException("strategy names must be unique: " + strategy.name());
            }
            positions.put(strategy.name(), new Position());
            fillQuantity.put(strategy.name(), 0L);
            maxAbsInventory.put(strategy.name(), 0L);
        }
    }

    public void run(int ticks) {
        for (int i = 0; i < ticks; i++) step();
    }

    private void step() {
        tick++;

        List<Strategy> activationOrder = new ArrayList<>(strategies);
        Collections.shuffle(activationOrder, schedulingRng);

        for (var strategy : activationOrder) {
            var view = new BookView(book.bestBid(), book.bestAsk(), lastTradePrice, tick);
            var position = positions.get(strategy.name());

            for (var action : strategy.onTick(view, position)) {
                if (action instanceof StrategyAction.Cancel cancel) {
                    book.cancel(cancel.orderId());
                    orderOwner.remove(cancel.orderId());
                } else if (action instanceof StrategyAction.Submit submit) {
                    Order order = submit.order();
                    orderOwner.put(order.id(), strategy.name());

                    for (var trade : book.submit(order)) {
                        lastTradePrice = trade.priceTicks();
                        tradeCount++;
                        attribute(trade);
                    }

                    if (order.isFilled()) {
                        orderOwner.remove(order.id());
                    }
                }
            }
        }
    }

    private void attribute(Trade trade) {
        var buyer = orderOwner.get(trade.buyOrderId());
        var seller = orderOwner.get(trade.sellOrderId());

        if (buyer != null) {
            applyFill(buyer, Side.BUY, trade.priceTicks(), trade.quantity());
        }
        if (seller != null) {
            applyFill(seller, Side.SELL, trade.priceTicks(), trade.quantity());
        }

        // Fully-filled resting orders are gone from the book; stale ownership entries
        // are harmless but can be cleaned when they later get cancel requests.
    }

    private void applyFill(String owner, Side side, long price, long quantity) {
        Position p = positions.get(owner);
        p.apply(side, price, quantity);
        fillQuantity.merge(owner, quantity, Long::sum);
        maxAbsInventory.merge(owner, Math.abs(p.inventory()), Math::max);
    }

    public void printResults() {
        System.out.println("=== Results after " + tick + " ticks ===");
        System.out.println("Trades: " + tradeCount + ", resting orders: " + book.restingOrderCount());
        System.out.printf("%-15s %12s %14s %14s %14s %14s%n",
            "Strategy", "Inventory", "Cash", "PnL", "FillQty", "Max|Inv|");

        for (var entry : positions.entrySet()) {
            String name = entry.getKey();
            Position p = entry.getValue();
            System.out.printf("%-15s %12d %14d %14d %14d %14d%n",
                name,
                p.inventory(),
                p.cash(),
                p.pnl(lastTradePrice),
                fillQuantity.get(name),
                maxAbsInventory.get(name));
        }
    }
}
