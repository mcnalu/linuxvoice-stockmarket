/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mcnalu.stockmarket;

import java.util.LinkedList;

/**
 * Simple investment strategy for one time series of share prices. Sell all
 * shares if they are sellThreshold times the amount they were bought for. Spend
 * all available money buying shares if the last five months saw a fractional
 * drop in value of more than (negative of) buyThreshold.
 *
 * Raw profit=6001 Adjusted profit=379
 *
 * @author nalu
 */
public class SingleInvestment extends Investment {

    private static double sellThreshold = 2;
    private static double buyThreshold = -0.05;
    private StockTimeSeries timeSeries;

    public SingleInvestment(StockTimeSeries ts) {
        timeSeries=ts;
    }

    public void invest(double investment) {
        super.invest(investment);
        money=investment-timeSeries.purchaseShares(investment);
        int initialShares=timeSeries.getSharesHeld();
        do {
            sellShares();
            buyShares();
            StockTimeSeries.Datum d = timeSeries.getDatum();
            printProgress(StockTimeSeries.dateFormat.format(d.date) + "," + Math.round(timeSeries.getValue()) + "," + Math.round(money));
        } while (timeSeries.next());
        double rawProfit = money+timeSeries.getFinalValue() - investment;
        double adjustedProfit = (timeSeries.getSharesHeld() - initialShares) * timeSeries.getFinalPrice();
        System.err.println("Raw profit=" + Math.round(rawProfit));
        System.err.println("Adjusted profit=" + Math.round(adjustedProfit));
    }

    protected boolean sellShares() {
        if (timeSeries.getPrice() > sellThreshold * timeSeries.getPriceAtLastPurchase()) {
            money += timeSeries.sellShares(timeSeries.getSharesHeld());
            return true;
        }
        return false;
    }

    protected boolean buyShares() {
        double delta = timeSeries.getDelta(5);

        if (timeSeries.getSharesHeld()==0 && delta < buyThreshold * timeSeries.getPrice()) {
            money-=timeSeries.purchaseShares(money);
            return true;
        }

        return false;
    }
}
