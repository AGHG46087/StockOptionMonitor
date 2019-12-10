// PropertyConfigDialogBox.java
package com.stockmarket.app;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import com.hgutil.*;
import com.hgutil.swing.*;
import com.stockmarket.parsers.httpclient.*;
/**
 * Class Object to present a Dialog Box the stores all information to
 * to the Application properties Object.
 * @author Hans-Jurgen Greiner
 */
public class PropertyConfigDialogBox extends JPanel
{

  // To debug or not to debug, that is the question
  public static boolean DEBUG = ParseData.parseBool(System.getProperties().getProperty("HGDEBUG"), false);
  // Resource bundle for internationalized and accessible text
  private ResourceBundle bundle = null;
  // Private data members - Data Necessary for Proxy Info
  private JTextField user = new JTextField(20);
  private JPasswordField pass = new JPasswordField(20);
  private JTextField host = new DoubleTextField(0, 10);
  private JTextField port = new IntegerTextField(0, 5, 6);
  private JTextField realm = new JTextField(20);
  private JCheckBox proxy_req = new JCheckBox();
  private JFrame frame = null;
  private AppProperties appProps = null;
  private String btnTestCmd = null;
  private static final String AUTHUSERID = "HTTPClient.proxy_name";
  private static final String AUTHUSERPSWD = "HTTPClient.proxy_password";
  private static final String AUTHUSERHOST = "HTTPClient.proxy_host";
  private static final String AUTHUSERPORT = "HTTPClient.proxy_port";
  private static final String AUTHUSERREALM = "HTTPClient.proxy_realm";
  private static final String AUTHUSERPROXY_REQ = "HTTPClient.proxy_required";
  protected Border loweredBorder =
    new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED), new EmptyBorder(5, 5, 5, 5));
  protected Border raisedBorder =
    new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), new EmptyBorder(5, 5, 5, 5));

  // Private data members - Data Necessary for Additional Option Info
  private PercentTextField rfInterestRate = new PercentTextField(0.0, 10);

  // Action Trigger for Button Clicks
  protected ActionTrigger actionTrigger = new ActionTrigger();
  /*
  * Class ActionTrigger that implements action Listener.  This is used.
  * for the monitoring of events on Menus and Buttons, for which actions 
  * need to happen
  */
  class ActionTrigger implements ActionListener
  {

    public void actionPerformed(ActionEvent evt)
    {

      // Grab our command
      String cmd = evt.getActionCommand();

      if (btnTestCmd.equals(cmd))
      { // Allowing for Table Control to be presented
        interrogateProxy();
      }
    }
  }

  /**
   * HistoricRequestPanel constructor comment.
   */
  public PropertyConfigDialogBox(AppProperties appProps, JFrame frame)
  {
    super(new BorderLayout());

    this.appProps = appProps;
    if (appProps == null)
    {
      throw new NullPointerException("The Properties File Object cannot be null");
    }
    // This is the main Stream Panel that will be located CENTER of this

    String tabName = null;
    String toolTip = null;
    JTabbedPane mainPanel = new JTabbedPane();

    tabName = getString("PropertyConfigDialogBox.proxy_info_tab_text");
    toolTip = getString("PropertyConfigDialogBox.proxy_info_tab_tooltip_text");
    mainPanel.addTab(tabName, null, createProxyInfoPanel(), toolTip);

    tabName = getString("PropertyConfigDialogBox.option_info_tab_text");
    toolTip = getString("PropertyConfigDialogBox.option_info_tab_tooltip_text");
    mainPanel.addTab(tabName, createOptionDataPanel());

    this.add(mainPanel, BorderLayout.CENTER);
  }
  /**
   * Method to Create a Vertical Panel
   * @param rows The Number of Rows'
   * @param cols The Number of Columns
   * @param threeD If the Box is to be 3D
   * @return javax.swing.JPanel
   */
  protected JPanel createGridPanel(int rows, int cols, boolean threeD)
  {
    JPanel p = new JPanel();
    p.setLayout(new GridLayout(rows, cols));
    p.setAlignmentY(JPanel.TOP_ALIGNMENT);
    p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    if (threeD)
    {
      p.setBorder(loweredBorder);
    }
    return p;
  }
  /**
   * Method to Create a Horizontal Panel
   * @return javax.swing.JPanel
   */
  protected JPanel createHorizontalPanel(boolean threeD)
  {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.setAlignmentY(JPanel.TOP_ALIGNMENT);
    p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    if (threeD)
    {
      p.setBorder(loweredBorder);
    }
    return p;
  }
  /**
   * Method to return the panel with Additional Data for Options
   */
  protected JPanel createOptionDataPanel()
  {

    JLabel lblRiskFreeRate = new JLabel(getString("PropertyConfigDialogBox.option_info_interest_free_rate_label"));
    // Main Option Info Panel that will be returned
    JPanel infoPanel = new JPanel(new BorderLayout());
    // Vertical Panel that all rows will be added
    JPanel panel = createGridPanel(5, 1, true);
    // This is a working panel for each row
    JPanel work = null;

    // Text Field to enter the Risk Free Interest Rate
    work = createHorizontalPanel(false);
    work.add(lblRiskFreeRate);
    work.add(createSpacerObject(new Dimension(23, 1)));
    work.add(rfInterestRate);
    panel.add(work);

    // Add the vertical Panel to the center of the Panel 
    infoPanel.add(panel, BorderLayout.CENTER);

    return infoPanel;
  }
  /**
   * Method to Create the Panel Jor the Proxy Information
   * @return javax.swing.JPanel
   */
  protected JPanel createProxyInfoPanel()
  {

    // Set Up Labels for each Field
    JLabel lblUser = new JLabel(getString("PropertyConfigDialogBox.proxy_userid_label_text"));
    JLabel lblPswd = new JLabel(getString("PropertyConfigDialogBox.proxy_password_label_text"));
    JLabel lblHost = new JLabel(getString("PropertyConfigDialogBox.proxy_domain_label_text"));
    JLabel lblRealm = new JLabel(getString("PropertyConfigDialogBox.proxy_realm_label_text"));
    JLabel lblPort = new JLabel(getString("PropertyConfigDialogBox.proxy_port_label_text"));
    proxy_req.setText(getString("PropertyConfigDialogBox.proxy_required_label_text"));
    // Main Proxy Info Panel that will be returned
    JPanel infoPanel = new JPanel(new BorderLayout());
    // Vertical Panel that all rows will be added
    JPanel panel = createVerticalPanel(true);
    // This is a working panel for each row
    JPanel work = null;

    // Populate the Text fields
    populateTextFields();

    // Text Field to enter the Realm of the Proxy
    work = createHorizontalPanel(false);
    work.add(lblRealm);
    work.add(createSpacerObject(new Dimension(23, 1)));
    work.add(realm);
    panel.add(work);

    // Text Field to enter the Host Domain and Port for the Proxy
    work = createHorizontalPanel(false);
    work.add(lblHost);
    work.add(createSpacerObject(new Dimension(33, 1)));
    work.add(host);
    work.add(createSpacerObject(new Dimension(10, 1)));
    work.add(lblPort);
    work.add(createSpacerObject(new Dimension(2, 1)));
    work.add(port);
    panel.add(work);

    // Text Field to enter the user ID for the Proxy Account
    work = createHorizontalPanel(false);
    work.add(lblUser);
    work.add(createSpacerObject(new Dimension(18, 1)));
    work.add(user);
    panel.add(work);

    // Text Field to enter the Passsword for the Proxy Account
    work = createHorizontalPanel(false);
    work.add(lblPswd);
    work.add(createSpacerObject(new Dimension(1, 1)));
    work.add(pass);
    panel.add(work);

    // This button is let the user fire a test of the proxy account
    btnTestCmd = getString("PropertyConfigDialogBox.proxy_button_test_text");
    JButton btnTest = new JButton(btnTestCmd);
    btnTest.addActionListener(actionTrigger);
    // Create new panel with a flow layout to the right, so that it always
    // self adjusts to the screen display
    JPanel rightShiftPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    rightShiftPanel.add(btnTest);

    // Text field requesting to turn off and on the Proxy Credentials
    work = createHorizontalPanel(false);
    work.add(proxy_req);
    // Add the right justified button on the same row
    work.add(rightShiftPanel);
    panel.add(work);

    // Add the vertical Panel to the center of the Panel 
    infoPanel.add(panel, BorderLayout.CENTER);

    return infoPanel;
  }
  /**
   * Method to Create a Spacers for the Vertical and Horizontal Panels
   * @return javax.swing.JPanel
   */
  protected Component createSpacerObject(Dimension dim)
  {
    return Box.createRigidArea(dim);
  }
  /**
   * Method to Create a Vertical Panel
   * @return javax.swing.JPanel
   */
  protected JPanel createVerticalPanel(boolean threeD)
  {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setAlignmentY(JPanel.TOP_ALIGNMENT);
    p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    if (threeD)
    {
      p.setBorder(loweredBorder);
    }
    return p;
  }
  /**
   * If DEBUG is defined, prints debug information out to std ouput.
   */
  public void debug(String s)
  {
    if (DEBUG)
    {
      System.out.println("PropertyConfigDialogBox::" + s);
    }
  }
  /**
   * Method to retrieve a string from the resource bundle
   * @param key The Key llokup value within  the resource bundle
   */
  public String getString(String key)
  {
    String value = "nill";
    if (bundle == null)
    {
      bundle = ResourceBundle.getBundle("resources.stockmarketapp");
    }
    try
    {
      value = bundle.getString(key);
    }
    catch (MissingResourceException e)
    {
      String message = "java.util.MissingResourceException: " + "Couldn't find value for: " + key;
      debug(message);
    }
    debug("getString(" + key + ") - Value ==> " + value);
    debug("getString(" + key + ") - retrieving key...Done");
    return value;
  }
  /**
   * Method to Interrogate the Proxy account
   */
  private void interrogateProxy()
  {

    GetProxyAuthParameters params = new GetProxyAuthParameters(host.getText(), port.getText());
    params.testURL();

    debug("Params is a " + (params.isSuccess() ? "SUCCSSS" : "FAILURE"));

    String schemeSz = params.getProxyScheme();
    String realmSz = params.getProxyRealm();
    String hostSz = params.getProxyHost();
    String portSz = params.getProxyPort();

    if (params.isSuccess())
    {
      realm.setText(realmSz);
      host.setText(hostSz);
      port.setText(portSz);
      JOptionPane.showMessageDialog(
        frame,
        "Testing Proxy pass through was a SUCCSSS.",
        "Proxy Test SUCCESS",
        JOptionPane.INFORMATION_MESSAGE);
    }
    else
    {
      JOptionPane.showMessageDialog(
        frame,
        "Testing Proxy pass through was a NOT Succesful.\n" + params.getMessage(),
        "Proxy Test FAILURE",
        JOptionPane.ERROR_MESSAGE);
    }

  }
  /**
   * Insert the method's description here.
   * @param args java.lang.String[]
   */
  public static void main(String[] args)
  {

    AppProperties appProps = new AppProperties("StockApp.ini");

    PropertyConfigDialogBox.showHistoricRequestDialog(appProps, null);

  }
  /**
   * Method to Populate the Text fields
   */
  private void populateTextFields()
  {

    host.setText(appProps.getProperty(AUTHUSERHOST, ""));
    port.setText(appProps.getProperty(AUTHUSERPORT, ""));
    realm.setText(appProps.getProperty(AUTHUSERREALM, ""));
    user.setText(appProps.getProperty(AUTHUSERID, ""));
    pass.setText(appProps.getProperty(AUTHUSERPSWD, ""));
    proxy_req.setSelected(appProps.getBooleanProperty(AUTHUSERPROXY_REQ, false));

    String tempRate = appProps.getProperty("RiskFreeInterestRate", "");
    rfInterestRate.setValue(ParseData.parseNum(tempRate, 0.0));
  }
  /**
   * Method to report any Proxy Errors
   * @param message The Message being returned from the proxy
   */
  private void reportProxyInfo(String message)
  {
    // Log the information to the Err console
    String userID = user.getText();
    String pswdword = new String(pass.getPassword());

    debug(message + "-> ProxyInfo:");
    debug("   UserID:   " + userID);
    debug("   PassWord: " + pswdword);
  }
  /**
   * Method that will display the Dialog Box to Modify the User Defined Properties
   * @param appProps A Property Container that points to the Properties configuration file
   * @param frame    The Main Frame for which this dialog box is associated
   */
  public static void showHistoricRequestDialog(AppProperties appProps, JFrame frame)
  {

    final PropertyConfigDialogBox controlPanel; // Blank Final
    final JDialog customDialog = new JDialog(frame, "User Defined Configuration", true);

    try
    {
      controlPanel = new PropertyConfigDialogBox(appProps, frame);
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
              controlPanel.storeUserConfigInfo();
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
      customDialog.setLocationRelativeTo(frame);
      customDialog.setVisible(true);

    }
    catch (RuntimeException exc)
    {}

  }
  /**
   * Method to store and Save all user Defined Information
   */
  public void storeUserConfigInfo()
  {

    // Store the Results in our Properties Object
    String proxyReq = (proxy_req.isSelected()) ? "true" : "false";
    String realmID = realm.getText();
    String hostID = host.getText();
    String portID = port.getText();
    String userID = user.getText();
    String pswdword = new String(pass.getPassword());
    appProps.setProperty(AUTHUSERID, userID);
    appProps.setProperty(AUTHUSERPSWD, pswdword);
    appProps.setProperty(AUTHUSERHOST, hostID);
    appProps.setProperty(AUTHUSERPORT, portID);
    appProps.setProperty(AUTHUSERREALM, realmID);
    appProps.setProperty(AUTHUSERPROXY_REQ, proxyReq);

    appProps.saveProperties(getString("PropertyConfigDialogBox.application_ini_header_title"));

    // Now Store Some information into the System Properties

    System.getProperties().put(AUTHUSERID, userID);
    System.getProperties().put(AUTHUSERPSWD, pswdword);
    System.getProperties().put(AUTHUSERHOST, hostID);
    System.getProperties().put(AUTHUSERPORT, portID);
    System.getProperties().put(AUTHUSERREALM, realmID);
    System.getProperties().put(AUTHUSERPROXY_REQ, proxyReq);

  }
}
