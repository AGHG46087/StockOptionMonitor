// OptionDataParser.java
package com.stockmarket.parsers;
import com.stockmarket.parsers.httpclient.GetPageInfo;
import com.stockmarket.data.OptionData;
import com.stockmarket.data.OptionDataVerticalComparator;
import com.stockmarket.data.StockData;
import com.stockmarket.data.StockMarketUtils;
import com.hgutil.*;
import com.hgutil.data.*;
import java.util.*;

/**
 * Class Object to parse OptionData information from the Internet
 * @author Hans-Jurgen Greiner
 */
public class OptionDataParser extends GetPageInfo
{

  private final String propertyFileName = "resources.HtmlStockParsers";
  private ResourceBundle bundle = null;

  private final boolean OPTION_EXCLUDE_DEEP_IN_THE_MONEY =
    ParseData.parseBool(getString("OptionDataHTML.option_exclude_deep_in_the_money"), false);
  private final double OPTION_EXCLUDE_DEEP_IN_THE_MONEY_VALUE =
    ParseData.parseNum(getString("OptionDataHTML.option_exclude_deep_in_the_money_value"), 0.0);

  private int symbolStates = 0;
  private int linePos = 0;
  private static boolean realTimeQuotes = true;

  private StockData stockData = null;
  private String fieldsList = null;
  private Vector optionList = null;
  private String strikeAmount = null;
  /**
   * Constructor for OptionDataParser.
   */
  public OptionDataParser()
  {
    super();
  }

  /**
   * Method getOptionData. This method will retireve a Stock Data item 
   * and the associated Option chain.
   * @param symbol
   */
  public synchronized Vector getOptionData(StockData sd, Vector optionList, int numberOfMonths) throws RuntimeException
  {
    debug("OptionDataParser::getOptionData() - Entry into Method");
    if (sd == null || sd.getSymbol().getDataSz().equals(""))
    {
      throw new RuntimeException("OptionDataParser::getOptionData()\nStock Data is null - Required Data must be present");
    }

    String stockDataSource = appProps.getProperty("OptionData.stock_symbol_source", "YAHOO");
    this.optionList = optionList;
    if (stockDataSource.equals("YAHOO"))
    {
      getYahooData(sd, numberOfMonths);
    }
    else if (stockDataSource.equals("PCQUOTE"))
    {
      getPCQuoteData(sd);
    }
    debug("OptionDataParser::getOptionData() - Exit the Method");
    return this.optionList;
  }

  /**
   * Method getPCQuoteData. Sets up the request to retrieve data form PCQuote.com
   * @param sd
   * @param numberOfMonths
   */
  private void getPCQuoteData(StockData sd)
  {
    debug("OptionDataParser::getPCQuoteData() - Entry into Method");
    this.stockData = sd;
    // Get the Stock Data
    processPCQuoteStockData(this.stockData);
    //http://www.pcquote.com/options/stringget.php?ticker=IBM&THEORETICALS=0&RANGE=30.5&SHOW=1&FIRSTMONTH=0&MONTHS=2
    String localURL = getString("OptionDataHTML.pcquote.option_data_url");
    String symbol = sd.getSymbol().getDataSz();
    if( symbol.indexOf('^') >= 0 )
    {
      symbol = symbol.substring( symbol.indexOf( '^' ) + 1 );
    }

    localURL += getString("OptionDataHTML.pcquote.option_symbol_descriptor");
    localURL += symbol;
    localURL += "&";
    localURL += getString("OptionDataHTML.pcquote.option_symbol_fields_descriptor");
    try
    {
      if (DEBUG)
      {
        String theSymbol = symbol;
        System.out.println(
          "Attempting Retrievel of a comma seperated vector " + "stockQuote for " + theSymbol + " from the following URL");
        System.out.println("Compiled URL is: \n" + localURL);
      }
      getInfoPage(localURL);
    }
    catch (RuntimeException exc)
    {
      String errorMsg = "OptionDataParser::getPCQuoteData() - We have a problem: \n[" + exc + "]";
      System.out.println(errorMsg);
      this.dumpProperties();
    }
    finally
    { // Reset the Symbol States when we are done
      symbolStates = 0;
      linePos = 0;
    }

    debug("OptionDataParser::getPCQuoteData() - Exit the Method");
    return;
  }

  /**
   * Method getYahooData. This method will retireve a Stock Data item 
   * and the associated Option chain.
   * @param symbol
   */
  private void getYahooData(StockData sd, int numberOfMonths)
  {
    debug("OptionDataParser::getYahooData() - Entry into Method");
    this.stockData = sd;
    //http://finance.yahoo.com/q?q=a&s=IBM&f=snob3b2k1k2vwp&d=o
    String localURL = null; //getString("OptionDataHTML.yahoo.option_data_url");
    String symbol = sd.getSymbol().getDataSz();

    for (int i = 0; i < numberOfMonths; i++)
    {
      localURL = getString("OptionDataHTML.yahoo.option_data_url");
      // At times our Calendar for Yahoo is a off, 
      char monthLetter = (i == 0) ? 'A' : 'B';
      // We only have 1 symbol here so we can move right along 
      // with the other stuff
      localURL += getString("OptionDataHTML.yahoo.option_data_querymonth");
      localURL += monthLetter;
      localURL += "&";
      localURL += getString("OptionDataHTML.yahoo.option_symbol_descriptor");
      localURL += symbol;
      localURL += "&";
      // Now add the fields we are interested in - Assign it to fieldsList
      // for parsing as well.
      localURL += getString("OptionDataHTML.yahoo.option_symbol_fields_descriptor");
      if (realTimeQuotes)
      {
        fieldsList = getString("OptionDataHTML.yahoo.option_symbol_real_time_fields");
        localURL += fieldsList;
      }
      else
      {
        fieldsList = getString("OptionDataHTML.yahoo.option_symbol_fields");
        localURL += fieldsList;
      }
      localURL += "&";
      localURL += getString("OptionDataHTML.yahoo.option_symbol_real_time_descriptor");
      localURL += getString("OptionDataHTML.yahoo.option_symbol_real_time_value");

      try
      {
        if (DEBUG)
        {
          String theSymbol = symbol;
          System.out.println(
            "Attempting Retrievel of a comma seperated vector " + "stockQuote for " + theSymbol + " from the following URL");
          System.out.println("Compiled URL is: \n" + localURL);
        }
        getInfoPage(localURL);
      }
      catch (RuntimeException exc)
      {
        String errorMsg = "OptionDataParser::getYahooData() - We have a problem: \n[" + exc + "]";
        System.out.println(errorMsg);
        this.dumpProperties();
      }
      finally
      { // Reset the Symbol States when we are done
        symbolStates = 0;
        linePos = 0;
      }
    }

    debug("OptionDataParser::getYahooData() - Exit the Method");
    return;
  }
  /**
   * Method processPCQuoteStockData. This method processes the Stock Data object
   * that is passed in as a formal parameter to retrieve the data required.
   * @param sd the StockData object to process.
   */
  private void processPCQuoteStockData(StockData sd)
  {
    debug("OptionDataParser::processYahooOptionDataLine() - Entry into the Method");
    StockDataCSVParser parser = new StockDataCSVParser();
    parser.getData(sd);
    if ( sd.getName().startsWith("Error data") )
    {
      sd.setSymbol("^" + sd.getSymbol().getDataSz());
      parser.getData(sd);
    }
    debug("OptionDataParser::processYahooOptionDataLine() - Exit the Method");
  }
  /**
   * Method processPCQuoteHTMLLine. Method to process a HTML Line from
   * PC QUOTE.com
   * @param fileLine
   */
  protected void processPCQuoteHTMLLine(String fileLine)
  {
    debug("OptionDataParser::processPCQuoteHTMLLine() - Entry into Method");
    try
    {
      String line = fileLine;

      if (line != null) // Keep reading until a runtime exception occurrs
      {
        final String SYMBOL_START_TAG = getString("OptionDataHTML.pcquote.option_option_symbol_start_line_tag");
        if (line.indexOf(SYMBOL_START_TAG) >= 0)
        {
          processPCQuoteOptionDataLine(line);
        }
      }
    }
    catch (Exception exc)
    {
      String errorMsg = "OptionDataParser::processPCQuoteHTMLLine() - We have a problem: [" + exc + "]";
      if (StockMarketTypes.DEBUG)
      {
        System.out.println(errorMsg);
      }
      throw new RuntimeException(errorMsg);
    }
    debug("OptionDataParser::processPCQuoteHTMLLine() - Exit the Method");
  }
  /**
   * Method processPCQuoteOptionDataLine. This method processes a line of data on the option screen
   * from PCQuote.com, This is meat of the screen and poles information about the Calls and Puts
   * @param line
   */
  private void processPCQuoteOptionDataLine(String line)
  {
    debug("OptionDataParser::processPCQuoteOptionDataLine() - Entry into the Method");
    int lineState = 0;
    final int TOTAL_ITEMS_IN_LINE = 8;
    String symbol = null;
    String lastTrade = null;
    String change = null;
    String bid = null;
    String ask = null;
    String volume = null;
    String openInterest = null;
    String strikePrice = null;
    final String OPTION_SYMBOL_START_TAG = getString("OptionDataHTML.pcquote.option_option_symbol_start_line_tag");
    final String OPTION_SYMBOL_END_TAG = getString("OptionDataHTML.pcquote.option_option_symbol_end_line_tag");
    final String OPTION_CALL_START_LINE_TAG = getString("OptionDataHTML.pcquote.option_call_data_start_line_tag");
    final String OPTION_CALL_END_LINE_TAG = getString("OptionDataHTML.pcquote.option_call_data_end_line_tag");
    final String OPTION_PUT_START_LINE_TAG = getString("OptionDataHTML.pcquote.option_put_data_start_line_tag");
    final String OPTION_PUT_END_LINE_TAG = getString("OptionDataHTML.pcquote.option_put_data_end_line_tag");

    if (lineState == 0 && line.indexOf(OPTION_SYMBOL_START_TAG) >= 0)
    {
      symbol = decryptString(OPTION_SYMBOL_START_TAG, OPTION_SYMBOL_END_TAG, line);
      if (symbol.startsWith("."))
      {
        symbol = symbol.substring(symbol.indexOf(".") + 1);
      }
      // Now substring the line data as we are going to scan the same line for more data
      line = line.substring(linePos);
      lineState = 1;
    }
    for (int i = lineState; i < TOTAL_ITEMS_IN_LINE; i++)
    {
      String temp = null;
      if (line.indexOf(OPTION_CALL_START_LINE_TAG) >= 0)
      {
        temp = decryptString(OPTION_CALL_START_LINE_TAG, OPTION_CALL_END_LINE_TAG, line);
        // Now substring the line data as we are going to scan the same line for more data
        line = line.substring(linePos);
        lineState++;
      }
      else if (line.indexOf(OPTION_PUT_START_LINE_TAG) >= 0)
      {
        temp = decryptString(OPTION_PUT_START_LINE_TAG, OPTION_PUT_END_LINE_TAG, line);
        // Now substring the line data as we are going to scan the same line for more data
        line = line.substring(linePos);
        lineState++;
      }
      
      // Strip any residual HTML for example: <b>55</b>
      if (temp != null && temp.indexOf("<") >= 0)
      {
        temp = temp.substring(temp.indexOf(">") + 1, temp.lastIndexOf("<"));
      }
      switch (i)
      {
        case 1 :
          lastTrade = (temp.equals("")) ? "0.0" : temp;
          break;
        case 2 :
          change = (temp.equals("")) ? "0.0" : temp;
          break;
        case 3 :
          bid = (temp.equals("")) ? "0.0" : temp;
          break;
        case 4 :
          ask = (temp.equals("")) ? "0.0" : temp;
          break;
        case 5 :
          volume = (temp.equals("")) ? "0" : temp;
          break;
        case 6 :
          openInterest = (temp.equals("")) ? "0" : temp;
          break;
        case 7 :
          strikePrice = "0.0";
          if (temp != null)
          {
            strikeAmount = (temp.equals("")) ? "0.0" : temp;
            strikePrice = strikeAmount;
          }
          else
          {
            strikePrice = strikeAmount;
            strikeAmount = null;
          }
          break;
        default :
          break;
      }

    }
    // add the information to the vector
    addOptionDataToVector(symbol, lastTrade, change, bid, ask, volume, openInterest, strikePrice);

    debug("OptionDataParser::processPCQuoteOptionDataLine() - Exit the Method");
  }
  /**
   * Method processYahooHTMLLine. Process the information if it is comming 
   * from YAHOO
   * @param fileLine
   */
  protected void processYahooHTMLLine(String fileLine)
  {
    debug("OptionDataParser::processYahooHTMLLine() - Entry into Method");
    try
    {
      String line = fileLine;

      if (line != null) // Keep reading until a runtime exception occurrs
      {
        final String SYMBOL_TITLE_LOC = getString("OptionDataHTML.yahoo.option_symbol_start_line_tag");
        final String SYMBOL_START_TAG = getString("OptionDataHTML.yahoo.stockdata_actual_symbol_start_line_tag");
        final String OPTION_END_ALL_DATA_TAG = getString("OptionDataHTML.yahoo.option_end_all_data_line_tag");
        final String OPTION_SYMBOL_START_TAG = getString("OptionDataHTML.yahoo.option_option_symbol_start_line_tag");
        // Start the symbol states when we have reached the Titles of the Stock Data
        // on the page.
        if ((symbolStates == 0) && (line.indexOf(SYMBOL_TITLE_LOC) > 0))
        {
          debug("OptionDataParser::processYahooHTMLLine() - Symbolstates==0 and start of StockData Found");
          symbolStates++;
          linePos = 0;
        }
        // Now we should have the basic information we need for populating
        // The option data
        else if ((symbolStates == 1) && (line.indexOf(SYMBOL_START_TAG) >= 0))
        {
          debug("OptionDataParser::processYahooHTMLLine() - Symbolstates==1 and processing of StockData Found");
          processYahooStockDataLine(line);
          symbolStates++;
        }
        // Now we should have the the Stock Data info - begin search for Option
        // Data, The tag is the same for the option data as for the stock data
        else if ((symbolStates == 2) && (line.indexOf(SYMBOL_TITLE_LOC) >= 0))
        {
          debug("OptionDataParser::processYahooHTMLLine() - Symbolstates==2 and start of OptionData Found");
          symbolStates++;
        }
        else if ((symbolStates == 3) && (line.indexOf(OPTION_END_ALL_DATA_TAG) >= 0))
        {
          // If we are here, there is no need to even think about getting more
          // options we are at the end of the options page.
          // Advance the counter to ensure we are not going in any farther
          debug("OptionDataParser::processYahooHTMLLine() - Symbolstates==3 and End Of Page found");
          symbolStates++;
        }
        // Now we should have the basic information we need for populating
        // The option data
        else if ((symbolStates == 3) && (line.indexOf(OPTION_SYMBOL_START_TAG) >= 0))
        {
          debug("OptionDataParser::processYahooHTMLLine() - Symbolstates==3 and processing of OptionData Found");
          processYahooOptionDataLine(line);
          // Do not advance the symbolStates counter, we will continue on this
          // until we reached the end.
        }
      }
    }
    catch (Exception exc)
    {
      String errorMsg = "OptionDataParser::processYahooHTMLLine() - We have a problem: [" + exc + "]";
      if (StockMarketTypes.DEBUG)
      {
        System.out.println(errorMsg);
      }
      throw new RuntimeException(errorMsg);
    }
    debug("OptionDataParser::processYahooHTMLLine() - Exit the Method");
  }
  /**
   * Method processYahooStockDataLine. This method processes a line of data on the option screen
   * from Yahoo, This is meat of the screen and poles information about the Calls and Puts
   * @param line
   */
  private void processYahooOptionDataLine(String line)
  {
    debug("OptionDataParser::processYahooOptionDataLine() - Entry into the Method");
    int lineState = 0;
    final int DATAITEMS = 8;
    String symbol = null;
    String lastTrade = null;
    String change = null;
    String bid = null;
    String ask = null;
    String volume = null;
    String openInterest = null;
    String strikePrice = null;
    final String OPTION_SYMBOL_START_TAG = getString("OptionDataHTML.yahoo.option_option_symbol_start_line_tag");
    final String OPTION_SYMBOL_END_TAG = getString("OptionDataHTML.yahoo.option_option_symbol_end_line_tag");
    final String OPTION_LAST_TRADE_START_TAG = getString("OptionDataHTML.yahoo.option_option_last_start_line_tag");
    final String OPTION_LAST_TRADE_END_TAG = getString("OptionDataHTML.yahoo.option_option_last_end_line_tag");
    final String OPTION_STD_START_LINE_TAG = getString("OptionDataHTML.yahoo.option_std_data_start_line_tag");
    final String OPTION_STD_END_LINE_TAG = getString("OptionDataHTML.yahoo.option_std_data_end_line_tag");
    final String OPTION_STRIKE_PRICE_START_TAG = getString("OptionDataHTML.yahoo.option_strike_price_start_line_tag");
    final String OPTION_STRIKE_PRICE_END_TAG = getString("OptionDataHTML.yahoo.option_strike_price_end_line_tag");

    // YES we continue processing on the same line, Therefore no else if type
    // logic here, this line can be inclusive. notice how we substringed the 
    // line data above.  We will do this on the same line for each time
    for (int i = 0; i < DATAITEMS; i++)
    {
      String temp = null;
      debug("OptionDataParser::processYahooOptionDataLine() - LineState==" + i);
      if (line.indexOf(OPTION_SYMBOL_START_TAG) >= 0)
      {
        temp = decryptString(OPTION_SYMBOL_START_TAG, OPTION_SYMBOL_END_TAG, line);
        // Now substring the line data as we are going to scan the same line for more data
        line = line.substring(linePos);
        lineState++; // Target the line for next process
      }
      else if (line.indexOf(OPTION_LAST_TRADE_START_TAG) >= 0)
      {
        temp = decryptString(OPTION_LAST_TRADE_START_TAG, OPTION_LAST_TRADE_END_TAG, line);
        // Now substring the line data as we are going to scan the same line for more data
        line = line.substring(linePos);
        lineState++; // Target the line for next process
      }
      else if (line.indexOf(OPTION_STD_START_LINE_TAG) >= 0)
      {
        temp = decryptString(OPTION_STD_START_LINE_TAG, OPTION_STD_END_LINE_TAG, line);
        // Now substring the line data as we are going to scan the same line for more data
        line = line.substring(linePos);
        lineState++; // Target the line for next process
      }
      else if ((line.indexOf(OPTION_STRIKE_PRICE_START_TAG) >= 0) && (strikeAmount == null))
      {
        // Maintain the strike amount as it will be needed for the PUT option
        temp = decryptString(OPTION_STRIKE_PRICE_START_TAG, OPTION_STRIKE_PRICE_END_TAG, line);
        // Now substring the line data as we are going to scan the same line for more data
        line = line.substring(linePos);
        lineState++; // Target the line for next process
      }

      switch (i)
      {
        case 0 :
          symbol = temp;
          if (symbol.endsWith(".X"))
          {
            symbol = symbol.substring(0, symbol.lastIndexOf(".X"));
          }
          break;
        case 1 :
          lastTrade = temp;
          break;
        case 2 :
          change = temp;
          break;
        case 3 :
          bid = temp;
          break;
        case 4 :
          ask = temp;
          break;
        case 5 :
          String tempVolume = temp;
          // Strip any punctuation in the volume
          volume = "";
          for (int j = 0; j < tempVolume.length(); j++)
          {
            char ch = tempVolume.charAt(j);
            if (Character.isDigit(ch))
            {
              volume += ch;
            }
          }
          break;
        case 6 :
          String tempOpenInt = temp;
          // Strip any punctuation in the open interest
          openInterest = "";
          for (int j = 0; j < tempOpenInt.length(); j++)
          {
            char ch = tempOpenInt.charAt(j);
            if (Character.isDigit(ch))
            {
              openInterest += ch;
            }
          }
          break;
        case 7 :
          strikePrice = "0";
          if (temp != null)
          {
            strikeAmount = temp;
            // Strip any residual HTML for example: <b>55</b>
            if (strikeAmount.indexOf("<") >= 0)
            {
              strikeAmount = strikeAmount.substring(strikeAmount.indexOf(">") + 1, strikeAmount.lastIndexOf("<"));
            }
            strikePrice = strikeAmount;
          }
          else
          {
            strikePrice = strikeAmount;
            strikeAmount = null;
          }
          break;
        default :
          break;
      }
    }
    // add the information to the vector
    addOptionDataToVector(symbol, lastTrade, change, bid, ask, volume, openInterest, strikePrice);
    debug("OptionDataParser::processYahooOptionDataLine() - Exit the Method");
  }

  /**
   * Method isDeepInTheMoney. Method will determine if the option being reviewed is 
   * truly in the money either as a Call option or a Put Option
   * @param symbol String - the Option Symbol
   * @param strike double the Strike value of the Option
   * @return boolean - returns true if the option is in the money and greater than diff
   */
  private boolean isDeepInTheMoney(String symbol, double strike)
  {
    debug("OptionDataParser::isDeepInTheMoney() - Entry into the Method");
    double stockValue = this.stockData.getLast().doubleValue();
    double diff = ((StockMarketUtils.isCallOption(symbol)) ? (stockValue - strike) : (strike - stockValue));
    boolean rc = diff >= OPTION_EXCLUDE_DEEP_IN_THE_MONEY_VALUE;

    debug("OptionDataParser::isDeepInTheMoney() - Exit the Method");
    return rc;
  }
  /**
   * Method addOptionDataToVector. Method to add or set the parsed data to the Vector.
   * depending on if the vector contains the data.
   * @param symbol
   * @param lastTrade
   * @param change
   * @param bid
   * @param ask
   * @param volume
   * @param openInterest
   * @param strikePrice
   */
  private void addOptionDataToVector(
    String symbol,
    String lastTrade,
    String change,
    String bid,
    String ask,
    String volume,
    String openInterest,
    String strikePrice)
  {
    debug("OptionDataParser::addOptionDataToVector() - Entry into the Method");
    // If the Resource Value is set to true to exclude any Deep in the Money
    // Options.  Then process the request to verify the option is not deep in the money
    if (OPTION_EXCLUDE_DEEP_IN_THE_MONEY && isDeepInTheMoney(symbol, ParseData.parseNum(strikePrice, 0.0)))
    {
      debug("OptionDataParser::addOptionDataToVector() - We are deep in the Money");
      debug("OptionDataParser::addOptionDataToVector() - Exit the Method");
      return;
    }

    if (optionList == null)
    {
      optionList = new Vector();
      optionList.removeAllElements();
    }

    OptionData od = null;
    // Loop through all available options that we have.
    for (int i = 0; i < optionList.size(); i++)
    { // Compare the current option in list with the symbol
      if (optionList.elementAt(i).equals(symbol))
      { // We have a match - get a reference to the object
        // short circuit the loop
        debug("OptionDataParser::addOptionDataToVector() - Found Option Data in List");
        od = (OptionData) optionList.elementAt(i);
        i = optionList.size();
      }
    }
    // First check if the option was found
    if (od == null)
    { // We did not have one, so lets create a new instance
      // and add it to the vector
      od = new OptionData();
      optionList.addElement(od);
    }

    debug("OptionDataParser::addOptionDataToVector() - We are valid, setting data");
    // Set the data of the OptionData.
    od.setOptionData(symbol, lastTrade, change, bid, ask, volume, openInterest, strikePrice);
    debug("OptionDataParser::addOptionDataToVector() - Exit the Method");
  }
  /**
   * Method processYahooStockDataLine. This method processes a line of data on the option screen
   * from Yahoo, of just the stock data.
   * @param line
   * @return StockData
   */
  private StockData processYahooStockDataLine(String line)
  {
    debug("OptionDataParser::processYahooStockDataLine() - Entry into the Method");
    final int DATAITEMS = 12;
    int lineState = 0;
    String symbol = null;
    String name = null;
    String open = null;
    String bid = null;
    String ask = null;
    String lastTrade = null;
    String change = null;
    String changePercent = null;
    String volume = null;
    String week52Low = null;
    String week52High = null;
    String prevClose = null;
    final String STD_END_LINE_TAG = getString("OptionDataHTML.yahoo.stockdata_std_data_end_line_tag");
    final String STD_START_LINE_TAG = getString("OptionDataHTML.yahoo.stockdata_std_data_start_line_tag");
    final String SYMBOL_START_TAG = getString("OptionDataHTML.yahoo.stockdata_actual_symbol_start_line_tag");
    final String SYMBOL_END_TAG = getString("OptionDataHTML.yahoo.stockdata_actual_symbol_end_line_tag");

    // YES we continue processing on the same line, Therefore no else if type
    // logic here, this line can be inclusive. notice how we substringed the 
    // line data above.  We will do this on the same line for each time

    for (int i = lineState; i < DATAITEMS; i++)
    {
      String temp = null;
      debug("OptionDataParser::processYahooStockDataLine() - lineState==" + i);
      if (line.indexOf(STD_START_LINE_TAG) >= 0)
      {
        if (line.indexOf(SYMBOL_START_TAG) >= 0)
        {
          temp = decryptString(SYMBOL_START_TAG, SYMBOL_END_TAG, line);
        }
        else
        {
          temp = decryptString(STD_START_LINE_TAG, STD_END_LINE_TAG, line);
        }
        // Now substring the line data as we are going to scan the same line for more data
        line = line.substring(linePos);
        lineState++; // Target the line for next process
      }
      //      else if (line.indexOf(NAME_START_LINE_TAG) >= 0)
      //      {
      //        debug("OptionDataParser::processYahooStockDataLine() - lineState==" + i + " and processing NAME");
      //        temp = decryptString(NAME_START_LINE_TAG, NAME_END_LINE_TAG, line);
      //        line = line.substring(linePos);
      //        lineState++;
      //      }

      switch (i)
      {
        case 0 :
          symbol = temp;
          break;
        case 1 :
          name = temp;
          break;
        case 2 :
          open = temp;
          break;
        case 3 :
          bid = temp;
          break;
        case 4 :
          ask = temp;
          break;
        case 5 :
          String lastDate = temp;
          break;
        case 6 :
          lastTrade = temp;
          if (lastTrade.indexOf("<") >= 0)
          {
            lastTrade = lastTrade.substring(lastTrade.indexOf(">") + 1, lastTrade.lastIndexOf("<"));
          }
          break;
        case 7 :
          change = temp;
          if (change.indexOf("<") >= 0)
          {
            change = change.substring(change.indexOf(">") + 1, change.lastIndexOf("<"));
          }
          break;
        case 8 :
          changePercent = temp;
          if (changePercent.indexOf("<") >= 0)
          {
            changePercent = changePercent.substring(changePercent.indexOf(">") + 1, changePercent.lastIndexOf("<"));
          }
          break;
        case 9 :
          String tempVolume = temp;
          volume = "";
          // Strip any punctuation in the volume
          for (int j = 0; j < tempVolume.length(); j++)
          {
            char ch = tempVolume.charAt(j);
            if (Character.isDigit(ch))
            {
              volume += ch;
            }
          }
          break;
        case 10 :
          String week52Range = temp;
          // Strip any residual HTML for example: <b>55</b>
          if (week52Range.indexOf("<") >= 0)
          {
            week52Range = week52Range.substring(week52Range.indexOf(">") + 1);
          }
          String[] tempArr = ParseData.parseString(week52Range, " -");
          week52Low = tempArr[0];
          week52High = tempArr[1];
          break;
        case 11 :
          prevClose = temp;
          break;
        default :
          break;
      }
    }

    debug("OptionDataParser::processYahooStockDataLine() - We are valid, setting Stock Data");
    this.stockData.setStockData(
      symbol,
      name,
      bid,
      ask,
      lastTrade,
      open,
      change,
      changePercent,
      volume,
      week52Low,
      week52High);
    linePos = 0;
    debug("OptionDataParser::processYahooStockDataLine() - Exit the Method");
    return (this.stockData);
  }
  /**
   * @see GetPageInfo#processHTMLLine(String)
   */
  protected synchronized void processHTMLLine(String fileLine)
  {
    debug("OptionDataParser::processHTMLLine() - Entry into the Method");
    String stockDataSource = appProps.getProperty("OptionData.stock_symbol_source", "YAHOO");

    if (stockDataSource.equals("YAHOO"))
    {
      processYahooHTMLLine(fileLine);
    }
    else if (stockDataSource.equals("PCQUOTE"))
    {
      processPCQuoteHTMLLine(fileLine);
    }
    debug("OptionDataParser::processHTMLLine() - Exit the Method");
    return;
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
    int startPos = 0;
    if (title.endsWith(">"))
    {
      startPos = value.indexOf(title) + title.lastIndexOf(">");
    }
    else
    {
      startPos = value.indexOf(title) + title.length();
    }
    int begin = value.indexOf(">", startPos) + ">".length();
    int end = value.indexOf(endData, begin);
    if ((0 <= begin && begin <= end) && (begin <= end && end < value.length()))
    {
      striptSz = value.substring(begin, end);
      int htmlCharacters = striptSz.indexOf("&");
      if (htmlCharacters > 0)
      {
        striptSz = striptSz.substring(0, htmlCharacters);
      }
      else if (htmlCharacters == 0)
      {
        striptSz = ""; // striptSz.substring(htmlCharacters);
      }
      striptSz = striptSz.trim();

      linePos = end;
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
   * Method main. Test Driver.
   * @param args
   */
  public static void main(String[] args)
  {
    OptionDataParser.preloadProxyInfo("StockApp.ini");
    OptionDataParser parser = new OptionDataParser();

    StockData sd = new StockData();
    sd.setSymbol("BMS");
    Vector list = parser.getOptionData(sd, null, 1);

    Vector calls = StockMarketUtils.getSetOfCallOptions(list, 1);
    Vector puts = StockMarketUtils.getSetOfPutOptions(list, 1);

    list.removeAllElements();

    list.addAll(calls);
    list.addAll(puts);

    System.out.println("Stock Data ==> " + sd);
    for (int i = 0; i < list.size(); i++)
    {
      System.out.println(" " + list.elementAt(i));
    }

    System.exit(0);
  }
  /**
   * @see GetPageInfo#getResourceBundlePropName()
   */
  protected String getResourceBundlePropName()
  {
    return this.propertyFileName;
  }
}
