// StockData.java
package com.stockmarket.data;

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
public class StockData implements ColumnDataCellValue, Comparable, Serializable
{
  protected IconData symbol = null;
  protected String name = null;
  protected Fraction last = null;
  protected Fraction open = null;
  protected Fraction bid = null;
  protected Fraction ask = null;
  protected ColoredData change = null;
  protected ColoredData changePr = null;
  protected LongValue volume = null;
  protected Fraction week52Low = null;
  protected Fraction week52High = null;
  protected ColoredData volatility = null;
  protected Fraction previousClose = null;

  protected transient DataTrendTick trendTicker = null;

  protected static transient ImageIcon ICON_UP = null;
  protected static transient ImageIcon ICON_DOWN = null;
  protected static transient ImageIcon ICON_BLANK = null;
  protected static transient boolean ICONS_SET = false;
  protected static transient boolean DEBUG = ParseData.parseBool(System.getProperties().getProperty("HGDEBUG"), false);

  static final public transient String SYMBOLTEXT = "Symbol";
  static final public transient ColumnHeaderData columns[] =
    {
      new ColumnHeaderData(SYMBOLTEXT, 100, JLabel.LEFT, new DefaultCellEditor(new JTextField())),
      new ColumnHeaderData("Name", 160, JLabel.LEFT),
      new ColumnHeaderData("Bid", 100, JLabel.RIGHT),
      new ColumnHeaderData("Ask", 100, JLabel.RIGHT),
      new ColumnHeaderData("Last", 100, JLabel.RIGHT),
      new ColumnHeaderData("Open", 100, JLabel.RIGHT),
      new ColumnHeaderData("Change", 100, JLabel.RIGHT),
      new ColumnHeaderData("Change %", 100, JLabel.RIGHT),
      new ColumnHeaderData("Volume", 100, JLabel.RIGHT),
      new ColumnHeaderData("Trend", 100, JLabel.RIGHT),
      new ColumnHeaderData("52 Week Low", 100, JLabel.RIGHT),
      new ColumnHeaderData("52 Week High", 100, JLabel.RIGHT),
      new ColumnHeaderData("Prev Close", 100, JLabel.RIGHT),
      new ColumnHeaderData("Volatility", 100, JLabel.RIGHT)};

  /**
   * StockData constructor comment.
   */
  public StockData()
  {
    super();
    setStockData("", "", 0.0, 0.0, 0.0, 0.0, -0.0001, -0.0001, 0, 0.0, 0.0);
  }
  /**
   * Constructor, Contructor builds a Stock Object
   * @param symbol String
   * @param name   String
   * @param bid    double
   * @param ask    double
   * @param last   double
   * @param open   double
   * @param change double
   * @param changePr double
   * @param volume long
   * @param week52Low double
   * @param week52High double
   */
  public StockData(
    String symbol,
    String name,
    double bid,
    double ask,
    double last,
    double open,
    double change,
    double changePr,
    long volume,
    double week52Low,
    double week52High)
  {
    // Previous Close was not passed in here so we are going to set it with Zero
    this(symbol, name, bid, ask, last, open, change, changePr, volume, week52Low, week52High, 0.0);
  }
  /**
   * Constructor, Contructor builds a Stock Object
   * @param symbol String
   * @param name   String
   * @param bid    double
   * @param ask    double
   * @param last   double
   * @param open   double
   * @param change double
   * @param changePr double
   * @param volume long
   * @param week52Low double
   * @param week52High double
   * @param prevClose double
   */
  public StockData(
    String symbol,
    String name,
    double bid,
    double ask,
    double last,
    double open,
    double change,
    double changePr,
    long volume,
    double week52Low,
    double week52High,
    double prevClose)
  {
    initDataTrendTicker();
    setStockData(symbol, name, bid, ask, last, open, change, changePr, volume, week52Low, week52High, prevClose);
  }
  /**
   * Sets the data builds a Stock Object
   * @param symbol String
   * @param name   String
   * @param bid    String
   * @param ask    String
   * @param last   String
   * @param open   String
   * @param change String
   * @param changePr String
   * @param volume String
   * @param week52Low String
   * @param week52High String
   */
  public StockData(
    String symbol,
    String name,
    String bid,
    String ask,
    String last,
    String open,
    String change,
    String changePr,
    String volume,
    String week52Low,
    String week52High)
  {
    // Previous Close was not passed in her so we are going to set it "0.0"
    this(symbol, name, bid, ask, last, open, change, changePr, volume, week52Low, week52High, "N/A");
  }
  /**
   * Sets the data builds a Stock Object
   * @param symbol String
   * @param name   String
   * @param bid    String
   * @param ask    String
   * @param last   String
   * @param open   String
   * @param change String
   * @param changePr String
   * @param volume String
   * @param week52Low String
   * @param week52High String
   * @param prevClose String
   */
  public StockData(
    String symbol,
    String name,
    String bid,
    String ask,
    String last,
    String open,
    String change,
    String changePr,
    String volume,
    String week52Low,
    String week52High,
    String prevClose)
  {
    initDataTrendTicker();
    setStockData(symbol, name, bid, ask, last, open, change, changePr, volume, week52Low, week52High, prevClose);
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
    if (obj instanceof StockData)
    {
      StockData tmpStockData = (StockData) obj;
      tmpSymbol = tmpStockData.getSymbol().getDataSz();
    }
    else if (obj instanceof String)
    {
      tmpSymbol = (String) obj;
    }
    else if (obj == null)
    {
      return rc;
    }
    rc = symbol.getDataSz().compareTo(tmpSymbol);

    return (rc);
  }
  /**
   * Return an ImageIcon by the specified name
   * @param filename String
   * @return ImageIcon
   */
  public ImageIcon createImageIcon(String filename)
  {
    String path = "/resources/images/" + filename;
    ImageIcon ii = new ImageIcon(getClass().getResource(path));
    return ii;
  }
  /**
   * Return an ImageIcon by the specified name
   * @param filename String
   * @param description String
   * @return ImageIcon
   */
  protected ImageIcon createImageIcon(String filename, String description)
  {
    String path = "/resources/images/" + filename;
    ImageIcon ii = new ImageIcon(getClass().getResource(path));
    return ii;
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
    else if (obj instanceof String)
    {
      tmpSymbol = (String) obj;
    }
    else
    {
      return rc;
    }
    rc = symbol.getDataSz().equals(tmpSymbol);

    return (rc);
  }
  /**
   * Return the 52 week High Object
   * @return Fraction
   */
  public Fraction get52WeekHigh()
  {
    return (week52High);
  }
  /**
   * Return the 52 week Low Object
   * @return Fraction
   */
  public Fraction get52WeekLow()
  {
    return (week52Low);
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
   * Return bid price Object
   * @return Fraction
   */
  public Fraction getBid()
  {
    return (bid);
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
   * Return the Change Percent Object
   * @return ColoredData
   */
  public ColoredData getChangePr()
  {
    return (changePr);
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
        IconData iconData = getSymbol();
        if( iconData.getIcon() == null )
        {
          iconData.setIcon(getIcon(0.0));
        }
        obj = iconData;
        break;
      case 1 :
        obj = getName();
        break;
      case 2 :
        obj = getBid();
        break;
      case 3 :
        obj = getAsk();
        break;
      case 4 :
        obj = getLast();
        break;
      case 5 :
        obj = getOpen();
        break;
      case 6 :
        obj = getChange();
        break;
      case 7 :
        obj = getChangePr();
        break;
      case 8 :
        obj = getVolume();
        break;
      case 9 :
        obj = getDataTrendTicker();
        break;
      case 10 :
        obj = get52WeekLow();
        break;
      case 11 :
        obj = get52WeekHigh();
        break;
      case 12 :
        obj = getPreviousClose();
        break;
      case 13 :
        obj = getVolatility();
        break;
      default :
        obj = "";
        break;
    }
    return obj;
  }
  /**
   * Method to return the Data Trend Ticker, it is mainly protected as to 
   * be used by extending classes
   * @return com.hgutil.datarenderer.DataTrendTick
   */
  protected DataTrendTick getDataTrendTicker()
  {
    if (trendTicker == null)
    {
      trendTicker = new DataTrendTick();
    }
    return trendTicker;
  }
  /**
   * Creates an Image Icon, Based upon change
   * @param change double
   * @return ImageIcon
   */
  public ImageIcon getIcon(double change)
  {
    if (!ICONS_SET)
    {
      initIcons();
    }
    ImageIcon ii = ((change > 0) ? ICON_UP : (change < 0 ? ICON_DOWN : ICON_BLANK));
    return ii;
  }
  /**
   * Return last price Object
   * @return Fraction
   */
  public Fraction getLast()
  {
    return (last);
  }
  /**
   * Return the Name of the Company
   * @return String
   */
  public String getName()
  {
    return (name);
  }
  /**
   * Return open price Object
   * @return Fraction
   */
  public Fraction getOpen()
  {
    return (open);
  }
  /**
   * Method to get the Previous Close
   * @return com.hgutil.datarenderer.Fraction
   */
  public Fraction getPreviousClose()
  {
    return previousClose;
  }
  /**
   * Return Symbol and Icon Object
   * @return IconData
   */
  public IconData getSymbol()
  {
    return (symbol);
  }
  /**
   * Method to get the Volatility of a Stock
   * @return com.hgutil.datarenderer.Fraction
   */
  public ColoredData getVolatility()
  {
    ColoredData rc = volatility;
    if (volatility == null)
    {
      double week52Low = get52WeekLow().doubleValue();
      double week52High = get52WeekHigh().doubleValue();

      //      double historicVolatility = (week52High - week52Low) / ((week52High + week52Low) / 2);
      double historicVolatility = (week52High - week52Low) / ((week52Low > 0.0) ? week52Low : 0.00000001);

      ColoredData tempRc = new ColoredData(new FractionPercent(historicVolatility));
      tempRc.setBGColor(new java.awt.Color(100, 100, 0)); // Dark Yellow
      tempRc.setFGColor(new java.awt.Color(255, 255, 0)); // Bright Yellow

      // Assign The Temp Value to rc, and rc to will return it.
      rc = tempRc;

    }
    return rc;
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
   * Method to re-init Data Trend Ticker Cell Count default is 10 cells
   */
  public void initDataTrendTicker()
  {
    initDataTrendTicker(10);
  }
  /**
   * Method to re-init Data Trend Ticker Cell Count
   * @param cellCount int
   */
  public void initDataTrendTicker(int cellCount)
  {
    trendTicker = new DataTrendTick(cellCount);
  }
  /**
   * Method to initialize Image Icons
   */
  private void initIcons()
  {
    if (ICON_UP == null)
    {
      ICON_UP = createImageIcon("stocktable/ArrUp.gif", "Arrow Up");
    }
    if (ICON_DOWN == null)
    {
      ICON_DOWN = createImageIcon("stocktable/ArrDown.gif", "Arrow Down");
    }
    if (ICON_BLANK == null)
    {
      ICON_BLANK = createImageIcon("stocktable/blank.gif", "No Change");
    }
    
    ICONS_SET = true;

  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param high double
   */
  public void set52WeekHigh(double high)
  {
    if (this.week52High == null)
    {
      this.week52High = new Fraction(high);
    }
    else
    {
      this.week52High.setValue(high);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param high String
   */
  public void set52WeekHigh(String high)
  {
    double tmpHigh = ParseData.parseNum(high, 0.0);
    set52WeekHigh(tmpHigh);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param low double
   */
  public void set52WeekLow(double low)
  {
    if (this.week52Low == null)
    {
      this.week52Low = new Fraction(low);
    }
    else
    {
      this.week52Low.setValue(low);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param low String
   */
  public void set52WeekLow(String low)
  {
    double tmpLow = ParseData.parseNum(low, 0.0);
    set52WeekLow(tmpLow);
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
   * @param changePr double
   */
  public void setChangePr(double changePr)
  {
    if (this.changePr == null)
    {
      this.changePr = new ColoredData(new FractionPercent(changePr));
    }
    else
    {
      this.changePr.setData(new FractionPercent(changePr));
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param changePr String
   */
  public void setChangePr(String changePr)
  {
    String value = "";
    // Strip everything but the value, and valid punctuation
    for (int i = 0; i < changePr.length(); i++)
    {
      char ch = changePr.charAt(i);
      if ((Character.isDigit(ch)) || (ch == '-') || (ch == '.'))
      {
        value += ch;
      }
    }

    double tmpChangePr = ParseData.parseNum(value, 0.0);
    setChangePr(tmpChangePr);
  }
  /**
   * Method to re-init Data Trend Ticker Cell Count
   * @param cellCount int
   */
  public void setDataTrendTicker(int tick)
  {
    if (trendTicker == null)
    {
      initDataTrendTicker();
    }

    trendTicker.addTick(tick);

  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * This method also sets the Trend Ticker for the Last value
   * @param last double
   */
  public void setLast(double last)
  {
    double d1 = 0.0;
    double d2 = last;
    if (this.last == null)
    {
      this.last = new Fraction(last);
    }
    else
    {
      d1 = this.last.doubleValue();
      int result = d1 > d2 ? -1 : (d1 < d2 ? 1 : 0);
      setDataTrendTicker(result);
      this.last.setValue(last);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param last String
   */
  public void setLast(String last)
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
    setLast(tmpLast);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param name String
   */
  public void setName(String name)
  {
    if (name == null)
    {
      this.name = "";
    }
    else
    {
      this.name = "" + name;
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param open double
   */
  public void setOpen(double open)
  {
    if (this.open == null)
    {
      this.open = new Fraction(open);
    }
    else
    {
      this.open.setValue(open);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param open String
   */
  public void setOpen(String open)
  {
    double tmpOpen = ParseData.parseNum(open, 0.0);
    setOpen(tmpOpen);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param newPreviousClose double
   */
  public void setPreviousClose(double newPreviousClose)
  {
    if (this.previousClose == null)
    {
      this.previousClose = new Fraction(newPreviousClose);
    }
    else
    {
      this.previousClose.setValue(newPreviousClose);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param newPreviousClose double
   */
  public void setPreviousClose(String newPreviousClose)
  {
    double tmpPrevClose = ParseData.parseNum(newPreviousClose, 0.0);
    setPreviousClose(tmpPrevClose);
  }
  /**
   * Sets the data builds a Stock Object
   * @param symbol String
   * @param name   String
   * @param bid    double
   * @param ask    double
   * @param last   double
   * @param open   double
   * @param change double
   * @param changePr double
   * @param volume long
   * @param week52Low double
   * @param week52High double
   */
  public void setStockData(
    String symbol,
    String name,
    double bid,
    double ask,
    double last,
    double open,
    double change,
    double changePr,
    long volume,
    double week52Low,
    double week52High)
  {
    setSymbol(symbol, change);
    setName(name);
    setBid(bid);
    setAsk(ask);
    setLast(last);
    setOpen(open);
    setChangePr(changePr);
    setVolume(volume);
    set52WeekLow(week52Low);
    set52WeekHigh(week52High);
    setPreviousClose(0.0);

  }
  /**
   * Sets the data builds a Stock Object
   * @param symbol String
   * @param name   String
   * @param bid    double
   * @param ask    double
   * @param last   double
   * @param open   double
   * @param change double
   * @param changePr double
   * @param volume long
   * @param week52Low double
   * @param week52High double
   * @param prevClose double
   */
  public void setStockData(
    String symbol,
    String name,
    double bid,
    double ask,
    double last,
    double open,
    double change,
    double changePr,
    long volume,
    double week52Low,
    double week52High,
    double prevClose)
  {
    setSymbol(symbol, change);
    setName(name);
    setBid(bid);
    setAsk(ask);
    setLast(last);
    setOpen(open);
    setChangePr(changePr);
    setVolume(volume);
    set52WeekLow(week52Low);
    set52WeekHigh(week52High);
    setPreviousClose(prevClose);

  }
  /**
   * Sets the data builds a Stock Object
   * @param symbol String
   * @param name   String
   * @param bid    String
   * @param ask    String
   * @param last   String
   * @param open   String
   * @param change String
   * @param changePr String
   * @param volume String
   * @param week52Low String
   * @param week52High String
   */
  public void setStockData(
    String symbol,
    String name,
    String bid,
    String ask,
    String last,
    String open,
    String change,
    String changePr,
    String volume,
    String week52Low,
    String week52High)
  {
    setSymbol(symbol, change);
    setName(name);
    setBid(bid);
    setAsk(ask);
    setLast(last);
    setOpen(open);
    setChangePr(changePr);
    setVolume(volume);
    set52WeekLow(week52Low);
    set52WeekHigh(week52High);
    setPreviousClose("N/A");
  }
  /**
   * Sets the data builds a Stock Object
   * @param symbol String
   * @param name   String
   * @param bid    String
   * @param ask    String
   * @param last   String
   * @param open   String
   * @param change String
   * @param changePr String
   * @param volume String
   * @param week52Low String
   * @param week52High String
   * @param prevClose String
   */
  public void setStockData(
    String symbol,
    String name,
    String bid,
    String ask,
    String last,
    String open,
    String change,
    String changePr,
    String volume,
    String week52Low,
    String week52High,
    String prevClose)
  {
    setSymbol(symbol, change);
    setName(name);
    setBid(bid);
    setAsk(ask);
    setLast(last);
    setOpen(open);
    setChangePr(changePr);
    setVolume(volume);
    set52WeekLow(week52Low);
    set52WeekHigh(week52High);
    setPreviousClose(prevClose);

  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param symbol String
   */
  public void setSymbol(String symbol)
  {
    setSymbol(symbol, 0.0);
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param symbol String
   * @param change double
   */
  public void setSymbol(String symbol, double change)
  {
    setChange(change);
    ImageIcon ii = getIcon(change);
    if (this.symbol == null)
    {
      this.symbol = new IconData(getIcon(change), symbol);
    }
    else
    {
      this.symbol.setData(getIcon(change), symbol);
    }
  }
  /**
   * Sets the data member to the specified value
   * if the Data object does not exist at time of method it will
   * create a new one, others it overwrites the currect set of data
   * @param symbol String
   * @param change String
   */
  public void setSymbol(String symbol, String change)
  {
    double tmpChange = ParseData.parseNum(change, 0.0);
    setSymbol(symbol, tmpChange);
  }
  /**
   * Method to set the Volatility of a Stock
   * @param newVolatility double
   */
  public void setVolatility(double newVolatility)
  {
    // This method ensures the field is null if we do not have any volatility
    if ((this.volatility == null) || (newVolatility > 0.99))
    {
      this.volatility = new ColoredData(new FractionPercent(newVolatility));
      this.volatility.setBGColor(new java.awt.Color(100, 100, 0)); // Dark Yellow
      this.volatility.setFGColor(new java.awt.Color(255, 255, 0)); // Bright Yellow
    }
    else
    {
      this.volatility.setData(new FractionPercent(newVolatility));
      this.volatility.setBGColor(null); // No Color Preferences
      this.volatility.setFGColor(null); // No Color Preferences
    }
  }
  /**
   * Method to set the Volatility of a Stock
   * @param newVolatility String
   */
  public void setVolatility(String newVolatility)
  {
    double tmpVolatility = ParseData.parseNum(newVolatility, 0.0);
    if ((newVolatility == null) || newVolatility.equals(""))
    {
      volatility = null;
    }
    else
    {
      setVolatility(tmpVolatility);
    }
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
    if (volume == -9999)
    {
      if ( getSymbol().getDataSz().indexOf('^') < 0 ) //  A Non Index Equity
      { // Index at times do not report Volume
        setName("Error data on " + getSymbol().getDataSz());
      }
    }
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
   * Converts a representation of the datamamebers into a single String
   * @return String
   */
  public String toString()
  {
    String rc = "";

    rc += symbol.getDataSz() + ", ";
    rc += name + ", ";
    rc += bid.doubleValue() + ", ";
    rc += ask.doubleValue() + ", ";
    rc += last.doubleValue() + ", ";
    rc += open.doubleValue() + ", ";
    rc += change.toString() + ", ";
    rc += changePr.toString() + ", ";
    rc += volume.longValue() + ", ";
    rc += week52Low.doubleValue() + ", ";
    rc += week52High.doubleValue();

    return rc;
  }
}
