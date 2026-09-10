package com.linalg.exchange;

import org.junit.Test;
import java.util.Optional;

import static org.junit.Assert.*;

public class OrderBookTest {

    @Test
    public void nonCrossingOrderRests() {
        var book = new OrderBook();
        var trades = book.submit(new Order(1, Side.BUY, 100, 50, 1));

        assertTrue(trades.isEmpty());
        assertEquals(Optional.of(100L), book.bestBid());
    }

    @Test
    public void executionUsesRestingPrice() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.SELL, 103, 50, 1));
        var trades = book.submit(new Order(2, Side.BUY, 105, 50, 2));

        assertEquals(1, trades.size());
        assertEquals(103L, trades.get(0).priceTicks());
        assertTrue(book.bestAsk().isEmpty());
    }

    @Test
    public void partialExecutionLeavesRemainder() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.SELL, 100, 30, 1));
        var trades = book.submit(new Order(2, Side.BUY, 100, 50, 2));

        assertEquals(1, trades.size());
        assertEquals(30L, trades.get(0).quantity());
        assertEquals(Optional.of(100L), book.bestBid());
    }

    @Test
    public void fifoAtSamePrice() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.SELL, 100, 10, 1));
        book.submit(new Order(2, Side.SELL, 100, 10, 2));

        var trades = book.submit(new Order(3, Side.BUY, 100, 10, 3));

        assertEquals(1, trades.size());
        assertEquals(1L, trades.get(0).sellOrderId());
    }

    @Test
    public void walksMultiplePriceLevels() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.SELL, 100, 10, 1));
        book.submit(new Order(2, Side.SELL, 101, 10, 2));
        book.submit(new Order(3, Side.SELL, 102, 10, 3));

        var trades = book.submit(new Order(4, Side.BUY, 101, 25, 4));

        assertEquals(2, trades.size());
        assertEquals(100L, trades.get(0).priceTicks());
        assertEquals(101L, trades.get(1).priceTicks());
        assertEquals(Optional.of(101L), book.bestBid());
    }

    @Test
    public void cancelRemovesRestingOrder() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.BUY, 100, 10, 1));

        assertTrue(book.cancel(1));
        assertTrue(book.bestBid().isEmpty());
        assertFalse(book.cancel(1));
    }

    @Test
    public void cancelOneOrderPreservesOthersAtSamePrice() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.SELL, 100, 10, 1));
        book.submit(new Order(2, Side.SELL, 100, 10, 2));
        book.submit(new Order(3, Side.SELL, 100, 10, 3));

        assertTrue(book.cancel(2));
        var trades = book.submit(new Order(4, Side.BUY, 100, 20, 4));

        assertEquals(2, trades.size());
        assertEquals(1L, trades.get(0).sellOrderId());
        assertEquals(3L, trades.get(1).sellOrderId());
    }

    @Test
    public void cancelRemainingQuantityAfterPartialFill() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.SELL, 100, 20, 1));
        book.submit(new Order(2, Side.BUY, 100, 5, 2));

        assertTrue(book.cancel(1));
        assertTrue(book.bestAsk().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidQuantity() {
        new Order(1, Side.BUY, 100, 0, 1);
    }
}
