package com.linalg.exchange;

import java.util.*;

public final class OrderBook {
    
    private final TreeMap<Long, ArrayDeque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    private final TreeMap<Long, ArrayDeque<Order>> asks = new TreeMap<>();
   

    public List<Trade> submit(Order incoming){
        List<Trade> trades = new ArrayList<>();
        var opp = (incoming.side == Side.BUY) ? asks : bids;

        while ( !incoming.isFilled() && !opp.isEmpty()){
            var best= opp.firstEntry();
            long restingprice = best.getKey();

            if ( !crosses(incoming, restingprice)) break; // si pas croisement sortir

            var queue = best.getValue();

            while (!queue.isEmpty() && !incoming.isFilled()){
                Order resting = queue.peekFirst();
                long quant = Math.min(resting.quantity, incoming.quantity);

                incoming.quantity -= quant;
                resting.quantity -= quant;

                trades.add(maketrade(incoming, resting, restingprice, quant));

                if(resting.isFilled()) queue.pollFirst();
            }

            if (queue.isEmpty()) opp.pollFirstEntry();

        }

        if (!incoming.isFilled()) rest(incoming);

        return trades;

    }
    private boolean crosses(Order incoming, long restingprice){
        return incoming.side== Side.BUY ? incoming.priceTicks >= restingprice 
                                        : incoming.priceTicks <= restingprice ;
    }


    private Trade maketrade (Order incoming, Order resting, long price, long quant){
        return incoming.side == Side.BUY ? new Trade(incoming.id, resting.id, price, quant, incoming.timestamp)
                                         : new Trade(resting.id, incoming.id, price, quant, incoming.timestamp);
    }

    private void rest(Order o) {
        var book = (o.side == Side.BUY) ? bids : asks;
        if (!book.containsKey(o.priceTicks)) {
            book.put(o.priceTicks, new ArrayDeque<>());
        }
        book.get(o.priceTicks).addLast(o);  
    }

    public Optional<Long> bestbid() {
        return bids.isEmpty() ? Optional.empty() : Optional.of(bids.firstKey());
    }

    public Optional<Long> bestask() {
        return asks.isEmpty() ? Optional.empty() : Optional.of(asks.firstKey());
    }



    
}
