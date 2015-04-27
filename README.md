#Linux voice Stockmarket

This code was written for a tutorial that appeared in [Linux Voice](http://linuxvoice.com) issue 14.

To build it I used netbeans and created a project with the src directory specified as the location of the source code. It should be straightforward using any java-capable IDE, or even on the command line if you know how to use javac. 

Some points to note:

- it was developed and tested under Java 1.7, but shouldn't be too fussy about
  which java you used
- there are no dependencies, except CSVs of stockmarket data from Yahoo, as
  described in the Linux Voice article for details
- The main class is Stockmarket.java and it expects exactly one argument - 
  the path to where the CSV files are kept. 
- GPL v3 licensed

Andrew Conway
27 April 2015

[blog.mcnalu.net](http://blog.mcnalu.net)

mcnalu@mcnalu.net

