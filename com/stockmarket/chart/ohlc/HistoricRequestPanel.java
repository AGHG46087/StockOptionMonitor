// HistoricRequestPanel.java
package com.stockmarket.chart.ohlc;

import com.stockmarket.data.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.hgutil.*;
import com.hgutil.data.*;
import com.hgutil.swing.*;
import com.stockmarket.parsers.*;
import com.jrefinery.chart.*;
/**
 * Insert the type's description here.
 * @author Hans-Jurgen Greiner
 */
class HistoricRequestPanel extends JPanel implements StockMarketTypes
{

  private ItemTrigger itemTrigger = new ItemTrigger();
  ////////////////////////////////////////////////////////////
  // Inner Class ItemTrigger
  ////////////////////////////////////////////////////////////
  class ItemTrigger implements ItemListener
  {

    public void itemStateChanged(ItemEvent evt)
    {
      if (evt.getSource() instanceof JComboBox)
      {
        JComboBox src = (JComboBox) evt.getSource();
        if (src == startMonthComboBox)
        {
          int month = getSelectedStartMonth();
        }
        if (src == endMonthComboBox)
        {
          int month = getSelectedEndMonth();
        }
        if (src == startMonthComboBox)
        {
          int value = getSelectedFrequency();
        }
      }
    }
  }

  private JFreeChart chart = null;
  private ButtonGroup group = null;
  private JLabel lblStart = new JLabel("Start:");
  private JLabel lblEnd = new JLabel("End:");
  private JLabel lblMonth = new JLabel("Month");
  private JLabel lblDay = new JLabel("Day");
  private JLabel lblYear = new JLabel("Year");
  private IntegerTextField txtStartDay = new IntegerTextField(1, 2, 2);
  private IntegerTextField txtEndDay = new IntegerTextField(1, 2, 2);
  private IntegerTextField txtStartYear = new IntegerTextField(2001, 4, 4);
  private IntegerTextField txtEndYear = new IntegerTextField(2001, 4, 4);
  private JComboBox startMonthComboBox = createMonthNameComboBox();
  private JComboBox endMonthComboBox = createMonthNameComboBox();

  private JComponent[] objArray =
    {
      lblStart,
      lblEnd,
      lblMonth,
      lblDay,
      lblYear,
      txtStartDay,
      txtEndDay,
      txtStartYear,
      txtEndYear,
      startMonthComboBox,
      endMonthComboBox };

  /**
   * HistoricRequestPanel constructor comment.
   */
  public HistoricRequestPanel(JFreeChart theChart)
  {
    super(new BorderLayout());

    chart = theChart;
    if (theChart == null)
    {
      throw new NullPointerException("The JFreeChart Object cannot be null");
    }
    // This is the main Stream Panel that will be located CENTER of this
    JPanel mainPanel = new JPanel(new FlowLayout());

    JPanel p1 = new JPanel(new GridLayout(3, 1));
    p1.add(new JLabel(""));
    p1.add(lblStart);
    p1.add(lblEnd);
    p1.setOpaque(false);

    JPanel p2 = new JPanel(new GridLayout(3, 1));
    p2.add(lblMonth);
    p2.add(startMonthComboBox);
    p2.add(endMonthComboBox);
    p2.setOpaque(false);

    JPanel p3 = new JPanel(new GridLayout(3, 1));
    p3.add(lblDay);
    p3.add(txtStartDay);
    p3.add(txtEndDay);
    p3.setOpaque(false);

    JPanel p4 = new JPanel(new GridLayout(3, 1));
    p4.add(lblYear);
    p4.add(txtStartYear);
    p4.add(txtEndYear);
    p4.setOpaque(false);

    JPanel tmpPanel = createFrequencyPanel();
    tmpPanel.setOpaque(false);
    JPanel p5 = new JPanel(new BorderLayout());
    p5.add(tmpPanel, BorderLayout.CENTER);
    p5.setOpaque(false);

    mainPanel.add(p1);
    mainPanel.add(p2);
    mainPanel.add(p3);
    mainPanel.add(p4);
    mainPanel.add(p5);
    mainPanel.setOpaque(false);
    mainPanel.setBorder(BorderFactory.createEtchedBorder());

    // Reset the size of all presentable objects to 
    setPreferredSize();
    initDateRanges();

    this.setBackground(Color.white);

    this.add(mainPanel, BorderLayout.CENTER);
  }
  /**
   * Insert the method's description here.
   * @return javax.swing.JComboBox
   */
  public JPanel createFrequencyPanel()
  {

    JPanel buttonPanel = new JPanel(new ColumnLayout());
    group = new ButtonGroup();

    for (int i = 0; i < FREQNAMESARR.length; i++)
    {
      String name = FREQNAMESARR[i].name;
      boolean selected = (i == 0) ? true : false;
      JRadioButton btn = new JRadioButton(name, selected);
      btn.setOpaque(false);
      btn.setActionCommand(name);
      buttonPanel.add(btn);
      group.add(btn);
    }

    buttonPanel.setBorder(BorderFactory.createEtchedBorder());

    return buttonPanel;
  }
  /**
   * Insert the method's description here.
   * @return javax.swing.JComboBox
   */
  public JComboBox createMonthNameComboBox()
  {

    JComboBox comboBox = new JComboBox();
    comboBox.setVisible(true);
    int initial = 0;
    String[] itemsList = new String[MONTHSARR.length];
    for (int i = 0; i < MONTHSARR.length; i++)
    {
      itemsList[i] = new String(MONTHSARR[i].name);
      comboBox.addItem(itemsList[i]);
    }
    comboBox.addItemListener(itemTrigger);

    comboBox.setOpaque(false);

    comboBox.setToolTipText("Select a Month");

    return comboBox;
  }
  /**
   * Insert the method's description here.
   */
  public StocksHighLowDataset getDataSet()
  {

    StocksHighLowDataset ds = (StocksHighLowDataset) chart.getDataset();

    return ds;
  }
  /**
   * Method to return the Container object of all the Historical data
   */
  public HistoricStockDataContainer getHistoricStockContainer()
  {

    HistoricStockDataContainer container = getDataSet().getHistoricListContainer();

    return container;
  }
  /**
   * Insert the method's description here.
   * @return java.lang.String
   */
  public String getSelectedEndDay()
  {
    String value = txtEndDay.getText();
    return value;
  }
  /**
   * Insert the method's description here.
   * @return int
   */
  public int getSelectedEndMonth()
  {
    int rc = endMonthComboBox.getSelectedIndex();
    return (rc);
  }
  /**
   * Insert the method's description here.
   * @return java.lang.String
   */
  public String getSelectedEndYear()
  {
    String value = txtEndYear.getText();
    return value;
  }
  /**
   * Insert the method's description here.
   * @return int
   */
  public int getSelectedFrequency()
  {
    String cmd = group.getSelection().getActionCommand();
    int rc = 0;
    for (int i = 0; i < FREQNAMESARR.length; i++)
    {
      if (cmd.equals(FREQNAMESARR[i].name))
      {
        rc = FREQNAMESARR[i].value;
      }
    }

    return (rc);
  }
  /**
   * Insert the method's description here.
   * @return java.lang.String
   */
  public String getSelectedStartDay()
  {
    String value = txtStartDay.getText();
    return value;
  }
  /**
   * Insert the method's description here.
   * @return int
   */
  public int getSelectedStartMonth()
  {
    int rc = startMonthComboBox.getSelectedIndex();
    return (rc);
  }
  /**
   * Insert the method's description here.
   * @return java.lang.String
   */
  public String getSelectedStartYear()
  {
    String value = txtStartYear.getText();
    return value;
  }
  /**
   * Method to return the Stock symbol.
   */
  public String getStockMarketSymbol()
  {

    String symbol = getHistoricStockContainer().getStockSymbol();

    return symbol;
  }
  /**
   * Insert the method's description here.
   */
  public String getStockSymbol()
  {

    String theSymbol = getStockMarketSymbol();

    return theSymbol;
  }
  /**
   * Insert the method's description here.
   */
  private void initDateRanges()
  {

    HGCalendar now = new HGCalendar();
    HGCalendar from = new HGCalendar();

    from.add(Calendar.MONTH, -4);

    startMonthComboBox.setSelectedIndex(from.get(Calendar.MONTH));
    txtStartDay.setText(ParseData.format(from.get(Calendar.DAY_OF_MONTH), "00"));
    txtStartYear.setText(ParseData.format(from.get(Calendar.YEAR), "00"));

    endMonthComboBox.setSelectedIndex(now.get(Calendar.MONTH));
    txtEndDay.setText(ParseData.format(now.get(Calendar.DAY_OF_MONTH), "00"));
    txtEndYear.setText(ParseData.format(now.get(Calendar.YEAR), "00"));
  }
  /**
   * Insert the method's description here.
   */
  public void refreshChart()
  {

    String startMonth = ParseData.format(getSelectedStartMonth() + 1, "00");
    String startDay = "" + getSelectedStartDay();
    String startYear = "" + getSelectedStartYear();

    String endMonth = ParseData.format(getSelectedEndMonth() + 1, "00");
    String endDay = "" + getSelectedEndDay();
    String endYear = "" + getSelectedEndYear();

    String startDate = startDay + "-" + startMonth + "-" + startYear;
    String endDate = endDay + "-" + endMonth + "-" + endYear;

    int x = getSelectedFrequency();
    String frequency = FREQLETTERSARR[x].name;

    refreshChart(startDate, endDate, frequency);

  }
  /**
   * Insert the method's description here.
   * @param param java.util.Vector
   */
  public void refreshChart(String startDate, String endDate, String frequency)
  {

    StockDataHistCSVParser parser = new StockDataHistCSVParser();

    // StockMarketSymbols symbol, String fromDate,
    // String toDate, String FREQUENCY
    Vector list = parser.getHistoricDataBetweenDates(getStockMarketSymbol(), startDate, endDate, frequency);

    refreshChart(list);

  }
  /**
   * Insert the method's description here.
   * @param param java.util.Vector
   */
  public void refreshChart(Vector list)
  {

    HistoricStockDataContainer container = new HistoricStockDataContainer(list);
    container.setStockSymbol(getStockMarketSymbol());
    StocksHighLowDataset data = new StocksHighLowDataset(container);
    chart.setDataset(data);

  }
  /**
   * Insert the method's description here.
   */
  private void setPreferredSize()
  {
    int height = 23;
    int width = 0;

    // first determine , the largest height, so everything is equal
    for (int i = 0; i < objArray.length; i++)
    {
      int tmpHeight = objArray[i].getPreferredSize().height;
      height = (height >= tmpHeight) ? height : tmpHeight;
    }

    // The resize all of them maintaining the original width
    for (int i = 0; i < objArray.length; i++)
    {
      width = objArray[i].getPreferredSize().width;
      objArray[i].setPreferredSize(new Dimension(width, height));
    }

  }
  /**
   * Insert the method's description here.
   */
  public static void showHistoricRequestDialog(JFrame theFrame, JFreeChart theChart)
  {

    final HistoricRequestPanel controlPanel; // Blank Final
    final JDialog customDialog = new JDialog(theFrame, "Historic Data Request", false);

    try
    {
      controlPanel = new HistoricRequestPanel(theChart);
      customDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      customDialog.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent we)
        {
          // Do not allow close operation on
          // Clicking the X Button
          System.out.println("Click \"OK\" or click \"Cancel\".");
        }
      });
      // The only way to close this dialog is by
      // pressing one of the following buttons.
      // Do you understand?
      final JOptionPane optionPane =
        new JOptionPane(controlPanel, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

      optionPane.addPropertyChangeListener(new java.beans.PropertyChangeListener()
      {
        public void propertyChange(java.beans.PropertyChangeEvent e)
        {
          String prop = e.getPropertyName();
          if (customDialog.isVisible()
            && (e.getSource() == optionPane)
            && (prop.equals(JOptionPane.VALUE_PROPERTY) || prop.equals(JOptionPane.INPUT_VALUE_PROPERTY)))
          {
            //If you were going to check something
            //before closing the window, you'd do
            //it here.
            customDialog.setVisible(false);
            int value = ((Integer) optionPane.getValue()).intValue();
            if (value == JOptionPane.OK_OPTION)
            {
              controlPanel.refreshChart();
            }
            //else if (value == JOptionPane.CANCEL_OPTION) 
            //{
            //// Do Nothing
            //}
            return;
          }
        }
      });
      customDialog.setModal(true);
      customDialog.setContentPane(optionPane);
      customDialog.pack();
      customDialog.setLocationRelativeTo(theFrame);
      customDialog.setVisible(true);

    }
    catch (RuntimeException exc)
    {}

  }
}
