/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mcnalu.stockmarket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * The main class that should be 
 * @author nalu
 */
public class StockMarket {

    /**
     * @param args the command line arguments. Expects exactly one argument - 
     * the path to where the CSV files are kept.
     */
    public static void main(String[] args) {
        //https://uk.finance.yahoo.com/q/hp?s=^FTSE&b=30&a=11&c=1983&e=1&d=10&f=2014&g=m
        //Expects exactly one argument - the path to where the CSV files are kept.
        File basePath = new File(args[0]);
        
        //FTSE portfolio - Buy all or sell all
        File ftse100File = new File(basePath,"ftse100.csv");
        StockTimeSeries ftse100 = new StockTimeSeries(ftse100File);
        Investment si = new SingleInvestment(ftse100);
        si.invest(1000.);
        
        //FTSE portfolio - Buy a fraction, sell a fraction
        ftse100.reset();
        si = new SingleFractionalInvestment(ftse100);
        si.invest(1000.);
        
        //Only hold shares from one company at a time
        MultipleInvestment mi = new MultipleInvestment();
        File path = new File(basePath,"downloader/");
        String[] stocks = {"BP.L", "MKS.L", "ITV.L", "LLOY.L", "NG.L", "TSCO.L"};
        for (String s : stocks) {
            StockTimeSeries series = new StockTimeSeries(new File(path, s + ".csv"), 6);
            mi.add(series);
        }
        mi.isVerbose = true;
        mi.invest(1000.);

        //Hold shares from several companies
        MultipleFractionalInvestment mfi = new MultipleFractionalInvestment();
        for (String s : stocks) {
            StockTimeSeries series = new StockTimeSeries(new File(path, s + ".csv"), 6);
            mfi.add(series);
        }
        mfi.isVerbose = true;
        mfi.invest(1000.);
    }

    public static BufferedReader getBufferedReader(File file) {
        BufferedReader br = null;

        if (file != null) {
            try {
                br = new BufferedReader(new FileReader(file.getAbsolutePath()));
                return br;
            } catch (IOException e) {
                System.err.println("IOException opening file " + file + " for reading: " + e);
            }
        }
        return null;
    }
}
