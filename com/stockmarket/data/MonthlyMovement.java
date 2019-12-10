// MonthlyMovement.java
package com.stockmarket.data;

import java.io.Serializable;
import java.util.*;
/**
 * Utility Data Class That holds a Monthly high, Monthly Low
 * 
 * @author: Hans-Jurgen Greiner
 */
public class MonthlyMovement implements Comparable, Serializable
{
  protected GregorianCalendar calendar = null;
  private double high = 0.0;
  private double low = 0.0;

  /**
   * MonthlyMovement constructor comment.
   */
  public MonthlyMovement()
  {
    super();
  }
  /**
   * Comparison compare to method
   * @param obj Object
   * @return int
   */
  public int compareTo(Object obj)
  {
    int rc = -1;

    if (obj instanceof MonthlyMovement)
    {
      MonthlyMovement mm = (MonthlyMovement) obj;

      Date date1 = (Date) this.calendar.getTime();
      Date date2 = (Date) mm.getCalendar().getTime();
      rc = date1.compareTo(date2);
    }
    //    rc = date.getTime().compareTo(tmpDate);
    return (rc);
  }
  /**
   * Method to get the Month Movement
   * @return double The difference in the High and Low
   */
  public double doubleValue()
  {

    double diff = (this.high - this.low);
    return (diff);
  }
  /**
   * Method to comparte the equality of the Monthly Movement Object
   * @param obj A Object to be compared for equality
   * @return boolean True if Equal, False if not Equal
   */
  public boolean equals(Object obj)
  {
    boolean Rc = false;

    if (obj instanceof MonthlyMovement)
    {
      MonthlyMovement b = (MonthlyMovement) obj;

      int thisYear = this.calendar.get(Calendar.YEAR);
      int thatYear = b.calendar.get(Calendar.YEAR);
      int thisMonth = this.calendar.get(Calendar.MONTH);
      int thatMonth = b.calendar.get(Calendar.MONTH);
      Rc = ((thisYear == thatYear) && (thisMonth == thatMonth));
    }
    return (Rc);
  }
  /**
   * Method to get the Calendar of the object
   * @return calendar the Calendar year and month of the Object
   */
  public GregorianCalendar getCalendar()
  {

    return (this.calendar);
  }
  /**
   * Method to get the High Value if known.
   * @return double The highValue
   */
  public double getHigh()
  {

    return (this.high);
  }
  /**
   * Method to get the Low Value if known.
   * @return double The Low Value
   */
  public double getLow()
  {

    return (this.low);
  }
  /**
   * Method to get the Month Movement
   * @return double The difference in the High and Low
   */
  public double getMovement()
  {

    double diff = (this.high - this.low);
    return (diff);
  }
  /**
   * Method to set the Calendar of the object
   * @param calendarObj the Calendar year and month of the Object
   */
  public void setCalendar(GregorianCalendar calendarObj)
  {

    if (calendarObj != null)
    {
      this.calendar = new GregorianCalendar(calendarObj.get(Calendar.YEAR), calendarObj.get(Calendar.MONTH), 1, 0, 1, 0);
    }
    else
    { // The Calendar was null so we will invent one
      this.calendar = new GregorianCalendar(1900, 00, 01, 0, 1, 0);
    }
  }
  /**
   * Method to set the High Value if known.
   * NOTE, Values will bstored as 0.0 if they are negative
   * if there is a question of which aof two dates are higher
   * @see setHigh( double high1, double high2 )
   * @param highValue the Value of the Highest value
   */
  public void setHigh(double highValue)
  {

    this.high = (highValue >= 0.0) ? highValue : 0.0;
  }
  /**
   * Method to to set the High Value of two values, It will select that shich is larger
   * NOTE, Values will bstored as 0.0 if they are negative
   * @param high1 the Value of the Highest value 1
   * @param high2 the Value of the Highest value 2
   */
  public void setHigh(double high1, double high2)
  {

    setHigh(Math.max(high1, high2));
  }
  /**
   * Method to set the Low Value if known.
   * NOTE, Values will bstored as 0.0 if they are negative
   * if there is a question of which of two dates are lower
   * @see setLow( double low1, double low2 )
   * @param lowValue the Value of the lowest value
   */
  public void setLow(double lowValue)
  {

    this.low = (lowValue >= 0.0) ? lowValue : 0.0;
  }
  /**
   * Method to to set the Low Value of two values, It will select that shich is smaller
   * NOTE, Values will bstored as 0.0 if they are negative
   * @param low1 the Value of the lowest value 1
   * @param low2 the Value of the lowest value 2
   */
  public void setLow(double low1, double low2)
  {

    setLow(Math.min(low1, low2));
  }
}
