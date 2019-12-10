// SymbolLookupModule.java
package com.stockmarket.app;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;
import javax.swing.filechooser.*;
import javax.accessibility.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;

import com.hgmenu.HGMenuItem;
import com.hgutil.*;
import com.hgutil.data.*;
import com.hgutil.swing.event.PopupListener;
import com.stockmarket.parsers.*;

/**
 * Insert the type's description here.
 * @author Hans-Jurgen Greiner
 */
public class SymbolLookupModule extends StockMarketBaseModule implements StockModuleDataExchange
{
  private JTextField stockName = null;
  private JButton btnGo = null;
  private JButton btnClear = null;
  private JTabbedPane tabPane = null;
  private Vector tableList = null;
  // Get the icon on the selected tab
  private CancelSearchIconProxy icon = null;
  private String btnGoCommand = "";
  private String btnClearCommand = "";
  private String addToWatchListCmd = "";
  private String killSearchCmd = "";

  private String selectedStockDataName = null;

  private PopupListener popupListener = null;

  private ActionTrigger actionTrigger = new ActionTrigger();

  /**
   * This class acts as a wrapper around the "kill" icon displayed in the
   * search tabs.
   */
  private class CancelSearchIconProxy implements Icon
  {

    /**
     * the ImageIcon for our cancel image.
     */
    private Icon cancelIcon;

    /**
     * the width of the icon in pixels.
     */
    private int width;

    /**
     * the height of the icon in pixels.
     */
    private int height;

    /**
     * the x position of the icon within its tab.
     */
    private int posX;

    /**
     * the y position of the icon within its tab.
     */
    private int posY;

    /**
     * If this is a combined image, then the closing cross
     * is on the left half of the image.
     */
    private boolean combinedImage = false;

    //    /**
    //     * the constructor loads the image icon and stores the location
    //     * and dimensions.
    //     */
    //    CancelSearchIconProxy()
    //    {
    //      cancelIcon = GUIMediator.getImageResource("kill.gif");
    //      this.width = cancelIcon.getIconWidth();
    //      this.height = cancelIcon.getIconHeight();
    //      this.posX = 0;
    //      this.posY = 0;
    //    }
    /**
     * A new constructor that deals with combined images. When this 
     * constructor is called we know we are dealing with a combined image
     * @param icon The combined image. 
     */
    CancelSearchIconProxy(Icon icon)
    {
      cancelIcon = icon;
      this.width = cancelIcon.getIconWidth();
      this.height = cancelIcon.getIconHeight();
      this.posX = 0;
      this.posY = 0;
      this.combinedImage = false;
      //      this.combinedImage = true;
    }

    /**
     * implements Icon interface.
     * Gets the width of the icon.
     * @return the width in pixels of this icon
     */
    public int getIconWidth()
    {
      return this.width;
    }

    /**
    * implements Icon interface.
     * Gets the height of the icon.
    *
     * @return the height in pixels of this icon
     */
    public int getIconHeight()
    {
      return this.height;
    }

    /**
     * implements Icon interface.
     * forwards the call to the proxied Icon object and stores the
     * x and y coordinates of the icon.
     */
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
      this.posX = x;
      this.posY = y;
      cancelIcon.paintIcon(c, g, x, y);
    }

    /**
     * Determines whether or not a click at the given x, y position
     * is a "hit" on the kill search icon.
     * 
     * @param x the x location of the mouse event
     *
     * @param y the y location of the mouse event
     *
     * @return <tt>true</tt> if the mouse event occurred within the 
     *         bounding rectangle of the icon, <tt>false</tt> otherwise.
     */
    public boolean shouldKill(int x, int y)
    {
      int xMax;
      int yMax;
      if (!combinedImage)
      {
        xMax = this.posX + this.width;
        yMax = this.posY + this.height;
      }
      else
      { //combined image. Use approx half of width and height
        xMax = this.posX + 16; //the width of the closing x
        yMax = this.posY + 13; //the height of the closing x
      }
      if (!((x >= this.posX) && (x <= xMax)))
        return false;
      if (!((y >= this.posY) && (y <= yMax)))
        return false;

      return true;
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

      if (btnGoCommand.equals(cmd))
      {
        this.loadSearchThread();
      }
      else if (btnClearCommand.equals(cmd))
      {
        removeAll();
      }
      else if (addToWatchListCmd.equals(cmd))
      {
        debug("Add to WatchList command, index = [" + index + "]");
        JTable table = (JTable) tableList.elementAt(index);

        int row = table.rowAtPoint(new java.awt.Point(popupListener.getX(), popupListener.getY()));
        SymbolLookupModule.this.setDataObject(table.getValueAt(row, 1));
        SymbolLookupModule.this.invokeStockMarketModule(getString("SymbolLookupModule.view_watchlist.module_name"));
      }
      else if (killSearchCmd.equals(cmd))
      {
        removePane(index);
      }

      popupListener.resetPoints();
    }
    /**
     * Key Pressed Event will monitor for Key presses
     * @see KeyListener#keyPressed(KeyEvent)
     */
    public void keyPressed(KeyEvent e)
    {
      int keyCode = e.getKeyCode();
      if (e.VK_ENTER == keyCode)
      {
        loadSearchThread();
      }
    }

    /**
     * Method loadSearchThread. Load the Thread to search on
     */
    private void loadSearchThread()
    {
      // do the following on the gui thread
      Thread t = new LoadThread(SymbolLookupModule.this);
      t.start();
      stockName.setText("");
      stockName.requestFocus();
    }
  }

  /**
   * Class TabListener. listens to the tab events.  If they are selected
   * it makes an attempt to find the location of the button being selected.
   */
  private class TabListener implements MouseListener // extends MouseAdapter
  {
    /**
     * @see MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent evt)
    {}
    /**
     * @see MouseListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent evt)
    {}
    /**
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent evt)
    {}

    /**
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent evt)
    {}
    /**
     * Invoked when a mouse button has been released on a component.
     * Checks to see if the Icon object for the selected tab has been
     * clicked, canceling the search if it has.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent e)
    {
      int x = e.getX();
      int y = e.getY();
      if (((CancelSearchIconProxy) icon).shouldKill(x, y))
      {
        // The index of the selected tab
        int index = tabPane.getSelectedIndex();
        removePane(index);
      }
    }

  }

  /**
   * Class LoadThread.  When Indicated to start, the class
   * will fire an event to the method populate tab, as a thread, therefore
   * freeing any required processing from the user.
   */
  private class LoadThread extends Thread
  {
    SymbolLookupModule lookupApp;
    String text = null;
    int index = -1;

    /**
     * Method LoadThread. Main Constructor
     * @param lookupApp
     */
    public LoadThread(SymbolLookupModule lookupApp)
    {
      this.lookupApp = lookupApp;
      this.text = lookupApp.stockName.getText();
      index = this.lookupApp.addTab(this.text);
    }

    /**
     * Runs the Thread
     * @see Runnable#run()
     */
    public void run()
    {
      lookupApp.populateTab(index, text);
    }
  }
  /**
   * Method SymbolLookupModule. The Main Contructor
   * @param stockMarketApp
   */
  public SymbolLookupModule(StockMarketApp stockMarketApp)
  {

    super(stockMarketApp, "SymbolLookupModule", "toolbar/SymbolSearch.gif");
    getModulePanel().add(createPanel(), BorderLayout.CENTER);

  }
  /**
   * Method addTab. Add a new Tab to the Tab Pane, with the specified Text.
   * @param text The Text to display
   * @return int The index of the new Tab
   */
  public int addTab(String text)
  {

    int index = -1;
    if (tabPane == null)
    {
      return index;
    }

    Object[] arguments = { text };

    String toolTipMsg = ParseData.format(getString("SymbolLookupModule.SearchTab.search_icon_tooltip"), arguments);

    // Now add the Scroll Pane to a newly created Tab
    String title = text + " (0)";
    JLabel label = new JLabel("   Searching...", JLabel.LEFT);
    tabPane.addTab(title, icon, label, toolTipMsg);
    //    tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    // Get the index of the latest created tabPane
    index = tabPane.getTabCount() - 1;
    tabPane.repaint();
    // Return the index
    return index;
  }
  /**
   * Method createPanel.Method creates the Panel for display
   * @return JPanel The Panel Created.
   */
  public JPanel createPanel()
  {
    JPanel panel = new JPanel(new BorderLayout());

    JLabel entryLabel = new JLabel(getString("SymbolLookupModule.SearchLabel.label_text"));
    stockName = new JTextField(20);
    stockName.addKeyListener(actionTrigger);
    stockName.setToolTipText(getString("SymbolLookupModule.SearchField.search_tooltip"));
    stockName.setRequestFocusEnabled(true);

    addToWatchListCmd = getString("SymbolLookupModule.PopupMenu.add_to_watchlist_actionCommand");
    killSearchCmd = getString("SymbolLookupModule.PopupMenu.kill_search_actionCommand");

    btnGoCommand = getString("SymbolLookupModule.SearchButton.search_actionCommand");

    btnGo = new JButton(getString("SymbolLookupModule.SearchButton.search_label"));
    btnGo.setActionCommand(btnGoCommand);
    btnGo.addActionListener(actionTrigger);
    btnGo.setToolTipText(getString("SymbolLookupModule.SearchButton.search_tooltip"));

    btnClearCommand = getString("SymbolLookupModule.ClearButton.clear_actionCommand");

    btnClear = new JButton(getString("SymbolLookupModule.ClearButton.clear_label"));
    btnClear.setActionCommand(btnClearCommand);
    btnClear.addActionListener(actionTrigger);
    btnClear.setToolTipText(getString("SymbolLookupModule.ClearButton.clear_tooltip"));

    icon =
      new CancelSearchIconProxy(
        createImageIcon(
          getString("SymbolLookupModule.SearchTab.search_icon"),
          getString("SymbolLookupModule.SearchTab.search_icon_description")));

    boolean verticalPanel_3D = getBool("SymbolLookupModule.SearchTab.label_panel_3D");
    boolean horizontalPanel_3D = getBool("SymbolLookupModule.SearchTab.action_panel_3D");
    JPanel lblPanel = createVerticalPanel(verticalPanel_3D);
    JPanel actionPanel = createHorizontalPanel(horizontalPanel_3D);
    // Add	all the Horizontal elements
    actionPanel.add(stockName);
    actionPanel.add(createSpacerObject(HGAP10));
    actionPanel.add(btnGo);
    actionPanel.add(createSpacerObject(HGAP10));
    actionPanel.add(btnClear);

    // Add	all the Vertical elements
    lblPanel.add(entryLabel);
    lblPanel.add(actionPanel);

    tabPane = new JTabbedPane();
    tabPane.addMouseListener(new TabListener());

    panel.add(lblPanel, BorderLayout.NORTH);
    panel.add(tabPane, BorderLayout.CENTER);
    return panel;
  }
  /**
   * Method deletePane. Removes a specific Pane from the TabPane
   * @param index The index of the tab to remove.
   */
  public void deletePane(int index)
  {
    removePane(index);
  }
  /**
   * Method getStockNameText. Returns the Stock name from the Tab text field
   * @return String
   */
  private String getStockNameText()
  {
    return ("" + stockName.getText());
  }
  /**
   * main method allows us to run as a standalone demo.
   */
  public static void main(String[] args)
  {
    SymbolLookupModule demo = new SymbolLookupModule(null);
    demo.mainImpl();
  }
  /**
   * Method populateTab. Populates a new Tab index to update the text displayed
   * search for the text selected
   * @param index The Index of the Tab
   * @param text The text to search for
   * @return int If a Tab is found it will return the value, if the tab is not 
   * found - It will return -1
   */
  private int populateTab(int index, String text)
  {

    if ((tabPane == null) || (index < 0))
    {
      return -1;
    }
    StockSymbolParser parser = new StockSymbolParser();

    Vector header = new Vector();
    header.add("Company Name");
    header.add("Stock Symbol");
    header.add("Option symbol");

    Vector list = parser.getSymbolsForLetter(text);
    Vector data = new Vector();
    for (int i = 0; i < list.size(); i++)
    {
      StockMarketSymbols sms = (StockMarketSymbols) list.elementAt(i);
      data.add(sms.symbolDataVector());
    }

    String title = text + " (" + data.size() + ")";

    boolean found = false;
    for (int i = 0; i < tabPane.getTabCount() && !found; i++)
    {
      String titleText = tabPane.getTitleAt(i);
      found = titleText.endsWith(text + " (0)");
      index = (found) ? i : -i;
    }
    if (!found)
    {
      return -1;
    }

    try
    {
      // Create a list with the title and the size of the list
      Object[] arguments = { text, new Integer(data.size())};

      String toolTipMsg = ParseData.format(getString("SymbolLookupModule.SearchTab.search_icon_tooltip_update"), arguments);
      tabPane.setToolTipTextAt(index, toolTipMsg);
      // Reset the Title on the Tab.
      tabPane.setTitleAt(index, title);

      JTable table = new JTable(data, header);
      table.setColumnSelectionAllowed(false);
      table.setRowSelectionAllowed(true);
      table.getTableHeader().setReorderingAllowed(false);
      table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      addPopupMenuToTable(table);

      // Create a Scroll Pane
      JScrollPane scrollPane = new JScrollPane(table);
      // Set our Scroll Bar Policy
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrollPane.getViewport().revalidate();

      tabPane.setComponentAt(index, scrollPane);
      scrollPane.getViewport().revalidate();

      if (tableList == null)
      {
        tableList = new Vector();
        tableList.removeAllElements();
      }

      tableList.add(table);
    }
    catch (IndexOutOfBoundsException exc)
    {
      return -1;
    }
    finally
    {
      tabPane.repaint();
    }
    return (index);
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
   * Creates a popup edit menu for the table
   */
  protected JPopupMenu createPopupMenu()
  {

    // Add  all the Horizontal elements
    JPopupMenu result = null;

    result = HGMenuItem.makePopupMenu(new Object[] { addToWatchListCmd, null, killSearchCmd, null }, actionTrigger);

    // Set the Alignment and return the MenuBar
    result.setAlignmentX(JMenuBar.LEFT_ALIGNMENT);
    return result;
  }
  /**
   * Method to invoke another Module and leave this one.
   * @param resourceName java.lang.String
   */
  private void invokeStockMarketModule(String resourceName)
  {

    debug("invokeStockMarketModule(" + resourceName + ") - preparing to change");
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
   * Method removeAll. Removes all Tabs displayed on the Tab Pane
   */
  public void removeAll()
  {
    if (tabPane != null)
    {
      tabPane.removeAll();
      tabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
      tabPane.repaint();
      System.gc();
    }

  }
  /**
   * Method to return a Object if it is a instance of the StockModuleDataExchange
   * @return java.lang.Object
   */
  public Object getDataObject()
  {
    return selectedStockDataName;
  }
  /**
   * Method to set a Object if it is a instance of the StockModuleDataExchange
   * @param dataObj java.lang.Object
   */
  public void setDataObject(Object dataObj)
  {

    debug("setDataObject() - Set Data member Obj");
    // Null out any reference to this data that may be present
    if (dataObj != null && dataObj instanceof String)
    {
      selectedStockDataName = (String) dataObj;
    }

    debug("setDataObject() - stockData is [" + selectedStockDataName + "]");
  }
  /**
   * Method removePane. Removes a pane specified by the index parameter
   * @param index The index of the Selected Tab Pane
   */
  public void removePane(int index)
  {
    if (tabPane != null)
    {
      if (0 <= index && index < tabPane.getTabCount())
      {
        tabPane.removeTabAt(index);
        tabPane.repaint();
      }
      System.gc();
    }
  }
  /**
   * Method to shutdown an application and perform any cleanup neccesarry
   */
  public void shutdown()
  {
    if ( tableList != null )
    {
      tableList.removeAllElements();
      tableList = null;
    }
    
    if( tabPane != null )
    {
      tabPane.removeAll();
      tabPane = null;
    }

  }
  /**
   * Method to startup any threads if neccesary
   */
  public void start()
  {}
  /**
   * Method to stop any threads if neccesary
   */
  public void stop()
  {}

}
