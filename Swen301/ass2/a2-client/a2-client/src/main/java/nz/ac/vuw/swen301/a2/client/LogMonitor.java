package nz.ac.vuw.swen301.a2.client;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;
import java.awt.GridBagConstraints;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;

public class LogMonitor extends JFrame {

	private JPanel contentPane;
	private JTextField txtLimit;
	private JTable tblLogs;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LogMonitor frame = new LogMonitor("LOG Monitor");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LogMonitor(String title) {
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 546, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblMinLevel = new JLabel("min level:");
		String[] levels = {"All", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"};

		JComboBox cmbMinLevel = new JComboBox(levels);
		
		JLabel lblLimit = new JLabel("limit:");

		txtLimit = new JTextField();
		txtLimit.setText("100");
		txtLimit.setColumns(10);

		JButton btnFetchData = new JButton("FETCH DATA");

		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblMinLevel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(cmbMinLevel, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblLimit)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(txtLimit, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
							.addGap(31)
							.addComponent(btnFetchData))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(19)
							.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)))
					.addGap(42))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMinLevel)
						.addComponent(cmbMinLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnFetchData)
						.addComponent(lblLimit)
						.addComponent(txtLimit, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
					.addContainerGap())
		);

		tblLogs = new JTable();
		scrollPane.setViewportView(tblLogs);
		contentPane.setLayout(gl_contentPane);
		
		btnFetchData.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				try {

					String limitStr = txtLimit.getText();
					String levelStr = cmbMinLevel.getSelectedItem().toString();
					//System.out.println(levelStr);
					//Level level = Level.toLevel(levelStr);
					int limitInt  = Integer.valueOf( limitStr);
					//System.out.println(limitInt);
					HttpClient httpclient = HttpClients.createDefault();
					URIBuilder builder = new URIBuilder("http://localhost:8080/resthome4logs/logs");
					builder.setParameter("level", levelStr).setParameter("limit", String.valueOf(limitInt));

					HttpGet httpget = new HttpGet(builder.build());

					HttpResponse httpresponse = httpclient.execute(httpget);

					HttpEntity entity = httpresponse.getEntity();
					System.out.println(httpresponse.getStatusLine().getStatusCode());
					// Read the contents of an entity and return it as a String.
					String content = EntityUtils.toString(entity);
					System.out.println(content);
					long lenght = entity.getContentLength();

					JSONArray logArr = new JSONArray(content);

					String col[] = { "time", "level", "logger", "thread", "message" };

					DefaultTableModel tableModel = new DefaultTableModel(col, 0); // The 0 argument is number rows.

					tblLogs.setModel(tableModel);
					for (int i = 0; i < logArr.length(); i++) {
						JSONObject log = (JSONObject) logArr.get(i);
						Object[] objs = { log.get("timestamp"), log.get("level"), log.get("logger"), log.get("thread"), log.get("message") };
						tableModel.addRow(objs);
					}
					

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}
}
