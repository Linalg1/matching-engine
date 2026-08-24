package com.linalg.exchange;

import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) throws IOException {

        long[] qqq = PriceLoader.loadCloses("data/qqq.csv", 1);
        System.out.println("Prix chargés : " + qqq.length + ", premier = " + qqq[0]);

        var sim = new Simulation(
            List.of(
                new NaiveMarketMaker(2, 10),
                new NoiseTrader(42),
                new ExternalPriceFollower(qqq)
            ),
            qqq[0]
        );

        sim.run(qqq.length);
        sim.printResults();
    }
}