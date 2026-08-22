package com.linalg.exchange;

public final class Position {
    private long inventory = 0;
    private long cash = 0;

    public void apply(Side side, long priceTicks, long quant){
        if (side == Side.BUY){
            inventory += quant ;
            cash -= quant * priceTicks;
        }
        else {
            inventory -= quant;
            cash += quant * priceTicks;
        }

    }

    public long pnl(long price) {
        return cash + inventory * price;
    }

    public long inventory(){ return inventory;}
    public long cash(){ return cash;}
    
}
