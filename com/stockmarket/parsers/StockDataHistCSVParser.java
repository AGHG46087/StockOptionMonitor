// StockDataHistCSVParser.java
package com.stockmarket.parsers;

import com.stockmarket.data.*;
import com.stockmarket.parsers.httpclient.GetPageInfo;
import com.hgutil.*;
import com.hgutil.data.*;
import java.util.*;

/**
 * Class Object to retrieve Historic Stock Market Data from the Internet
 * @author Hans-Jurgen Greiner
 */
public class StockDataHistCSVParser extends GetPageInfo
{
  private final String propertyFileName = "resources.HtmlStockParsers";
  private ResourceBundle bundle = null;
  private String[] titles = null;
  private Vector vector = null;
  protected int symbolStates = 0;
  private String localSymbol = null;
  /**
   * StockSymbolParser constructor comment.
   */
  public StockDataHistCSVParser()
  {
    super();
  }
  /**
   * Method parses the data and setup a post request to the
   * the web site - In this case Yahoo.com
   * the Date Format string is the form dd-mm-yyyy
   * @param symbol The StockMarket Symbol;
   * @param fromDate a String representation of the start date dd-mm-yyyy
   * @param toDate a String representation of the end date dd-mm-yyyy
   * @param frequency a String representation the freq    uency ( "d", "w", "m" ) daily, weekly, monthly 
   * @return Vector a list of the result data 
   */
  public Vector getHistoricDataBetweenDates(String symbol, String fromDate, String toDate, String FREQUENCY)
  {

    String stockDataSource = appProps.getProperty("StockDataHistCSV.stock_symbol_source", "YAHOO");
    if ( stockDataSource.equals( "YAHOO" ) )
    {
      return getYahooHistoricData(symbol, fromDate, toDate, FREQUENCY);
    }
		return null;
  }
  /**
   * Method parses the data and setup a post request to the
   * the web site - In this case Yahoo.com
   * the Date Format string is the form dd-mm-yyyy
   * @param symbol
   * @param fromDate
   * @param toDate
   * @param FREQUENCY
   * @return Vector
   */
	protected Vector getYahooHistoricData(String symbol, String fromDate, String toDate, String FREQUENCY) {
		localSymbol = symbol;
		String[] startDateValue = ParseData.parseString(fromDate, "-/");
		String[] endDateValue = ParseData.parseString(toDate, "-/");
		//http://chart.yahoo.com/table.csv?s=ibm&a=3&b=26&c=2001&d=7&e=2&f=2001&g=d&q=q&y=0&x=.csv
		String localURL = "";
		
		localURL += getString("StockDataHistCSV.yahoo.stock_symbol_url");
		localURL += getString("StockDataHistCSV.yahoo.stock_symbol_label");
		localURL += symbol + "&";
		localURL += getString("StockDataHistCSV.yahoo.stock_from_month_label");
		localURL += startDateValue[1] + "&";
		localURL += getString("StockDataHistCSV.yahoo.stock_from_day_label");
		localURL += startDateValue[0] + "&";
		localURL += getString("StockDataHistCSV.yahoo.stock_from_year_label");
		localURL += startDateValue[2] + "&";
		localURL += getString("StockDataHistCSV.yahoo.stock_to_month_label");
		localURL += endDateValue[1] + "&";
		localURL += getString("StockDataHistCSV.yahoo.stock_to_day_label");
		localURL += endDateValue[0] + "&";
		localURL += getString("StockDataHistCSV.yahoo.stock_to_year_label");
		localURL += endDateValue[2] + "&";
		localURL += getString("StockDataHistCSV.yahoo.stock_frequency_label");
		localURL += FREQUENCY + "&";
		localURL += getString("StockDataHistCSV.yahoo.stock_required_extra_params") + "&";
		localURL += getString("StockDataHistCSV.yahoo.stock_file_type_label");
		localURL += getString("StockDataHistCSV.yahoo.stock_file_type_value");
		try
		{
		  if (DEBUG)
		  {
		    System.out.println(
		      "Attempting Retrievel of historic " + "stockQuotes for " + symbol + " between " + fromDate + " to " + toDate);
		    System.out.println("Compiled URL is: \n" + localURL);
		  }
		  getInfoPage(localURL);
		}
		catch (RuntimeException exc)
		{
		  String errorMsg = "StockDataHistCSVParser::getHistoricDataBetweenDates() - We have a problem: \n[" + exc + "]";
		  System.out.println(errorMsg);
		  this.dumpProperties();
		  System.out.println("Error ocurred processing URL:\n" + localURL);
		}
		finally
		{ // Reset the Symbol States when we are done
		  symbolStates = 0;
		  if (vector == null)
		  {
		    vector = new Vector();
		  }
		}
		
		return vector;
	}
  /**
   * Method parses the data and setup a post request to the
   * the web site - In this case Yahoo.com
   * the Date Format string is the form dd-mm-yyyy
   * @param symbol The StockMarket Symbol;
   * @param fromDate a Date representation of the start date dd-mm-yyyy
   * @param toDate a Date representation of the end date dd-mm-yyyy
   * @param frequency a String representation the frequency ( "d", "w", "m" ) daily, weekly, monthly 
   * @return Vector a list of the result data 
   */
  public Vector getHistoricDataBetweenDates(String symbol, Date fromDate, Date toDate, String FREQUENCY)
  {

    String dateFmt = getString("StockDataHistCSV.yahoo.historic_data_date_format");
    String fromDateSz = ParseData.format(fromDate, dateFmt);
    String toDateSz = ParseData.format(toDate, dateFmt);
    return (getHistoricDataBetweenDates(symbol, fromDateSz, toDateSz, FREQUENCY));

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
   * Method main. Main Test Method
   * @param args
   */
  public static void main(String[] args)
  {
    StockDataHistCSVParser.preloadProxyInfo("StockApp.ini");
    StockDataHistCSVParser parser = new StockDataHistCSVParser();

    // StockMarketSymbols symbol, String fromDate, String toDate, String FREQUENCY
    GregorianCalendar now = new GregorianCalendar();
    GregorianCalendar then = new GregorianCalendar();
    then.add(Calendar.YEAR, -1);
    Vector list = parser.getHistoricDataBetweenDates("IBM", then.getTime(), now.getTime(), StockMarketTypes.DAILY);
    for (int i = 0; i < list.size(); i++)
    {
      System.out.println("List[" + i + "] : " + list.elementAt(i));
    }
    HistoricStockDataContainer hsd = new HistoricStockDataContainer(list);
    System.out.println("Volatility = " + hsd.getHistoricVolatitlity());

    System.exit(0);
  }
  /**
   * Implementation of required Method
   * @see GetPageInfo#processHTMLLine(String)
   */
  protected void processHTMLLine(String fileLine)
  {
    String stockDataSource = appProps.getProperty("StockDataHistCSV.stock_symbol_source", "YAHOO");
    if ( stockDataSource.equals( "YAHOO" ) )
    {
      processYahooHTMLLine(fileLine);
    }

  }
  /**
   * Method processYahooHTMLLine.
   * @param fileLine
   */
	protected void processYahooHTMLLine(String fileLine) {
		try
		{
		  if (vector == null)
		  {
		    vector = new Vector();
		  }
		
		  String line = fileLine;
		
		  HistoricStockData stockInfo = null;
		  boolean isData = (Character.isDigit(line.charAt(0)) && !"DATE".startsWith(line.toUpperCase()));
		  if (line != null)
		  { // Keep reading until a runtime exception occurrs
		    if (symbolStates == 0 && !isData)
		    {
		      vector.removeAllElements();
		      symbolStates++;
		      titles = ParseData.parseString(line, ",");
		    }
		
		    if (symbolStates > 0 && isData)
		    {
		      String[] data = ParseData.parseString(line, ",");
		      stockInfo = new HistoricStockData(titles, data);
		      vector.addElement(stockInfo);
		      symbolStates++;
		    }
		  }
		}
		catch (Exception exc)
		{
		  String errorMsg = "StockDataHistCSVParser::processHTMLLine() - We have a problem: [" + exc + "]";
		  if (StockMarketTypes.DEBUG)
		  {
		    System.out.println(errorMsg);
		    exc.printStackTrace();
		  }
		  throw new RuntimeException(errorMsg);
		}
	}
  protected String getResourceBundlePropName()
  {
    return this.propertyFileName;
  }
}
