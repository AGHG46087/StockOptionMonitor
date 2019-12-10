// StockSymbolParser.java
package com.stockmarket.parsers;

import com.stockmarket.parsers.httpclient.GetPageInfo;
import com.hgutil.*;
import com.hgutil.data.*;
import java.util.*;

/**
 * Class Object to parse Symbol information fromt the Internet
 * @author Hans-Jurgen Greiner
 */
public class StockSymbolParser extends GetPageInfo
{
  private final String propertyFileName = "resources.HtmlStockParsers";
  private ResourceBundle bundle = null;
  private String stockSymbolURL = getString("StockSymbolParser.cboe_stock_symbol_url");
  private Vector vector = null;
  final String COMPANY_NAME_START_TAG = getString("StockSymbolParser.cboe_company_name_start_line_tag");
  //COMPANYNAMELINE
  final String COMPANY_NAME_END_TAG = getString("StockSymbolParser.cboe_company_name_end_line_tag");
  // COMPANYENDLINE
  final String TICKER_NAME_START_TAG = getString("StockSymbolParser.cboe_ticker_symbol_start_line_tag");
  //  TKRSYMBOL
  final String TICKER_NAME_END_TAG = getString("StockSymbolParser.cboe_ticker_symbol_end_line_tag");
  // TKRSYMBOLEND  
  final String OPTION_NAME_START_TAG = getString("StockSymbolParser.cboe_option_symbol_start_line_tag");
  //  OPTSYMBOL  
  final String OPTION_NAME_END_TAG = getString("StockSymbolParser.cboe_option_symbol_end_line_tag");
  //  OPTSYMBOLEND  
  private static int symbolStates = 0;
  private String companyName = null;
  private String tkrName = null;
  private String optName = null;
  /**
   * StockSymbolParser constructor comment.
   */
  public StockSymbolParser()
  {
    super();
  }
  /**
   * Insert the method's description here.
   * @param title java.lang.String
   * @param value java.lang.String
   */
  private String decryptString(String title, String endData, String value)
  {

    String striptSz = null;

    if (title == null || value == null)
    {
      return "";
    }
    int begin = value.indexOf(">", value.indexOf(title)) + ">".length();
    int end = value.indexOf(endData, begin);
    if ((0 <= begin && begin <= end) && (begin <= end && end < value.length()))
    {
      striptSz = value.substring(begin, end);
      int htmlCharacters = striptSz.indexOf("&");
      if (htmlCharacters > 0)
      {
        striptSz = striptSz.substring(0, htmlCharacters);
      }
      striptSz = striptSz.trim();
    }
    else
    {
      System.out.println("ERROR: parse String length = [" + value.length() + "]");
      System.out.println("ERROR: ocurred searching for [" + title + "], and end = [" + endData + "]");
      System.out.println("ERROR: Begin index = [" + begin + "], End index = [" + end + "]");
      System.out.println("ERROR: value from begin is [" + value.substring(begin) + "]");
    }

    return (striptSz);
  }
  /**
   * Method getAll.
   * @return Vector
   */
  public Vector getAll()
  {

    vector = new Vector();
    for (char i = 'A'; i <= 'Z'; i++)
    {

      Vector temp = getSymbolsForLetter("" + i);
      for (int j = 0; j < temp.size(); j++)
      {
        vector.addElement(temp.elementAt(j));
      }
    }
    return vector;
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
   * Method getSymbolsForLetter.
   * @param letter
   * @return Vector
   */
  public Vector getSymbolsForLetter(String letter)
  {

    String localURL = stockSymbolURL + letter;

    try
    {
      getInfoPage(localURL);
    }
    catch (RuntimeException exc)
    {
      String errorMsg = "StockSymbolParser::getSymbolsForLetter() - We have a problem: \n[" + exc + "]";
      System.out.println(errorMsg);
      this.dumpProperties();
    }

    if (vector == null)
    {
      vector = new Vector();
    }
    return vector;
  }
  /**
   * Method main.
   * @param args
   */
  public static void main(String[] args)
  {
    StockSymbolParser.preloadProxyInfo("StockApp.ini");
    StockSymbolParser parser = new StockSymbolParser();

    Vector list = null;
    if (args.length > 0)
    {
      list = parser.getSymbolsForLetter(args[0].toUpperCase());
    }
    else
    {
      list = parser.getSymbolsForLetter("I");
    }
    System.out.println("List Size = " + list.size());
    for (int i = 0; i < list.size(); i++)
    {
      System.out.println("List[" + i + "] : " + list.elementAt(i));
    }

    System.exit(0);
  }
  /**
   * @see GetPageInfo#processHTMLLine(String)
   */
  protected void processHTMLLine(String fileLine)
  {
    try
    {
      if (vector == null)
      {
        vector = new Vector();
      }

      String line = fileLine;

      StockMarketSymbols marketSymbol = null;

      if (line != null) // Keep reading until a runtime exception occurrs
      {
        if (DEBUG)
        {
          System.out.println(line);
        }
        if (symbolStates == 0 && line.indexOf(COMPANY_NAME_START_TAG) >= 0)
        {
          companyName = decryptString(COMPANY_NAME_START_TAG, COMPANY_NAME_END_TAG, line);
          symbolStates++;
        }
        else if (symbolStates == 1 && line.indexOf(OPTION_NAME_START_TAG) >= 0)
        {
          optName = decryptString(OPTION_NAME_START_TAG, OPTION_NAME_END_TAG, line);
          symbolStates++;
        }
        else if (symbolStates == 2 && line.indexOf(TICKER_NAME_START_TAG) >= 0)
        {
          tkrName = decryptString(TICKER_NAME_START_TAG, TICKER_NAME_END_TAG, line);
          symbolStates++;
        }
        if (symbolStates == 3)
        {
          marketSymbol = new StockMarketSymbols(companyName, tkrName, optName);
          vector.addElement(marketSymbol);
          symbolStates = 0;
          companyName = tkrName = optName = "";
        }
      }
    }
    catch (Exception exc)
    {
      String errorMsg = "StockSymbolParser::processHTMLLine() - We have a problem: [" + exc + "]";
      if (StockMarketTypes.DEBUG)
      {
        System.out.println(errorMsg);
      }
      throw new RuntimeException(errorMsg);
    }

  }
  protected String getResourceBundlePropName()
  {
    return this.propertyFileName;
  }
}
