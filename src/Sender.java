import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class Sender {

    private final String HOST = "localhost";
    private final int PORT = 4495;
    private final int BUFSIZE = 512;
    private final int TIMEOUT = 2000;
    private DatagramSocket socket;
    private DatagramPacket packetOut;
    private DatagramPacket packetIn;
    private boolean handshakeReceived = false;
    int handshakeCounter = 0; //Anzahl der zu erwartenden Anfragen
    int counter = 0;

    Sender() {

    }

    public void start() {

        try {
            InetAddress iaddr = InetAddress.getByName(HOST);
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT);
            packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
            packetOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE, iaddr, PORT);
            if (!handshakeReceived) {
                handshake();
            }

            sendData(handshakeCounter);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private byte[] randomData() {
        double random = 1.2 + Math.random() * (41.23 - 1.2);
        return new String(random +"").getBytes();
    }


    private void handshake () throws IOException {
        //Empfänger (Server) anpingen
        byte [] ping = new byte[1];
        packetOut.setData(ping);
        packetOut.setLength(ping.length);
        socket.send(packetOut);

        socket.receive(packetIn);
        int data = Integer.parseInt(new String (packetIn.getData()).trim());
        this.handshakeCounter = data;
        this.handshakeReceived = true;
        System.out.println("Übertragener Handshake Counter: " + data);
    }

    private void sendData(int handshakeCounter) throws IOException, InterruptedException {
        for (int i = 0; i< handshakeCounter; i++) {
            byte [] data = randomData();
            Thread.sleep(20);
            byte[] outputData = addChecksum(data);
            packetOut.setData(outputData);
            packetOut.setLength(outputData.length);
            socket.send(packetOut);
            System.out.println("Gesendet :" + new String(data) + " Index "+ i);
        }
    }

    private byte[] addChecksum (byte [] data) {
        //Daten mit Checksum Wert anreichern
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        CheckedInputStream cs = new CheckedInputStream(is, new CRC32());
        Long checksum=cs.getChecksum().getValue();

        byte[] dataWithCheck= new byte[data.length+1];
        for(int i=0; i<data.length; i++) {
            dataWithCheck[i]=data[i];
        }
        dataWithCheck[dataWithCheck.length-1]= checksum.byteValue();
        System.out.println("Checksum: " + checksum);
        return dataWithCheck;
    }

    public static void main(String[] args) {
        Sender client = new Sender();
        client.start();
    }





}
