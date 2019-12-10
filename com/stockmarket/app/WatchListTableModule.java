// WatchListTableModule.java
package com.stockmarket.app;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.io.File;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.hgtable.ColumnHeaderData;
import com.hgtable.HGTable;
import com.hgtable.HGTableModel;
import com.hgtable.HGTableControlPanel;
import com.stockmarket.data.StockDataTableModel;
import com.stockmarket.data.HistoricStockDataContainer;
import com.stockmarket.data.StockData;
import com.stockmarket.data.StockMarketUtils;
import com.hgutil.AppProperties;
import com.hgutil.ParseData;
import com.hgutil.data.StockMarketTypes;
import com.hgutil.swing.event.PopupListener;
import com.hgmenu.HGMenuItem;
import com.hgmenu.HGMenuListItem;
import com.stockmarket.chart.ohlc.HighLowStockChart;
import com.stockmarket.parsers.StockDataCSVParser;
import com.stockmarket.parsers.StockDataHistCSVParser;
/**
 * Class Module to manage the display of the Stocks Watch Lists
 * It may be important to note the startup order and the complexity 
 * of the way the module starts up.
 * createPanel() is the first of the methods to be called.  Where in
 * It will create a JTabbedPane, then load a TimerTask to task to populate all
 * WatchLists.
 * The TimerTask will invoke the method loadPersistantWatchLists()
 * The first of all tasks is to expunge all data files residing on the drive.
 * followed spawing a new TimerTask for each Tab ( WatchList ) of StockData.
 * The loadHistoricData(init) spawns new TimerTasks to for each individual 
 * StockData within a list for which it begins to download historic data.
 * After a few seconds the WatchListTableModule#PopulateWatchListTask 
 * Begins to download StockData for each Item on the Watch List, Note
 * The Displayed WatchList Takes Priority.
 * @author Hans-Jurgen Greiner
 */
public class WatchListTableModule extends StockMarketBaseModule implements StockModuleDataExchange, Printable
{
  // Constants
  private static boolean MODULE_READY = false;
  public static final int ONE_SECOND = 1000; // MilliSeconds
  public static final int ONE_MINUTE = 60 * ONE_SECOND;
  public static final int ONE_HOUR = 60 * ONE_MINUTE;
  private final static long MONITOR_DELAY = ONE_MINUTE * 15;

  private JTabbedPane tabPane = null;
  private Vector populateListVector = null;
  private Vector modelVector = null;
  private Vector tableVector = null;
  private AppProperties appProps = null;
  private int standardDelay = ONE_MINUTE;
  private int extendedDelay = ONE_HOUR;
  // For the Printing Operation
  protected int maxNumPage = -1;
  protected JLabel printTitleLabel = null;
  protected int printTableIndex = -1;

  private String tableProps = null;
  private String fractionCmd = null;
  private String decimalCmd = null;
  private String insertAfterCmd = null;
  private String insertBeforeCmd = null;
  private String deleteRowCmd = null;
  private String addNewWatchListCmd = null;
  private String deleteWatchListCmd = null;
  private String renameListCmd = null;
  private String printListCmd = null;
  private String viewOptionChainCmd = null;
  private String viewHistoricChartCmd = null;

  private StockData stockData = null;

  private PopupListener popupListener = null;
  // Make a new Action Trigger, as it is generic and used in many places.
  private ActionTrigger actionTrigger = null;

  private WatchListMonitorTask monitorTask = null;
  ////////////////////////////////////////////////////////////////////////////
  // Begin Inner Class declarations
  ////////////////////////////////////////////////////////////////////////////
  /** MyFilter FilenameFilter - Inner class that will all for filtering of file
   * names, specified in the contructor
   */
  private class MyFilter implements java.io.FilenameFilter
  {

    private String name = null;

    /**
     * Constructor
     */
    public MyFilter(String name)
    {
      this.name = name;
    }

    /**
     * Mandatory method of FileNameFilter.  This method validates if the
     * file is truly a file and the Filename starts with the filename specified.
     */
    public boolean accept(java.io.File pathName, String fileName)
    {

      boolean valid = false;
      java.io.File testFile = new java.io.File(pathName, fileName);

      valid = (testFile.isFile() && fileName != null && fileName.startsWith(name));

      return valid;

    }

  }

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
     * MEthod that is called when the JTabbedPane state changes.
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
      int index = WatchListTableModule.this.tabPane.getSelectedIndex();
      debug("TabListener Called - State Changed - Tab Pane index is now [" + index + "]");

      // If we are the same index, then simply return, as we are already
      // processing for the this tab
      if (index == this.lastIndex)
      { // Return - As this is not changing
        return;
      }
      // Record our new index if it is at least 0
      this.lastIndex = index;
      if (WatchListTableModule.this.MODULE_READY)
      {
        // Ok We are done with a state change, continue processing.
        WatchListTableModule.this.startupWatchListTimers(index);
      }
    }
  }

  /**
   * PopulateWatchListTask Timer Task. This is intended to run on the
   * <B>Util Event</B> thread so as not to muck things up by doing
   * gui work off the gui thread. Accepts a WatchListTableModule and an int
   * This class performs a set of tasks as updating the current watch list being viewed.
   * <B><I>NOTE if this Thread is invoked directly it will run on the Main GUI Thread.</I></B>
   * An Example of the usage would be as follows:
   * <PRE>
   *   TimerTask updateWatchList = new PopulateWatchListTrigger( WatchListTableModule.this, 1 );
   *   timer new Timer( true );  // Deamon
   *   timer.schedule( updateWatchList, 1000 );
   * </PRE>
   */
  private class PopulateWatchListTask extends TimerTask
  {
    private String text = "WatchList_";
    private int index = -1;
    private WatchListMonitorTask monitorTask = null;
    private StockDataCSVParser stockDataParser = null;

    /**
     * Method PopulateWatchListTask., Constructor for the inner class.
     * @param watchListDataApp
     * @param tabIndex
     */
    public PopulateWatchListTask(WatchListMonitorTask monitorTask, int tabIndex)
    { // The main application
      this.index = tabIndex; // Get the index of which list we are watching
      // Create a text message for the list.
      this.text = WatchListTableModule.this.getString("WatchListTableModule.edit.watch_list_basic_name") + (tabIndex + 1);
      // Get A reference to the MonitorTask
      this.monitorTask = monitorTask;

      debug("PopulateWatchListTrigger() - Constructor - Index is [" + this.index + "] text is [" + this.text + "]");
    }
    /**
     * Performs the action run method which inturn refresh the watch list data
     * for the index.
     * @see TimerTask#run(ActionEvent)
     */
    public void run()
    {
      debug("PopulateWatchListTrigger::run() - Index is [" + this.index + "] text is [" + this.text + "]");

      this.refreshWatchListData(this.index);
    }
    /**
     * Method to setup a Parser object and Collect current Stock Price
     * Changes and update the StockData Vector with the new information.
     * @param index int
     */
    private synchronized void refreshWatchListData(int index)
    {
      debug("PopulateWatchListTrigger::refreshWatchListData(" + index + ") called - ");
      if (DEMO_MODE)
      {
        debug("PopulateWatchListTrigger::refreshWatchListData() - is considered to be refreshed - DEMO_MODE");
      }
      else
      {
        try
        {

          // Grab the appropriate TableModel
          StockDataTableModel model = (StockDataTableModel) modelVector.elementAt(index);

          debug("PopulateWatchListTrigger::refreshWatchListData(" + index + ") Single Threading of one StockData Vector");
          // Stock Data items
          if (stockDataParser == null)
          {
            stockDataParser = new StockDataCSVParser();
          }
          // Get the most recent set of data
          stockDataParser.getData(model.getData());
          // Indicate to the table model that the Data has changed
          model.fireTableDataChanged();

          // Save the Data that we have
          debug("PopulateWatchListTrigger::refreshWatchListData(" + index + ") attempting save of vector...");
          saveVectorData(index);
          appProps.saveProperties(getString("WatchListTableModule.application_ini_header_title"));

        }
        catch (Exception exc)
        {
          // This is a safety precaution.  If we think that a tab is selected for which
          // This processing is ocurring, It is possible that a user, is attemting.to delete
          // the watch list. If so, an exception would ocurr where an index into the vector
          // would cause an exception
          debug("PopulateWatchListTrigger::refreshWatchListData(" + index + ") - Exception caught: " + exc.getClass().getName());
          debug("PopulateWatchListTrigger::refreshWatchListData(" + index + ") - Exception message: " + exc);
        }

        debug("PopulateWatchListTrigger::refreshWatchListData(" + index + ") - Live Update Complete");
        // Notify the WatchListTask Monitor that we have fired.
        // If our Monitor is still alive then update it.  
        // It will update the status bar as well
        if (monitorTask != null)
        {
          monitorTask.notifyMonitorOnUpdate(index);
        }
      }
      debug("PopulateWatchListTrigger::refreshWatchListData(" + index + ") - complete ");
    }

  }

  /**
   * WatchListMonitorTask extends TimerTask
   * is designed to help with notification when a List may have stopped running,
   * The Class will restart the object
   */
  private class WatchListMonitorTask extends TimerTask
  {
    // Calcualte a reasonable timer delay i.e 7/5 minutes
    private final long MAX_WAIT_TIME = (long) (WatchListTableModule.this.MONITOR_DELAY / 2);
    private GregorianCalendar lastUpdate = null;

    /**
     * Method PopulateWatchListTask., Constructor for the inner class.
     * @param watchListDataApp
     * @param tabIndex
     */
    public WatchListMonitorTask()
    {
      debug("WatchListMonitorTask() - Constructor ");
    }
    /**
     * Performs the action run method which inturn refresh the watch list data
     * for the index.
     * @see TimerTask#run(ActionEvent)
     */
    public void run()
    {
      debug("WatchListMonitorTask::run()");
      if ( lastUpdate == null )
      {
        debug("WatchListMonitorTask::run() - Last Update is null");
        return;
      }
      // Get the current time
      GregorianCalendar t1 = new GregorianCalendar();
      // Get the Displayed Tab index
      int index = WatchListTableModule.this.tabPane.getSelectedIndex();
      // calculate the time difference from the last time
      long diffTime = t1.getTimeInMillis() - lastUpdate.getTimeInMillis();
      // Is the panel even displayed???
      boolean moduleDisplayed = WatchListTableModule.this.getModulePanel().isShowing();

      if ((diffTime >= MAX_WAIT_TIME) && (moduleDisplayed))
      {
        System.out.println("WatchListMonitorTask::run() - Fired a startupOnWatchListTimers on Index" + index);
        WatchListTableModule.this.startupWatchListTimers(index);
      }
    }

    /**
     * Method setLastUpdate. Method to support recording of the last update
     * @param index
     * @param lastUpdate
     */
    public void notifyMonitorOnUpdate(int index)
    {
      int selectedIndex = WatchListTableModule.this.tabPane.getSelectedIndex();
      GregorianCalendar t1 = new GregorianCalendar();
      if (index == selectedIndex)
      {
        this.lastUpdate = t1;
      }
      debug(
        "WatchListMonitorTask::notifyMonitorOnUpdate() - Table "
          + index
          + " - Time is "
          + ParseData.format(t1.getTime(), "hh:mm:ss"));
      WatchListTableModule.this.setStatusBar(
        "WatchList ["
          + WatchListTableModule.this.tabPane.getTitleAt(index)
          + "] - Last Update "
          + ParseData.format(t1.getTime(), "hh:mm:ss"));
    }
  }

  /**
   * Class ActionTrigger that implements action Listener.  This is used.
   * for the monitoring of events on Menus and Buttons, for which actions 
   * need to happen
   */
  private class ActionTrigger implements ActionListener
  {

    public void actionPerformed(ActionEvent evt)
    {

      // Since we are here - Find out which Tab we are showing.
      // Then get the Appropriate Table and Table Model
      int index = tabPane.getSelectedIndex();
      HGTable table = (HGTable) tableVector.elementAt(index);
      StockDataTableModel dataModel = (StockDataTableModel) modelVector.elementAt(index);
      // NOTE:  We do not know at this time if we were called via the
      //        popup menu, or the Normal Top menu - And we do not know
      //        if an action is desired on the displayed table.
      //        However, We will grab the point and determine the row.
      //        If the Row is less than zero attempt to get the row
      //        from the table as possible being selected.
      int row = table.rowAtPoint(new java.awt.Point(popupListener.getX(), popupListener.getY()));
      row = (row < 0) ? ((table.getSelectedRow() >= 0) ? table.getSelectedRow() : 0) : row;

      /////////////////////////////////////////////////
      // Grab our command
      /////////////////////////////////////////////////
      String cmd = evt.getActionCommand();
      debug("Menu - Action Performed Called with command [" + cmd + "]");

      /////////////////////////////////////////////////
      // Examine the options a user may have requested.
      // and act upon them accordingly.
      /////////////////////////////////////////////////
      if (tableProps.equals(cmd))
      { // Allowing for Table Control to be presented
        WatchListTableModule.this.showTableControl();
      }
      else if (fractionCmd.equals(cmd))
      { // Show Fractions
        com.hgutil.data.Fraction.setShowAsFraction(true);
        dataModel.fireTableDataChanged();
      }
      else if (decimalCmd.equals(cmd))
      { // Show Decimals
        com.hgutil.data.Fraction.setShowAsFraction(false);
        dataModel.fireTableDataChanged();
      }
      else if (insertBeforeCmd.equals(cmd))
      { // Insert a Row BEFORE selected
        debug("Insert Row Before row [" + row + "] The Row being inserted is " + row);
        dataModel.insert(row);
        table.tableChanged(new TableModelEvent(dataModel, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
        table.setRowSelectionInterval(row + 1, row + 1);
        table.repaint();
        StockData sd = (StockData) dataModel.getData().elementAt(row);
        WatchListTableModule.this.loadHistoricData(sd);
      }
      else if (insertAfterCmd.equals(cmd))
      { // Insert a Row AFTER selected
        debug("Insert Row After row [" + row + "] The Row being inserted is " + (row + 1));
        dataModel.insert(row + 1);
        table.tableChanged(
          new TableModelEvent(dataModel, row + 1, row + 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
        table.setRowSelectionInterval(row, row);
        table.repaint();
        StockData sd = (StockData) dataModel.getData().elementAt(row + 1);
        WatchListTableModule.this.loadHistoricData(sd);
      }
      else if (deleteRowCmd.equals(cmd))
      { // Delete the selected Row
        debug("Delete row [" + row + "] The Row being deleted is " + row);
        if (dataModel.delete(row))
        {
          table.tableChanged(new TableModelEvent(dataModel, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
          table.setRowSelectionInterval(row + 1, row + 1);
          table.repaint();
        }
      }
      else if (addNewWatchListCmd.equals(cmd))
      {
        debug("Add New WatchList Command...");
        WatchListTableModule.this.addNewWatchList();
      }
      else if (deleteWatchListCmd.equals(cmd))
      {
        debug("Delete current WatchList Command, index = [" + index + "]");
        WatchListTableModule.this.deleteWatchList(index);
      }
      else if (renameListCmd.equals(cmd))
      { // Rename the WatchList
        debug("Rename current WatchList Command, index = [" + index + "]");
        WatchListTableModule.this.setNewTitle(index);
      }
      else if (printListCmd.equals(cmd))
      { // Print the WatchList
        debug("Printout current WatchList Command, index = [" + index + "]");
        WatchListTableModule.this.printWatchList(index);
      }
      else if (viewOptionChainCmd.equals(cmd))
      {
        debug("View Option Chain Command, index = [" + index + "]");
        StockData sd = (StockData) dataModel.getData().elementAt(row);
        WatchListTableModule.this.setDataObject(sd);
        WatchListTableModule.this.invokeStockMarketModule(getString("WatchListTableModule.view_option_chain.module_name"));
      }
      else if (viewHistoricChartCmd.equals(cmd))
      {
        debug("View Option Chain Command, index = [" + index + "]");
        StockData sd = (StockData) dataModel.getData().elementAt(row);
        WatchListTableModule.this.invokeHistoricChart(sd);
      }

      popupListener.resetPoints();
    }
  }

  /**
   * Class ColumnKeeper extends AbstractAction that implements ActionListener.  
   * This is used for Monitoring the event of Removal and Adding of selected columns.
   * within a specified table that need to happen
   */
  private class ColumnKeeper extends AbstractAction implements ActionListener
  {
    protected TableColumn column;
    protected ColumnHeaderData colData;
    protected int columnIndex = -1;

    /**
     * Method ColumnKeeper. Inner Class Contructor
     * @param colData
     */
    public ColumnKeeper(ColumnHeaderData colData)
    {
      this.column = null;
      this.colData = colData;
    }

    /**
     * Mandatory method.  It validates if a column in the table has been removed
     * from visibility.  And updates the Table with the approriate information.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
      // What Tab are we showing
      int index = tabPane.getSelectedIndex();
      // Grab the appropriate Table and TableModel
      HGTable table = (HGTable) tableVector.elementAt(index);
      StockDataTableModel dataModel = (StockDataTableModel) modelVector.elementAt(index);
      JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
      TableColumnModel model = table.getColumnModel();
      // If the Item we are trying to enable or disable is the Symbol Column
      // Then Just Return and do not let it happen
      if (item.getText().startsWith(StockData.columns[0].getTitle()))
      {
        return;
      }
      if (column == null)
      { // If theColumn has not be initialized - then get it now
        column = table.getColumn(item.getText());
      }
      if (item.isSelected())
      { // Check is on, Add it in
        model.addColumn(column);
        int currIndex = model.getColumnIndex(item.getText());
        model.moveColumn(currIndex, columnIndex);
      }
      else
      { // Check is off - remove it
        columnIndex = model.getColumnIndex(item.getText());
        model.removeColumn(column);
      }
      // Notify the Table - that Our Model Has changed.
      table.tableChanged(new TableModelEvent(dataModel));
      table.repaint();
    }
  }
  /** 
   * LoadHistoricData TimerTask - Inner class that will load the historic 
   * data on the Event Thread - This Task essentially recieves the
   * Watch List for which to load the Historic data, and spawns sub-tasks
   * load each individual stock historic data
   * NOTE: This task will cancel itself after spawning all sub-tasks 
   * within a watch list.
   */
  private class LoadHistoricListDataTask extends TimerTask
  {
    private int index = -1;

    /**
     * LoadHistoricListDataTask constructor
     */
    public LoadHistoricListDataTask(int index)
    {
      this.index = index;
    }

    /**
     * LoadHistoricListDataTask::run() method invokes the method 
     * WatchListTableModule::loadHistoricData(index) where the index represents 
     * index of the Tab on a JTabbedPane and the Vector of data of StockData
     * After which the Thread will die.
     */
    public void run()
    {
      debug("LoadHistoricData::run() - calling WatchListTableModule.loadHistoricData(" + index + ")");
      this.loadHistoricData(index);
      this.cancel();
    }
    /**
     * Load the Historic Data for each Stock Data Object in the Vector
     * @param index int
     */
    private synchronized void loadHistoricData(int index)
    {

      debug("LoadHistoricData::loadHistoricData(" + index + ") - called...");
      if (DEMO_MODE)
      {
        debug("LoadHistoricData::loadHistoricData(" + index + ") - called...considered to be Refreshed - DEMO_MODE");
      }
      else
      {
        // We are running live.
        try
        {
          // Get our Model
          StockDataTableModel model = (StockDataTableModel) modelVector.elementAt(index);
          Vector data = model.getData();

          // Now loop through our vector list for all the StockData 
          // items and create a new thread to run on these.
          for (int i = 0; i < data.size(); i++)
          {
            StockData sd = (StockData) data.elementAt(i);
            WatchListTableModule.this.loadHistoricData(sd);
            // Be Gentle we need the information, 
            // But not enough to kill the application, sleep a second
            Thread.currentThread().sleep(ONE_SECOND);
          }
        }
        catch (Exception exc)
        {
          // This is a safety precaution.  If we think that a tab is selected for which
          // This processing is ocurring, It is possible that a user, is attemting.to delete
          // the watch list. If so, an exception would ocurr where an index into the vector
          // would cause an exception
          debug("LoadHistoricData::loadHistoricData(" + index + ") - Exception caught: " + exc.getClass().getName());
          debug("LoadHistoricData::loadHistoricData(" + index + ") - Exception message: " + exc);
        }
      }
    }
  }
  /**
   * HistoricStockDataLoaderTask extends TimerTask
   * Used to load a specific StockData and refreshes on the table
   * If the item is null or empty it wait for 20 minutes then purge.
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

      if (this.waitUntilReady() < 0)
      {
        this.cancel();
        return;
      }
      // Instantiate a parser
      WatchListTableModule.this.setStatusBar("Downloading Historic Details for " + sd.getSymbol().getDataSz() + ".");

      StockDataHistCSVParser parser = new StockDataHistCSVParser();
      fileName = getString("WatchListTableModule.edit.historic_details_basic_name") + sd.getSymbol().getDataSz();

      // Create our date range of 1 year ago until today
      GregorianCalendar now = new GregorianCalendar();
      GregorianCalendar then = new GregorianCalendar();
      then.add(Calendar.YEAR, -1);
      Vector list =
        parser.getHistoricDataBetweenDates(sd.getSymbol().getDataSz(), then.getTime(), now.getTime(), StockMarketTypes.DAILY);

      HistoricStockDataContainer hsd = new HistoricStockDataContainer(sd, list);

      StockMarketUtils.serializeData(hsd, this.fileName);
      this.cancel();
      // Signal the Garbage Collector that we are ready to rumble
      System.gc();
      WatchListTableModule.this.setStatusBar("Historic Details for " + sd.getSymbol().getDataSz() + " complete.");
    }
    /**
     * private method to wait until the user has either entered some data
     * or a time has elaped and no entry was made.
     */
    private int waitUntilReady()
    {
      int rc = 0;
      // If a stock Item is in the Table, We do not want to start
      // The Lookup, if the name of the stock has not been identified.
      // here we will wait just a couple a second at a time.
      // until, the user has entered the name of the stock.
      // MAXWAIT is 20 minutes, since we are 1 second loops.
      // 60 loops is 1 minute and 20 of those is 20 minutes
      final int MAXWAIT = 60 * 20;
      int counter = 0;
      do
      {
        try
        {
          Thread.currentThread().sleep(ONE_SECOND);
        }
        catch (InterruptedException exc)
        {
          // Do Nothing - Try again
        }
        if (counter++ > MAXWAIT)
        { // Abort the Lookup for historic data
          this.cancel();
          return -1;
        }
      }
      while ("".equals(sd.getSymbol().getDataSz().trim()));

      return 0;
    }

  }

  /**
   * WatchListTableModule constructor comment.
   * @param stockMarketApp The StockMarket Main Application
   */
  public WatchListTableModule(StockMarketApp stockMarketApp)
  {
    super(stockMarketApp, "WatchListTableModule", "toolbar/StockArrow.gif");
    getModulePanel().add(createPanel(), BorderLayout.CENTER);
  }
  /**
   * Method to watch all Watch Lists elemetns to their associated Vectors.
   * This method will add a timer ( via the index ) the table and tableModel
   * the appropriate vector containers.
   * @param table A JTable The Display table used for graphical decisions
   * @param model The HGTableModel used for logic based decisions
   * @param index The index of the Tab being added, to fire Timers threads
   */
  private void addDataToVectorContainers(JTable table, HGTableModel model, int index)
  {

    debug(
      "addDataToVectorContainers() - Watch List("
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
   * Method to add a new Tab and Watch List for application
   */
  private synchronized void addNewWatchList()
  {

    // Create a NEW header Vector
    Vector headers = new Vector();
    headers.removeAllElements();
    for (int i = 0; i < StockData.columns.length; i++)
    {
      headers.addElement(StockData.columns[i]);
    }

    // Now get the Count of the Lists already available
    int watchListCount = tabPane.getTabCount();
    // Create a new DATA Vector and add at least one item to it
    Vector data = new Vector();
    data.removeAllElements();

    data.addElement(new StockData());

    // Create our table Model with the headers and data
    StockDataTableModel tableModel = new StockDataTableModel(headers, data);

    // Create a new Table with the tablemodel
    JTable table = new HGTable(tableModel);

    // This method will add a timer ( via the index ) the table and tableModel
    // the appropriate vector containers.
    addDataToVectorContainers(table, tableModel, watchListCount);
    // This Method will add the Popup menu to the this table
    addPopupMenuToTable(table);
    // Create a new JSCrollPane with the the Table
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.getViewport().add(table);
    JPanel panel = new JPanel(new BorderLayout());

    // Create a new Vertical Panel to place our menu and JScrollPane Table
    JPanel lblPanel = createVerticalPanel(true);

    lblPanel.add(createDropDownMenu());
    panel.add(lblPanel, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);

    // Build a defailt label for the TabPane
    String watchListName = getString("WatchListTableModule.edit.watch_list_basic_name") + watchListCount;

    tabPane.addTab(watchListName, null, panel, new String("Click here to view " + watchListName));

    watchListCount = tabPane.getTabCount();
    // Set the WatchList title via Properties
    // Set the WatchList count Property
    appProps.setProperty(watchListName, watchListName);
    appProps.setProperty(getString("WatchListTableModule.application_watchlist_count_key_title"), "" + watchListCount);

    //// Request Focus on our tab pane
    //tabPane.requestFocus();
    // Set our selected index to the new Tab
    int index = tabPane.getTabCount() - 1;
    setNewTitle(index);

    tabPane.setSelectedIndex(0);
    tabPane.repaint();

  }
  /**
   * Method to add a popup menu to the JTable
   * @param table A JTable The Display table used for graphical decisions
   */
  private void addPopupMenuToTable(JTable table)
  {

    if (popupListener == null)
    {
      popupListener = new PopupListener(createPopupMenu());
    }

    table.addMouseListener(popupListener);
  }
  /**
   * Creates a dropdown edit menu for the panel.
   */
  protected JMenuBar createDropDownMenu()
  {

    // Setup menu Items String values that are shared
    setSharedMenuItemStrings();
    // Make a new Action Trigger, as it is generic and used in many places.
    ActionTrigger actionTrigger = new ActionTrigger();
    // Add	all the Horizontal elements
    JMenuBar result = new JMenuBar();

    // to the button group - Set the Fraction Decimal Visible as being
    // selected below.
    com.hgutil.data.Fraction.setShowAsFraction(false);

    // Create two individual check button menu items, and add
    ButtonGroup fractionGroup = new ButtonGroup();
    HGMenuItem fractionCheck =
      new HGMenuItem(
        HGMenuListItem.JCHECKBOXMNUITEM,
        getString("WatchListTableModule.edit_menu.fractions_on_text"),
        fractionCmd,
        null,
        KeyEvent.VK_F,
        InputEvent.CTRL_MASK,
        fractionGroup,
        false);
    HGMenuItem decimalCheck =
      new HGMenuItem(
        HGMenuListItem.JCHECKBOXMNUITEM,
        getString("WatchListTableModule.edit_menu.decimals_on_text"),
        decimalCmd,
        null,
        KeyEvent.VK_D,
        InputEvent.CTRL_MASK,
        fractionGroup,
        true);
    JMenu viewColumnNumbers =
      HGMenuItem.makeMenu(
        getString("WatchListTableModule.edit_menu.view_columns_fields_as"),
        'C',
        new Object[] { fractionCheck, decimalCheck },
        actionTrigger);

    // Lets build a Menu List of Columns that we can either 
    // view or not view
    // Build Check Boxes, for all Items, except the Symbol Column
    HGMenuItem[] columnsChecks = new HGMenuItem[StockData.columns.length];
    for (int k = 1; k < StockData.columns.length; k++)
    {
      columnsChecks[k] =
        new HGMenuItem(
          HGMenuListItem.JCHECKBOXMNUITEM,
          StockData.columns[k].getTitle(),
          null,
          null,
          0,
          0,
          null,
          true,
          new ColumnKeeper(StockData.columns[k]));
    }

    // Add in the Viewing menu
    JMenu viewColumns =
      HGMenuItem.makeMenu(getString("WatchListTableModule.edit_menu.view_columns_text"), 'V', columnsChecks, null);

    JMenu insertRows =
      HGMenuItem.makeMenu(
        getString("WatchListTableModule.edit_menu.view_insert_row_text"),
        'I',
        new Object[] { insertBeforeCmd, insertAfterCmd },
        actionTrigger);

    JMenu editMenu = null;
    editMenu =
      HGMenuItem.makeMenu(
        getString("WatchListTableModule.edit_menu.title"),
        'E',
        new Object[] {
          viewColumns,
          viewColumnNumbers,
          null,
          insertRows,
          deleteRowCmd,
          null,
          addNewWatchListCmd,
          deleteWatchListCmd,
          renameListCmd,
          null,
          printListCmd,
          null,
          tableProps },
        actionTrigger);

    // Add the Edit Menu to the result set the Alignment and return the MenuBar
    result.add(editMenu);
    result.setAlignmentX(JMenuBar.LEFT_ALIGNMENT);
    return result;
  }
  /**
   * Creates a our main working panel - However this panel is a JTabbedPane
   */
  public JTabbedPane createPanel()
  {

    /** LoadTabPaneData Thread - Inner class that will load the historic data on the AWT
     *  Thread as to not muck up things on the GUI thread
     *  The Thread will die after it loads everything.
     */
    class LoadTabPaneDataTask extends TimerTask
    {
      // Handle to main outer class
      WatchListTableModule watchListTableModule = null;

      /** 
       * LoadTabPaneDataTask constructor
       */
      public LoadTabPaneDataTask(WatchListTableModule watchListTableModule)
      {
        this.watchListTableModule = watchListTableModule;
      }

      /** 
       * LoadTabPaneDataTask::run() this method simply invokes. 
       * WatchListTableModule::loadPersistantWatchLists() method.
       * The method will populate all watchlists to be loaded on
       * the JTabbedPane then it dies off
       */
      public void run()
      {
        debug("LoadTabPaneDataTask::run() - calling watchListTableModule.loadPersistantWatchLists()");
        watchListTableModule.loadPersistantWatchLists();
        this.cancel();
      }
    }

    // Create an Application Properties instance
    appProps = new AppProperties(getString("WatchListTableModule.application_ini_filename"));
    // Setup our Time Delays
    standardDelay = ONE_SECOND * ParseData.parseNum(getString("WatchListTableModule.standard_time_delay"), ONE_MINUTE);
    extendedDelay = ONE_SECOND * ParseData.parseNum(getString("WatchListTableModule.extended_time_delay"), ONE_HOUR);

    // Create our Basic Tabbed Pane and Listener ( inner class )
    tabPane = new JTabbedPane();
    tabPane.addChangeListener(new TabPaneListener());

    // Load the Tab Pane Data on the Util Event Thread as 
    // to get our screen up and going as fast as possible
    // So we can get out of here - This Inner class thread
    // invokes method
    // loadPersistantWatchLists();
    TimerTask tabPaneTask = new LoadTabPaneDataTask(this);
    Timer timer = new Timer(true); // Deamon
    timer.schedule(tabPaneTask, ONE_SECOND);

    return tabPane;
  }
  /**
   * Creates a popup edit menu for the table
   */
  protected JPopupMenu createPopupMenu()
  {

    // Setup menu Items String values that are shared
    setSharedMenuItemStrings();
    // Add	all the Horizontal elements
    JPopupMenu result = null;

    JMenu insertRows =
      HGMenuItem.makeMenu(
        getString("WatchListTableModule.edit_menu.view_insert_row_text"),
        'I',
        new Object[] { insertBeforeCmd, insertAfterCmd },
        actionTrigger);

    result =
      HGMenuItem.makePopupMenu(
        new Object[] {
          viewOptionChainCmd,
          null,
          viewHistoricChartCmd,
          null,
          insertRows,
          deleteRowCmd,
          null,
          addNewWatchListCmd,
          deleteWatchListCmd,
          renameListCmd,
          null,
          printListCmd },
        actionTrigger);

    // Set the Alignment and return the MenuBar
    result.setAlignmentX(JMenuBar.LEFT_ALIGNMENT);
    return result;
  }
  /**
   * Method to delete an WatchList in the list
   * @param index The index of the Tab being deleted
   */
  private void deleteWatchList(int index)
  {

    debug("deleteWatchList(" + index + ") - Delete WatchList");
    int watchListCount = tabPane.getTabCount();
    // Do not permit removal of the last watchlist on the tab pane
    if (watchListCount <= 1)
    {
      return;
    }
    // Stop all AWT threads from doing work
    shutdownWatchListTimers();
    // Remove the Data from the Vectors required
    tableVector.remove(index);
    modelVector.remove(index);
    populateListVector.remove(index);

    // Remove the Tab from the Tab Pane
    tabPane.removeTabAt(index);

    // Save all remaining data...
    saveAllVectorData();

    // Request Focus on our tab pane
    tabPane.requestFocus();
    // Set our selected index to the first tab
    tabPane.setSelectedIndex(0);
    debug("deleteWatchList(" + index + ") - Delete WatchList - complete");
  }
  /**
   * Method to delete all Watch Lists Files.
   * @param index The index of the Tab being deleted
   */
  private void expungeAllHistoricFiles()
  {
    debug("expungeAllHistoricFiles() - Delete ALL HISTORIC Files");

    java.io.File fileList = new java.io.File("./");
    String rootName = getString("WatchListTableModule.edit.historic_details_basic_name");
    String[] list = fileList.list(new MyFilter(rootName));
    for (int i = 0; i < list.length; i++)
    {
      StockMarketUtils.expungeListsFile(list[i]);
    }
    debug("expungeAllHistoricFiles() - Delete ALL HISTORIC Files - complete");

  }
  /**
   * Method to delete all Watch Lists Files.
   * @param index The index of the Tab being deleted
   */
  private void expungeAllWatchListsFiles()
  {
    debug("expungeAllWatchListsFiles() - Delete ALL WATCHLIST Files");

    java.io.File fileList = new java.io.File("./");
    String rootName = getString("WatchListTableModule.edit.watch_list_basic_name");
    String[] list = fileList.list(new MyFilter(rootName));
    for (int i = 0; i < list.length; i++)
    {
      StockMarketUtils.expungeListsFile(list[i]);
    }

    debug("expungeAllWatchListsFiles() - Delete ALL WATCHLIST Files - Complete");
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
   * Method to get data populated in Demo Mode
   * @param data java.util.Vector to add the Data too.
   */
  private void getDemoData(Vector data)
  {

    debug("getDemoData() - preparing to get Canned Data");

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
      new StockData("IBM", "Intl. Bus. Machines", 183, 183.125, 183, 183.125, -0.125, -0.51, 4371400, 0.0, 0.0));
    data.addElement(new StockData("HWP", "Hewlett-Packard", 70, 71.0625, 70, 71.0625, -1.4375, -2.01, 2410700, 0.0, 0.0));
    data.addElement(new StockData("UIS", "Unisys Corp.", 28.25, 29, 28.25, 29, -0.75, -2.59, 2576200, 0.0, 0.0));
    data.addElement(new StockData("SNE", "Sony Corp.", 28.25, 29, 28.25, 29, -0.75, -2.59, 2576200, 0.0, 0.0));
    data.addElement(
      new StockData("NOVL", "Novell Inc.", 24.0625, 24.375, 24.0625, 24.375, -0.3125, -3.02, 6047900, 0.0, 0.0));
    data.addElement(new StockData("HIT", "Hitachi, Ltd.", 78.5, 77.625, 78.5, 77.625, 0.875, 1.12, 49400, 0.0, 0.0));

    debug("getDemoData() - complete");
  }
  /**
   * Method to get data populated in LiveMode
   * @param data java.util.Vector to add the Data too.
   * @param index the index of the current set to load
   */
  private void getLiveData(Vector data, int index)
  {

    debug("getLiveData() - preparing to get Live Data from File");
    // Create a filename to retrieve the Data 
    String fileName = getString("WatchListTableModule.edit.watch_list_basic_name") + index;
    Vector tempVector = null;

    java.io.ObjectInputStream istream = null;
    try
    { // Open a new output stream to save the data
      istream = new java.io.ObjectInputStream(new java.io.FileInputStream(fileName));

      // Write the data to the file
      tempVector = (Vector) istream.readObject();

      // Close the data
      istream.close();

    }
    catch (ClassNotFoundException exc)
    {
      debug("getLiveData() - ClassNotFoundException: Failed Reading data from [" + fileName + "]");
      debug("getLiveData() - exc info ==> " + exc);
    }
    catch (java.io.NotSerializableException exc)
    {
      debug("getLiveData() - NotSerializableException: Failed Reading data from [" + fileName + "]");
      debug("getLiveData() - exc info ==> " + exc);
    }
    catch (java.io.IOException exc)
    {
      debug("getLiveData() - IOException: Failed Reading data from [" + fileName + "]");
      debug("getLiveData() - exc info ==> " + exc);
      exc.printStackTrace();
    }
    finally
    {
      if (istream != null)
      {
        try
        {
          istream.close();
        }
        catch (Exception exc)
        {}
      }
    }

    // Load our data vector with the data from the file
    // This should never happen, but if someone should play around
    // with the data files. we could be toast. So we are going to 
    // protect our watchlist with a blank data - so something
    // is readily available
    // Create a new Vector - And Add a new Empty Element
    if (tempVector == null || tempVector.size() < 1)
    {
      tempVector = new Vector();
      tempVector.addElement(new StockData());
    }
    // Copy all our data elements in the the data Vector to return
    for (int i = 0; i < tempVector.size(); i++)
    {
      data.addElement(tempVector.elementAt(i));
    }
    debug("getLiveData() - complete");
  }
  /**
    * This method is here as a place holder in the case that it is run
    * seperatly, and this situation, we will need to know what the
    * resource bundle name is.
    * So that we can get MultiModuleApp up and available to the user quickly.
    * Here is an Example of how to setup.
    * <PRE>
    * public String getResourceBundleName() {
    *   return "resources.stockmarketapp";
    * }
    * </PRE>
    */
  public String getResourceBundleName()
  {
    return null;
  }
  /**
   * Method to invoke the Historic StockChart
   * @param resourceName java.lang.String
   */
  private void invokeHistoricChart(StockData sd)
  {

    debug("invokeHistoricChart(" + sd.getName().toString() + ") - preparing to change");

    String fileName = getString("WatchListTableModule.edit.historic_details_basic_name") + sd.getSymbol().getDataSz();
    java.io.File theFile = new java.io.File(fileName);

    // First signal the chart if we are demo mode, we really do not want a problem
    // Then create a new instance of our chart.
    com.stockmarket.chart.ohlc.HighLowStockChart.DEMO_MODE = this.DEMO_MODE;
    com.stockmarket.chart.ohlc.HighLowStockChart chart = new com.stockmarket.chart.ohlc.HighLowStockChart();
    // If we have historic data send it through
    if (theFile.exists())
    {
      debug("invokeHistoricChart() - " + fileName + " exists preparing the Historic Chart");
      chart.displayHighLowChart(fileName);
    }
    else if (this.DEMO_MODE)
    {
      // If we are demo mode display the Chart. With its default data
      javax.swing.JOptionPane.showMessageDialog(
        null,
        "Application is in DEMO Mode\n"
          + "No historic data for "
          + sd.getName().toString()
          + " exists, displaying default graph.");
      chart.displayHighLowChart(null);
    }
    else
    {
      // If we are live mode and no historic data - tell the user to try later.
      // This is the case where the user reached a item to display as a chart
      // before the program has finished downloading.
      javax.swing.JOptionPane.showMessageDialog(
        null,
        "Application has no historic data for " + sd.getName().toString() + "\nTry again later!");
    }

    debug("invokeHistoricChart() - Processing completed");
  }
  /**
   * Method to invoke another Module and leave this one.
   * @param resourceName java.lang.String
   */
  private void invokeStockMarketModule(String resourceName)
  {

    debug("invokeStockMarketModule(" + resourceName + ") - preparing to change");
    debug("invokeStockMarketModule() - shudown thread timers");
    shutdownWatchListTimers();

    debug("invokeStockMarketModule() - get a handle to the main application");
    StockMarketApp mainApp = getMainApp();

    if (mainApp != null)
    {
      debug("invokeStockMarketModule() - Sending message to change applications");
      mainApp.notifyChangeModules(resourceName);
    }
    else
    {
      System.out.println("invokeStockMarketModule() - module [" + resourceName + "] cannot be invoked!");
      System.out.println("                          - This function only works when... ");
      System.out.println("                          - The Full Application is running.");
    }

    debug("invokeStockMarketModule() - Processing completed");
  }
  /**
   * Method to setup a Parser object and Collect enough Historical Data to
   * calculate a the Volatility and update the Stock Data Vector with the info
   * @param sd The StockData to retrieve
   */
  private synchronized void loadHistoricData(StockData sd)
  {
    debug("loadHistoricData(" + sd.getSymbol().getDataSz() + ") called - ");
    if (DEMO_MODE)
    {
      debug("loadHistoricData() - is considered to be refreshed - DEMO_MODE");
    }
    else
    {

      String fileName = getString("WatchListTableModule.edit.historic_details_basic_name") + sd.getSymbol().getDataSz();
      //Spawn a new thread on the Util event Thread as to provide a smooth transition
      //into our new thread proicessing.and to Load Historic Stock Data
      HistoricStockDataLoaderTask downloadTask = new HistoricStockDataLoaderTask(sd);
      Timer timer = new Timer(true); // Deamon
      timer.schedule(downloadTask, ONE_SECOND);

      if (DEBUG)
      {
        System.out.println("Results from: " + sd);
        System.out.println("StockData[" + sd.getSymbol().getDataSz() + "] = " + sd);
      }
    }

  }
  /**
   * This method clears all data files and loads all WatchLists to the TabbedPane
   * and loads each WatchList into a Timer task to retrieve the StockData
   * Historic Details for each individual stockData.  then the Each TimerTask will die.
   * When all WatchLists are loaded into a TimerTask, a new TimerTask is spwaned
   * to Monitor the Continued Progress of the the WatchLists, to ensure that they
   * continue processing, even if the List should die out.
   * Initial Load Time is coded to 6 seconds. A Little Breathing Room
   * @see WatchListTableModule#WatchListMonitorTask
   */
  private void loadPersistantWatchLists()
  {

    debug("loadPersistantWatchLists() called - ");

    expungeAllHistoricFiles();

    final int watchListCount =
      ParseData.parseNum(appProps.getProperty(getString("WatchListTableModule.application_watchlist_count_key_title")), 0);

    for (int index = 0; index < watchListCount; index++)
    {
      // Create a NEW header Vector
      Vector headers = new Vector();
      headers.removeAllElements();
      for (int i = 0; i < StockData.columns.length; i++)
      {
        headers.addElement(StockData.columns[i]);
      }

      // Create a new DATA Vector
      Vector data = new Vector();
      data.removeAllElements();
      // Are we in DEMO mode ior in Live Mode
      // Populate the Vectors
      if (DEMO_MODE)
      {
        getDemoData(data);
      }
      else
      {
        //getDemoData(data);
        getLiveData(data, index);
      }

      // Create a new Table Model with our headers and data
      StockDataTableModel tableModel = new StockDataTableModel(headers, data);

      // Create a new Table with our Tablemodel
      JTable table = new HGTable(tableModel);

      // This method will add a timer ( via the index ) the table and tableModel
      // the appropriate vector containers.
      addDataToVectorContainers(table, tableModel, index);
      // This Method will add the Popup menu to the this table
      addPopupMenuToTable(table);
      // Create a new ScrollPane and add our table to it
      JScrollPane scrollPane = new JScrollPane();
      scrollPane.getViewport().add(table);
      JPanel panel = new JPanel(new BorderLayout());

      // Creae a new Vertical Panel and add our Menu and JScrollPane Table
      JPanel lblPanel = createVerticalPanel(true);

      lblPanel.add(createDropDownMenu());
      panel.add(lblPanel, BorderLayout.NORTH);
      panel.add(scrollPane, BorderLayout.CENTER);

      // Create a default name for that tab and tab name lookup
      String tabName = getString("WatchListTableModule.edit.watch_list_basic_name") + index;
      // Get the WatchList title via Properties
      // Get the Property
      tabName = appProps.getProperty(tabName, tabName);
      tabPane.addTab(tabName, null, panel, new String("Click here to view " + tabName));

      // Set the selected index to the first in the list
      tabPane.setSelectedIndex(0);
      // Repaint the tabpane with the current data
      tabPane.repaint();

      // Load the Historical Data on the Util Event Thread as 
      // to get our primary data  up and going as fast as possible
      // So we can get out of here - This Inner class thread
      // invokes class instance method loadHistoricData(index);
      // NOTE: Startup Initial delay of 5 seconds, give the application,
      // Time to load.
      TimerTask historicDataTask = new LoadHistoricListDataTask(index);
      Timer timer = new Timer(true);
      timer.schedule(historicDataTask, ONE_SECOND );  // ONE_SECOND * 6);

      this.setStatusBar("Loading watch list [" + index + "] complete.");
    }

    this.MODULE_READY = true;

    this.monitorTask = new WatchListMonitorTask();
    Timer timer = new Timer(true); // Non-Deamon
    timer.scheduleAtFixedRate(monitorTask, MONITOR_DELAY, MONITOR_DELAY);

    debug("loadPersistantWatchLists() complete - ");

  }
  /**
   * Main Method - There are times when the application
   * needs to run as a seperate module.
   */
  public static void main(java.lang.String[] args)
  {
    // Insert code to start the application here.
    WatchListTableModule demo = new WatchListTableModule(null);
    demo.mainImpl();
  }
  /**
   * Prints the page at the specified index into the specified 
   * {@link Graphics} context in the specified
   * format.  A <code>PrinterJob</code> calls the 
   * <code>Printable</code> interface to request that a page be
   * rendered into the context specified by 
   * <code>graphics</code>.  The format of the page to be drawn is
   * specified by <code>pageFormat</code>.  The zero based index
   * of the requested page is specified by <code>pageIndex</code>. 
   * If the requested page does not exist then this method returns
   * NO_SUCH_PAGE; otherwise PAGE_EXISTS is returned.
   * The <code>Graphics</code> class or subclass implements the
   * {@link PrinterGraphics} interface to provide additional
   * information.  If the <code>Printable</code> object
   * aborts the print job then it throws a {@link PrinterException}.
   * @param graphics the context into which the page is drawn 
   * @param pageFormat the size and orientation of the page being drawn
   * @param pageIndex the zero based index of the page to be drawn
   * @return PAGE_EXISTS if the page is rendered successfully
   *         or NO_SUCH_PAGE if <code>pageIndex</code> specifies a
   *	       non-existent page.
   * @exception java.awt.print.PrinterException
   *         thrown when the print job is terminated.
   */
  public int print(Graphics pg, java.awt.print.PageFormat pageFormat, int pageIndex)
    throws java.awt.print.PrinterException
  {
    int Rc = NO_SUCH_PAGE;

    if (0 <= pageIndex && pageIndex < maxNumPage)
    {
      pg.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
      int wPage = 0;
      int hPage = 0;

      // Are we portrait or landscape
      if (pageFormat.getOrientation() == pageFormat.PORTRAIT)
      { // Portrait
        wPage = (int) pageFormat.getImageableWidth();
        hPage = (int) pageFormat.getImageableHeight();
      }
      else
      { // LandScape
        wPage = (int) pageFormat.getImageableWidth();
        wPage += wPage / 2; // Increases the Width By Half
        hPage = (int) pageFormat.getImageableHeight();
        pg.setClip(0, 0, wPage, hPage);
      }

      // Keeps track of the current vertical position and starts the rendering
      int y = 0;
      pg.setFont(printTitleLabel.getFont());
      pg.setColor(Color.black);
      Font fn = pg.getFont();
      FontMetrics fm = pg.getFontMetrics();
      y += fm.getAscent();
      pg.drawString(printTitleLabel.getText(), 0, y);
      // Add some Space between our Title and Column Headers
      y += 20;

      // Grab the Data from the Vectors required and Obtain a copy at the same time
      HGTable table = (HGTable) tableVector.elementAt(printTableIndex);

      Font headerFont = table.getFont().deriveFont(Font.BOLD);
      pg.setColor(Color.blue);
      pg.setFont(headerFont);
      fm = pg.getFontMetrics();

      TableColumnModel colModel = table.getColumnModel();
      int columns = colModel.getColumnCount();
      int[] x = new int[columns];
      x[0] = 0;

      int h = fm.getAscent();
      // add ascent of the header font because of the
      // baseline positioning
      y += h;

      // Draws All the Column Headers that will fit in the Page Width
      int nRow = 0;
      int nCol = 0;
      for (nCol = 0; nCol < columns; nCol++)
      {
        TableColumn tk = colModel.getColumn(nCol);
        int width = tk.getWidth();
        if (x[nCol] + width > wPage)
        {
          columns = nCol;
          break;
        }

        if (nCol < columns)
        {
          x[nCol + 1] = x[nCol] + width;
        }
        String title = (String) tk.getIdentifier();
        pg.drawString(title, x[nCol], y);
      }

      pg.setFont(table.getFont());
      fm = pg.getFontMetrics();

      // After Headers: Figure How many Body Rows will fit on the Page
      int header = y;
      h = fm.getHeight();
      int rowH = Math.max((int) (h * 1.5), 10);
      int rowPerPage = (int) ((hPage - header) / rowH);
      maxNumPage = Math.max((int) Math.ceil(table.getRowCount() / (double) rowPerPage), 1);

      // Prints the Rows Alloted to this Page
      StockDataTableModel model = (StockDataTableModel) modelVector.elementAt(printTableIndex);
      int iniRow = pageIndex * rowPerPage; // Initial Row
      int endRow = Math.min(table.getRowCount(), iniRow + rowPerPage); // End Row

      for (nRow = iniRow; nRow < endRow; nRow++)
      {
        y += h;
        for (nCol = 0; nCol < columns; nCol++)
        {
          int col = table.getColumnModel().getColumn(nCol).getModelIndex();
          Object obj = model.getValueAt(nRow, nCol);
          String objSz = obj.toString();
          pg.setColor(Color.black);
          if (obj instanceof com.hgutil.data.ColoredData)
          {
            pg.setColor(((com.hgutil.data.ColoredData) obj).getFGColor());
          }

          pg.drawString(objSz, x[nCol], y);

        }
      }

      System.gc();
      Rc = PAGE_EXISTS;
    }

    return Rc;
  }
  /**
   * Method to print a WatchList in the list
   * @param index The index of the Tab being printed
   */
  private void printWatchList(int index)
  {

    if (0 > index || index >= tabPane.getTabCount())
    {
      debug("printWatchList(" + index + ") - Invalid Index - resetting to 0: ");
      index = 0;
      return;
    }
    // Stop all AWT threads from doing work
    shutdownWatchListTimers();
    // Try our Print Job
    try
    {
      // Set Up Our Title
      GregorianCalendar now = new GregorianCalendar();
      String tabName = getString("WatchListTableModule.edit.watch_list_basic_name") + index;
      String titleText =
        "Stock Quotes on "
          + ParseData.format(now.getTime(), "MMM-dd-yyyy")
          + " WatchList => "
          + appProps.getProperty(tabName, tabName);
      printTitleLabel = new JLabel(titleText);

      // Setup our layout to Landscape
      PageFormat pageFormat = new PageFormat();
      pageFormat.setOrientation(PageFormat.LANDSCAPE);
      // Get a Handle on the PrnterJob
      PrinterJob prnJob = PrinterJob.getPrinterJob();
      // Notify the Printer Job we have a Printable Object - in Landscape Mode
      if (true)
      {
        prnJob.setPrintable(this);
      }
      else
      {
        prnJob.setPrintable(this, pageFormat);
      }

      if (!prnJob.printDialog())
      {
        return;
      }

      // Print the Object
      printTableIndex = index;
      maxNumPage = 1;
      prnJob.print();
    }
    catch (PrinterException exc)
    {
      debug("printWatchList(" + index + ") - Printer Exception caught: " + exc);
    }
    finally
    {
      // Request Focus on our tab pane
      tabPane.requestFocus();
      // Set our selected index to the first tab
      tabPane.setSelectedIndex(index);
      printTableIndex = -1;
      printTitleLabel = null;

    }
  }
  /**
   * Method to maintain Persistance of all Vector Model to their associated filenames
   * @param index integer representation of the vector we want to save
   */
  private synchronized void saveAllVectorData()
  {
    debug("saveAllVectorData() - to File");

    // Delete all extra data files we have floating out in the directory
    expungeAllWatchListsFiles();

    // Get the count of the Tabs available
    int watchListCount = tabPane.getTabCount();

    // Save each One of them
    for (int index = 0; index < watchListCount; index++)
    {
      // Save our individual Data Vector
      saveVectorData(index);

    }
    // Save off the Count of the WatchLists available
    appProps.setProperty(getString("WatchListTableModule.application_watchlist_count_key_title"), "" + watchListCount);
    appProps.saveProperties(getString("WatchListTableModule.application_ini_header_title"));

    debug("saveAllVectorData() - to File complete");
  }
  /**
   * Method to maintain Persistance of a Vector Model to a filename
   * @param index integer representation of the vector we want to save
   */
  private synchronized void saveVectorData(int index)
  {

    debug("saveVectorData(" + index + ") - to File");
    if (index >= modelVector.size())
    {
      debug("saveVectorData(" + index + ") - Vector size is the same or greater");
      return;
    }

    StockDataTableModel model = (StockDataTableModel) modelVector.elementAt(index);

    // Disable any use of the TabPane index during the delete operation
    // This is a preventative measure in the event that a mouse click
    // should ocurr during the deletion and saving process.
    tabPane.setEnabledAt(index, false);
    // Save our individual Data Vector
    saveVectorData(model.getData(), index);

    // Create a filename to save the Data 
    String fileName = getString("WatchListTableModule.edit.watch_list_basic_name") + index;
    // Get the WatchList title
    String tabName = tabPane.getTitleAt(index);
    // Save the The Tab Name to the Properties
    appProps.setProperty(fileName, tabName);
    // Allow user interaction on tab once again
    tabPane.setEnabledAt(index, true);

    debug("saveVectorData(" + index + ") - to File");
  }
  /**
   * Method to maintain Persistance of a Vector Model to a filename
   * @param index integer representation of the vector we want to save
   */
  private synchronized void saveVectorData(Vector data, int index)
  {
    debug("saveVectorData(Vector, " + index + ") - to File");

    // Create a filename to save the Data 
    String fileName = getString("WatchListTableModule.edit.watch_list_basic_name") + index;

    // Now we can send it to be serialized
    StockMarketUtils.serializeData(data, fileName);

    debug("saveVectorData(Vector, " + index + ") - to File complete");
  }
  /**
   * Method addSymbolToCurrentWatchList. This method allows for adding a 
   * new stock Item just after the selected item selected row, on the current
   * selected table, it will insert it into the table, set the Symbol
   * and sends a notification to download historic data for the new symbol.
   * @param symbol A String representing the stock data to add to the table
   */
  private StockData addSymbolToCurrentWatchList(String symbol)
  {
    debug("addSymbolToCurrentWatchList(" + symbol + ")");
    // Get the index into the current selected Tabpane
    int index = tabPane.getSelectedIndex();
    // Now grab our Table and Models
    HGTable table = (HGTable) tableVector.elementAt(index);
    StockDataTableModel dataModel = (StockDataTableModel) modelVector.elementAt(index);
    // Get the current Selected Row.
    int row = ((table.getSelectedRow() >= 0) ? table.getSelectedRow() : 0);

    debug("Insert Row After row [" + row + "] The Row being inserted is " + (row + 1));
    dataModel.insert(row + 1);
    // Fire a notification that we are changing the table
    table.tableChanged(
      new TableModelEvent(dataModel, row + 1, row + 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    // Set the row to be selected
    table.setRowSelectionInterval(row, row);
    table.repaint();
    // Now Get the StockData Obj at that row
    StockData sd = (StockData) dataModel.getData().elementAt(row + 1);
    // Now we have the Object - We still Need to pre-fill in the data
    sd.setSymbol(symbol, 0.00);
    // Set the Name as we are still Searching
    sd.setName("Search for " + symbol);
    // Lets Load the Historic Data.
    this.loadHistoricData(sd);
    // We are done now.
    debug("addSymbolToCurrentWatchList(" + symbol + ") - complete");

    return sd;
  }
  /**
   * Method to set a Object if it is a instance of the StockModuleDataExchange
   * @param dataObj java.lang.Object
   */
  public void setDataObject(Object dataObj)
  {

    debug("setDataObject() - Set Data member Obj");
    // Null out any reference to this data that may be present
    stockData = null;
    if (dataObj != null)
    {
      if (dataObj instanceof StockData)
      {
        stockData = (StockData) dataObj;
      }
      else if (dataObj instanceof String)
      {
        stockData = addSymbolToCurrentWatchList((String) dataObj);
      }

    }

    debug("setDataObject() - stockData is [" + ((stockData == null) ? "null" : stockData.getName()) + "]");
  }
  /**
   * This method provides a means for Change the name of the 
   * WatchList current being viewed
   * @param index The Index of the Tab selected to modify
   */
  private void setNewTitle(int index)
  {
    debug("setNewTitle(" + index + ") - on Tab Panel");
    // Get a copy of the Old Title
    String oldTitle = tabPane.getTitleAt(index);
    String newTitle = null;

    // Now we want to present a Dialog box to get a new title
    newTitle =
      JOptionPane.showInputDialog(
        this.getModulePanel(),
        "Please enter a new title for the list,\nCurrent name is [" + oldTitle + "]",
        "Name Watch List",
        JOptionPane.INFORMATION_MESSAGE);

    // If the Dialog box returned null or empy string then our new title will
    // the existing tab title
    if (newTitle == null || "".equals(newTitle.trim()))
    {
      newTitle = oldTitle;
    }

    // Build a defailt label for the TabPane
    String watchListName = getString("WatchListTableModule.edit.watch_list_basic_name") + index;

    // Set the Propery file to reflect the new title
    appProps.setProperty(watchListName, newTitle);

    // set the Name of the title for the Tab 
    tabPane.setTitleAt(index, newTitle);

    debug("setNewTitle(" + index + ") - on Tab Panel to [" + newTitle + "] - complete");
  }
  /**
   * Method to set the Shared Manu Item String
   */
  private void setSharedMenuItemStrings()
  {
    debug("setSharedMenuItemStrings() - assigning string values");
    // Build a Menu and Menuitems for Inserting and Deleting Watch Lists
    addNewWatchListCmd =
      (addNewWatchListCmd == null) ? getString("WatchListTableModule.edit_menu.insert_watchlist_text") : addNewWatchListCmd;
    deleteWatchListCmd =
      (deleteWatchListCmd == null) ? getString("WatchListTableModule.edit_menu.delete_watchlist_text") : deleteWatchListCmd;
    // Build a Menu and Menuitems for Inserting and Deleting Rows
    insertBeforeCmd =
      (insertBeforeCmd == null) ? getString("WatchListTableModule.edit_menu.insert_row_before_text") : insertBeforeCmd;
    insertAfterCmd =
      (insertAfterCmd == null) ? getString("WatchListTableModule.edit_menu.insert_row_after_text") : insertAfterCmd;
    deleteRowCmd = (deleteRowCmd == null) ? getString("WatchListTableModule.edit_menu.delete_row_text") : deleteRowCmd;

    // Fraction or Decimal Display
    fractionCmd = (fractionCmd == null) ? getString("WatchListTableModule.edit_menu.fractions_actionCommand") : fractionCmd;
    decimalCmd = (decimalCmd == null) ? getString("WatchListTableModule.edit_menu.decimals_actionCommand") : decimalCmd;

    // Rename the Watch List Command
    renameListCmd =
      (renameListCmd == null) ? getString("WatchListTableModule.edit_menu.rename_watchlist_text") : renameListCmd;

    // Build a Menu Item to print the WatchList
    printListCmd = (printListCmd == null) ? getString("WatchListTableModule.edit_menu.print_watchlist_text") : printListCmd;
    // Build a Menu Item for displaying a Table Property Editor
    tableProps = (tableProps == null) ? getString("WatchListTableModule.edit_menu.view_table_properties") : tableProps;

    viewOptionChainCmd =
      (viewOptionChainCmd == null) ? getString("WatchListTableModule.edit_menu.view_option_chain") : viewOptionChainCmd;

    viewHistoricChartCmd =
      (viewHistoricChartCmd == null) ? getString("WatchListTableModule.edit_menu.view_historic_chart") : viewHistoricChartCmd;

    // Make a new Action Trigger, as it is generic and used in many places.
    if (actionTrigger == null)
    {
      actionTrigger = new ActionTrigger();
    }
    debug("setSharedMenuItemStrings() - complete");
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
   * This method provides a means for changing table properties
   * for each individual table - It self determines which tab is currently
   * selected and present the Properties dialog for the current table
   */
  private void showTableControl()
  {
    debug("showTableControl() ");
    int index = tabPane.getSelectedIndex();
    JTable table = (JTable) tableVector.elementAt(index);
    this.setStatusBar("Showing table control panel for table[" + index + "]");
    HGTableControlPanel.showTableControlsDialog(frame, table, false);
    debug("showTableControl() - complete");
  }
  /**
   * Method to shutdown an application timers and perform any cleanup on the 
   * timers required by this module
   */
  private void shutdownWatchListTimers()
  {
    debug("shutdownWatchListTimers() all timers");
    // Null our our parser, It is not needed now.
    if (populateListVector == null)
    {
      return;
    }
    // Stop All of our timers.
    for (int i = 0; i < populateListVector.size(); i++)
    {
      PopulateWatchListTask task = (PopulateWatchListTask) populateListVector.elementAt(i);
      task.cancel();
      this.setStatusBar("WatchList [" + tabPane.getTitleAt(i) + "] - Stopped.");
      debug("WatchList [" + tabPane.getTitleAt(i) + "] - Stopped.");
    }
    // Clear all objects from the Timer List
    populateListVector.removeAllElements();
    populateListVector = null;
    // Signal the Garbage Collector to reclaim anything it may see neccessary
    System.gc();
    debug("shutdownWatchListTimers() all timers - complete");
  }
  /**
   * Method to shutdown an application and perform any cleanup neccesarry
   */
  public void shutdown()
  {
    debug("shutdown() the application module");

    // Shutdown all the Timers
    shutdownWatchListTimers();
    // Save Our WatchLists
    saveAllVectorData();
    // Our Container vectors need to be emptied and clear. 
    modelVector.removeAllElements();
    modelVector = null;

    tableVector.removeAllElements();
    tableVector = null;

    // Delete any additional Historic Data that is Serialized to disk
    expungeAllHistoricFiles();

    // Shutdown the task that monitors the update tasks
    if ( this.monitorTask != null )
    {
      this.monitorTask.cancel();
      this.monitorTask = null;
    }
    // Null out any reference to this data that may be present
    stockData = null;
    debug("shutdown() the application module - complete");

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
    startupWatchListTimers(index);
    debug("start() all timers - complete");
  }
  /**
   * Method to shutdown an application timers and perform any cleanup on the 
   * timers required by this module
   */
  private void startupWatchListTimers(int watchListWithPriority)
  {
    debug("startupWatchListTimers(" + watchListWithPriority + ")");
    if (populateListVector != null)
    {
      shutdownWatchListTimers();
    }
    // Create our new list vector, for later shutdown
    if (populateListVector == null)
    {
      populateListVector = new Vector();
    }

    for (int i = 0; i < tabPane.getTabCount(); i++)
    {
      int expire = (i == watchListWithPriority) ? standardDelay : extendedDelay;
      int initialDelay = ( i == watchListWithPriority) ? ONE_SECOND : standardDelay;
      TimerTask updateWatchList = new PopulateWatchListTask(monitorTask, i);
      populateListVector.add(updateWatchList);
      Timer timer = new Timer(true); // Deamon 
      timer.scheduleAtFixedRate(updateWatchList, initialDelay, expire);
      this.setStatusBar("WatchList [" + tabPane.getTitleAt(i) + "] - Started at [" + (expire / ONE_SECOND) + "] intervals.");
      debug("Add List for " + i + " with time " + (expire / ONE_SECOND));
    }
    debug("startupWatchListTimers(" + watchListWithPriority + ") - complete");

  }
  /**
   * Method to stop any threads if neccesary
   */
  public void stop()
  {
    debug("stop() all timers");
    // Shutdown all the Timers
    shutdownWatchListTimers();

    debug("stop() all timers - complete");
  }
}
