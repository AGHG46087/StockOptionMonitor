// OptionDataTableModule.java
package com.stockmarket.app;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.hgtable.HGTable;
import com.hgtable.HGTableModel;
import com.hgutil.AppProperties;
import com.hgutil.HGCalendar;
import com.hgutil.ParseData;
import com.hgutil.data.StockMarketTypes;
import com.hgutil.swing.GriddedPanel;
import com.stockmarket.data.HistoricStockDataContainer;
import com.stockmarket.data.OptionData;
import com.stockmarket.data.OptionDataVerticalTableModel;
import com.stockmarket.data.StockData;
import com.stockmarket.data.StockMarketUtils;
import com.stockmarket.parsers.OptionDataParser;
import com.stockmarket.parsers.StockDataHistCSVParser;

/**
 * Insert the type's description here.
 * @author Hans-Jurgen Greiner
 */
public class OptionDataTableModule extends StockMarketBaseModule implements StockModuleDataExchange
{
  // Constants
  private static boolean MODULE_READY = false;
  public static final int ONE_SECOND = 1000; // MilliSeconds
  public static final int ONE_MINUTE = 60 * ONE_SECOND;
  public static final int ONE_HOUR = 60 * ONE_MINUTE;
  private final static long MONITOR_DELAY = ONE_MINUTE * 15;

  private AppProperties appProps = null;
  private int standardDelay = ONE_MINUTE;
  private int extendedDelay = ONE_HOUR;

  private JTextField stockName = null;
  private StockData stockData = null;

  private JTextField txtSymbol = null;
  private JTextField txtName = null;
  private JTextField txt52WeekLow = null;
  private JTextField txt52WeekHigh = null;
  private JTextField txtExpireDays = null;
  private JTextField txtLastPrice = null;
  private JTextField txtCountdown = null;
  private JComboBox lstMonthList = null;

  private ActionTrigger actionTrigger = new ActionTrigger();
  private OptionDataMonitorTask monitorTask = null;
  private Vector modelVector = null;
  private Vector tableVector = null;
  private Vector populateListVector = null;
  private JTabbedPane tabPane = null;
  private HistoricStockDataContainer historicData = null;

  /**
   * TabListener implementing ChangeListener
   * is designed to help with notification when a tab has changed on the TabbedPane,
   * In addition when a tab has been selected and is different than the 
   * previous tab - then it spawn new threads to load and refresh data
   */
  private class TabPaneListener implements ChangeListener
  {
    private int lastIndex = -1;

    /**
     * Method that is called when the JTabbedPane state changes.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
      // If the source is not the JTabbedPane, then return
      if (!(e.getSource() instanceof JTabbedPane))
      {
        return;
      }
      // Get the selected index.
      int index = OptionDataTableModule.this.tabPane.getSelectedIndex();
      debug("TabListener Called - State Changed - Tab Pane index is now [" + index + "]");

      // If we are the same index, then simply return, as we are already
      // processing for the this tab
      if (index == this.lastIndex)
      { // Return - As this is not changing
        return;
      }
      // Record our new index if it is at least 0
      this.lastIndex = index;
      if (OptionDataTableModule.this.MODULE_READY)
      {
        // Ok We are done with a state change, continue processing.
        OptionDataTableModule.this.startupOptionChainTimers(index);
      }
    }
  }
  /**
   * HistoricStockDataLoaderTask extends TimerTask
   * Used to load a specific StockData and refreshes on the table
   * If the Historic Data already exists then it will load from Disk cache
   * load from parser.
   * NOTE: Class will cacel itself after the update
   */
  private class HistoricStockDataLoaderTask extends TimerTask
  {
    private StockData sd = null;
    private String fileName = null;

    /**
     * Method HistoricStockDataLoaderTask. constructor
     * @param sd
     */
    public HistoricStockDataLoaderTask(StockData sd)
    {
      this.sd = sd;
    }
    /**
     * @see Runnable#run()
     */
    public void run()
    {
      debug("HistoricStockDataLoaderTask::run() - processing for stock " + sd.getSymbol().getDataSz());
      fileName = getString("OptionDataTableModule.historic_details_basic_name") + sd.getSymbol().getDataSz();
      if (DEMO_MODE)
      {
        debug("HistoricStockDataLoaderTask::run() - is considered to be refreshed - DEMO_MODE");
      }
      else
      {

        File theFile = new File(fileName);
        if (!theFile.exists())
        {
          // Instantiate a parser
          OptionDataTableModule.this.setStatusBar("Downloading Historic Details for " + sd.getSymbol().getDataSz() + ".");

          StockDataHistCSVParser parser = new StockDataHistCSVParser();
          // Create our date range of 1 year ago until today
          GregorianCalendar now = new GregorianCalendar();
          GregorianCalendar then = new GregorianCalendar();
          then.add(Calendar.YEAR, -1);
          Vector list =
            parser.getHistoricDataBetweenDates(sd.getSymbol().getDataSz(), then.getTime(), now.getTime(), StockMarketTypes.DAILY);

          OptionDataTableModule.this.historicData = new HistoricStockDataContainer(sd, list);
          StockMarketUtils.serializeData(historicData, this.fileName);
        }
        else
        {
          historicData = StockMarketUtils.getHistoricDataFromDisk(fileName);
        }
        // We should have the historic data now.  So fill it up.
        for (int i = 0; i < modelVector.size(); i++)
        {
          // Grab or table models in the list
          HGTableModel model = (HGTableModel) modelVector.elementAt(i);
          // Sometable models may require the knowlege of historic data 
          // send a message with the HistoricData
          if (model instanceof OptionDataVerticalTableModel)
          {
            ((OptionDataVerticalTableModel) model).setHistoricDataContainer(historicData);
          }
        }
      }
      // Cancel this job
      this.cancel();
      // Signal the Garbage Collector that we are ready to rumble
      System.gc();
      OptionDataTableModule.this.setStatusBar("Historic Details for " + sd.getSymbol().getDataSz() + " complete.");
    }
  }
  /**
   * Class Action Trigger is used to handle standard Button Events
   */
  private class ActionTrigger extends KeyAdapter implements ActionListener
  {

    /**
     * Imnplementation of the actionPerformed method
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent evt)
    {

      // Since we are here - Find out which Tab we are showing.
      // Then get the Appropriate Table and Table Model
      int index = tabPane.getSelectedIndex();
      String cmd = evt.getActionCommand();
      Object source = evt.getSource();
      if (source instanceof JComboBox)
      {
        JComboBox tempSrc = (JComboBox) source;
        if (tempSrc == OptionDataTableModule.this.lstMonthList)
        {
          // If our text field is null or the Text being returned is null or Empty
          // then simply return, otherwise do some work.
          if ((OptionDataTableModule.this.txtSymbol == null)
            || (OptionDataTableModule.this.txtSymbol.getText() == null)
            || ("".equals(OptionDataTableModule.this.txtSymbol.getText().trim())))
          {
            return;
          }
          // Get the Text and ship to the update process.  The user changed the
          // the requested Month.and assumed the same data.
          String symbol = OptionDataTableModule.this.txtSymbol.getText().toUpperCase();
          OptionDataTableModule.this.setDataObject(symbol);
        }
      }

    }
    /**
     * Key Pressed Event will monitor for Key presses
     * @see KeyListener#keyPressed(KeyEvent)
     */
    public void keyPressed(KeyEvent e)
    {
      int keyCode = e.getKeyCode();
      Object objSource = e.getSource();
      if (e.VK_ENTER == keyCode && objSource == OptionDataTableModule.this.txtSymbol)
      {
        String symbol = OptionDataTableModule.this.txtSymbol.getText().toUpperCase();
        OptionDataTableModule.this.setDataObject(symbol);
      }
    }
  }

  /**
   * OptionDataMonitorTask extends TimerTask
   * is designed to help with notification when a List may have stopped running,
   * The Class will restart the object
   */
  private class OptionDataMonitorTask extends TimerTask
  {
    // Calcualte a reasonable timer delay i.e 7/5 minutes
    private final long MAX_WAIT_TIME = (long) (OptionDataTableModule.this.MONITOR_DELAY / 2);
    private GregorianCalendar lastUpdate = null;

    /**
     * Method OptionDataMonitorTask., Constructor for the inner class.
     * @param watchListDataApp
     * @param tabIndex
     */
    public OptionDataMonitorTask()
    {
      debug("OptionDataMonitorTask() - Constructor ");
    }
    /**
     * Performs the action run method which inturn refresh the option chain list data
     * for the index.
     * @see TimerTask#run(ActionEvent)
     */
    public void run()
    {
      debug("OptionDataMonitorTask::run()");
      // Get the current time
      GregorianCalendar t1 = new GregorianCalendar();
      // Get the Displayed Tab index
      int index = OptionDataTableModule.this.tabPane.getSelectedIndex();
      // calculate the time difference from the last time
      long diffTime = t1.getTimeInMillis() - lastUpdate.getTimeInMillis();
      // Is the panel even displayed???
      boolean moduleDisplayed = OptionDataTableModule.this.getModulePanel().isShowing();

      if ((diffTime >= MAX_WAIT_TIME) && (moduleDisplayed))
      {
        System.out.println("OptionDataMonitorTask::run() - Fired a startupOptionChainTimers on Index" + index);
        OptionDataTableModule.this.startupOptionChainTimers(index);
      }
      // Just for grins, lets make sure every once and a while we ensure
      // are columsn have a minimum size.
      for (int i = 0; i < tableVector.size(); i++)
      {
        HGTable table = (HGTable) tableVector.elementAt(index);
        table.setMinWidthToText(true);
      }
    }
    /**
     * Method setLastUpdate. Method to support recording of the last update
     * @param index
     * @param lastUpdate
     */
    public void notifyMonitorOnUpdate(int index)
    {
      int selectedIndex = OptionDataTableModule.this.tabPane.getSelectedIndex();
      GregorianCalendar t1 = new GregorianCalendar();
      if (index == selectedIndex)
      {
        this.lastUpdate = t1;
      }
      if ((OptionDataTableModule.this.tabPane == null) || (index < OptionDataTableModule.this.tabPane.getTabCount()))
      {
        return;
      }
      debug(
        "OptionDataMonitorTask::notifyMonitorOnUpdate() - Table "
          + index
          + " - Time is "
          + ParseData.format(t1.getTime(), "hh:mm:ss"));

      OptionDataTableModule.this.setStatusBar(
        "Option Chain ["
          + OptionDataTableModule.this.tabPane.getTitleAt(index)
          + "] - Last Update "
          + ParseData.format(t1.getTime(), "hh:mm:ss"));
    }
  }
  /**
   * PopulateOptionDataTask Timer Task. This is intended to run on the
   * <B>Util Event</B> thread so as not to muck things up by doing
   * gui work off the gui thread. Accepts a OptionDataTableModule and an int
   * This class performs a set of tasks as updating the current option data list 
   * being viewed. <B><I>NOTE if this Thread is invoked directly it will run on 
   * the Main GUI Thread.</I></B> An Example of the usage would be as follows:
   * <PRE>
   *   TimerTask updateOptionChain = new PopulateOptionDataTask( OptionDataMonitorTask, 1 );
   *   timer new Timer( true );  // Deamon
   *   timer.schedule( updateOptionChain, 1000 );
   * </PRE>
   */
  private class PopulateOptionDataTask extends TimerTask
  {
    private int index = -1;
    private OptionDataMonitorTask monitorTask = null;
    private OptionDataParser optionDataParser = null;

    /**
     * Method PopulateOptionDataTask., Constructor for the inner class.
     * @param monitorTask
     * @param tabIndex
     */
    public PopulateOptionDataTask(OptionDataMonitorTask monitorTask, int tabIndex)
    { // The main application
      this.index = tabIndex; // Get the index of which list we are watching
      // Get A reference to the MonitorTask
      this.monitorTask = monitorTask;

      debug("PopulateOptionDataTask() - Constructor - Index is [" + this.index + "]");
    }
    /**
     * Performs the action run method which inturn refresh the option data list data
     * for the index.
     * @see TimerTask#run(ActionEvent)
     */
    public void run()
    {
      debug("PopulateOptionDataTask::run() - Index is [" + this.index + "]");

      this.refreshOptionData(this.index);
    }
    /**
     * Method to setup a Parser object and Collect current Stock Price
     * Changes and update the StockData Vector with the new information.
     * @param index int
     */
    private synchronized void refreshOptionData(int index)
    {
      debug("PopulateOptionDataTask::refreshOptionData(" + index + ") called - ");
      if (DEMO_MODE)
      {
        debug("PopulateOptionDataTask::refreshOptionData() - is considered to be refreshed - DEMO_MODE");
      }
      else
      {
        try
        {
          // Grab the appropriate TableModel
          HGTableModel model = (HGTableModel) modelVector.elementAt(index);

          debug("PopulateOptionDataTask::refreshOptionData(" + index + ") Single Threading of one StockData Vector");
          // Stock Data items
          if (optionDataParser == null)
          {
            optionDataParser = new OptionDataParser();
          }

          int ordValue = OptionDataTableModule.this.getOrdinalMonthValueFromComboBox();
          // Get the most recent set of data
          if (model instanceof OptionDataVerticalTableModel)
          {
            int months = 1;
            // Now get the new StockData and OptionData
            optionDataParser.getOptionData(stockData, model.getData(), months);
            // Fire the change of the OptionData within our Controller
            OptionData.setStockData(stockData);
            // Let the Model know what which month and to sort the data 
            // we currently have.
             ((OptionDataVerticalTableModel) model).setMonthToView(ordValue);
            ((OptionDataVerticalTableModel) model).sortVerticalOptionData();
            // update the textfields to reflect the current StockData.
            populateTextFields();
          }

          // Indicate to the table model that the Data has changed
          model.fireTableDataChanged();

        }
        catch (Exception exc)
        {
          // This is a safety precaution.  If we think that a tab is selected for which
          // This processing is ocurring, It is possible that a user, is attemting.to delete
          // the option list. If so, an exception would ocurr where an index into the vector
          // would cause an exception
          debug("PopulateOptionDataTask::refreshOptionData(" + index + ") - Exception caught: " + exc.getClass().getName());
          debug("PopulateOptionDataTask::refreshOptionData(" + index + ") - Exception message: " + exc);
        }

        debug("PopulateOptionDataTask::refreshOptionData(" + index + ") - Live Update Complete");
        // Notify the monitorTask Monitor that we have fired.
        // If our Monitor is still alive then update it.  
        // It will update the status bar as well
        if (monitorTask != null)
        {
          monitorTask.notifyMonitorOnUpdate(index);
        }
      }
      debug("PopulateOptionDataTask::refreshOptionData(" + index + ") - complete ");
    }

  }
  /**
   * SymbolLookupModule constructor comment.
   */
  public OptionDataTableModule(StockMarketApp stockMarketApp)
  {

    super(stockMarketApp, "OptionDataTableModule", "toolbar/OptionData.gif");
    getModulePanel().add(createPanel(), BorderLayout.CENTER);

  }
  /**
   * Method createOptionMonthComboBox. Creates a Combo box with the starting 
   * four months included in it
   * @return JComboBox
   */
  private JComboBox createOptionMonthComboBox()
  {
    GregorianCalendar startMonth = StockMarketUtils.getNextExpirationDate();
    JComboBox list = new JComboBox();

    for (int i = 0; i < 4; i++)
    {
      String name = ParseData.format(startMonth.getTime(), "MMM-yy");
      list.addItem(name);
      startMonth.add(Calendar.MONTH, 1);
    }
    list.addActionListener(actionTrigger);
    return list;
  }
  /**
   * Method createTopDataPanel. Method to Create the Top Data Panel
   * This contains various Stock Data and Expiration Days 
   * @return JPanel
   */
  private JPanel createTopDataPanel()
  {
    // Create a new GriddedPanel, This is a Helper Class to assist
    // in the dreaded GridBagLayout()
    GriddedPanel panel = new GriddedPanel();
    panel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
    // How we want to define fields    
    Dimension shorterField = new Dimension(40, 20);
    Dimension shortField = new Dimension(80, 20);
    Dimension mediumField = new Dimension(120, 20);
    Dimension longField = new Dimension(240, 20);
    Dimension hugeField = new Dimension(240, 80);

    // Spacing between labels and Fields
    EmptyBorder border = new EmptyBorder(new Insets(0, 0, 0, 10));
    EmptyBorder border1 = new EmptyBorder(new Insets(0, 20, 0, 10));

    int col = 1;

    JLabel lblMonths = new JLabel("Month");
    lblMonths.setBorder(border);
    panel.addComponent(lblMonths, 1, col); // Row 1, Col 1
    lstMonthList = createOptionMonthComboBox();
    lstMonthList.setPreferredSize(shortField);
    panel.addComponent(lstMonthList, 2, col, 1, 1);
    col++;

    JLabel lblSymbol = new JLabel("Symbol");
    lblSymbol.setBorder(border);
    panel.addComponent(lblSymbol, 1, col); // Row 1, col 2
    txtSymbol = new JTextField();
    txtSymbol.setPreferredSize(shorterField);
    txtSymbol.addKeyListener(actionTrigger);
    txtSymbol.setToolTipText(getString("OptionDataTableModule.text_symbol_tooltip"));
    txtSymbol.setRequestFocusEnabled(true);
    panel.addComponent(txtSymbol, 2, col, 1, 1); // Row 2, Col 2, RowSpan 1, ColSpan 1
    col++;

    JLabel lblDescription = new JLabel("Company Name");
    lblDescription.setBorder(border);
    panel.addComponent(lblDescription, 1, col); // Row 1, Col 3
    txtName = new JTextField();
    txtName.setPreferredSize(mediumField);
    txtName.setEditable(false);
    panel.addComponent(txtName, 2, col, 1, 1); // Row 2, Col 3, RowSpan 1, ColSpan 1
    col++;

    JLabel lblLast = new JLabel("Last");
    lblLast.setBorder(border);
    panel.addComponent(lblLast, 1, col); // Row 1, Col 4
    txtLastPrice = new JTextField();
    txtLastPrice.setPreferredSize(shorterField);
    txtLastPrice.setEditable(false);
    panel.addComponent(txtLastPrice, 2, col, 1, 1); // Row 2, Col 4, RowSpan 1, ColSpan 1
    col++;

    // Create a new GriddedPanel to Maintain our Expiration Days
    GriddedPanel panel2 = new GriddedPanel();
    panel2.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
    JLabel lblExpireDays = new JLabel("Days to Expiration");
    lblExpireDays.setBorder(border);
    panel2.addComponent(lblExpireDays, 1, 1); // Row 1, Col 1
    txtExpireDays = new JTextField();
    txtExpireDays.setPreferredSize(shorterField);
    txtExpireDays.setEditable(false);
    // Row 2, Col 1, RowSpan 1, ColSpan 1, 
    // NOTE:  We are not using the FillComponent, here as the days 
    //        is a rather short field.  
    panel2.addComponent(txtExpireDays, 2, 1, 1, 1);

    // Now for the finally - Create a normal Panel and add our StockData and
    // Expiration Panel to it. so it moves appropriatly in resize
    JPanel panel3 = new JPanel(new BorderLayout());
    panel3.add(panel, BorderLayout.WEST);
    panel3.add(panel2, BorderLayout.EAST);
    // Return out BorderLayoutPanel.
    return panel3;
  }
  /**
   * Method createBottomDataPanel. Create the Data Panel for the Bottom of the
   * display, this contains various micelaneous data, that may pose interest
   * to the user
   * @return JPanel
   */
  private JPanel createBottomDataPanel()
  {
    // Create a new GriddedPanel, This is a Helper Class to assist
    // in the dreaded GridBagLayout()
    GriddedPanel panel = new GriddedPanel();
    panel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
    // How we want to define fields    
    Dimension shortField = new Dimension(40, 20);
    Dimension mediumField = new Dimension(120, 20);
    Dimension longField = new Dimension(240, 20);
    Dimension hugeField = new Dimension(240, 80);

    // Spacing between labels and Fields
    EmptyBorder border = new EmptyBorder(new Insets(0, 0, 0, 10));
    EmptyBorder border1 = new EmptyBorder(new Insets(0, 20, 0, 10));

    JLabel lbl52WeekLow = new JLabel("52 Week Low");
    lbl52WeekLow.setBorder(border);
    panel.addComponent(lbl52WeekLow, 1, 4); // Row 1, Col 4
    txt52WeekLow = new JTextField();
    txt52WeekLow.setPreferredSize(shortField);
    txt52WeekLow.setEditable(false);
    // Row 2, Col 4, RowSpan 1, ColSpan 1, Fill Horzontally the entire CELL in the Grid
    panel.addFilledComponent(txt52WeekLow, 2, 4, 1, 1, GridBagConstraints.HORIZONTAL);

    JLabel lbl52WeekHigh = new JLabel("52 Week High");
    lbl52WeekHigh.setBorder(border);
    panel.addComponent(lbl52WeekHigh, 1, 5); // Row 1, Col 5
    txt52WeekHigh = new JTextField();
    txt52WeekHigh.setPreferredSize(shortField);
    txt52WeekHigh.setEditable(false);
    // Row 2, Col 5, RowSpan 1, ColSpan 1, Fill Horzontally the entire CELL in the Grid
    panel.addFilledComponent(txt52WeekHigh, 2, 5, 1, 1, GridBagConstraints.HORIZONTAL);

    // Now for the finally - Create a normal Panel and add our StockData and
    JPanel panel3 = new JPanel(new BorderLayout());
    panel3.add(panel, BorderLayout.EAST);
    // Return out BorderLayoutPanel.
    return panel3;
  }
  /**
   * Method createPanel. Creates the main panel for this Module
   * @return JPanel
   */
  public JPanel createPanel()
  {

    JPanel panel = new JPanel(new BorderLayout());
    JPanel topPanel = createTopDataPanel();
    JPanel bottomPanel = createBottomDataPanel();

    // Create an Application Properties instance
    appProps = new AppProperties(getString("OptionDataTableModule.application_ini_filename"));
    // Setup our Time Delays
    standardDelay = ONE_SECOND * ParseData.parseNum(getString("OptionDataTableModule.standard_time_delay"), ONE_MINUTE);
    extendedDelay = ONE_SECOND * ParseData.parseNum(getString("OptionDataTableModule.extended_time_delay"), ONE_HOUR);

    // Create our Basic Tabbed Pane and Listener ( inner class )
    JPanel tabPanePanel = createVerticalPanel(false);
    tabPane = new JTabbedPane();
    tabPane.addChangeListener(new TabPaneListener());
    tabPanePanel.add(tabPane);

    panel.add(topPanel, BorderLayout.NORTH);
    panel.add(tabPanePanel, BorderLayout.CENTER);
    panel.add(bottomPanel, BorderLayout.SOUTH);
    return panel;
  }
  /**
   * Method to watch over all Option Datq Lists elements to their associated Vectors.
   * This method will add a timer ( via the index ) the table and tableModel
   * the appropriate vector containers.
   * @param table A JTable The Display table used for graphical decisions
   * @param model The HGTableModel used for logic based decisions
   * @param index The index of the Tab being added, to fire Timers threads
   */
  private void addDataToVectorContainers(JTable table, HGTableModel model, int index)
  {

    debug(
      "addDataToVectorContainers() - Option Chain List("
        + index
        + ") will begin processing in ["
        + (int) (extendedDelay / ONE_SECOND)
        + "] seconds");

    // Now add the Table to the Vector that maintains a list of Tables
    if (tableVector == null)
    {
      tableVector = new Vector();
      tableVector.removeAllElements();
    }
    tableVector.add(table);

    // Now add the TableModel to the Vector that maintains a list of TableModels
    if (modelVector == null)
    {
      modelVector = new Vector();
      modelVector.removeAllElements();
    }
    modelVector.add(model);

  }

  /**
   * Method loadTabPanePanels. Loads The Tab Pane with all Tabs required.
   */
  private void loadTabPanePanels()
  {
    debug("loadTabPanePanels() called - ");

    final int strategyListCount =
      ParseData.parseNum(getString("OptionDataTableModule.application_option_strategy_count"), 0);

    tableVector.removeAllElements();
    modelVector.removeAllElements();
    tabPane.removeAll();

    for (int index = 0; index < strategyListCount; index++)
    {
      // Create a NEW header Vector
      Vector headers = new Vector();
      headers.removeAllElements();
      for (int i = 0; i < OptionData.columns.length; i++)
      {
        headers.addElement(OptionData.columns[i]);
      }

      // Create a new DATA Vector
      Vector data = new Vector();
      data.removeAllElements();

      // Create a new Table Model with our headers and data
      OptionDataVerticalTableModel tableModel = new OptionDataVerticalTableModel(headers, data);

      // Create a new Table with our Tablemodel
      JTable table = new HGTable(tableModel);

      // This method will add a timer ( via the index ) the table and tableModel
      // the appropriate vector containers.
      addDataToVectorContainers(table, tableModel, index);

      // Create a new JSCrollPane with the the Table
      JScrollPane scrollPane = new JScrollPane();
      scrollPane.getViewport().add(table);
      JPanel panel = new JPanel(new BorderLayout());

      // Create a new Vertical Panel to place our menu and JScrollPane Table
      JPanel lblPanel = createVerticalPanel(true);
      panel.add(scrollPane, BorderLayout.CENTER);

      String tabTitle = getString("OptionDataTableModule.application_option_strategy_" + index);
      tabPane.addTab(tabTitle, null, panel, new String("Click here to view " + tabTitle));

      // Set the selected index to the first in the list
      tabPane.setSelectedIndex(0);
      // Repaint the tabpane with the current data
      tabPane.repaint();

      this.setStatusBar("Loading Option Tab [" + index + "] complete.");
    }

    this.MODULE_READY = true;

    if (this.monitorTask != null)
    {
      this.monitorTask.cancel();
      this.monitorTask = null;
    }

    this.monitorTask = new OptionDataMonitorTask();
    Timer timer = new Timer(true); // Non-Deamon
    timer.scheduleAtFixedRate(monitorTask, MONITOR_DELAY, MONITOR_DELAY);

    // Startup our timers
    startupOptionChainTimers(0);

    debug("loadTabPanePanels() complete - ");

  }
  /**
   * Method refreshTabPanePanels. Re-Loads refreshes the paint on the tab during a recycle.
   */
  private void refreshTabPanePanels()
  {
    debug("refreshTabPanePanels() called - ");

    // Repaint the tabpane with the current data
    tabPane.repaint();

    this.MODULE_READY = true;

    if (this.monitorTask == null)
    {
      this.monitorTask = new OptionDataMonitorTask();
      Timer timer = new Timer(true); // Non-Deamon
      timer.scheduleAtFixedRate(monitorTask, MONITOR_DELAY, MONITOR_DELAY);
    }
    
    // Startup our timers
    int index = tabPane.getSelectedIndex();
    startupOptionChainTimers(index);

    debug("refreshTabPanePanels() complete - ");

  }
  /**
   * main method allows us to run as a standalone demo.
   */
  public static void main(String[] args)
  {
    OptionDataTableModule demo = new OptionDataTableModule(null);
    demo.mainImpl();
  }
  /**
   * Method to return a Object if it is a instance of the StockModuleDataExchange
   * @return java.lang.Object
   */
  public Object getDataObject()
  {
    return stockData;
  }
  /**
   * Method to set a Object if it is a instance of the StockModuleDataExchange
   * @param dataObj java.lang.Object
   */
  public void setDataObject(Object dataObj)
  {

    debug("setDataObject() - Set Data member Obj");
    // Null out any reference to this data that may be present
    if (dataObj != null && dataObj instanceof StockData)
    {
      setStockData((StockData) dataObj);
    }
    else if (dataObj != null && dataObj instanceof String)
    {
      // First Chack if we already have a stock Data. If we do...
      // simply use the one we have
      String symbol = (String) dataObj;
      StockData sd = null;
      if (this.stockData != null && this.stockData.getSymbol().equals(symbol))
      {
        sd = this.stockData;
      }
      else
      { // We do not have a match on the StockData, lets create anew one here
        sd = new StockData();
        sd.setSymbol(symbol);
      }
      // Now set the Stock stuff
      setStockData(sd);
    }

    debug("setDataObject() - stockData is [" + stockData.getName() + "]");
  }

  /**
   * Method setStockData.Sets the stock data for the Option Module
   * @param sd
   */
  private void setStockData(StockData sd)
  {
    /** LoadTabPaneData Thread - Inner class that will load the historic data on the AWT
     *  Thread as to not muck up things on the GUI thread
     *  The Thread will die after it loads everything.
     */
    class LoadTabPaneDataTask extends TimerTask
    {
      // Handle to main outer class
      OptionDataTableModule optionDataTableModule = null;
      boolean refresh = false;

      /** 
       * LoadTabPaneDataTask constructor
       */
      public LoadTabPaneDataTask(OptionDataTableModule optionDataTableModule, boolean refresh)
      {
        this.optionDataTableModule = optionDataTableModule;
        this.refresh = refresh;
      }

      /** 
       * LoadTabPaneDataTask::run() this method simply invokes. 
       * OptionDataTableModule::loadTabPanePanels() method.
       * The method will populate all option chain lists to be loaded on
       * the JTabbedPane then it dies off
       */
      public void run()
      {
        debug("LoadTabPaneDataTask::run() - calling optionDataTableModule.loadTabPanePanels()");

        if (optionDataTableModule.tableVector == null)
        {
          optionDataTableModule.tableVector = new Vector();
          optionDataTableModule.tableVector.removeAllElements();
        }
        if (optionDataTableModule.modelVector == null)
        {
          optionDataTableModule.modelVector = new Vector();
          optionDataTableModule.modelVector.removeAllElements();
        }
        // If we are signaled to refresh the data then do so
        if ( this.refresh )
        {
          optionDataTableModule.refreshTabPanePanels();
        }
        else
        { // Otherwise we are re-loading
          optionDataTableModule.loadTabPanePanels();
        }
        this.cancel();
        System.gc();
      }
    }

    // Before We go any further, Ensure we shutdown any other Tasks running.
    // We want to amke a clean getaway and start with our new StockData
    this.shutdownOptionChainTimers();

    // Set our StockData
    final boolean refresh = ( stockData == sd );
    stockData = null;
    stockData = sd;
    if (stockData != null)
    {
      this.txtSymbol.setText(stockData.getSymbol().getDataSz());
      this.txtName.setText("Searching..." + stockData.getSymbol().getDataSz());
      this.setStatusBar("Searching..." + stockData.getSymbol().getDataSz());
    }

    // Load the Tab Pane Data on the Util Event Thread as 
    // to get our screen up and going as fast as possible
    // So we can get out of here - This Inner class thread
    // invokes method
    TimerTask tabPaneTask = new LoadTabPaneDataTask(this,refresh);
    Timer timer = new Timer(true); // Deamon
    timer.schedule(tabPaneTask, ONE_SECOND);

    TimerTask historicDataTask = new HistoricStockDataLoaderTask(stockData);
    Timer timer1 = new Timer(true); // Deamon
    timer1.schedule(historicDataTask, ONE_SECOND * 2);
  }
  /**
   * Method populateTextFields. Sets the values for each of the Text fields.
   */
  private void populateTextFields()
  {
    int month = this.getOrdinalMonthValueFromComboBox();
    GregorianCalendar expireMonth = StockMarketUtils.getExpirationDateForMonth(month);
    txtExpireDays.setText("T-" + StockMarketUtils.daysUntilExpiration(expireMonth, true));
    String statusMsg = "";
    if (stockData != null)
    {
      txtSymbol.setText(stockData.getSymbol().getDataSz());
      txtName.setText(stockData.getName());
      txtLastPrice.setText(stockData.getLast().toString());
      txt52WeekLow.setText(stockData.get52WeekLow().toString());
      txt52WeekHigh.setText(stockData.get52WeekHigh().toString());
      
      statusMsg += "Updated " + stockData.getSymbol().getDataSz() + " for " +
                   ParseData.format( expireMonth.getTime(), "MMM-yy" ) + 
                   " at " + ParseData.format( expireMonth.getTime(), "hh:mm:ss" );
    }
    this.setStatusBar(statusMsg);
  }
  /**
   * Method getOrdinalMonthValueFromComboBox.
   * Return the Ordinal value of the selected month in the Combo Box
   * @return int
   */
  private int getOrdinalMonthValueFromComboBox()
  {
    int ordValue = -1;
    int listIndex = lstMonthList.getSelectedIndex();
    String name = ((String) lstMonthList.getItemAt(listIndex)).toUpperCase();

    for (int i = 0; i < StockMarketUtils.MONTHSARR.length; i++)
    {
      String month = StockMarketUtils.MONTHSARR[i].name.toUpperCase();
      if (name.startsWith(month))
      {
        ordValue = i; // Found it!!!!
        i = StockMarketUtils.MONTHSARR.length;
      }
    }

    return ordValue;
  }
  /**
   * Method to invoke another Module and leave this one.
   * @param resourceName java.lang.String
   */
  private void setStatusBar(String statusMsg)
  {

    debug("setStatusBar() - get a handle to the main application");
    StockMarketApp mainApp = getMainApp();

    if (mainApp != null)
    {
      debug("setStatusBar() - Sending message to change applications");
      mainApp.setStatus(statusMsg);
    }
    debug("setStatusBar() - Processing completed");
  }
  /**
   * Method to shutdown an application timers and perform any cleanup on the 
   * timers required by this module
   */
  private void shutdownOptionChainTimers()
  {
    debug("shutdownOptionChainTimers() all timers");
    // Null our our parser, It is not needed now.
    if (populateListVector == null)
    {
      return;
    }
    // Stop All of our timers.
    for (int i = 0; i < populateListVector.size(); i++)
    {
      PopulateOptionDataTask task = (PopulateOptionDataTask) populateListVector.elementAt(i);
      task.cancel();
      if (tabPane != null)
      {
        this.setStatusBar("Option Chain [" + tabPane.getTitleAt(i) + "] - Stopped.");
        debug("Option Chain [" + tabPane.getTitleAt(i) + "] - Stopped.");
      }
    }
    // Clear all objects from the Timer List
    populateListVector.removeAllElements();
    populateListVector = null;
    // Signal the Garbage Collector to reclaim anything it may see neccessary
    System.gc();
    debug("shutdownOptionChainTimers() all timers - complete");
  }
  /**
   * Method to shutdown an application timers and perform any cleanup on the 
   * timers required by this module
   */
  private void startupOptionChainTimers(int optionListWithPriority)
  {
    debug("startupOptionChainTimers(" + optionListWithPriority + ")");
    if (populateListVector != null)
    {
      shutdownOptionChainTimers();
    }
    // Create our new list vector, for later shutdown
    if (populateListVector == null)
    {
      populateListVector = new Vector();
    }

    for (int i = 0; i < tabPane.getTabCount(); i++)
    {
      int expire = (i == optionListWithPriority) ? standardDelay : extendedDelay;
      int initialDelay = (i == optionListWithPriority) ? ONE_SECOND : standardDelay;
      TimerTask updateOptionChain = new PopulateOptionDataTask(monitorTask, i);
      populateListVector.add(updateOptionChain);
      Timer timer = new Timer(true); // Deamon 
      timer.schedule(updateOptionChain, initialDelay, expire);
      if (tabPane != null)
      {
        this.setStatusBar(
          "Option Chain [" + tabPane.getTitleAt(i) + "] - Started at [" + (expire / ONE_SECOND) + "] intervals.");
      }
      debug("Add List for " + i + " with time " + (expire / ONE_SECOND));
    }
    debug("startupOptionChainTimers(" + optionListWithPriority + ") - complete");

  }
  /**
   * Method to shutdown an application and perform any cleanup neccesarry
   */
  public void shutdown()
  {
    // Clear our tab pane and data vectors
    if (tabPane != null)
    {
      tabPane.removeAll();
      tabPane = null;
    }
    if (tableVector != null)
    {
      tableVector.removeAllElements();
      tableVector = null;
    }
    if (modelVector != null)
    {
      modelVector.removeAllElements();
      modelVector = null;
    }

    shutdownOptionChainTimers();
    // clear our Monitor Task
    if (this.monitorTask != null)
    {
      this.monitorTask.cancel();
      this.monitorTask = null;
    }

  }
  /**
   * Method to startup any threads if neccesary
   */
  public void start()
  {

    debug("start() all timers");
    // Get the Index of the Last Shown TabPane, 
    // As this method could be called more than once
    int index = tabPane.getSelectedIndex();
    startupOptionChainTimers(index);
    debug("start() all timers - complete");
  }
  /**
   * Method to stop any threads if neccesary
   */
  public void stop()
  {
    debug("stop() all timers");
    // Shutdown all the Timers
    shutdownOptionChainTimers();

    debug("stop() all timers - complete");
  }

}
