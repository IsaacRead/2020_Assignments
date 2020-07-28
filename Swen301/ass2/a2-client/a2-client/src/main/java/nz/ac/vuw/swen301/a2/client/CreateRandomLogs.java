package nz.ac.vuw.swen301.a2.client;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
public class CreateRandomLogs {

	public static void main(String[] args) {
		
		Logger logger = Logger.getLogger("clientLogger");
		logger.setAdditivity(false);
		logger.setLevel(Level.ALL);
		logger.addAppender(new Resthome4LogsAppender());
		Random rand = new Random();
		while(true) {
			int levelInt = rand.nextInt(6);		
			 String message= Double.toString(rand.nextDouble());
			switch (levelInt) {
			case 0:
				logger.trace(message);
				break;
			case 1:
				logger.debug(message);
				break;
			case 2:
				logger.info(message);
				break;
			case 3:
				logger.warn(message);
				break;
			case 4:
				logger.error(message);
				break;
			case 5:
				logger.fatal(message);
				break;
			default:
				break;
			
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

	}

}
