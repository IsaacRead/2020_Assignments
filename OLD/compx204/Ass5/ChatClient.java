import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ChatClient{

	public static void main(String[] args){
	
		try{
			Scanner scanner = new Scanner(System.in);
			InetAddress group = InetAddress.getByName("239.0.202.1");
			MulticastSocket s = new MulticastSocket(32778);
			s.joinGroup(group);
			RecThread t = new RecThread(s);
			t.start();
			while(true){
				String input = scanner.next();
				DatagramPacket msg = new DatagramPacket(input.getBytes(), input.length(), group, 32778);
				s.send(msg);
				
				
			}


		}catch(Exception ex){
			System.out.println(ex);		
		}


	}


}
