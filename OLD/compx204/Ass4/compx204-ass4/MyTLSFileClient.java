import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.*;
import javax.naming.ldap.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.*;
import java.security.cert.X509Certificate;
public class MyTLSFileClient{
    public static void main(String args[])
    {
        SSLSocket ssocket = null;
        try {
            String hostname = args[0];
            int port = Integer.parseInt(args[1]);
            String filename = args[2];

            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            ssocket = (SSLSocket) factory.createSocket(hostname, port);

            SSLParameters params= new SSLParameters();
            params.setEndpointIdentificationAlgorithm("HTTPS");
            ssocket.setSSLParameters(params);
            ssocket.startHandshake();

            /* get the X509Certificate for this session */
            SSLSession sesh = ssocket.getSession();
            X509Certificate cert = (X509Certificate)sesh.getPeerCertificates()[0];
            /* extract the CommonName, and then compare */
            displayCert(cert);

            DataOutputStream dos = new DataOutputStream(ssocket.getOutputStream());
            dos.writeUTF(filename);

                try {
                    DataInputStream dis = new DataInputStream(ssocket.getInputStream());
                    FileOutputStream fos = new FileOutputStream("_" + filename);
                    byte[] buffer = new byte[4096];
                    int read = 0;
                    int totalRead = 0;

                    while((read = dis.read(buffer, 0, buffer.length )) > 0){
                        totalRead += read;
                        //byte[] buffer1 = new byte[read];
                        System.out.println("read " + totalRead + " bytes.");
                        fos.write(buffer, 0, read);
                    }
                    fos.close();
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
        catch (Exception ex){
            System.out.println(ex);
        }
        finally {
            if(ssocket != null){
                System.out.println("closing client socket");
                try {
                    ssocket.close();
                }
                catch (Exception ex){
                    System.out.println(ex);
                }
            }
            else{
                System.out.println("client socket did not open");
            }
        }

    }

    public static void displayCert(X509Certificate cert)
    {
        try {
            String name = cert.getSubjectX500Principal().getName();
            LdapName ln = new LdapName(name);
            for (Rdn rdn : ln.getRdns())
                System.out.println(rdn.getValue().toString());
        }
        catch(Exception ex){
            System.out.println(ex);
        }

    }
}
