// StockMarketApp.java
package com.stockmarket.app;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.hgmenu.HGMenuItem;
import com.hgmenu.HGMenuListItem;
import com.hgutil.AppProperties;
import com.hgutil.ParseData;
import com.hgutil.datarenderer.HGTableColorModel;
import com.hgutil.swing.themes.*;

/**
 * Insert the type's description here.
 * @author Hans-Jurgen Greiner
 */
public class StockMarketApp extends JPanel
{

  Vector moduleList = null;
  // Possible Look & Feels
  private static final String mac = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
  private static final String metal = "javax.swing.plaf.metal.MetalLookAndFeel";
  private static final String motif = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
  private static final String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

  protected static final DefaultMetalTheme defaultStartupTheme = new ObsidianTheme();
  ;
  // The current Look & Feel
  private static String currentLookAndFeel = metal;
  // Menus
  private JMenuBar menuBar = null;
  private JMenu fileMenu = null;
  private JMenu plafMenu = null;
  private JMenu themesMenu = null;
  private JMenu audioMenu = null;
  private JMenu toolTipMenu = null;
  private ButtonGroup plafMenuGroup = new ButtonGroup();
  private ButtonGroup themesMenuGroup = new ButtonGroup();
  private ButtonGroup audioMenuGroup = new ButtonGroup();
  private ButtonGroup toolTipMenuGroup = new ButtonGroup();

  // List of demos
  private Vector demosVector = new Vector();

  // The preferred size of the demo
  private static final int PREFERRED_WIDTH = 720;
  private static final int PREFERRED_HEIGHT = 640;

  // Box spacers
  private Dimension HGAP = new Dimension(1, 5);
  private Dimension VGAP = new Dimension(5, 1);

  // Resource bundle for internationalized and accessible text
  private ResourceBundle bundle = null;
  private AppProperties appProps = null;

  // A place to hold on to the visible demo
  private StockMarketBaseModule currentDemo = null;

  // About Box
  private JDialog aboutBox = null;

  // Status Bar
  private JTextField statusField = null;

  // Tool Bar
  private ToggleButtonToolBar toolbar = null;
  private ButtonGroup toolbarGroup = new ButtonGroup();

  // Used only if swingset is an application 
  private JFrame frame = null;
  private JWindow splashScreen = null;

  // Used only if swingset is an applet 
  private JApplet stockMarketApplet = null;
  private JPanel demoPanel = null;

  // To debug or not to debug, that is the question
  private boolean DEBUG = ParseData.parseBool(System.getProperties().getProperty("HGDEBUG"), false);
  private int debugCounter = 0;

  // The tab pane that holds the demo
  private JTabbedPane tabbedPane = null;

  private JLabel splashLabel = null;

  // contentPane cache, saved from the applet or application frame
  private Container contentPane = null;

  // *******************************************************
  // **************   ToggleButtonToolbar  *****************
  // *******************************************************
  static final String ACTION_COMMAND = "ACTION_COMMAND";
  static Insets zeroInsets = new Insets(1, 1, 1, 1);
  protected class ToggleButtonToolBar extends JToolBar
  {
    /**
     * Class Constructor
     * @see Object#Object()
     */
    public ToggleButtonToolBar()
    {
      super();
    }

    /**
     * Method addToggleButton. Adds a action to the button bar
     * @param a
     * @return JToggleButton
     */
    JToggleButton addToggleButton(Action a)
    {
      JToggleButton tb = new JToggleButton((String) a.getValue(Action.NAME), (Icon) a.getValue(Action.SMALL_ICON));
      tb.setMargin(zeroInsets);
      tb.setText(null);
      tb.setEnabled(a.isEnabled());
      tb.setToolTipText((String) a.getValue(Action.SHORT_DESCRIPTION));
      tb.setActionCommand((String) a.getValue(ACTION_COMMAND));
      tb.addActionListener(a);
      tb.setAction(a);

      add(tb);
      return tb;
    }
  }
  /**
   * Generic StockMarketApp runnable. This is intended to run on the
   * AWT gui event thread so as not to muck things up by doing
   * gui work off the gui thread. Accepts a StockMarketApp and an Object
   * as arguments, which gives subtypes of this class the two
   * "must haves" needed in most runnables for this demo.
   */
  private class StockAppRunnable implements Runnable
  {
    protected StockMarketApp stockMarketApp;
    protected Object obj;

    /**
     * Method StockAppRunnable. contructor
     * @param stockMarketApp
     * @param obj
     */
    public StockAppRunnable(StockMarketApp stockMarketApp, Object obj)
    {
      this.stockMarketApp = stockMarketApp;
      this.obj = obj;
    }

    /**
     * Do Nothing
     * @see Runnable#run()
     */
    public void run()
    {}
  }

  ////////////////////////////////////////////////////////////
  // Inner Class ActionTrigger
  ////////////////////////////////////////////////////////////
  private ActionTrigger actionTrigger = new ActionTrigger();
  private class ActionTrigger implements ActionListener
  {

    /**
     * Method listens for a generic list of options that are possible from
     * the application
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent evt)
    {

      String cmd = evt.getActionCommand();

      if ("About".equals(cmd))
      { // Reorder Allowed
      }
      else if ("Options".equals(cmd))
      { // Bring Up the Options DialogBox
        PropertyConfigDialogBox.showHistoricRequestDialog(appProps, getFrame());
      }
      else if ("Exit".equals(cmd))
      { // Exit the Application
        StockMarketApp.this.shutdown();
      }

    }
  }
  // *******************************************************
  // ********************   Actions  ***********************
  // *******************************************************

  public class SwitchToDemoAction extends AbstractAction
  {
    StockMarketApp stockMarketApp;
    StockMarketBaseModule demo;

    /**
     * Method SwitchToDemoAction. Class Constructor
     * @param stockMarketApp
     * @param demo
     */
    public SwitchToDemoAction(StockMarketApp stockMarketApp, StockMarketBaseModule demo)
    {
      super(demo.getName(), demo.getIcon());
      this.stockMarketApp = stockMarketApp;
      this.demo = demo;
    }

    /**
     * Method that will switch Modules when an action is selected.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
      debug(" SwitchToDemoAction::actionPerformed() - Switching Module");
      stockMarketApp.setCurrentModule(demo);
    }
  }

  private class ChangeLookAndFeelAction extends AbstractAction
  {
    StockMarketApp stockMarketApp;
    String plaf;
    /**
     * Method ChangeLookAndFeelAction. Class Contructor
     * @param stockMarketApp
     * @param plaf
     */
    protected ChangeLookAndFeelAction(StockMarketApp stockMarketApp, String plaf)
    {
      super("ChangeL&F");
      this.stockMarketApp = stockMarketApp;
      this.plaf = plaf;
    }

    /**
     * Method listens for a change Look and Feel action
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
      stockMarketApp.setLookAndFeel(plaf);
    }
  }

  private class ChangeThemeAction extends AbstractAction
  {
    StockMarketApp stockMarketApp;
    DefaultMetalTheme theme;
    /**
     * Method ChangeThemeAction. Class Contructor
     * @param stockMarketApp
     * @param theme
     */
    protected ChangeThemeAction(StockMarketApp stockMarketApp, DefaultMetalTheme theme)
    {
      super(theme.getName());
      this.stockMarketApp = stockMarketApp;
      this.theme = theme;
    }

    /**
     * Method listens for a change theme action to take place
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
      MetalLookAndFeel.setCurrentTheme(theme);
      HGTableColorModel.getInstance().setTheme(theme);
      stockMarketApp.updateLookAndFeel();
    }
  }
  // Turns on or off the tool tips for the demo.
  private class ToolTipAction extends AbstractAction
  {
    StockMarketApp stockMarketApp;
    boolean status;
    /**
     * Method ToolTipAction. ToolTip Event Contructor
     * @param stockMarketApp
     * @param status
     */
    protected ToolTipAction(StockMarketApp stockMarketApp, boolean status)
    {
      super("ToolTip Control");
      this.stockMarketApp = stockMarketApp;
      this.status = status;
    }

    /**
     * Listens for actions to be be performed by the request to turn on and off
     * ToolTips
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
      ToolTipManager.sharedInstance().setEnabled(status);
    }
  }
  // *******************************************************
  // *********  ToolBar Panel / Docking Listener ***********
  // *******************************************************
  private class ToolBarPanel extends JPanel implements ContainerListener
  {

    /**
     * Verifies if point x,y are on the Image.
     * @see Component#contains(int, int)
     */
    public boolean contains(int x, int y)
    {
      Component c = getParent();
      if (c != null)
      {
        Rectangle r = c.getBounds();
        return (x >= 0) && (x < r.width) && (y >= 0) && (y < r.height);
      }
      else
      {
        return super.contains(x, y);
      }
    }

    /**
     * A Component has been 
     * @see ContainerListener#componentAdded(ContainerEvent)
     */
    public void componentAdded(ContainerEvent e)
    {
      Container c = e.getContainer().getParent();
      if (c != null)
      {
        c.getParent().validate();
        c.getParent().repaint();
      }
    }

    /**
     * A Component has been Removed.
     * @see ContainerListener#componentRemoved(ContainerEvent)
     */
    public void componentRemoved(ContainerEvent e)
    {
      Container c = e.getContainer().getParent();
      if (c != null)
      {
        c.getParent().validate();
        c.getParent().repaint();
      }
    }
  }

  StockMarketBaseModule currentTabDemo = null;
  private class TabListener implements ChangeListener
  {
    /**
     * Lister for the TabPane on which Tab is selected.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {

      SingleSelectionModel model = (SingleSelectionModel) e.getSource();
      boolean srcSelected = model.getSelectedIndex() == 1;
      debug("Main App, TabListener Called");

      if (currentTabDemo != currentDemo && srcSelected)
      {
        repaint();
      }

      if (currentTabDemo != currentDemo && srcSelected)
      {
        currentTabDemo = currentDemo;
      }
    }
  }

  private class DemoLoadThread extends Thread
  {
    StockMarketApp stockMarketApp;

    /**
     * Method DemoLoadThread.Class Constructor
     * @param stockMarketApp
     */
    public DemoLoadThread(StockMarketApp stockMarketApp)
    {
      this.stockMarketApp = stockMarketApp;
    }

    /**
     * Loads a Module specified.
     * @see Runnable#run()
     */
    public void run()
    {
      stockMarketApp.loadModules();
    }
  }

  /**
   * Inner class TimeMarketOpenClose.  This classes performs one to task to
   * update the Title bar with the standard title. and the Market, Open Close
   * signals as well as the time remaining.  Normally during the day we want the
   * timer ticked down in hours and minutes.  However, the object performs a 
   * special task, where it will cancel itself and respawn a new Task for 
   * One Second intervals if the remaining Time is under 10 seconds.
   * (Workbench>Preferences>Java>Templates)
   */
  public class TimeMarketOpenClose extends TimerTask
  {
    // New York Greenich Mean Time
    private TimeZone tz = TimeZone.getTimeZone("GMT-5");
    private Calendar close = GregorianCalendar.getInstance(tz);
    private Calendar open = GregorianCalendar.getInstance(tz);
    private int openHour = 8;
    private int openMinute = 30;
    private int openSeconds = 01;

    private int closeHour = 16;
    private int closeMinute = 30;
    private int closeSeconds = 0;

    private String mainTitle = StockMarketApp.this.getString("Frame.title");
    private boolean accelDelay = false;

    // String to be displayed in next update
    private String countString;

    private final int ONE_SECOND = 1000;
    /**
     * Constructor for TimeMarketOpenClose.
     */
    public TimeMarketOpenClose()
    {
      super();
      initOpenClose();

    }
    /**
     * Constructor for TimeMarketOpenClose.
     */
    private TimeMarketOpenClose(boolean setAccelerated)
    {
      super();
      initOpenClose();
      accelDelay = setAccelerated;
    }

    /**
     * Method initOpenClose.
     */
    private void initOpenClose()
    {
      open = GregorianCalendar.getInstance(tz);
      open.set(Calendar.HOUR_OF_DAY, openHour);
      open.set(Calendar.MINUTE, openMinute);
      open.set(Calendar.SECOND, openSeconds);

      close = GregorianCalendar.getInstance(tz);
      close.set(Calendar.HOUR_OF_DAY, closeHour);
      close.set(Calendar.MINUTE, closeMinute);
      close.set(Calendar.SECOND, closeSeconds);
    }

    /**
     * @see Runnable#run()
     */
    public void run()
    {
      Calendar calendar = GregorianCalendar.getInstance(tz);
      if (open.get(Calendar.DAY_OF_MONTH) != calendar.get(Calendar.DAY_OF_MONTH))
      {
        // Re-init the Open Close Date we have changed days
        initOpenClose();
      }
      if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
        || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY))
      {
        countString = "Market CLOSED";
      }
      else if (open.getTime().after(calendar.getTime()))
      {
        countString = "Market Opens ";
        processCountDown(open.getTime(), calendar.getTime());
      }
      else if (close.getTime().after(calendar.getTime()))
      {
        countString = "Market Closes ";
        processCountDown(close.getTime(), calendar.getTime());
      }
      else
      {
        countString = "Market CLOSED";
        if (accelDelay)
        {
          reschedule(false);
        }
      }
      StockMarketApp.this.getFrame().setTitle(this.mainTitle + "     " + countString);

    }
    /**
     *  If we are a count down Timer, and Hors and Minutes
     * are and we are accelerated then slow it down.
     * NOTE: This will occurr when we count down from pre-open of
     * the market, and the Market Close is being tracked.  Here we want
     * to slow it down again.
     * @param accelerate boolean falue if we are accelerating
     */
    private void reschedule(boolean accelerate)
    {
      int delay = (accelerate) ? ONE_SECOND : (60 * ONE_SECOND);
      this.cancel();
      TimerTask newClock = new TimeMarketOpenClose(accelerate);
      Timer timer = new Timer(true);
      timer.schedule(newClock, ONE_SECOND, delay);
    }
    /**
     * Method processCountDown.
     * @param targetDate
     * @param currentDate
     * @param sign
     */
    private void processCountDown(java.util.Date targetDate, java.util.Date currentDate)
    {
      // Calculate difference in dates
      long numericalDifference = targetDate.getTime() - currentDate.getTime();

      // Divide by 1000 to find number of seconds difference
      numericalDifference = numericalDifference / 1000;

      // Get seconds
      int seconds = (int) numericalDifference % 60;

      // Get minutes
      numericalDifference = numericalDifference / 60;
      int minutes = (int) numericalDifference % 60;

      // Get hours
      numericalDifference = numericalDifference / 60;
      int hours = (int) numericalDifference % 24;

      // Get days
      numericalDifference = numericalDifference / 24;
      int days = (int) numericalDifference % 365;

      // Get years
      numericalDifference = numericalDifference / 365;
      int years = (int) numericalDifference;

      // Generate count string
      countString += "T-";

      // Check to see if year will be displayed
      if (years > 1)
      {
        countString = countString + years + " years, ";
      }
      else if (years == 1)
      {
        countString = countString + years + " year, ";
      }

      // Check to see if days will be displayed
      if (days > 1)
      {
        countString = countString + days + " days, ";
      }
      else if (days == 1)
      {
        countString = countString + days + " day, ";
      }

      // Check to see if hours will be displayed
      if (hours >= 1)
      {
        countString = countString + hours + "h:";
      }

      // Check to see if minutes will be displayed
      if (minutes >= 1)
      {
        countString = countString + minutes + "m:";
      }
      // Check to see if seconds will be displayed
      if ((seconds >= 1) && (hours < 1) && (minutes <= 10))
      {
        countString = countString + seconds + "s";
      }

      if ((hours < 1) && (minutes <= 10) && !accelDelay)
      {
        reschedule(true);
      }
      else if ((hours > 0) && (minutes >= 10) && accelDelay)
      {
        reschedule(false);
      }
    }
  }
  /**
   * StockMarketApp constructor comment.
   */
  public StockMarketApp()
  {
    this(null);
  }
  /**
   * StockMarketApp constructor comment.
   */
  public StockMarketApp(JApplet applet)
  {
    super();

    debug("StockMarketAPP():: Starting");

    this.stockMarketApplet = applet;

    if (!isApplet())
    {
      frame = createFrame();
    }

    // setLayout(new BorderLayout());
    setLayout(new BorderLayout());

    // set the preferred size of the demo
    setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
    // Create and throw the splash screen up. Since this will
    // physically throw bits on the screen, we need to do this
    // on the GUI thread using invokeLater.
    createSplashScreen();

    // do the following on the gui thread
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        showSplashScreen();
      }
    });

    // NOTE:  Load Any Preliminary Stuff Here
    initializeDemo();
    preloadFirstDemo();

    // Then we can kick it off
    // Show the demo and take down the splash screen. Note that
    // we again must do this on the GUI thread using invokeLater.
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        showStockMarketApp();
        hideSplash();
      }
    });

    // Start loading the rest of the demo in the background
    // We are doing this on the AWT Thread, whichj in turn
    // will load a new thread on to the GUI Thread for each
    // indivdual Module
    DemoLoadThread demoLoader = new DemoLoadThread(this);
    demoLoader.start();

  }
  /**
   * Add a demo Module to the toolbar - The Method load the Image to the ToolBar and sets the tooltips
   * @param demo The StockMarket Module to load
   */
  public void addModule(StockMarketBaseModule demo)
  {
    demosVector.addElement(demo);
    // do the following on the gui thread
    SwingUtilities.invokeLater(new StockAppRunnable(this, demo)
    {
      public void run()
      {
        SwitchToDemoAction action = new SwitchToDemoAction(stockMarketApp, (StockMarketBaseModule) obj);
        JToggleButton tb = stockMarketApp.getToolBar().addToggleButton(action);
        stockMarketApp.getToolBarGroup().add(tb);
        if (stockMarketApp.getToolBarGroup().getSelection() == null)
        {
          tb.setSelected(true);
        }
        tb.setText(null);
        tb.setToolTipText(((StockMarketBaseModule) obj).getToolTip());

        if (((String) moduleList.elementAt(moduleList.size() - 1)).equals(obj.getClass().getName()))
        {
          setStatus("");
        }

      }
    });
  }
  /**
   * Create a frame to reside in if brought up
   * as an application.
   * @return JFrame
   */
  public JFrame createFrame()
  {
    JFrame frame = new JFrame();

    final StockMarketApp mainApp = StockMarketApp.this;

    WindowListener l = new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        mainApp.shutdown();
      }
    };
    frame.addWindowListener(l);
    return frame;
  }
  /**
   * Create a frame to reside in if brought up
   * as an application.
   * @return JFrame
   */
  public JFrame createFrame(GraphicsConfiguration gc)
  {
    JFrame frame = new JFrame();
    final StockMarketApp mainApp = StockMarketApp.this;

    //JFrame frame = new JFrame(gc);
    //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    WindowListener l = new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        mainApp.shutdown();
        System.exit(0);
      }
    };
    frame.addWindowListener(l);
    return frame;
  }
  /**
   * Creates an icon from an image contained in the "images" directory.
   * @return ImageIcon
   */
  public ImageIcon createImageIcon(String filename, String description)
  {
    String path = "/resources/images/" + filename;
    debug("createImageIcon() - ATTEMPTING load image " + path);
    ImageIcon ii = new ImageIcon(getClass().getResource(path));
    debug("createImageIcon() - Image is " + ii);
    return ii;
  }
  /**
   * Create menus
   * @return JMenuBar
   */
  public JMenuBar createMenus()
  {

    debug("createMenus() - Creating menus...");
    fileMenu = HGMenuItem.makeMenu("File", 'F', new Object[] { "About", "Options", null, "Exit" }, actionTrigger);

    // Create four individual check button menu items for L&F, and add
    // to the button group
    HGMenuItem motifRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("PlafMenu.motif_label"),
        getString("PlafMenu.motif_actionCommand"),
        null,
        KeyEvent.VK_M,
        InputEvent.CTRL_MASK,
        plafMenuGroup,
        ParseData.parseBool(getString("PlafMenu.motif_selected"), false),
        new ChangeLookAndFeelAction(this, motif));
    HGMenuItem metalRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("PlafMenu.metal_label"),
        getString("PlafMenu.metal_actionCommand"),
        null,
        KeyEvent.VK_J,
        InputEvent.CTRL_MASK,
        plafMenuGroup,
        ParseData.parseBool(getString("PlafMenu.metal_selected"), false),
        new ChangeLookAndFeelAction(this, metal));
    HGMenuItem nativeRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("PlafMenu.windows_label"),
        getString("PlafMenu.windows_actionCommand"),
        null,
        KeyEvent.VK_W,
        InputEvent.CTRL_MASK,
        plafMenuGroup,
        ParseData.parseBool(getString("PlafMenu.windows_selected"), false),
        new ChangeLookAndFeelAction(this, windows));

    plafMenu = HGMenuItem.makeMenu("Look & Feel", 'L', new Object[] { metalRadio, motifRadio, nativeRadio }, null);

    HGMenuItem defaultThemeRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ThemesMenu.default_label"),
        getString("ThemesMenu.default_actionCommand"),
        null,
        KeyEvent.VK_D,
        InputEvent.CTRL_MASK,
        themesMenuGroup,
        ParseData.parseBool(getString("ThemesMenu.default_selected"), false),
        new ChangeThemeAction(this, new DefaultMetalTheme()));

    HGMenuItem obsidianThemeRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ThemesMenu.obsidian_label"),
        getString("ThemesMenu.obsidian_actionCommand"),
        null,
        KeyEvent.VK_B,
        InputEvent.CTRL_MASK,
        themesMenuGroup,
        ParseData.parseBool(getString("ThemesMenu.obsidian_selected"), false),
        new ChangeThemeAction(this, new ObsidianTheme()));

    HGMenuItem aquaRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ThemesMenu.aqua_label"),
        getString("ThemesMenu.aqua_actionCommand"),
        null,
        KeyEvent.VK_A,
        InputEvent.CTRL_MASK,
        themesMenuGroup,
        ParseData.parseBool(getString("ThemesMenu.aqua_selected"), false),
        new ChangeThemeAction(this, new AquaTheme()));

    HGMenuItem charcoalRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ThemesMenu.charcoal_label"),
        getString("ThemesMenu.charcoal_actionCommand"),
        null,
        KeyEvent.VK_C,
        InputEvent.CTRL_MASK,
        themesMenuGroup,
        ParseData.parseBool(getString("ThemesMenu.charcoal_selected"), false),
        new ChangeThemeAction(this, new CharcoalTheme()));

    HGMenuItem contrastRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ThemesMenu.contrast_label"),
        getString("ThemesMenu.contrast_actionCommand"),
        null,
        KeyEvent.VK_X,
        InputEvent.CTRL_MASK,
        themesMenuGroup,
        ParseData.parseBool(getString("ThemesMenu.contrast_selected"), false),
        new ChangeThemeAction(this, new ContrastTheme()));

    HGMenuItem emeraldRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ThemesMenu.emerald_label"),
        getString("ThemesMenu.emerald_actionCommand"),
        null,
        KeyEvent.VK_E,
        InputEvent.CTRL_MASK,
        themesMenuGroup,
        ParseData.parseBool(getString("ThemesMenu.emerald_selected"), false),
        new ChangeThemeAction(this, new EmeraldTheme()));

    HGMenuItem khakiMetalRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ThemesMenu.khakiMetal_label"),
        getString("ThemesMenu.khakiMetal_actionCommand"),
        null,
        KeyEvent.VK_G,
        InputEvent.CTRL_MASK,
        themesMenuGroup,
        ParseData.parseBool(getString("ThemesMenu.khakiMetal_selected"), false),
        new ChangeThemeAction(this, new KhakiMetalTheme()));

    HGMenuItem rubyRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ThemesMenu.ruby_label"),
        getString("ThemesMenu.ruby_actionCommand"),
        null,
        KeyEvent.VK_R,
        InputEvent.CTRL_MASK,
        themesMenuGroup,
        ParseData.parseBool(getString("ThemesMenu.ruby_selected"), false),
        new ChangeThemeAction(this, new RubyTheme()));

    HGMenuItem presentationRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ThemesMenu.presentation_label"),
        getString("ThemesMenu.presentation_actionCommand"),
        null,
        KeyEvent.VK_P,
        InputEvent.CTRL_MASK,
        themesMenuGroup,
        ParseData.parseBool(getString("ThemesMenu.presentation_selected"), false),
        new ChangeThemeAction(this, new PresentationTheme()));

    themesMenu =
      HGMenuItem.makeMenu(
        "Themes",
        'T',
        new Object[] {
          defaultThemeRadio,
          obsidianThemeRadio,
          aquaRadio,
          khakiMetalRadio,
          charcoalRadio,
          contrastRadio,
          emeraldRadio,
          rubyRadio,
          presentationRadio },
        null);

    HGMenuItem toolTipOnRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ToolTipMenu.on_label"),
        getString("ToolTipMenu.on_actionCommand"),
        null,
        KeyEvent.VK_R,
        InputEvent.CTRL_MASK,
        toolTipMenuGroup,
        true,
        new ToolTipAction(this, true));

    HGMenuItem toolTipOffRadio =
      new HGMenuItem(
        HGMenuListItem.JRADIOBTNMNUITEM,
        getString("ToolTipMenu.off_label"),
        getString("ToolTipMenu.off_actionCommand"),
        null,
        KeyEvent.VK_R,
        InputEvent.CTRL_MASK,
        toolTipMenuGroup,
        false,
        new ToolTipAction(this, false));

    toolTipMenu = HGMenuItem.makeMenu("Tool Tips", 'T', new Object[] { toolTipOnRadio, toolTipOffRadio }, null);

    menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(plafMenu);
    menuBar.add(themesMenu);
    menuBar.add(toolTipMenu);

    debug("createMenus() - Creating menus...Done");
    return menuBar;
  }
  /**
   * Create the spash screen while the rest of the demo loads
   */
  public void createSplashScreen()
  {
    debug("createSplashScreen() - Creating Splash Screen...");
    splashLabel = new JLabel(createImageIcon("Splash.jpg", "Splash.accessible_description"));

    if (!isApplet())
    {
      splashScreen = new JWindow(getFrame());
      splashScreen.getContentPane().add(splashLabel);
      splashScreen.pack();
      //Rectangle screenRect =
      //     getFrame().getGraphicsConfiguration().getBounds();
      Dimension sDim = Toolkit.getDefaultToolkit().getScreenSize();
      Rectangle screenRect = getFrame().getBounds();
      splashScreen.setLocation(
        (int) (sDim.getWidth() - splashScreen.getPreferredSize().width) / 2,
        (int) (sDim.getHeight() - splashScreen.getPreferredSize().height) / 2);
      //screenRect.x + screenRect.width/2 - splashScreen.getPreferredSize().width/2,
      //screenRect.y + screenRect.height/2 - splashScreen.getPreferredSize().height/2);
    }
    debug("createSplashScreen() - Creating Splash Screen...Done");
  }
  /**
   * If DEBUG is defined, prints debug information out to std ouput.
   * @param s The message to print out
   */
  public void debug(String s)
  {
    if (DEBUG)
    {
      System.out.println((debugCounter++) + ": " + s);
    }
  }
  /**
   * Returns the content pane wether we're in an applet
   * or application
   * @return Container
   */
  public Container getContentPane()
  {
    if (contentPane == null)
    {
      if (getFrame() != null)
      {
        contentPane = getFrame().getContentPane();
      }
      //else if (getApplet() != null) 
      //{
      //contentPane = getApplet().getContentPane();
      //}
    }
    return contentPane;
  }
  /**
   * Returns the frame instance
   * @return JFrame
   */
  public JFrame getFrame()
  {
    return frame;
  }
  /**
   * This method returns a string from the demo's resource bundle.
   * @param key The Key in the resource bundle
   */
  public String getModuleString(String key)
  {
    debug("getModuleString(" + key + ") - retrieving Key...");
    String value = null;
    try
    {
      value = getResourceBundle().getString(key);
    }
    catch (MissingResourceException e)
    {
      System.out.println("java.util.MissingResourceException: " + "Couldn't find value for: " + key);
    }
    debug("getModuleString(" + key + ") - value ==> " + value);
    debug("getModuleString(" + key + ") - retrieving Key...Done");
    return value;
  }
  /**
   * Returns the resource bundle associated with this demo. Used
   * to get accessable and internationalized strings.
   * @return ResourceBundle
   */
  public ResourceBundle getResourceBundle()
  {
    if (bundle == null)
    {
      bundle = ResourceBundle.getBundle("resources.stockmarketapp");
    }
    return bundle;
  }
  /**
   * This method returns a string from the demo's resource bundle.
   * @param key The Key within the resource bundle
   */
  public String getString(String key)
  {
    debug("getString(" + key + ") - retrieving Key...");
    String value = null;
    try
    {
      value = getResourceBundle().getString(key);
    }
    catch (MissingResourceException e)
    {
      System.out.println("java.util.MissingResourceException: " + "Couldn't find value for: " + key);
    }
    if (value == null)
    {
      value = "Could not find resource: " + key + "  ";
    }
    debug("getString(" + key + ") - value ==> " + value);
    debug("getString(" + key + ") - retrieving Key...Done");
    return value;
  }
  /**
   * Returns the toolbar
   */
  public ToggleButtonToolBar getToolBar()
  {
    return toolbar;
  }
  /**
   * Returns the toolbar button group
   * @return ButtonGroup
   */
  public ButtonGroup getToolBarGroup()
  {
    return toolbarGroup;
  }
  /**
   * Hides the spash screen
   */
  public void hideSplash()
  {
    debug("hideSplash() - Hiding Splash Screen...");
    if (!isApplet())
    {
      splashScreen.setVisible(false);
      splashScreen = null;
      splashLabel = null;
    }
    debug("hideSplash() - Hiding Splash Screen...Done");
  }
  /**
   * Preliminary Inializing of data before anything else should start
   */
  public void initializeDemo()
  {
    debug("initializeDemo() - Init...");
    // First lets build the App Properties
    preloadAppProperties();

    // Now Build the top Panel
    JPanel top = new JPanel();
    top.setLayout(new BorderLayout());
    add(top, BorderLayout.NORTH);

    // Create our menu bar items
    menuBar = createMenus();
    top.add(menuBar, BorderLayout.NORTH);

    // Now build the ToolBar Panel
    ToolBarPanel toolbarPanel = new ToolBarPanel();
    toolbarPanel.setLayout(new BorderLayout());
    toolbar = new ToggleButtonToolBar();
    toolbarPanel.add(toolbar, BorderLayout.CENTER);
    top.add(toolbarPanel, BorderLayout.SOUTH);
    toolbarPanel.addContainerListener(toolbarPanel);
    // Here we are building the main content area - using a TabbedPane
    tabbedPane = new JTabbedPane();
    add(tabbedPane, BorderLayout.CENTER);
    tabbedPane.getModel().addChangeListener(new TabListener());

    // Construct a Status bar at the bottom of the Window
    statusField = new JTextField("");
    statusField.setEditable(false);
    add(statusField, BorderLayout.SOUTH);

    // This is the Panel for which we display the Module
    demoPanel = new JPanel();
    demoPanel.setLayout(new BorderLayout());
    demoPanel.setBorder(new EtchedBorder());
    tabbedPane.addTab("Welcome", demoPanel);

    // Get a List of all the available Module to load
    moduleList = new Vector();
    String moduleNames = null;
    int count = 0;
    do
    {
      String key = "StockAppModules.module_" + count;
      moduleNames = getModuleString(key);
      if (moduleNames != null && !moduleNames.trim().equals(""))
      {
        moduleList.addElement(moduleNames);
      }
      count++;
    }
    while (moduleNames != null);

    final int ONE_SECOND = 1000;

    TimerTask clock = new TimeMarketOpenClose();
    Timer timer = new Timer(true); // Deamon 
    timer.scheduleAtFixedRate(clock, 10 * ONE_SECOND, 60 * ONE_SECOND); // 10 Second Delay, 1 minutel

    debug("initializeDemo() - Init...Done");
  }
  /**
   * Determines if this is an applet or application
   * return boolean
   */
  public boolean isApplet()
  {
    return (stockMarketApplet != null);
  }
  /**
   * Loads all modules that are losted in the moduleList
   */
  private void loadModules()
  {
    debug("loadModules() - Loading modules...");
    for (int i = 0; i < moduleList.size(); i++)
    {
      String moduleName = (String) moduleList.elementAt(i);
      debug("loadModules() - Start Loading " + moduleName + "...");
      loadTheModule(moduleName);
    }
    debug("loadModules() - Loading modules...Done");
  }
  /**
   * Loads a demo from a classname
   * @param classname The Class to load
   */
  private void loadTheModule(String classname)
  {
    debug("loadTheModule(" + classname + ") - Loading the module...");
    setStatus(getString("Status.loading") + getString(classname + ".name"));
    StockMarketBaseModule demo = null;
    try
    {
      String packageName = getString(classname + ".package");
      packageName += classname;
      Class demoClass = Class.forName(packageName);
      Constructor demoConstructor = demoClass.getConstructor(new Class[] { StockMarketApp.class });
      demo = (StockMarketBaseModule) demoConstructor.newInstance(new Object[] { this });
      addModule(demo);
    }
    catch (Exception e)
    {
      System.out.println("Error occurred loading demo: " + classname);
    }
    debug("loadTheModule(" + classname + ") - Loading the module...Done");
  }
  /**
   * Main. Called only if we're an application, not an applet.
   */
  public static void main(String[] args)
  {
    // Create StockMarketApp on the default monitor
    StockMarketApp stockMarketApp = new StockMarketApp();
  }
  /**
   * Method to send a message that a change of modules is requested
   * by a sub module and the user did not select the button bar.
   * @param resourceName java.lang.String
   */
  public void notifyChangeModules(String resourceName)
  {

    debug("notifyChangeModules(" + resourceName + ") attempting module requested change");
    // Begin lookup of the module we have loaded.
    StockMarketBaseModule module = null;
    for (int i = 0; i < demosVector.size(); i++)
    {
      StockMarketBaseModule temp = (StockMarketBaseModule) demosVector.elementAt(i);
      if (resourceName.equals(temp.getResourceName()))
      {
        debug("notifyChangeModules(" + resourceName + ") - found match resource");
        i = demosVector.size();
        module = temp;
      }
    }

    // if the Module is loaded then change to it
    if (module != null)
    {
      // We first need to evaluate if the modules are instances of StockModuleDataExchange interface
      Object data = null;
      if ((currentDemo instanceof StockModuleDataExchange) && (module instanceof StockModuleDataExchange))
      {
        // They are instances so grab the data from old, and set to the new.
        debug(
          "notifyChangeModules("
            + resourceName
            + ") currentDemo ["
            + currentDemo.getResourceName()
            + "] is a StockModuleDataExchange");
        debug(
          "notifyChangeModules("
            + resourceName
            + ")      module ["
            + module.getResourceName()
            + "] is a StockModuleDataExchange");
        data = ((StockModuleDataExchange) currentDemo).getDataObject();
        ((StockModuleDataExchange) module).setDataObject(data);
      }

      debug("notifyChangeModules(" + resourceName + ") invoking setCurrentModule()");
      setCurrentModule(module);
    }
    debug("notifyChangeModules(" + resourceName + ") completed processing");
  }
  /**
   * Load Property Ini File - Dome values will be loaded into the System Data
   * so that we can get StockMarketApp up and available to the user quickly.
   */
  private void preloadAppProperties()
  {
    // Set up the INI File for our application
    appProps = new AppProperties(getString("PropertyConfigDialogBox.application_ini_filename"));
    // Set up the Parser INI file Proxy Info
    com.stockmarket.parsers.StockDataCSVParser.preloadProxyInfo(
      getString("PropertyConfigDialogBox.application_ini_filename"));
  }
  /**
   * Load the first demo. This is done separately from the remaining demos
   * so that we can get StockMarketApp up and available to the user quickly.
   */
  private void preloadFirstDemo()
  {
    StockMarketBaseModule demo = new WelcomeModule(this);
    addModule(demo);
    setCurrentModule(demo);
  }
  /**
   * Set the Current Module that will be visible
   * @param demo The Module to display
   */
  public void setCurrentModule(StockMarketBaseModule demo)
  {

    // If our current Demo is null then we are the first time through
    // this method.  Do not shutdown anything.    
    if (currentDemo != null)
    {
      // If we have the same module twice then simply return
      if (currentDemo == demo)
      {
        return;
      }

      // We have an active module signal a stop mechanism.
      currentDemo.stop();
    }
    // Set the new Demo as Current
    currentDemo = demo;

    // Ensure panel's UI is current before making visible
    JComponent currentDemoPanel = demo.getModulePanel();
    SwingUtilities.updateComponentTreeUI(currentDemoPanel);

    demoPanel.removeAll();
    demoPanel.add(currentDemoPanel, BorderLayout.CENTER);

    tabbedPane.setSelectedIndex(0);
    tabbedPane.setTitleAt(0, demo.getName());
    // tabbedPane.setToolTipText(demo.getToolTip());
    tabbedPane.setToolTipTextAt(0, demo.getToolTip());

    // Lastly, we want to call startup in the case there is anything
    // That needs to be started, or initialized on the Module
    currentDemo.start();
    setStatus("Requested module change complete.");
  }
  /**
   * Method setStartupLookAndFeel.  Sets the initial Look and Feel and Theme
   */
  private void setStartupLookAndFeel()
  {

    MetalLookAndFeel.setCurrentTheme(defaultStartupTheme);
    HGTableColorModel.getInstance().setTheme(defaultStartupTheme);

    this.updateLookAndFeel();
  }
  /**
   * Stores the current L&F, and calls updateLookAndFeel,
   * @param laf The Loof and Feel constant we want to display
   */
  public void setLookAndFeel(String laf)
  {
    debug("setLookAndFeel() - Setting Look and Feel...");
    if (currentLookAndFeel != laf)
    {
      currentLookAndFeel = laf;
      boolean menuEnabled = (laf == metal);
      themesMenu.setEnabled(menuEnabled);
      updateLookAndFeel();
    }
    debug("setLookAndFeel() - Setting Look and Feel...Done");
  }
  /**
   * Set the status bar
   * @param s The Message to disply in the status bar
   */
  public void setStatus(String s)
  {
    // do the following on the gui thread
    SwingUtilities.invokeLater(new StockAppRunnable(this, s)
    {
      public void run()
      {
        statusField.setText((String) obj);
      }
    });
  }
  /**
   * Method will display Splash Screen while the Application is loading in Background
   */
  private void showSplashScreen()
  {
    if (!isApplet())
    {
      splashScreen.show();
    }
    else
    {
      add(splashLabel, BorderLayout.CENTER);
      validate();
      repaint();
    }
  }
  /**
   * Bring up the StockMarketApp demo by showing the frame (only
   * applicable if coming up as an application, not an applet);
   */
  public void showStockMarketApp()
  {
    if (!isApplet() && getFrame() != null)
    {
      // We will sleep for just a bit, I want to ensure that the application Threads
      // are catching up.
      try
      {
        Thread.currentThread().sleep(1000);
      }
      catch (InterruptedException e)
      {
        // Do Nothing
      }
      // put swingset in a frame and show it
      JFrame f = getFrame();
      f.setTitle(getString("Frame.title"));
      f.getContentPane().add(this, BorderLayout.CENTER);
      f.pack();

      this.setStartupLookAndFeel();

      Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

      Rectangle screenRect = new Rectangle(0, 0, screenDim.width, screenDim.height);

      // Make sure we don't place the demo off the screen.
      int centerWidth =
        screenRect.width < f.getSize().width ? screenRect.x : screenRect.x + screenRect.width / 2 - f.getSize().width / 2;
      int centerHeight =
        screenRect.height < f.getSize().height ? screenRect.y : screenRect.y + screenRect.height / 2 - f.getSize().height / 2;

      f.setLocation(centerWidth, centerHeight);
      f.show();
    }
  }
  /**
   * Method to call all individual appliction shutdown methods for cleanup purposes.
   */
  public void shutdown()
  {
    for (int i = 0; i < this.demosVector.size(); i++)
    {
      Object obj = this.demosVector.elementAt(i);
      if (obj instanceof StockMarketBaseModule)
      {
        StockMarketBaseModule appModule = (StockMarketBaseModule) obj;
        appModule.shutdown();
      }
    }
    demosVector.removeAllElements();
    // Null out any modules
    this.demosVector = null;
    // Kill The System
    System.exit(0);
  }
  /**
   * Sets the current L&F on each demo module
   */
  protected void updateLookAndFeel()
  {
    debug("updateLookAndFeel() - Modifying Look and Feel...");
    try
    {
      UIManager.setLookAndFeel(currentLookAndFeel);
      // update LAF for the toplevel frame, too
      SwingUtilities.updateComponentTreeUI(frame);
    }
    catch (Exception ex)
    {
      System.out.println("Failed loading L&F: " + currentLookAndFeel);
      System.out.println(ex);
    }

    // lazily update update the UI's for the remaining demos
    for (int i = 0; i < demosVector.size(); i++)
    {
      debug("updateLookAndFeel() - " + "Modifying Look and Feel for module(" + i + ")...");
      StockMarketBaseModule demo = (StockMarketBaseModule) demosVector.elementAt(i);
      if (currentDemo != demo)
      {
        // do the following on the gui thread
        SwingUtilities.invokeLater(new StockAppRunnable(this, demo)
        {
          public void run()
          {
            SwingUtilities.updateComponentTreeUI(((StockMarketBaseModule) obj).getModulePanel());
          }
        });
      }
    }
    debug("updateLookAndFeel() - Modifying Look and Feel...Done");

  }

}
