package nz.ac.vuw.engr301.group7.mission_control_proj;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.Scanner;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.SwingConstants;
import javax.swing.JSpinner;
import java.awt.Component;
import java.awt.Button;
import javax.swing.border.BevelBorder;

public class MissionControl {

	JFrame frame;
	private JTextField altitudeTxtFld;
	private JTextField latitudeTxtFld;
	private JTextField longTxtFld;
	private JTextField windSpeedTxtFld;
	private JTextField windDirTxtFld;
	private JTextField stdDevTxtFld;
	static SerialPort chosenPort;
	private JTextField landingLatTxt;
	private JTextField landingLongTxt;
	private JTextField landingDistTxt;


	// Create the application.
	public MissionControl() {
		initialize();
	}

	// Initialize the contents of the frame.
	private void initialize() {
		frame = new JFrame();
		
		// create wind/rocket data area
		JPanel westPanel = new JPanel();
		GridBagLayout gbl_westPanel = new GridBagLayout();
		gbl_westPanel.columnWidths = new int[] {160};
		gbl_westPanel.rowHeights = new int[] {150, 0, 150};
		gbl_westPanel.columnWeights = new double[]{0.0};
		gbl_westPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		westPanel.setLayout(gbl_westPanel);
		
		//Creating Location Area
		JPanel locationGroup = new JPanel();
		locationGroup.setBorder(new TitledBorder(null, "Location", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		locationGroup.setName("Location");
		
		GridBagConstraints gbc_locationGroup = new GridBagConstraints();
		gbc_locationGroup.fill = GridBagConstraints.HORIZONTAL;
		gbc_locationGroup.insets = new Insets(0, 0, 5, 5);
		gbc_locationGroup.gridx = 0;
		gbc_locationGroup.gridy = 0;
		westPanel.add(locationGroup, gbc_locationGroup);
		
		JLabel altLabel = new JLabel("Altitude");
		
		JLabel latLabel = new JLabel("Latitude");
		
		JLabel longLabel = new JLabel("Longitude");
		
		altitudeTxtFld = new JTextField();
		altitudeTxtFld.setText("0");
		altitudeTxtFld.setHorizontalAlignment(SwingConstants.RIGHT);
		altitudeTxtFld.setEditable(false);
		altitudeTxtFld.setColumns(8);
		
		latitudeTxtFld = new JTextField();
		latitudeTxtFld.setText("0");
		latitudeTxtFld.setHorizontalAlignment(SwingConstants.RIGHT);
		latitudeTxtFld.setEditable(false);
		latitudeTxtFld.setColumns(8);
		
		longTxtFld = new JTextField();
		longTxtFld.setText("0");
		longTxtFld.setHorizontalAlignment(SwingConstants.RIGHT);
		longTxtFld.setEditable(false);
		longTxtFld.setColumns(8);
		
		final JButton connectBtn = new JButton("Connect");
		connectBtn.setEnabled(false);
		
		//Creates and populate the port List
		final JComboBox<String> portList = new JComboBox<String>();			
		SerialPort[] portNames = SerialPort.getCommPorts();
		for(int i = 0; i < portNames.length; i++)
			portList.addItem(portNames[i].getSystemPortName());
		connectBtn.setEnabled(portList.getItemCount()!=0);
			

		// Creating line graph
		final XYSeries accSerie = new XYSeries("Acceleration");
		final XYSeries altSerie = new XYSeries("Altitude");
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(accSerie);
		dataset.addSeries(altSerie);
		
		JFreeChart chart = ChartFactory.createXYLineChart("Rocket Data", "Time (seconds)", "Acceleration/Altitude", dataset);
		ChartPanel chartPanel = new ChartPanel(chart);
		frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
		frame.setBounds(100, 100, 1200, 420);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Allows the user to type location manually
		final JCheckBox manualLocationChckBx = new JCheckBox("Manual");
		manualLocationChckBx.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				//If manualLocationChckBx is selected => set longTxtFld & latitudeTxtFld Editable
					longTxtFld.setEditable(manualLocationChckBx.isSelected());
					latitudeTxtFld.setEditable(manualLocationChckBx.isSelected());
			}
		});	
		
		//Connects the software to the ground station and creates a parallel threat to read the serial port 	
		connectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(connectBtn.getText().equals("Connect")) {
					// attempt to connect to the serial port
					chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
					chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					if(chosenPort.openPort()) {
						connectBtn.setText("Disconnect");
						portList.setEnabled(false);
					}
					
					// create a new thread that listens for incoming text and populates the graph
					Thread thread = new Thread(){
						@Override public void run() {
							Scanner scanner = new Scanner(chosenPort.getInputStream());
							while(scanner.hasNextLine()) {
								try {
									String line = scanner.nextLine();
									String [] data = line.split(",");
									accSerie.add(Integer.parseInt(data[4]), Integer.parseInt(data[0]));
									altSerie.add(Integer.parseInt(data[4]), Integer.parseInt(data[1]));
									altitudeTxtFld.setText(data[1]);
									latitudeTxtFld.setText(data[2]);
									longTxtFld.setText(data[3]);
									frame.repaint();
								} catch(Exception e) {}
							}
							scanner.close();
						}
					};
					thread.start();
				} else {
					// disconnect from the serial port
					chosenPort.closePort();
					portList.setEnabled(true);
					connectBtn.setText("Connect");
				}
			}
		});
		
		JLabel lblM = new JLabel("m");
		
		JLabel lblN = new JLabel("N");
		
		JLabel lblE = new JLabel("E");

		//GUI stuffs Location Region
		GroupLayout gl_locationGroup = new GroupLayout(locationGroup);
		gl_locationGroup.setHorizontalGroup(
			gl_locationGroup.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_locationGroup.createSequentialGroup()
					.addGroup(gl_locationGroup.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_locationGroup.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_locationGroup.createParallelGroup(Alignment.LEADING)
								.addComponent(altLabel)
								.addComponent(latLabel)
								.addComponent(longLabel)
								.addComponent(portList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
							.addGroup(gl_locationGroup.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(connectBtn, 0, 0, Short.MAX_VALUE)
								.addComponent(latitudeTxtFld)
								.addComponent(altitudeTxtFld)
								.addComponent(longTxtFld)))
						.addComponent(manualLocationChckBx))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_locationGroup.createParallelGroup(Alignment.LEADING)
						.addComponent(lblM)
						.addComponent(lblN)
						.addComponent(lblE))
					.addContainerGap())
		);
		gl_locationGroup.setVerticalGroup(
			gl_locationGroup.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_locationGroup.createSequentialGroup()
					.addContainerGap()
					.addComponent(manualLocationChckBx)
					.addGap(7)
					.addGroup(gl_locationGroup.createParallelGroup(Alignment.BASELINE)
						.addComponent(altLabel)
						.addComponent(altitudeTxtFld, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblM))
					.addGap(6)
					.addGroup(gl_locationGroup.createParallelGroup(Alignment.BASELINE)
						.addComponent(latLabel)
						.addComponent(latitudeTxtFld, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblN))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_locationGroup.createParallelGroup(Alignment.BASELINE)
						.addComponent(longLabel)
						.addComponent(longTxtFld, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_locationGroup.createParallelGroup(Alignment.BASELINE)
						.addComponent(connectBtn)
						.addComponent(portList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		locationGroup.setLayout(gl_locationGroup);
		frame.getContentPane().add(westPanel, BorderLayout.WEST);
		
		//Creating and populating Wind Profile region
		JPanel windGroup = new JPanel();
		windGroup.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Wind Profile", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		windGroup.setName("");
		GridBagConstraints gbc_windGroup = new GridBagConstraints();
		gbc_windGroup.fill = GridBagConstraints.HORIZONTAL;
		gbc_windGroup.insets = new Insets(0, 0, 0, 5);
		gbc_windGroup.gridx = 0;
		gbc_windGroup.gridy = 1;
		westPanel.add(windGroup, gbc_windGroup);
		
		JLabel windSpeedLabel = new JLabel("Speed");
		
		JLabel windDirectionLabel = new JLabel("Direction");
		
		JLabel stdDeviationLabel = new JLabel("Std Deviation");
		
		windSpeedTxtFld = new JTextField();
		windSpeedTxtFld.setText("0");
		windSpeedTxtFld.setHorizontalAlignment(SwingConstants.RIGHT);
		windSpeedTxtFld.setEditable(false);
		windSpeedTxtFld.setColumns(8);
		
		windDirTxtFld = new JTextField();
		windDirTxtFld.setText("0");
		windDirTxtFld.setHorizontalAlignment(SwingConstants.RIGHT);
		windDirTxtFld.setEditable(false);
		windDirTxtFld.setColumns(8);
		
		stdDevTxtFld = new JTextField();
		stdDevTxtFld.setHorizontalAlignment(SwingConstants.RIGHT);
		stdDevTxtFld.setText("0.2");
		stdDevTxtFld.setColumns(8);
		
		//Connect to server and get wind profile
		final JButton getWindDataBtn = new JButton("Get Data");
		getWindDataBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				double lat,lon;
				String API;
				try {
					API="8d77e42c64b50633eb0a2dde20f2d765";
					if (!latitudeTxtFld.getText().isEmpty()&&
							!longTxtFld.getText().isEmpty()){
						lat = Double.parseDouble(latitudeTxtFld.getText());
						lon = Double.parseDouble(longTxtFld.getText());
						Wind wind = HTTP_Request.call_me(lat,lon,API);
						windSpeedTxtFld.setText(Double.toString(wind.speed));
						windDirTxtFld.setText(Double.toString(wind.direction));
					}
					else {JOptionPane.showMessageDialog(null, "The Latitude or Longitude values are empty");}
					
				}catch(Exception exp) {
					JOptionPane.showMessageDialog(null, "The Latitude or Longitude values are not numbers");
				}
			}
		});
		
		//Allows the user to type wind parameters manually
		final JCheckBox manualWindChckBx = new JCheckBox("Manual");
		manualWindChckBx.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				//If manualChckBx is selected => Disable Get Button && 
				//set windSpeed & windDir text Field Editable
					getWindDataBtn.setEnabled(!manualWindChckBx.isSelected());
					windSpeedTxtFld.setEditable(manualWindChckBx.isSelected());
					windDirTxtFld.setEditable(manualWindChckBx.isSelected());
				}
			
		});
		
		JLabel lblMs = new JLabel("m/s");
		
		JLabel label = new JLabel("\u00B0");
		
		JLabel lblMs_1 = new JLabel("m/s");
		
		//GUI Stuffs Wind Profile Region
		GroupLayout gl_windGroup = new GroupLayout(windGroup);
		gl_windGroup.setHorizontalGroup(
			gl_windGroup.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_windGroup.createSequentialGroup()
					.addGroup(gl_windGroup.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_windGroup.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_windGroup.createParallelGroup(Alignment.LEADING)
								.addComponent(stdDeviationLabel)
								.addComponent(windDirectionLabel)
								.addComponent(windSpeedLabel))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_windGroup.createParallelGroup(Alignment.LEADING)
								.addComponent(windSpeedTxtFld)
								.addGroup(gl_windGroup.createParallelGroup(Alignment.LEADING, false)
									.addComponent(getWindDataBtn, Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
									.addComponent(windDirTxtFld, Alignment.TRAILING)
									.addComponent(stdDevTxtFld, Alignment.TRAILING))))
						.addComponent(manualWindChckBx))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_windGroup.createParallelGroup(Alignment.LEADING)
						.addComponent(lblMs)
						.addComponent(label)
						.addComponent(lblMs_1))
					.addContainerGap())
		);
		gl_windGroup.setVerticalGroup(
			gl_windGroup.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_windGroup.createSequentialGroup()
					.addContainerGap()
					.addComponent(manualWindChckBx)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_windGroup.createParallelGroup(Alignment.BASELINE)
						.addComponent(windSpeedLabel)
						.addComponent(windSpeedTxtFld, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblMs))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_windGroup.createParallelGroup(Alignment.BASELINE)
						.addComponent(windDirTxtFld, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(windDirectionLabel)
						.addComponent(label))
					.addGap(6)
					.addGroup(gl_windGroup.createParallelGroup(Alignment.BASELINE)
						.addComponent(stdDeviationLabel)
						.addComponent(stdDevTxtFld, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblMs_1))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getWindDataBtn)
					.addContainerGap())
		);
		windGroup.setLayout(gl_windGroup);
		
		//Create MCS area
		JPanel eastPanel = new JPanel();
		frame.getContentPane().add(eastPanel, BorderLayout.EAST);		
		GridBagLayout gbl_eastPanel = new GridBagLayout();
		gbl_eastPanel.columnWidths = new int[]{100};
		gbl_eastPanel.rowHeights = new int[]{0};
		gbl_eastPanel.columnWeights = new double[]{1.0};
		gbl_eastPanel.rowWeights = new double[]{1.0};
		eastPanel.setLayout(gbl_eastPanel);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		eastPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{140, 0};
		gbl_panel.rowHeights = new int[]{50, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JPanel rodPanel = new JPanel();
		rodPanel.setName("");
		rodPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Launching rod", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_rodPanel = new GridBagConstraints();
		gbc_rodPanel.anchor = GridBagConstraints.EAST;
		gbc_rodPanel.insets = new Insets(0, 0, 5, 0);
		gbc_rodPanel.gridx = 0;
		gbc_rodPanel.gridy = 1;
		panel.add(rodPanel, gbc_rodPanel);
		
		JLabel lengthLabel = new JLabel("Length");
		
		JLabel angleLabel = new JLabel("Angle");
		
		JButton button = new JButton("Connect");
		button.setEnabled(false);
		
		JLabel lblCm = new JLabel("cm");
		
		JLabel label_5 = new JLabel("\u00B0");
		
		JSpinner spinner = new JSpinner();
		
		//GUI Stuffs Launching ROD Area
		JSpinner spinner_1 = new JSpinner();
		GroupLayout gl_rodPanel = new GroupLayout(rodPanel);
		gl_rodPanel.setHorizontalGroup(
			gl_rodPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_rodPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_rodPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(lengthLabel)
						.addComponent(angleLabel))
					.addGap(18)
					.addGroup(gl_rodPanel.createParallelGroup(Alignment.LEADING, false)
						.addComponent(spinner_1)
						.addComponent(spinner, GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_rodPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(button, GroupLayout.PREFERRED_SIZE, 0, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_rodPanel.createParallelGroup(Alignment.TRAILING)
							.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblCm, Alignment.LEADING)))
					.addContainerGap())
		);
		gl_rodPanel.setVerticalGroup(
			gl_rodPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_rodPanel.createSequentialGroup()
					.addGap(12)
					.addGroup(gl_rodPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lengthLabel)
						.addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblCm))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_rodPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_rodPanel.createSequentialGroup()
							.addGroup(gl_rodPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(angleLabel)
								.addComponent(spinner_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(button))
						.addComponent(label_5))
					.addContainerGap())
		);
		rodPanel.setLayout(gl_rodPanel);
		
		//Populating Landing Area 
		JPanel landingPanel = new JPanel();
		landingPanel.setName("");
		landingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Landing Site", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_landingPanel = new GridBagConstraints();
		gbc_landingPanel.insets = new Insets(0, 0, 5, 0);
		gbc_landingPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_landingPanel.gridx = 0;
		gbc_landingPanel.gridy = 2;
		panel.add(landingPanel, gbc_landingPanel);
		
		JLabel landingDistLabel = new JLabel("Distance");
		
		JLabel landingLongLabel = new JLabel("Longitude");
		
		JLabel landingLatLabel = new JLabel("Latitude");
		
		landingLatTxt = new JTextField();
		landingLatTxt.setText("0");
		landingLatTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		landingLatTxt.setEditable(false);
		landingLatTxt.setColumns(8);
		
		JButton simulateBtn = new JButton("Simulate");
		
		landingLongTxt = new JTextField();
		landingLongTxt.setText("0");
		landingLongTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		landingLongTxt.setEditable(false);
		landingLongTxt.setColumns(8);
		
		landingDistTxt = new JTextField();
		landingDistTxt.setEditable(false);
		landingDistTxt.setText("0");
		landingDistTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		landingDistTxt.setColumns(8);
		
		JLabel landNLabel = new JLabel("N");
		
		JLabel landELabel = new JLabel("E");
		
		//GUI Stuffs Landing Region
		JLabel landMLabel = new JLabel("m");
		GroupLayout gl_landingPanel = new GroupLayout(landingPanel);
		gl_landingPanel.setHorizontalGroup(
			gl_landingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_landingPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_landingPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(landingLatLabel)
						.addComponent(landingLongLabel)
						.addComponent(landingDistLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_landingPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(simulateBtn, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
						.addGroup(gl_landingPanel.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(landingLatTxt, Alignment.LEADING)
							.addComponent(landingDistTxt, Alignment.LEADING)
							.addComponent(landingLongTxt, Alignment.LEADING, 72, 72, Short.MAX_VALUE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_landingPanel.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(landNLabel, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
						.addComponent(landELabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(landMLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_landingPanel.setVerticalGroup(
			gl_landingPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_landingPanel.createSequentialGroup()
					.addGap(12)
					.addGroup(gl_landingPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(landingLatLabel)
						.addComponent(landingLatTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(landNLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_landingPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(landingLongTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(landingLongLabel)
						.addComponent(landELabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_landingPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(landingDistTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(landingDistLabel)
						.addComponent(landMLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(simulateBtn)
					.addContainerGap())
		);
		landingPanel.setLayout(gl_landingPanel);
		
		Button goBtn = new Button("Send GO");
		GridBagConstraints gbc_goBtn = new GridBagConstraints();
		gbc_goBtn.anchor = GridBagConstraints.EAST;
		gbc_goBtn.gridx = 0;
		gbc_goBtn.gridy = 3;
		panel.add(goBtn, gbc_goBtn);
		
		//Creating and populating State Bar
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		statusPanel.setLayout(new BorderLayout(0, 0));
		frame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		JLabel statusLabel = new JLabel("Not connected");
		statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		statusPanel.add(statusLabel);
		
	}
}
