// StockDataCSVParser.java
package com.stockmarket.parsers;

import com.stockmarket.data.*;
/**
 * Parser Class to retrieve Stock Market Data from the Internet
 * @author Hans-Jurgen Greiner
 */
import com.stockmarket.parsers.httpclient.GetPageInfo;
import com.hgutil.*;
import com.hgutil.data.*;
import java.util.*;

public class StockDataCSVParser extends GetPageInfo
{
  private final String propertyFileName = "resources.HtmlStockParsers";
  private ResourceBundle bundle = null;

  private String fieldsList = null;

  private String[] titles = null;
  private Vector vector = null;

  private String symbol = null;
  private String name = null;
  private String bid = null;
  private String ask = null;
  private String last = null;
  private String open = null;
  private String change = null;
  private String changePr = null;
  private String volume = null;
  private String week52Low = null;
  private String week52High = null;
  private String previousClose = null;

  private StockData sd = null;

  private static boolean realTimeQuotes = true;
  /**
   * StockSymbolParser constructor comment.
   */
  public StockDataCSVParser()
  {
    super();
  }
  /**
   * Method parses the data and setup a post request to the
   * the web site - In this case Yahoo.com
   * the Date Format string is the form dd-mm-yyyy
   * @param symbol The StockData Symbol;
   */
  public void getData(Vector stockDataVector)
  {

    String stockDataSource = appProps.getProperty("StockDataCSV.stock_symbol_source", "YAHOO");

    if (stockDataSource.equals("YAHOO"))
    {
      getYahooData(stockDataVector);
    }
    return;
  }
  /**
   * Method parses the data and setup a post request to the
   * the web site - In this case Yahoo.com
   * the Date Format string is the form dd-mm-yyyy
   * @param symbol The StockData Symbol;
   */
  public void getData(StockData stockData)
  {
    String stockDataSource = appProps.getProperty("StockDataCSV.stock_symbol_source", "YAHOO");

    if (stockDataSource.equals("YAHOO"))
    {
      getYahooData(stockData);
    }
    return;
  }
  /**
   * Method getYahooData.  Performs the Stock Data setup of data to 
   * retrieve from YAHOO
   * @param stockData
   */
  protected void getYahooData(StockData stockData)
  {
    sd = stockData;
    this.vector = null;
    //http://finance.yahoo.com/d/quotes.csv?s=IBM+AXP+INSP&f=snobalcvw&e=.csv
    String localURL = getString("StockDataCSV.yahoo.stock_symbol_url");

    // We only have 1 symbol here so we can move right along 
    // with the other stuff
    localURL += getString("StockDataCSV.yahoo.stock_symbol_descriptor");
    String symbol = stockData.getSymbol().getDataSz();
    if (symbol.length() >= 5) // Are we an Option
    {
      symbol += ".x";
    }
    localURL += symbol;
    localURL += "&";
    // Now add the fields we are interested in - Assign it to fieldsList
    // for parsing as well.
    localURL += getString("StockDataCSV.yahoo.stock_symbol_fields_descriptor");
    if (realTimeQuotes)
    {
      fieldsList = getString("StockDataCSV.yahoo.stock_symbol_real_time_fields");
      localURL += fieldsList;
      localURL += "&";
      localURL += getString("StockDataCSV.yahoo.stock_symbol_real_time_descriptor");
      localURL += getString("StockDataCSV.yahoo.stock_symbol_real_time_value");
    }
    else
    {
      fieldsList = getString("StockDataCSV.yahoo.stock_symbol_fields");
      localURL += fieldsList;
    }
    localURL += "&";

    // And finally terminate it with the file type ( i.e. A comma seperated vector .csv )
    localURL += getString("StockDataCSV.yahoo.stock_symbol_filetype_descriptor");
    localURL += getString("StockDataCSV.yahoo.stock_symbol_filetype");

    try
    {
      if (DEBUG)
      {
        String theSymbol = stockData.getSymbol().getDataSz();
        System.out.println(
          "Attempting Retrievel of a comma seperated vector " + "stockQuote for " + theSymbol + " from the following URL");
        System.out.println("Compiled URL is: \n" + localURL);
      }
      getInfoPage(localURL);
    }
    catch (RuntimeException exc)
    {
      String errorMsg = "StockDataCSVParser::getData() - We have a problem: \n[" + exc + "]";
      System.out.println(errorMsg);
      this.dumpProperties();
    }
    finally
    { // Reset the Symbol States when we are done
      vector = null;
      sd = null;
    }

    return;
  }
  /**
   * Method getYahooData.
   * @param stockDataVector
   */
  protected void getYahooData(Vector stockDataVector)
  {
    this.vector = stockDataVector;
    sd = null;
    //http://finance.yahoo.com/d/quotes.csv?s=IBM+AXP+INSP&f=snobalcvw&e=.csv
    String localURL = getString("StockDataCSV.yahoo.stock_symbol_url");

    // We only have 1 symbol here so we can move right along 
    // with the other stuff
    localURL += getString("StockDataCSV.yahoo.stock_symbol_descriptor");
    String symbolList = "";
    for (int i = 0; i < vector.size(); i++)
    {
      StockData stock = (StockData) vector.elementAt(i);
      String symbol = stock.getSymbol().getDataSz();
      if (symbol.length() >= 5) // Are we an Option
      {
        symbol += getString("StockDataCSV.yahoo.option_symbol_yahoo_extension");
      }
      symbolList += symbol;
      if ((i + 1) < vector.size())
      {
        symbolList += '+';
      }
    }
    localURL += symbolList;
    localURL += "&";
    // Now add the fields we are interested in - Assign it to fieldsList
    // for parsing as well.
    localURL += getString("StockDataCSV.yahoo.stock_symbol_fields_descriptor");
    if (realTimeQuotes)
    {
      fieldsList = getString("StockDataCSV.yahoo.stock_symbol_real_time_fields");
      localURL += fieldsList;
      localURL += "&";
      localURL += getString("StockDataCSV.yahoo.stock_symbol_real_time_descriptor");
      localURL += getString("StockDataCSV.yahoo.stock_symbol_real_time_value");
    }
    else
    {
      fieldsList = getString("StockDataCSV.yahoo.stock_symbol_fields");
      localURL += fieldsList;
    }
    // Append Another AmPersand Before the next Fields
    localURL += "&";

    // And finally terminate it with the file type ( i.e. A comma seperated vector .csv )
    localURL += getString("StockDataCSV.yahoo.stock_symbol_filetype_descriptor");
    localURL += getString("StockDataCSV.yahoo.stock_symbol_filetype");

    try
    {
      if (DEBUG)
      {
        System.out.println(
          "Attempting Retrievel of a comma seperated vector " + "stockQuote for " + symbolList + " from the following URL");
        System.out.println("Compiled URL is: \n" + localURL);
      }
      getInfoPage(localURL);
    }
    catch (RuntimeException exc)
    {
      String errorMsg = "StockDataCSVParser::getData() - We have a problem: \n[" + exc + "]";
      System.out.println(errorMsg);
      this.dumpProperties();
    }
    finally
    { // Reset the Symbol States when we are done
      vector = null;
      sd = null;
    }

    return;
  }
  /**
   * Returns the resource bundle associated with this demo. Used
   * to get accessable and internationalized strings.
   */
  public ResourceBundle getResourceBundle()
  {
    if (bundle == null)
    {
      bundle = ResourceBundle.getBundle(propertyFileName);
    }
    return bundle;
  }
  /**
   * Method to determine if Real Time Quotes are set
   * @return boolean
   */
  public static boolean isRealTimeQuotes()
  {
    return realTimeQuotes;
  }
  /**
   * Method main.
   * @param args
   */
  public static void main(String[] args)
  {
    StockDataCSVParser.preloadProxyInfo("StockApp.ini");
    StockDataCSVParser parser = new StockDataCSVParser();

    Vector data = new Vector();
    data.removeAllElements();
    data.addElement(
      new StockData("ORCL", "Oracle Corp.", 22.456, 23.285, 23.6875, 25.375, -1.6875, -6.42, 24976600, 0.0, 0.0));
    data.addElement(new StockData("EGGS", "Egghead.com", 17.20, 17.43, 17.25, 17.4375, -0.1875, -1.43, 2146400, 0.0, 0.0));
    data.addElement(new StockData("T", "AT&T", 65.078, 66, 65.1875, 66, -0.8125, -0.10, 554000, 0.0, 0.0));
    data.addElement(
      new StockData("LU", "Lucent Technology", 64.625, 59.9375, 64.625, 59.9375, 4.6875, 9.65, 29856300, 0.0, 0.0));
    data.addElement(
      new StockData("FON", "Sprint", 104.5625, 106.375, 104.5625, 106.375, -1.8125, -1.82, 1135100, 0.0, 0.0));
    data.addElement(new StockData("ENML", "Enamelon Inc.", 4.875, 5, 4.875, 5, -0.125, 0, 35900, 0.0, 0.0));
    data.addElement(
      new StockData("CPQ", "Compaq Computers", 30.875, 31.25, 30.875, 31.25, -0.375, -2.18, 11853900, 0.0, 0.0));
    data.addElement(
      new StockData("MSFT", "Microsoft Corp.", 94.0625, 95.1875, 94.0625, 95.1875, -1.125, -0.92, 19836900, 0.0, 0.0));
    data.addElement(
      new StockData("DELL", "Dell Computers", 46.1875, 44.5, 46.1875, 44.5, 1.6875, 6.24, 47310000, 0.0, 0.0));
    data.addElement(
      new StockData("SUNW", "Sun Microsystems", 140.625, 130.9375, 140.625, 130.9375, 10, 10.625, 17734600, 0.0, 0.0));
    data.addElement(
      new StockData("IBMAD", "Intl. Bus. Machines", 183, 183.125, 183, 183.125, -0.125, -0.51, 4371400, 0.0, 0.0));
    data.addElement(new StockData("HWP", "Hewlett-Packard", 70, 71.0625, 70, 71.0625, -1.4375, -2.01, 2410700, 0.0, 0.0));
    data.addElement(new StockData("UIS", "Unisys Corp.", 28.25, 29, 28.25, 29, -0.75, -2.59, 2576200, 0.0, 0.0));
    data.addElement(new StockData("SNE", "Sony Corp.", 28.25, 29, 28.25, 29, -0.75, -2.59, 2576200, 0.0, 0.0));
    data.addElement(
      new StockData("NOVL", "Novell Inc.", 24.0625, 24.375, 24.0625, 24.375, -0.3125, -3.02, 6047900, 0.0, 0.0));
    data.addElement(new StockData("HIT", "Hitachi, Ltd.", 78.5, 77.625, 78.5, 77.625, 0.875, 1.12, 49400, 0.0, 0.0));

//    parser.getData(data);
//    System.out.println("Here is the result");
//    for (int i = 0; i < data.size(); i++)
//    {
//      System.out.println(data.elementAt(i));
//    }

    StockData sd =
      new StockData("IBM", "Intl. Bus. Machines", 183, 183.125, 183, 183.125, -0.125, -0.51, 4371400, 0.0, 0.0);
    parser.getData(sd);
    System.out.println("Here is the result");
    System.out.println(sd);

    System.exit(0);
  }
  /**
   * @see GetPageInfo#processHTMLLine(String)
   */
  protected synchronized void processHTMLLine(String fileLine)
  {
    String stockDataSource = appProps.getProperty("StockDataCSV.stock_symbol_source", "YAHOO");

    if (stockDataSource.equals("YAHOO"))
    {
      processYahooHTMLLine(fileLine);
    }
    return;
  }
  /**
   * Method processYahooHTMLLine. Process the information if it is comming 
   * from YAHOO
   * @param fileLine
   */
  protected void processYahooHTMLLine(String fileLine)
  {
    try
    {
      String line = fileLine;

      // Keep reading until a runtime exception occurrs
      if (line != null)
      {
        String[] lineArr = ParseData.splitByChars(line, ",");
        if (lineArr.length < 2)
        {
          return;
        }
        for (int i = 0; i < lineArr.length; i++)
        {
          try
          { // Strip any quotes if they exist.  NOTE: this may throw an
            // Exception if there are no Quotes.
            int startIndex = lineArr[i].indexOf("\"");
            int endIndex = lineArr[i].indexOf("\"", startIndex + 1);
            lineArr[i] = lineArr[i].substring(startIndex + 1, endIndex);
          }
          // We know we may encounter an StringIndexOutOfBoundsException, 
          // so swallow it - as not all data contains quotes around it
          catch (StringIndexOutOfBoundsException exc)
          {}
        }
        int j = 0;
        char[] charArr = fieldsList.toCharArray();
        for (int i = 0; i < lineArr.length; i++, j++)
        {
          char ch = charArr[j];
          switch (ch)
          {
            //////////////////// snok1k2vwp

            //////////////////// snobalcvw
            case 'l' : // Last
              try
              {
                String[] lastArr = ParseData.splitByChars(lineArr[i], ">");
                this.last = lastArr[1];
              }
              catch (StringIndexOutOfBoundsException exc)
              {}
              break;
            case 'k' :
              if ((j + 1) < charArr.length)
              {
                char ch2 = charArr[j + 1];
                switch (ch2)
                {
                  case '1' : // Real Time Market Last Trade and Time  - k1
                    try
                    {
                      if ( lineArr[i].indexOf("-") > 0 )
                      {
                        lineArr[i] = lineArr[i].substring( lineArr[i].indexOf("-") );
                      }
                      this.last = lineArr[i].substring(lineArr[i].indexOf("<") );
                    }
                    catch (StringIndexOutOfBoundsException exc)
                    {}
                    j++;
                    break;
                  case '2' : // Real Time Market Change and Change Percent - k2
                    try
                    {
                      // This string can come in a form of postive and negative
                      // for example: -0.60 - -0.49%
                      // so spliting on a negative sign would not be a good thing.
                      String[] changeArr = ParseData.splitByChars(lineArr[i].trim(), " ");
                      this.change = changeArr[0];
                      this.changePr = changeArr[2];
                    }
                    catch (StringIndexOutOfBoundsException exc)
                    {}
                    j++;
                    break;
                  default :
                    break;
                }
              }
              break;
            case 'c' : // Delayed Change and Change Percent
              try
              {
                // This string can come in a form of postive and negative
                // for example: -0.60 - -0.49%
                // so spliting on a negative sign would not be a good thing.
                String[] changeArr = ParseData.splitByChars(lineArr[i].trim(), " ");
                this.change = changeArr[0];
                this.changePr = changeArr[2];
              }
              catch (StringIndexOutOfBoundsException exc)
              {}
              break;
            case 's' : // Symbol Name
              this.symbol = lineArr[i];
              String optionEXT = getString("StockDataCSV.yahoo.option_symbol_yahoo_extension");
              int xPos = this.symbol.indexOf(optionEXT, 0);
              if (xPos > 0)
              {
                this.symbol = this.symbol.substring(0, xPos);
              }
              break;
            case 'n' : // Company Name
              this.name = lineArr[i];
              break;
            case 'p' : // Previous Close
              this.previousClose = lineArr[i];
              break;
            case 'o' : // Open
              this.open = lineArr[i];
              break;
            case 'b' : // Bid || Ask
              if ((j + 1) < charArr.length)
              {
                char ch2 = charArr[j + 1];
                switch (ch2)
                {
                  case '2' : // Real Time Market Ask = b2
                    this.ask = lineArr[i];
                    j++;
                    break;
                  case '3' : // Real Time Market Bid = b3
                    this.bid = lineArr[i];
                    j++;
                    break;
                  default : // Delayed Market Bid
                    this.bid = lineArr[i];
                    break;
                }
              }
              else
              { // Delayed Market Bid
                this.bid = lineArr[i];
              }
              break;
            case 'a' : // Ask
              this.ask = lineArr[i];
              break;
            case 'v' :
              this.volume = lineArr[i];
              break;
            case 'w' :
              try
              {
                // This is divided by a dash - However for safety sake we are using the space
                // as a divider
                String[] rangeArr = ParseData.splitByChars(lineArr[i].trim(), " ");
                this.week52Low = rangeArr[0];
                this.week52High = (rangeArr.length == 2) ? rangeArr[1] : rangeArr[2];
              }
              catch (StringIndexOutOfBoundsException exc)
              {}
              break;
            default :
              System.err.println("StockDataCSVParser::ProcessHTMLLine() - ERROR");
              System.err.println("Unknown field Option [" + ch + "], However it has a returned value of [" + lineArr[i] + "]");
              break;
          }
        }
      }

      // If we were using a Vector, then lets find the approipriate
      // object to insert 
      if (vector != null)
      {
        for (int i = 0; i < vector.size(); i++)
        {
          if (vector.elementAt(i).equals(this.symbol))
          {
            sd = (StockData) vector.elementAt(i);
            i = vector.size();
          }
        }
      }
      if (sd != null)
      {
        sd.setStockData(
          this.symbol,
          this.name,
          this.bid,
          this.ask,
          this.last,
          this.open,
          this.change,
          this.changePr,
          this.volume,
          this.week52Low,
          this.week52High,
          this.previousClose);
        sd = null;
      }
    }
    catch (NullPointerException exc)
    {
      String errorMsg = "StockDataCSVParser::processHTMLLine() Caught: [" + exc + "]";
      if (DEBUG)
      {
        System.out.println("NullPointerExc Caught During Processing of data");
        exc.printStackTrace();
        System.out.println("fileLine = " + fileLine);
        System.out.println("    this.symbol        = " + this.symbol);
        System.out.println("    this.name          = " + this.name);
        System.out.println("    this.bid           = " + this.bid);
        System.out.println("    this.ask           = " + this.ask);
        System.out.println("    this.last          = " + this.last);
        System.out.println("    this.open          = " + this.open);
        System.out.println("    this.change        = " + this.change);
        System.out.println("    this.changePr      = " + this.changePr);
        System.out.println("    this.volume        = " + this.volume);
        System.out.println("    this.week52Low     = " + this.week52Low);
        System.out.println("    this.week52High    = " + this.week52High);
        System.out.println("    this.previousClose = " + this.previousClose);
        System.exit(2);
      }
      throw new RuntimeException(errorMsg);
    }
    catch (Exception exc)
    {
      String errorMsg = "StockDataCSVParser::processHTMLLine() Caught: [" + exc + "]";
      if (StockMarketTypes.DEBUG)
      {
        System.out.println(errorMsg);
      }
      throw new RuntimeException(errorMsg);
    }
    return;
  }
  /**
   * Method to set if Real Time Quotes
   * @param newRealTimeQuotes boolean
   */
  public static void setRealTimeQuotes(boolean newRealTimeQuotes)
  {
    realTimeQuotes = newRealTimeQuotes;
  }
  protected String getResourceBundlePropName()
  {
    return this.propertyFileName;
  }
}
