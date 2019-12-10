// StockMarketBaseModule .java
package com.stockmarket.app;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
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
import com.hgutil.ParseData;

/**
 * Module was modled after
 * c:/jdk1.4/demo/jfc/SwingSet2/src/DemoModule.java
 * @author Hans-Jurgen Greiner
 */
public abstract class StockMarketBaseModule
{
  // Can be null if we are being used as a module rather than stand alone
  protected JFrame frame = null;
  // The preferred size of the demo
  protected int PREFERRED_WIDTH = 680;
  protected int PREFERRED_HEIGHT = 600;

  Border loweredBorder = new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED), new EmptyBorder(5, 5, 5, 5));

  // Premade convenience dimensions, for use wherever you need 'em.
  public static Dimension HGAP2 = new Dimension(2, 1);
  public static Dimension VGAP2 = new Dimension(1, 2);

  public static Dimension HGAP5 = new Dimension(5, 1);
  public static Dimension VGAP5 = new Dimension(1, 5);

  public static Dimension HGAP10 = new Dimension(10, 1);
  public static Dimension VGAP10 = new Dimension(1, 10);

  public static Dimension HGAP15 = new Dimension(15, 1);
  public static Dimension VGAP15 = new Dimension(1, 15);

  public static Dimension HGAP20 = new Dimension(20, 1);
  public static Dimension VGAP20 = new Dimension(1, 20);

  public static Dimension HGAP25 = new Dimension(25, 1);
  public static Dimension VGAP25 = new Dimension(1, 25);

  public static Dimension HGAP30 = new Dimension(30, 1);
  public static Dimension VGAP30 = new Dimension(1, 30);

  private JPanel mainApp = null;
  private JPanel panel = null;
  private String resourceName = null;
  private String iconPath = null;
  private String sourceCode = null;
  public static boolean DEMO_MODE = ParseData.parseBool(System.getProperties().getProperty("HGDEMO"), false);
  // To debug or not to debug, that is the question
  public static boolean DEBUG = ParseData.parseBool(System.getProperties().getProperty("HGDEBUG"), false);
  // Resource bundle for internationalized and accessible text
  private ResourceBundle bundle = null;

  /**
   * Generic ModuleAppRunnable runnable. This is intended to run on the
   * AWT gui event thread so as not to muck things up by doing
   * gui work off the gui thread. Accepts a StockMarketApp and an Object
   * as arguments, which gives subtypes of this class the two
   * "must haves" needed in most runnables for this demo.
   *
   * A Example of how to use the runnable class is as follows
   * <P>
   * <code><pre>
   * String text = "Blue Mountain";
   * addTab( text );
   * SwingUtilities.invokeLater(new ModuleAppRunnable(this, text )
   *   {
   *      public void run()
   *      {
   *        lookupApp.populateTab(1, (String)obj );
   *      }
   *   } );
   * </pre></code>
   */
  private class ModuleAppRunnable implements Runnable
  {
    protected StockMarketBaseModule moduleApp;
    protected Object obj;

    public ModuleAppRunnable(StockMarketBaseModule moduleApp, Object obj)
    {
      this.moduleApp = moduleApp;
      this.obj = obj;
    }

    public void run()
    {}
  }

  /**
   * StockMarketBaseModule constructor comment.
   */
  public StockMarketBaseModule()
  {
    super();
  }
  /**
   * Constructor
   * @param mainApp the Main Application
   */
  public StockMarketBaseModule(JPanel mainApp)
  {
    this(mainApp, null, null);
  }
  /**
   * Constructor
   * @param mainApp the Main Application
   * @param resourceName the Resource name 
   * @param iconPath main Toolbar Icon
   */
  public StockMarketBaseModule(JPanel mainApp, String resourceName, String iconPath)
  {
    panel = new JPanel();
    panel.setLayout(new BorderLayout());

    this.resourceName = resourceName;
    this.iconPath = iconPath;
    this.mainApp = mainApp;
  }
  /**
   * Create a Horizontal Panel in normal or 3D mode
   * @param threeD boolean value to construct as 3D
   * @return JPanel
   */
  public JPanel createHorizontalPanel(boolean threeD)
  {
    debug("createHorizontalPanel() - Creating Horizontal Panel...");
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.setAlignmentY(JPanel.TOP_ALIGNMENT);
    p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    if (threeD)
    {
      p.setBorder(loweredBorder);
    }
    debug("createHorizontalPanel() - Creating Horizontal Panel...Done");
    return p;
  }
  /**
   * Create Image icon from the specified file name
   * @param filename The Filename of the image
   * @param description FileName descirption
   * @return ImageIcon
   */
  public ImageIcon createImageIcon(String filename, String description)
  {
    String path = "/resources/images/" + filename;
    debug("createImageIcon() - ATTEMPTING load image " + path + ", " + description);
    ImageIcon ii = new ImageIcon(getClass().getResource(path), description);
    debug("createImageIcon() - Image is " + ii);
    return ii;
  }
  /**
   * Create spacer for either Horizontal or Vertical Panels
   * @param dim  The width or height Dimensions
   * @return Component
   */
  public Component createSpacerObject(Dimension dim)
  {
    return Box.createRigidArea(dim);
  }
  /**
   * Create a Vertical Panel in normal or 3D mode
   * @param threeD boolean value to construct as 3D
   * @return JPanel
   */
  public JPanel createVerticalPanel(boolean threeD)
  {
    debug("createVerticalPanel() - Creating Vertical Panel...");
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setAlignmentY(JPanel.TOP_ALIGNMENT);
    p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    if (threeD)
    {
      p.setBorder(loweredBorder);
    }
    debug("createVerticalPanel() - Creating Vertical Panel...Done");
    return p;
  }
  /**
   * If DEBUG is defined, prints debug information out to std ouput.
   */
  public void debug(String s)
  {
    if (getMainApp() != null)
    {
      getMainApp().debug(this.resourceName + "::" + s);
    }
    else if (DEBUG)
    {
      System.out.println(this.resourceName + "::" + s);
    }
  }
  /**
   * Returns the boolean value for the specified key
   * @param key A Sring representation for the nMeomnic
   * @return boolean
   */
  public boolean getBool(String key)
  {
    String value = "nada";
    debug("getBool(" + key + ") - retrieving key...");
    value = getString(key);

    boolean rc = new Boolean(value).booleanValue();
    debug("getBool(" + key + ") - Value ==> " + value);
    debug("getBool(" + key + ") - retrieving key...Done");
    return rc;
  }
  /**
   * Returns icon from the specified file name
   * @return ImageIcon
   */
  public Icon getIcon()
  {
    debug("getIcon() - retrieving icon!");
    return createImageIcon(iconPath, getResourceName() + ".name");
  }
  /**
   * Returns the Main App instance
   * @return StockMarketApp
   */
  public StockMarketApp getMainApp()
  {
    return ((StockMarketApp) mainApp);
  }
  /**
   * Returns the Nmemonic for a specified key
   * @param key A Sring representation for the nMeomnic
   * @return char
   */
  public char getMnemonic(String key)
  {
    debug("getMnemonic(" + key + ")");
    return (getString(key)).charAt(0);
  }
  /**
   * Returns tthe Module Panel
   * @return JPanel
   */
  public JPanel getModulePanel()
  {
    return panel;
  }
  /**
   * Returns the Reource as a name
   * @return String
   */
  public String getName()
  {
    return getString(getResourceName() + ".name");
  }
  /**
   * Returns the resource name
   * @return String
   */
  public String getResourceName()
  {
    return resourceName;
  }
  /**
   * Method to retrieve a string from the resource bundle
   * @param key The Key llokup value within  the resource bundle
   */
  public String getString(String key)
  {
    String value = "nada";
    debug("getString(" + key + ") - retrieving key...");
    if (bundle == null)
    {
      if (getMainApp() != null)
      {
        bundle = getMainApp().getResourceBundle();
      }
      else
      {
        bundle = ResourceBundle.getBundle("resources.stockmarketapp");
      }
    }
    try
    {
      value = bundle.getString(key);
    }
    catch (MissingResourceException e)
    {
      String message = "java.util.MissingResourceException: " + "Couldn't find value for: " + key;
      debug(message);
      System.out.println(message);
    }
    debug("getString(" + key + ") - Value ==> " + value);
    debug("getString(" + key + ") - retrieving key...Done");
    return value;
  }
  /**
   * Returns the Tool tip for the current resource name
   * @return String
   */
  public String getToolTip()
  {
    return getString(getResourceName() + ".tooltip");
  }
  /**
   * A Main Method, this method can generically drive any of the modules extended 
   * from this base class. Note that it signales to call shutdown() on window closing
   */
  public void mainImpl()
  {
    frame = new JFrame(getName());
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(getModulePanel(), BorderLayout.CENTER);
    getModulePanel().setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));

    // Set our Proxy info
    com.stockmarket.parsers.StockDataCSVParser.preloadProxyInfo(
      getString("PropertyConfigDialogBox.application_ini_filename"));

    // Set up our window closer
    WindowListener l = new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        StockMarketBaseModule.this.shutdown();
        System.exit(0);
      }
    };

    Dimension sDim = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(
      (int) (sDim.getWidth() - frame.getPreferredSize().width) / 2,
      (int) (sDim.getHeight() - frame.getPreferredSize().height) / 2);

    frame.addWindowListener(l);
    frame.pack();
    frame.show();
  }
  /**
   * Method to shutdown an application and perform any cleanup neccesarry
   */
  abstract public void shutdown();

  /**
   * Method to startup any threads if neccesary
   */
  abstract public void start();
  /**
   * Method to stop any threads if neccesary
   */
  abstract public void stop();
}
