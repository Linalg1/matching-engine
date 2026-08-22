package com.linalg.exchange;

public final class Order {
    public final long id;
    public final Side side;
    public final long priceTicks;
    public long quantity;
    public final long timestamp;

    public Order(long id, Side side, long priceTicks, long quantity, long timestamp){
        this.id= id;
        this.side= side;
        this.priceTicks= priceTicks;
        this.quantity= quantity;
        this.timestamp= timestamp;

    }

    public boolean isFilled(){
        return quantity ==0;
    }

    @Override
    public String toString() {
        return "%s %d @ %d (id= %d)".formatted(side, quantity, priceTicks, id);
    }

    
}
