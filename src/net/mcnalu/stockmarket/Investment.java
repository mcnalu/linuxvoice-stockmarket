/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mcnalu.stockmarket;

/**
 *
 * @author nalu
 */
public abstract class Investment {

    public boolean isVerbose = false;
    protected double money;

    public void invest(double investment){
        System.err.println("=== Investing "+investment+" using "+this.getClass().getSimpleName());
    }

    public void printProgress(String s) {
        if (isVerbose) {
            System.err.println(s);
        }
    }

    protected abstract boolean sellShares();

    protected abstract boolean buyShares();
}
