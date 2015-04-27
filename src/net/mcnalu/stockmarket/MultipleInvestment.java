/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mcnalu.stockmarket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple-minded investment scheme that chooses from several shares and only
 * invests in one at a time. If the currently held shares rise in value above
 * the given threshold, those shares will be sold and used to buy shares that
 * currently decreasing in value.
 *
 * @author nalu
 */
public class MultipleInvestment extends Investment {

    private static double sellThreshold = 1.5;
    private static double buyThreshold = -0.05;
    private static int NO_MORE_DATA = 0;
    private static int NO_NEW_DATA = 1;
    private static int NEW_DATA = 2;
    private ArrayList<StockTimeSeries> seriesList = new ArrayList();
    private ArrayList<StockTimeSeries> availableList = new ArrayList();
    private int index;
    Date dateToEnd = new Date();

    public void add(StockTimeSeries timeSeries) {
        //System.err.print("Adding " + timeSeries.name);
        if (seriesList.isEmpty() || timeSeries.getFirstDate().after(seriesList.get(0).getFirstDate())) {
            seriesList.add(timeSeries);
            //System.err.println("to end.");
        } else {
            seriesList.add(0, timeSeries);
            //System.err.println("to beginning.");
        }
    }

    public void invest(double investment) {
        super.invest(investment);
        money = investment;
        Calendar cal = Calendar.getInstance();
        cal.setTime(seriesList.get(0).getFirstDate());
        int status = NEW_DATA;
        index=-1;
        status = updateAvailableList(cal.getTime());
        if(status!=NEW_DATA){
            System.err.println("Should never happen!");
            System.exit(999);
        }
        do {
            String event = "";
            if (index == -1) {
                if (buyShares()) {
                    event = "BUY: " + seriesList.get(index).name;
                }
            } else if (sellShares()) {
                event = "SELL: " + seriesList.get(index).name;
                index = -1;
            }
            String shareValue = "-";
            if (index != -1) {
                shareValue = "" + Math.round(seriesList.get(index).getValue());
            }
            if (event.length() > 0) {
                printProgress(StockTimeSeries.dateFormat.format(cal.getTime())
                        + "," + shareValue
                        + "," + Math.round(money)
                        + "," + availableList.size()
                        + " " + event);
            }

            do {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                status = updateAvailableList(cal.getTime());
            } while (status == NO_NEW_DATA);

        } while (status != NO_MORE_DATA);
        double finalValue = 0;
        if (index != -1) {
            finalValue = seriesList.get(index).getFinalValue();
        }
        double rawProfit = money + finalValue - investment;
        System.err.println("Final money=" + Math.round(money));
        System.err.println("Final value of held shares=" + Math.round(finalValue));
        System.err.println("Raw profit=" + Math.round(rawProfit));
    }

    private int updateAvailableList(Date date) {
        if (date.after(dateToEnd)) {
            return NO_MORE_DATA;
        }
        int status = NO_NEW_DATA;
        availableList.clear();
        for (StockTimeSeries sts : seriesList) {
            if (sts.findDatum(date)) {
                availableList.add(sts);
                status = NEW_DATA;
            }
        }
        return status;
    }

    @Override
    protected boolean sellShares() {
        StockTimeSeries timeSeries = seriesList.get(index);
        if (timeSeries.getPrice() > sellThreshold * timeSeries.getPriceAtLastPurchase()) {
            money += timeSeries.sellShares(timeSeries.getSharesHeld());
            return true;
        }
        return false;
    }

    @Override
    protected boolean buyShares() {
        //Could be improved by finding shares with biggest drop, but this'll do
        for (int i = 0; i < availableList.size(); i++) {
            StockTimeSeries timeSeries = availableList.get(i);
            double delta = timeSeries.getDelta(5);

            if (delta < buyThreshold * timeSeries.getPrice()) {
                money -= timeSeries.purchaseShares(money);
                index = i;
                return true;
            }
        }
        return false;
    }
}
