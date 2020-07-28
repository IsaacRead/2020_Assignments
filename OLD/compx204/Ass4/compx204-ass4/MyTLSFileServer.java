import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.naming.ldap.*;
import javax.net.*;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.*;

public class MyTLSFileServer {

    public static void main(String args[]) {
        SSLServerSocket sserverSocket = null;
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            /**
             * store the passphrase to unlock the JKS file.
             **/
            char[] passphrase = "compx204".toCharArray();
            /**load the keystore file **/
            ks.load(new FileInputStream("server.jks"), passphrase);
            /**
             * use the KeyManager Class to manage the key
             **/
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, passphrase);
            /**
             * Get an SSL Context that speaks some version of TLS
             */
            SSLContext ctx = SSLContext.getInstance("TLS");
            /** initialise the SSL context with the keys. **/
            ctx.init(kmf.getKeyManagers(), null, null);

            ServerSocketFactory ssf = ctx.getServerSocketFactory();
            sserverSocket = (SSLServerSocket) ssf.createServerSocket(9991);
            String EnabledProtocols[] = {"TLSv1.2", "TLSv1.1"};
            sserverSocket.setEnabledProtocols(EnabledProtocols);
            System.out.println("waiting on connection...");

            SSLSocket sclient = (SSLSocket) sserverSocket.accept();//blocking
            System.out.println("server accepted");
            BufferedReader buffReader = new BufferedReader( new InputStreamReader(sclient.getInputStream()));

           // buffReader.readLine();
            DataInputStream dis=new DataInputStream(sclient.getInputStream());
            String filename = dis.readUTF();



            try {
                DataOutputStream dos = new DataOutputStream(sclient.getOutputStream());
                FileInputStream fis = new FileInputStream(filename);
                System.out.println("got here too");
                System.out.println(fis.getChannel().size());
                int read = 0;
                byte[] buffer = new byte[4096];
                while ((read = fis.read(buffer)) > 0) {
                    dos.write(buffer,0,read);
                }

                fis.close();
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        catch(Exception ex){
            System.out.println(ex);
        }
        finally {
            if(sserverSocket != null){
                System.out.println("closing server socket");
                try {
                    sserverSocket.close();
                }
                catch (Exception ex){
                    System.out.println(ex);
                }

            }
            else{
                System.out.println("server socket did not open");
            }
        }

    }

}