
import netscape.javascript.JSObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;



public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 4711;
    private static final int BUFSIZE = 512;
    private static final int TIMEOUT = 4000;
    private static  DatagramPacket packetOut = null;
    private static  DatagramPacket packetOut2 = null;

    public static void main(String[] args) {
       String[]array = new String [2];

        Checksum checksum = new Adler32();
        byte[] data = args[0].getBytes();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT); // Zeit in ms, für wie lange ein read() auf socket blockiert.
            // Bei timeout is java.net.SocketTimeoutException (TIMEOUT == 0
            // => blockiert für immer)
            InetAddress iaddr = InetAddress.getByName(HOST);
            // checksum klakulieren
            long value = 0;
            packetOut = new DatagramPacket(data, data.length, iaddr, PORT);
            packetOut2 = new DatagramPacket(new byte[BUFSIZE], BUFSIZE, iaddr, PORT);
            DatagramPacket packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
            boolean bool = true;
            while (bool) {
                Thread.sleep(2000);
                System.out.println("Vorher " + new String(data));
                checksum.update(data,0, data.length);
                value = checksum.getValue();
                byte[] sumStringData = ("Check " + value).getBytes(); // sumcheck als String und dann zu Byte
                System.out.println("Danach " +  new String(data));
                System.out.println(value);
                socket.send(packetOut);
                Thread.sleep(1000);
                setData(sumStringData);
                System.out.println( new String (sumStringData));
                socket.send(packetOut2);
                checksum.reset();
                socket.receive(packetIn);
                String received = new String(packetIn.getData(), 0, packetIn.getLength());
                System.out.println("Received: " + received + "Paketgröße " + packetIn.getLength());


                bool = true;
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout: " + e.getMessage());
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void setData(byte[] data){
        packetOut2.setData(data);
        packetOut2.setLength(data.length);

    }
}




