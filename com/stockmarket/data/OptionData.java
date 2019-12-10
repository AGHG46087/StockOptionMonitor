// OptionData.java
package com.stockmarket.data;

import java.awt.Color;
import java.awt.Image;
import java.io.Serializable;
import javax.swing.*;
import com.hgtable.*;
import com.hgutil.*;
import com.hgutil.data.*;

/**
 * Class Object contains method an data to display a
 * particular Stock
 * @author: Hans-Jurgen Greiner
 */
public class OptionData implements ColumnDataCellValue, ColumnDataCellTooltip, Comparable, Serializable
{
  private String symbol = null;
  private Fraction lastTrade = null;
  private ColoredData change = null;
  private Fraction bid = null;
  private Fraction ask = null;

  private ColoredData bid2_5 = null;
  private ColoredData ask2_5 = null;
  private ColoredData bid5 = null;
  private ColoredData ask5 = null;
  private ColoredData bid10 = null;
  private ColoredData ask10 = null;

  private LongValue volume = null;
  private LongValue openInterest = null;
  private Fraction strikePrice = null;
  private int optionTypeValue = StockMarketUtils.NONE;

  private transient OptionData option2_50 = null;
  private transient OptionData option5_00 = null;
  private transient OptionData option10_0 = null;
  private transient int spreadStrategy2_50 = 0;
  private transient int spreadStrategy5_00 = 0;
  private transient int spreadStrategy10_0 = 0;

  private transient int tradeDaysUntilExpiration = 0;
  protected static transient StockData stockData = null;
  protected static transient double riskFreeInterestRate = 0.0;

  static final public transient String SYMBOLTEXT = "Symbol";
  static final public transient ColumnHeaderData columns[] =
    {
      new ColumnHeaderData(" Symbol   ", 100, JLabel.LEFT, new DefaultCellEditor(new JTextField())),
      new ColumnHeaderData(" Strike   ", 120, JLabel.RIGHT),
      new ColumnHeaderData(" C/P      ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" Last     ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" Bid      ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" Ask      ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" $2.5 Bid ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" $2.5 Ask ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" $5 Bid   ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" $5 Ask   ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" $10 Bid  ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" $10 Ask  ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" Volume   ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" Open Interest ", 100, JLabel.RIGHT),
      new ColumnHeaderData(" Change   ", 100, JLabel.RIGHT)};
  /**
   * Constructor for OptionData.
   */
  public OptionData()
  {
    super();
    setLastTrade(0.0);
    setChange(0.0);
    setBid(0.0);
    setAsk(0.0);
    setVolume(0);
    setOpenInterest(0);
    setStrikePrice(0.0);

  }

  /**
   * Method setOptionData. sets the data for the particular option.
   * @param symbol A String describing the Option Symbol
   * @param lastTrade double representing the last trade
   * @param change double the amount of change since last trade
   * @param bid double the current bid price
   * @param ask double the current ask price
   * @param volume long the volume of the stock option being traded
   * @param openInterest the open interest in the stock option
   * @param strikePrice the strike price of the stock option
   */
  public void setOptionData(
    String symbol,
    double lastTrade,
    double change,
    double bid,
    double ask,
    long volume,
    long openInterest,
    double strikePrice)
  {
    setSymbol(symbol);
    setLastTrade(lastTrade);
    setChange(change);
    setBid(bid);
    setAsk(ask);
    setVolume(volume);
    setOpenInterest(openInterest);
    setStrikePrice(strikePrice);
  }
  /**
   * Method setOptionData. sets the data for the particular option.
   * @param symbol A String describing the Option Symbol
   * @param lastTrade A String representing the last trade
   * @param change A String the amount of change since last trade
   * @param bid A String  the current bid price
   * @param ask A String the current ask price
   * @param volume A String the volume of the stock option being traded
   * @param openInterest A String the open interest in the stock option
   * @param strikePrice A String the strike price of the stock option
   */
  public void setOptionData(
    String symbol,
    String lastTrade,
    String change,
    String bid,
    String ask,
    String volume,
    String openInterest,
    String strikePrice)
  {
    setSymbol(symbol);
    setLastTrade(lastTrade);
    setChange(change);
    setBid(bid);
    setAsk(ask);
    setVolume(volume);
    setOpenInterest(openInterest);
    setStrikePrice(strikePrice);
  }
  /**
   * Gets the data member to the specified value
   * @param symbol String
   */
  public String getSymbol()
  {
    return (symbol);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param last String
   */
  public void setLastTrade(String last)
  {
    String value = "";
    // Strip everything but the value, and valid punctuation
    for (int i = 0; i < last.length(); i++)
    {
      char ch = last.charAt(i);
      if ((Character.isDigit(ch)) || (ch == '-') || (ch == '.'))
      {
        value += ch;
      }
    }

    double tmpLast = ParseData.parseNum(value, 0.0);
    setLastTrade(tmpLast);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * This method also sets the Trend Ticker for the Last value
   * @param last double
   */
  public void setLastTrade(double lastTrade)
  {
    double d1 = 0.0;
    double d2 = lastTrade;
    if (this.lastTrade == null)
    {
      this.lastTrade = new Fraction(lastTrade);
    }
    else
    {
      d1 = this.lastTrade.doubleValue();
      int result = d1 > d2 ? -1 : (d1 < d2 ? 1 : 0);
      this.lastTrade.setValue(lastTrade);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param symbol String
   */
  public void setSymbol(String symbol)
  {
    int daysUntilExpire = 0;
    if (symbol == null)
    {
      this.symbol = "ERROR";
      this.optionTypeValue = StockMarketUtils.NONE;
    }
    else if (symbol != null)
    {
      this.symbol = symbol;
      this.optionTypeValue = (StockMarketUtils.isCallOption(symbol)) ? StockMarketUtils.CALL : StockMarketUtils.PUT;
      daysUntilExpire = StockMarketUtils.daysUntilExpiration(this.symbol, false);
    }
    this.setDaysUntilExpiration(daysUntilExpire);
  }
  /**
   * Method setDaysUntilExpiration. Method to set the number of days until expiration
   * This method should be called when the symbol is set.
   * @param days
   */
  private void setDaysUntilExpiration(int days)
  {
    tradeDaysUntilExpiration = (days >= 0) ? days : 0;
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param change String
   */
  public void setChange(String change)
  {
    double tmpChange = ParseData.parseNum(change, 0.0);
    setChange(tmpChange);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param change double
   */
  public void setChange(double change)
  {
    if (this.change == null)
    {
      this.change = new ColoredData(new Fraction(change));
    }
    else
    {
      this.change.setData(new Fraction(change));
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param bid String
   */
  public void setBid(String bid)
  {
    double tmpBid = ParseData.parseNum(bid, 0.0);
    setBid(tmpBid);
  }

  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param bid double
   */
  public void setBid(double bid)
  {
    if (this.bid == null)
    {
      this.bid = new Fraction(bid);
    }
    else
    {
      this.bid.setValue(bid);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param bid double
   */
  public void set2_5Bid(double bid)
  {
    if (this.bid2_5 == null)
    {
      this.bid2_5 = new ColoredData(new Fraction(bid));
    }
    else
    {
      this.bid2_5.setData(new Fraction(bid));
    }
    this.bid2_5.setBGColor(null); // No Color Preferences
    this.bid2_5.setFGColor(null); // No Color Preferences
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param bid double
   */
  public void set5Bid(double bid)
  {
    if (this.bid5 == null)
    {
      this.bid5 = new ColoredData(new Fraction(bid));
    }
    else
    {
      this.bid5.setData(new Fraction(bid));
    }
    this.bid5.setBGColor(null); // No Color Preferences
    this.bid5.setFGColor(null); // No Color Preferences
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param bid double
   */
  public void set10Bid(double bid)
  {
    if (this.bid10 == null)
    {
      this.bid10 = new ColoredData(new Fraction(bid));
    }
    else
    {
      this.bid10.setData(new Fraction(bid));
    }
    this.bid10.setBGColor(null); // No Color Preferences
    this.bid10.setFGColor(null); // No Color Preferences
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param ask String
   */
  public void setAsk(String ask)
  {
    double tmpAsk = ParseData.parseNum(ask, 0.0);
    setAsk(tmpAsk);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param ask double
   */
  public void setAsk(double ask)
  {
    if (this.ask == null)
    {
      this.ask = new Fraction(ask);
    }
    else
    {
      this.ask.setValue(ask);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param ask double
   */
  public void set2_5Ask(double ask)
  {
    if (this.ask2_5 == null)
    {
      this.ask2_5 = new ColoredData(new Fraction(ask));
    }
    else
    {
      this.ask2_5.setData(new Fraction(ask));
    }
    this.ask2_5.setBGColor(null); // No Color Preferences
    this.ask2_5.setFGColor(null); // No Color Preferences
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param ask double
   */
  public void set5Ask(double ask)
  {
    if (this.ask5 == null)
    {
      this.ask5 = new ColoredData(new Fraction(ask));
    }
    else
    {
      this.ask5.setData(new Fraction(ask));
    }
    this.ask5.setBGColor(null); // No Color Preferences
    this.ask5.setFGColor(null); // No Color Preferences
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param ask double
   */
  public void set10Ask(double ask)
  {
    if (this.ask10 == null)
    {
      this.ask10 = new ColoredData(new Fraction(ask));
    }
    else
    {
      this.ask10.setData(new Fraction(ask));
    }
    this.ask10.setBGColor(null); // No Color Preferences
    this.ask10.setFGColor(null); // No Color Preferences
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param volume String
   */
  public void setVolume(String volume)
  {
    long tmpVolume = ParseData.parseNum(volume, -9999);
    setVolume(tmpVolume);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param volume long
   */
  public void setVolume(long volume)
  {
    if (this.volume == null)
    {
      this.volume = new LongValue(volume);
    }
    else
    {
      this.volume.setValue(volume);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param volume String
   */
  public void setOpenInterest(String openInterest)
  {
    long tmpOpenInterest = ParseData.parseNum(openInterest, -9999);
    setOpenInterest(tmpOpenInterest);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param volume long
   */
  public void setOpenInterest(long openInterest)
  {
    if (this.openInterest == null)
    {
      this.openInterest = new LongValue(openInterest);
    }
    else
    {
      this.openInterest.setValue(openInterest);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param ask String
   */
  public void setStrikePrice(String ask)
  {
    double tmpStrikePrice = ParseData.parseNum(ask, 0.0);
    setStrikePrice(tmpStrikePrice);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param ask double
   */
  public void setStrikePrice(double strikePrice)
  {
    if (this.strikePrice == null)
    {
      this.strikePrice = new Fraction(strikePrice);
    }
    else
    {
      this.strikePrice.setValue(strikePrice);
    }
  }
  /**
   * Comparison compare to method
   * @param obj Object
   * @return int
   */
  public int compareTo(Object obj)
  {
    int rc = -1;

    String tmpSymbol = null;
    if (obj instanceof String)
    {
      tmpSymbol = (String) obj;
    }
    else if (obj instanceof OptionData)
    {
      OptionData tmpOptionData = (OptionData) obj;
      tmpSymbol = tmpOptionData.getSymbol();
    }
    else if (obj instanceof StockData)
    {
      StockData tmpStockData = (StockData) obj;
      tmpSymbol = tmpStockData.getSymbol().getDataSz();
    }
    rc = symbol.compareTo(tmpSymbol);
    return rc;
  }
  /**
   * Comparison equals
   * @param obj Object
   * @return boolean
   */
  public boolean equals(Object obj)
  {
    boolean rc = false;

    String tmpSymbol = null;
    if (obj instanceof StockData)
    {
      StockData tmpStockData = (StockData) obj;
      tmpSymbol = tmpStockData.getSymbol().getDataSz();
    }
    else if (obj instanceof OptionData)
    {
      OptionData tmpOptionData = (OptionData) obj;
      tmpSymbol = tmpOptionData.getSymbol();
    }
    else if (obj instanceof String)
    {
      tmpSymbol = (String) obj;
    }
    else if (obj == null)
    {
      return rc;
    }
    rc = symbol.equals(tmpSymbol);

    return (rc);
  }
  /**
   * @see ColumnDataCellTooltip#getCellToolTip(int)
   */
  public String getCellToolTip(int col)
  {
    String rc = "";
    switch (col)
    {
      case 0 :
        rc = createSymbolTooltipText();
        break;
      case 6 : // $2.50 Bid
        rc = create2_5BidTooltipText();
        break;
      case 7 : // $2.50 Ask
        rc = create2_5AskTooltipText();
        break;
      case 8 : // $5.00 Bid
        rc = create5_0BidTooltipText();
        break;
      case 9 : // $5.00 Ask
        rc = create5_0AskTooltipText();
        break;
      case 10 : // $10.00 Bid
        rc = create10_0BidTooltipText();
        break;
      case 11 : // $10.00 Ask
        rc = create10_0AskTooltipText();
        break;
      default :
        break;
    }

    return rc;
  }
  /**
   * Return Object to be displyed in the Column
   * @param col int
   * @return Object
   */
  public Object getColumnDataCell(int col)
  {
    Object obj = "";
    switch (col)
    {
      case 0 :
        obj = getSymbol();
        break;
      case 1 :
        obj = getStrikePrice();
        break;
      case 2 :
        obj = getOptionType();
        break;
      case 3 :
        obj = getLastTrade();
        break;
      case 4 :
        obj = getBid();
        break;
      case 5 :
        obj = getAsk();
        break;
      case 6 :
        obj = get2_5Bid();
        break;
      case 7 :
        obj = get2_5Ask();
        break;
      case 8 :
        obj = get5Bid();
        break;
      case 9 :
        obj = get5Ask();
        break;
      case 10 :
        obj = get10Bid();
        break;
      case 11 :
        obj = get10Ask();
        break;
      case 12 :
        obj = getVolume();
        break;
      case 13 :
        obj = getOpenInterest();
        break;
      case 14 :
        obj = getChange();
        break;
      default :
        obj = "";
        break;
    }
    return obj;
  }
  /**
   * Return last price Object
   * @return Fraction
   */
  public Fraction getLastTrade()
  {
    return (lastTrade);
  }
  /**
   * Return the Change Object
   * @return ColoredData
   */
  public ColoredData getChange()
  {
    return (change);
  }
  /**
   * Return ask price Object
   * @return Fraction
   */
  public Fraction getAsk()
  {
    return (ask);
  }
  /**
   * Return ask price Object
   * @return ColoredData
   */
  public ColoredData get2_5Ask()
  {
    return (ask2_5);
  }
  /**
   * Return ask price Object
   * @return ColoredData
   */
  public ColoredData get5Ask()
  {
    return (ask5);
  }
  /**
   * Return ask price Object
   * @return ColoredData
   */
  public ColoredData get10Ask()
  {
    return (ask10);
  }
  /**
   * Return bid price Object
   * @return Fraction
   */
  public Fraction getBid()
  {
    return (bid);
  }
  /**
   * Return bid price Object
   * @return ColoredData
   */
  public ColoredData get2_5Bid()
  {
    return (bid2_5);
  }
  /**
   * Return bid price Object
   * @return ColoredData
   */
  public ColoredData get5Bid()
  {
    return (bid5);
  }
  /**
   * Return bid price Object
   * @return ColoredData
   */
  public ColoredData get10Bid()
  {
    return (bid10);
  }
  /**
   * Return Volume Object
   * @return LongValue
   */
  public LongValue getVolume()
  {
    return (volume);
  }
  /**
   * Return Open Interest Object
   * @return LongValue
   */
  public LongValue getOpenInterest()
  {
    return (openInterest);
  }
  /**
   * Return strike Price Object
   * @return Fraction
   */
  public Fraction getStrikePrice()
  {
    return (strikePrice);
  }

  /**
   * Method getOptionTypeValue. Returns the Option Type as Defined By the StockMarketUtils
   * @see StockMarketUtils#TYPESARR for the list of available types
   * @return int
   */
  public int getOptionTypeValue()
  {
    return (optionTypeValue);
  }
  /**
   * Method getOptionType. Returns the Option Type as Defined By the StockMarketUtils
   * @see StockMarketUtils#TYPESARR for the list of available types
   * @return String
   */
  public String getOptionType()
  {
    return (StockMarketUtils.TYPESARR[this.optionTypeValue].name);
  }
  /**
   * Method getOptionMonth. Returns the Option Month as Defined By the StockMarketUtils
   * @see StockMarketUtils#CALLMONTHARR for the list of available CALLS months
   * @see StockMarketUtils#PUTMONTHARR for the list of available PUTS months
   * @see StockMarketUtils#MONTHSARR for the list of available months
   * @return String
   */
  public String getOptionMonth()
  {
    String monthChar = getSymbol().substring(getSymbol().length() - 2, getSymbol().length() - 1);
    int index = 0;
    if (optionTypeValue == StockMarketUtils.CALL)
    {
      for (int i = 0; i < StockMarketUtils.CALLMONTHARR.length; i++)
      {
        if (monthChar.equals(StockMarketUtils.CALLMONTHARR[i].name))
        {
          index = i;
          i = StockMarketUtils.CALLMONTHARR.length;
        }
      }
    }
    else
    {
      for (int i = 0; i < StockMarketUtils.PUTMONTHARR.length; i++)
      {
        if (monthChar.equals(StockMarketUtils.PUTMONTHARR[i].name))
        {
          index = i;
          i = StockMarketUtils.PUTMONTHARR.length;
        }
      }
    }
    String optionMonth = StockMarketUtils.MONTHSARR[index].name;
    return (optionMonth);
  }
  /**
   * @see Object#toString()
   */
  public String toString()
  {
    String temp = "";
    temp += getSymbol() + ";";
    temp += getLastTrade() + ";";
    temp += getChange().getData() + ";";
    temp += getBid() + ";";
    temp += getAsk() + ";";
    temp += getVolume() + ";";
    temp += getOpenInterest() + ";";
    temp += getStrikePrice();

    return temp;

  }
  /**
   * Method setVerticalSpreadStrategy.This method queries the base option
   * for the data object implied by the spreadtype and updates the values as required
   * to signal the base option that it meets all criteria of a Vertical Spread.
   * @param spreadType
   * @param strategyType
   * @param newHighLowNeeded
   */
  public void setVerticalSpreadStrategy(int spreadType, int strategyType, boolean newHighLowNeeded)
  {
    // First target the Data Object based on SpreadType, and Flag the Strategy applied
    ColoredData coloredData = null;
    Color fg = null;
    Color bg = null;
    // Bull Call and Bear Put are based on targeting the Ask price - Pay Premium
    // Bull Call Stock move Up
    // Bear Put  Stock move Down
    if ((strategyType == StockMarketUtils.STRATEGY_BULL_CALL_SPREAD)
      || (strategyType == StockMarketUtils.STRATEGY_BEAR_PUT_SPREAD))
    {
      switch (spreadType)
      {
        case StockMarketUtils.SPREAD_2_50 :
          coloredData = this.get2_5Ask();
          spreadStrategy2_50 = (coloredData != null) ? strategyType : 0;
          break;
        case StockMarketUtils.SPREAD_5_00 :
          coloredData = this.get5Ask();
          spreadStrategy5_00 = (coloredData != null) ? strategyType : 0;
          break;
        case StockMarketUtils.SPREAD_10_00 :
          coloredData = this.get10Ask();
          spreadStrategy10_0 = (coloredData != null) ? strategyType : 0;
          break;
      }
      if (newHighLowNeeded)
      {
        fg = Color.yellow;
        bg = Color.blue;
      }
      else
      {
        fg = Color.black;
        bg = Color.green;
      }
    }
    // Bear Call and Bull Put are based on targeting the Bid price - Receive Premium
    // Bear Call Stock move Down
    // Bull Put  Stock move Up
    else if (
      (strategyType == StockMarketUtils.STRATEGY_BEAR_CALL_SPREAD)
        || (strategyType == StockMarketUtils.STRATEGY_BULL_PUT_SPREAD))
    {
      switch (spreadType)
      {
        case StockMarketUtils.SPREAD_2_50 :
          coloredData = this.get2_5Bid();
          spreadStrategy2_50 = (coloredData != null) ? strategyType : 0;
          break;
        case StockMarketUtils.SPREAD_5_00 :
          coloredData = this.get5Bid();
          spreadStrategy5_00 = (coloredData != null) ? strategyType : 0;
          break;
        case StockMarketUtils.SPREAD_10_00 :
          coloredData = this.get10Bid();
          spreadStrategy10_0 = (coloredData != null) ? strategyType : 0;
          break;
      }
      if (newHighLowNeeded)
      {
        fg = Color.yellow;
        bg = Color.blue;
      }
      else
      {
        fg = Color.cyan;
        bg = Color.blue;
      }
    }
    // if Colored Data is not Null set the colors
    if (coloredData != null)
    {
      coloredData.setFG_BGColor(fg, bg);
    }
  }
  /**
   * Method set2_50Pair. Sets the Paired 2_50 Option with the this option
   * Calculation will be the WorstCase Scenario between BID and ASK
   * @param od The OptionData that is a 2.50 strike Price pair
   */
  public void set2_50Pair(OptionData od)
  {
    option2_50 = od;

    if (od != null)
    {
      // Here is how it works:
      // We grab the values of the next strike price
      double ask2 = od.getAsk().doubleValue();
      double bid2 = od.getBid().doubleValue();

      // Compute the Worst case scenario OF bid and Ask
      // NOW set the 2.50 ASK by subtracting Strike Price target ASK from Out Of Money BID value
      this.set2_5Ask(Math.abs(bid2 - this.getAsk().doubleValue()));
      // NOW set the 2.50 BID by subtracting Strike Price target BID from Out Of Money ASK value
      this.set2_5Bid(Math.abs(ask2 - this.getBid().doubleValue()));
    }
  }
  /**
   * Method set5_00Pair. Sets the Paired 2_50 Option with the this option
   * Calculation will be the WorstCase Scenario between BID and ASK
   * @param od The OptionData that is a 2.50 strike Price pair
   */
  public void set5_00Pair(OptionData od)
  {
    option5_00 = od;
    if (od != null)
    {
      double ask2 = od.getAsk().doubleValue();
      double bid2 = od.getBid().doubleValue();

      // Compute the Worst case scenario OF bid and Ask
      // NOW set the 5.00 ASK by subtracting Strike Price target ASK from Out Of Money BID value
      this.set5Ask(Math.abs(bid2 - this.getAsk().doubleValue()));
      // NOW set the 5.00 BID by subtracting Strike Price target BID from Out Of Money ASK value
      this.set5Bid(Math.abs(ask2 - this.getBid().doubleValue()));
    }
  }
  /**
   * Method set10_0Pair. Sets the Paired 2_50 Option with the this option
   * Calculation will be the WorstCase Scenario between BID and ASK
   * @param od The OptionData that is a 2.50 strike Price pair
   */
  public void set10_0Pair(OptionData od)
  {
    option10_0 = od;

    if (od != null)
    {
      double ask2 = od.getAsk().doubleValue();
      double bid2 = od.getBid().doubleValue();

      // Compute the Worst case scenario OF BID and ASK
      // NOW set the 10.00 ASK by subtracting Strike Price target ASK from Out Of Money BID value
      this.set10Ask(Math.abs(bid2 - this.getAsk().doubleValue()));
      // NOW set the 10.00 BID by subtracting Strike Price target BID from Out Of Money ASK value
      this.set10Bid(Math.abs(ask2 - this.getBid().doubleValue()));
    }
  }
  /**
   * Method setStockData. Static method to set the stock data referenced by
   * this option
   * @param sd The StockData
   */
  public static void setStockData(StockData sd)
  {
    stockData = sd;
  }
  /**
   * Method setStockData. Static method to set the stock data referenced by
   * this option
   * @param StockData
   */
  public static StockData getStockData()
  {
    return (stockData);
  }
  /**
   * Method get2_50Pair. Gets the Paired 2_50 Option with the this option
   * @return OptionData The OptionData that is a 2.50 strike Price Pair
   */
  public OptionData get2_50Pair()
  {
    return (option2_50);
  }
  /**
   * Method get5_00Pair. Gets the Paired 2_50 Option with the this option
   * @return OptionData The OptionData that is a 2.50 strike Price Pair
   */
  public OptionData get5_00Pair()
  {
    return (option5_00);
  }
  /**
   * Method get10_0Pair. Gets the Paired 2_50 Option with the this option
   * @return OptionData The OptionData that is a 2.50 strike Price Pair
   */
  public OptionData get10_0Pair()
  {
    return (option10_0);
  }
  /**
   * Method setRiskFreeRate. Sets the Risk Free Interest Rate
   * @param rate
   */
  public static void setRiskFreeRate(double rate)
  {
    riskFreeInterestRate = rate;
  }
  /**
   * Method getRiskFreeRate. Returns the RiskFreeRate
   * @return double
   */
  public static double getRiskFreeRate()
  {
    return (riskFreeInterestRate);
  }
  /**
   * Method createSymbolTooltipText. Method to create a tool tip based on
   * the Symbol field.
   * @return String
   */
  private String createSymbolTooltipText()
  {
    String rc = "";

    if (stockData != null)
    {
      // GEEK - When Dave finishes the calculations make adjustments to add all the 
      //        Greek Values and Volatility values
      String msg =
        "<html>"
          + "<B>"
          + stockData.getSymbol().getDataSz()
          + "</B> "
          + getOptionMonth()
          + " "
          + ParseData.format(this.getStrikePrice().doubleValue(), "$#0.00")
          + " "
          + this.getOptionType()
          + "</html>";
      rc = msg;

    }

    return rc;
  }
  /**
   * Method create2_5BidTooltipText Method to create a tool tip based on
   * the 2.50 bid Spread
   * @return String
   */
  private String create2_5BidTooltipText()
  {
    String rc = "";

    if ((option2_50 != null)
      && ((spreadStrategy2_50 == StockMarketUtils.STRATEGY_BEAR_CALL_SPREAD)
        || (spreadStrategy2_50 == StockMarketUtils.STRATEGY_BULL_PUT_SPREAD)))
    {
      String strategyMsg = null;
      String symbolMsg = null;
      if (spreadStrategy2_50 == StockMarketUtils.STRATEGY_BEAR_CALL_SPREAD)
      {
        strategyMsg = "Bear Call $2.50 Spread";
        symbolMsg = this.symbol + "\\" + option2_50.getSymbol();
      }
      else
      {
        strategyMsg = "Bull Put $2.50 Spread";
        symbolMsg = this.symbol + "/" + option2_50.getSymbol();
      }
      String msg = "<html>" + "<B>" + strategyMsg + "</B><BR>" + symbolMsg + "</html>";
      rc = msg;

    }

    return rc;
  }
  /**
   * Method create2_5AskTooltipText. Method to create a tool tip based on
   * the 2.50 ask Spread
   * @return String
   */
  private String create2_5AskTooltipText()
  {
    String rc = "";

    if ((option2_50 != null)
      && ((spreadStrategy2_50 == StockMarketUtils.STRATEGY_BULL_CALL_SPREAD)
        || (spreadStrategy2_50 == StockMarketUtils.STRATEGY_BEAR_PUT_SPREAD)))
    {
      String strategyMsg = null;
      String symbolMsg = null;
      if (spreadStrategy2_50 == StockMarketUtils.STRATEGY_BULL_CALL_SPREAD)
      {
        strategyMsg = "Bull Call $2.50 Spread";
        symbolMsg = this.symbol + "/" + option2_50.getSymbol();
      }
      else
      {
        strategyMsg = "Bear Put $2.50 Spread";
        symbolMsg = this.symbol + "\\" + option2_50.getSymbol();
      }
      String msg = "<html>" + "<B>" + strategyMsg + "</B><BR>" + symbolMsg + "</html>";
      rc = msg;

    }

    return rc;
  }
  /**
   * Method create5_0BidTooltipText Method to create a tool tip based on
   * the 5.00 bid Spread
   * @return String
   */
  private String create5_0BidTooltipText()
  {
    String rc = "";

    if ((option5_00 != null)
      && ((spreadStrategy5_00 == StockMarketUtils.STRATEGY_BEAR_CALL_SPREAD)
        || (spreadStrategy5_00 == StockMarketUtils.STRATEGY_BULL_PUT_SPREAD)))
    {
      String strategyMsg = null;
      String symbolMsg = null;
      if (spreadStrategy5_00 == StockMarketUtils.STRATEGY_BEAR_CALL_SPREAD)
      {
        strategyMsg = "Bear Call $5.00 Spread";
        symbolMsg = this.symbol + "\\" + option5_00.getSymbol();
      }
      else
      {
        strategyMsg = "Bull Put $5.00 Spread";
        symbolMsg = this.symbol + "/" + option5_00.getSymbol();
      }
      String msg = "<html>" + "<B>" + strategyMsg + "</B><BR>" + symbolMsg + "</html>";
      rc = msg;

    }

    return rc;
  }
  /**
   * Method create5_0AskTooltipText. Method to create a tool tip based on
   * the 5.00 ask Spread
   * @return String
   */
  private String create5_0AskTooltipText()
  {
    String rc = "";

    if ((option5_00 != null)
      && ((spreadStrategy5_00 == StockMarketUtils.STRATEGY_BULL_CALL_SPREAD)
        || (spreadStrategy5_00 == StockMarketUtils.STRATEGY_BEAR_PUT_SPREAD)))
    {
      String strategyMsg = null;
      String symbolMsg = null;
      if (spreadStrategy5_00 == StockMarketUtils.STRATEGY_BULL_CALL_SPREAD)
      {
        strategyMsg = "Bull Call $5.00 Spread";
        symbolMsg = this.symbol + "/" + option5_00.getSymbol();
      }
      else
      {
        strategyMsg = "Bear Put $5.00 Spread";
        symbolMsg = this.symbol + "\\" + option5_00.getSymbol();
      }
      String msg = "<html>" + "<B>" + strategyMsg + "</B><BR>" + symbolMsg + "</html>";
      rc = msg;

    }

    return rc;
  }
  /**
   * Method create10_0BidTooltipText. Method to create a tool tip based on
   * the 10.00 bid Spread
   * @return String
   */
  private String create10_0BidTooltipText()
  {
    String rc = "";

    if ((option10_0 != null)
      && ((spreadStrategy10_0 == StockMarketUtils.STRATEGY_BEAR_CALL_SPREAD)
        || (spreadStrategy10_0 == StockMarketUtils.STRATEGY_BULL_PUT_SPREAD)))
    {
      String strategyMsg = null;
      String symbolMsg = null;
      if (spreadStrategy10_0 == StockMarketUtils.STRATEGY_BEAR_CALL_SPREAD)
      {
        strategyMsg = "Bear Call $10.00 Spread";
        symbolMsg = this.symbol + "\\" + option10_0.getSymbol();
      }
      else
      {
        strategyMsg = "Bull Put $10.00 Spread";
        symbolMsg = this.symbol + "/" + option10_0.getSymbol();
      }
      String msg = "<html>" + "<B>" + strategyMsg + "</B><BR>" + symbolMsg + "</html>";
      rc = msg;

    }

    return rc;
  }
  /**
   * Method create10_0AskTooltipText. Method to create a tool tip based on
   * the 10.00 ask Spread
   * @return String
   */
  private String create10_0AskTooltipText()
  {
    String rc = "";

    if ((option10_0 != null)
      && ((spreadStrategy10_0 == StockMarketUtils.STRATEGY_BULL_CALL_SPREAD)
        || (spreadStrategy10_0 == StockMarketUtils.STRATEGY_BEAR_PUT_SPREAD)))
    {
      String strategyMsg = null;
      String symbolMsg = null;
      if (spreadStrategy10_0 == StockMarketUtils.STRATEGY_BULL_CALL_SPREAD)
      {
        strategyMsg = "Bull Call $10.00 Spread";
        symbolMsg = this.symbol + "/" + option10_0.getSymbol();
      }
      else
      {
        strategyMsg = "Bear Put $10.00 Spread";
        symbolMsg = this.symbol + "\\" + option10_0.getSymbol();
      }
      String msg = "<html>" + "<B>" + strategyMsg + "</B><BR>" + symbolMsg + "</html>";
      rc = msg;

    }

    return rc;
  }

  /**
   * Method resetPairedValues. This method forces all values 
   * that are considered paired values to null
   */
  public void resetPairedValues()
  {
    bid2_5 = null;
    ask2_5 = null;
    bid5 = null;
    ask5 = null;
    bid10 = null;
    ask10 = null;
    option2_50 = null;
    option5_00 = null;
    option10_0 = null;
    spreadStrategy2_50 = 0;
    spreadStrategy5_00 = 0;
    spreadStrategy10_0 = 0;
    setDaysUntilExpiration(0);
  }
  /**
   * Method initPairedValues.
   * @see OptionData#resetPairedValues
   */
  public void initPairedValues()
  {
    this.resetPairedValues();
  }
  /**
   * Method getGreekDelta. Delta measures the sensitivity of an option's theoretical 
   * value to a change in the price of the underlying asset.  Delta is a very 
   * important number to consider when constructing combination positions.  
   * Call options have positive deltas and put options have negative deltas.  
   * At-the-money options generally have deltas around 50.  Deeper in-the-money 
   * options might have a delta of 80 or higher.  Out-of-the-money options have 
   * deltas as small as 20 or less.  Delta will change as the option becomes 
   * further in or out-of- the money.  When a stock option is deep in the money, 
   * it will begin to trade like the stock - moving dollar for dollar with the 
   * underlying stock, while the far out-of-the-money options don’t move much.
   * @see Financials#getGreekDelta
   * @return double
   */
  protected double getGreekDelta()
  {
    double rc = 0.0;
    if (stockData != null)
    {
      double stockPrice = stockData.getLast().doubleValue();
      double strikePrice = this.getStrikePrice().doubleValue();
      double timeValue = this.tradeDaysUntilExpiration / StockMarketUtils.TRADINGDAYSINYEAR;
      double riskFreeRate = this.riskFreeInterestRate;
      Object data = stockData.getVolatility().getData();
      double volatility = ((Fraction) data).doubleValue();
      rc = Financials.getGreekDelta(Financials.CALL_FLAG, stockPrice, strikePrice, timeValue, riskFreeRate, volatility);
    }
    return rc;
  }
  /**
   * Method getGreekGamma.Since Delta is such an important factor, the marketplace is 
   * interested in how Delta changes.  Gamma measures the rate of change in the delta 
   * for each one-point increase in the underlying asset.  It is a valuable tool in 
   * helping you forecast changes in the delta of an option or an overall position.  
   * Gamma is largest for the at-the-money options, and gets progressively lower for 
   * both the in and out-of-the-money options.  Unlike Delta, Gamma is always 
   * positive for both calls and puts. 
   * @see Financials#getGreekGamma
   * @return double
   */
  protected double getGreekGamma()
  {
    double rc = 0.0;
    if (stockData != null)
    {
      double stockPrice = stockData.getLast().doubleValue();
      double strikePrice = this.getStrikePrice().doubleValue();
      double timeValue = this.tradeDaysUntilExpiration / StockMarketUtils.TRADINGDAYSINYEAR;
      double riskFreeRate = this.riskFreeInterestRate;
      Object data = stockData.getVolatility().getData();
      double volatility = ((Fraction) data).doubleValue();
      rc = Financials.getGreekGamma(stockPrice, strikePrice, timeValue, riskFreeRate, volatility);
    }
    return rc;
  }
  /**
   * Method getGreekVega. Many people confuse Vega and volatility.  
   * Volatility measures fluctuations in the asset itself.  Vega measures the 
   * sensitivity of the price of an option to changes in volatility.  
   * Changes in volatility affect calls and puts and in a similar way.  
   * An increase in volatility will increase the prices of all the options 
   * in an asset, and visa versa.  However, each individual option has its 
   * own Vega and will react differently.  The impact of volatility changes 
   * is greater for at-the-money options than the in or out-of-the-money options.  
   * While Vega affects calls and puts similarly, it seems to affect calls 
   * more than puts.  Perhaps because of the anticipation of market growth 
   * over time, this effect becomes more pronounced for longer-term options, 
   * especially LEAPS. 
   * @see Financials#getGreekVega
   * @return double
   */
  protected double getGreekVega()
  {
    double rc = 0.0;
    if (stockData != null)
    {
      double stockPrice = stockData.getLast().doubleValue();
      double strikePrice = this.getStrikePrice().doubleValue();
      double timeValue = this.tradeDaysUntilExpiration / StockMarketUtils.TRADINGDAYSINYEAR;
      double riskFreeRate = this.riskFreeInterestRate;
      Object data = stockData.getVolatility().getData();
      double volatility = ((Fraction) data).doubleValue();
      rc = Financials.getGreekVega(stockPrice, strikePrice, timeValue, riskFreeRate, volatility);
    }
    return rc;
  }
  /**
   * Method getGreekTheta. Theta is a measure of the time decay of an option.  
   * It is the dollar amount that an option will lose each day.  For at-the-money 
   * options, Theta increases as an option approaches the expiration date.  For in 
   * and out-of-the-money options, theta decreases as an option approaches 
   * expiration.  Theta is one of the most important concepts for a beginning 
   * option trader to understand, because it explains the effect of time on the 
   * premium of the options that have been purchased or sold.  The further out in 
   * time you go, the smaller the time decay will be for an option.  If you want 
   * to own an option, it is advantageous to purchase longer-term contracts.  
   * If you want a strategy that profits from time decay, then you will want to 
   * be short the shorter-term options, so that the loss in value due to time 
   * happens quickly. 
   * @see Financials#getGreekTheta
   * @return double
   */
  protected double getGreekTheta()
  {
    double rc = 0.0;
    if (stockData != null)
    {
      double stockPrice = stockData.getLast().doubleValue();
      double strikePrice = this.getStrikePrice().doubleValue();
      double timeValue = this.tradeDaysUntilExpiration / StockMarketUtils.TRADINGDAYSINYEAR;
      double riskFreeRate = this.riskFreeInterestRate;
      Object data = stockData.getVolatility().getData();
      double volatility = ((Fraction) data).doubleValue();
      rc = Financials.getGreekTheta(Financials.CALL_FLAG, stockPrice, strikePrice, timeValue, riskFreeRate, volatility);
    }
    return rc;
  }
}
