import java.net.*;
import java.io.IOException;

public class RecThread extends Thread{

public MulticastSocket s;
 
	public RecThread(MulticastSocket socket){
		s = socket;
	}
	
	public void run(){
		
		try {
			while(true){
				byte[] buf = new byte[1000];
			DatagramPacket dp = new DatagramPacket(buf, buf.length);
				s.receive(dp);
				String rec = new String(dp.getData(), 0, dp.getLength());
				System.out.println(dp.getAddress() + ": " + rec);
			
			}
		
		
		}catch(Exception ex){
			System.out.println(ex);
		}
		
	}


}