package com.linalg.exchange;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Optional;
public class OrderBookTest {

    @Test
    public void ordreSansContrepartieResteAuCarnet() {
        var book = new OrderBook();
        var trades = book.submit(new Order(1, Side.BUY, 100, 50, 1));

        assertTrue(trades.isEmpty());
        assertEquals(Optional.of(100L), book.bestbid());
    }

    @Test
    public void prixDExecutionEstCeluiDuResting() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.SELL, 103, 50, 1));
        var trades = book.submit(new Order(2, Side.BUY, 105, 50, 2));

        assertEquals(1, trades.size());
        assertEquals(103, trades.get(0).priceTicks());   // pas 105
        assertTrue(book.bestask().isEmpty());
    }

    @Test
    public void executionPartielle() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.SELL, 100, 30, 1));
        var trades = book.submit(new Order(2, Side.BUY, 100, 50, 2));

        assertEquals(1, trades.size());
        assertEquals(30L, trades.get(0).quantity());
        assertEquals(Optional.of(100L), book.bestbid());  // 20 restent au carnet
    }

    @Test
    public void prioriteTemporelleAPrixEgal() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.SELL, 100, 10, 1));   // arrivé en premier
        book.submit(new Order(2, Side.SELL, 100, 10, 2));

        var trades = book.submit(new Order(3, Side.BUY, 100, 10, 3));

        assertEquals(1, trades.size());
        assertEquals(1L, trades.get(0).sellOrderId());   // le #1, pas le #2
    }

    @Test
    public void traverseePlusieursNiveaux() {
        var book = new OrderBook();
        book.submit(new Order(1, Side.SELL, 100, 10, 1));
        book.submit(new Order(2, Side.SELL, 101, 10, 2));
        book.submit(new Order(3, Side.SELL, 102, 10, 3));

        var trades = book.submit(new Order(4, Side.BUY, 101, 25, 4));

        assertEquals(2, trades.size());          // 100 puis 101, pas 102
        assertEquals(100L, trades.get(0).priceTicks());
        assertEquals(101L, trades.get(1).priceTicks());
        assertEquals(Optional.of(101L), book.bestbid());  // reliquat de 5
    }
}