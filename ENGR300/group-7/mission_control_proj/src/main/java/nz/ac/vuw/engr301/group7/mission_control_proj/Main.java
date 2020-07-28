package nz.ac.vuw.engr301.group7.mission_control_proj;

import java.awt.EventQueue;

public class Main {

	// Launch the application.
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MissionControl window = new MissionControl();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


}
