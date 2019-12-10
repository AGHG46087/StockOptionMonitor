// OptionDataVerticalComparator.java
package com.stockmarket.data;

import com.hgutil.data.*;

/**
 * Class is used by the OptionDataTableModel to provide a generic Sort
 * mechanism for the OptionData Class
 * @author: Hans-Jurgen Greiner
 */
public class OptionDataVerticalComparator implements java.util.Comparator
{
  /**
   * OptionDataVerticalComparator constructor comment.
   */
  public OptionDataVerticalComparator()
  {
    super();
  }
  /**
   * Compares the Option Data compared to as Calls or Options
   * @param o1 Object
   * @param o2 Object
   * @return int
   */
  public int compare(Object o1, Object o2)
  {
    if (!(o1 instanceof OptionData) || !(o2 instanceof OptionData))
    {
      return 0;
    }
    OptionData s1 = (OptionData) o1;
    OptionData s2 = (OptionData) o2;
    int result = 0;
    double d1, d2;
    String str1 = (String) s1.getSymbol();
    String str2 = (String) s2.getSymbol();

    if (StockMarketUtils.isCallOption(str1) && StockMarketUtils.isCallOption(str2))
    {
      d1 = s1.getStrikePrice().doubleValue();
      d2 = s2.getStrikePrice().doubleValue();
      result = d1 < d2 ? 1 : (d1 > d2 ? -1 : 0);
    }
    else if (StockMarketUtils.isPutOption(str1) && StockMarketUtils.isPutOption(str2))
    {
      d1 = s1.getStrikePrice().doubleValue();
      d2 = s2.getStrikePrice().doubleValue();
      result = d1 < d2 ? 1 : (d1 > d2 ? -1 : 0);
    }
    return result;
  }
  /**
   * equals
   * @param obj Object
   * @return boolean
   */
  public boolean equals(Object obj)
  {
    return false;
  }
}
