import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Empfänger {

    private final int PORT = 4495;
    private final int BUFSIZE = 512;
    private final int TIMEOUT = 3000;

    DatagramSocket socket;
    private DatagramPacket packetIn;
    private DatagramPacket packetOut;
    private boolean handshakeSend= false;
    int handshakeNumber = 25; //Anzahl der zu erwartenden Anfragen
    int counter=0;

    public Empfänger () {

    }

    public void start() {

        try {
            socket = new DatagramSocket(PORT);
            packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
            packetOut= new DatagramPacket(new byte[BUFSIZE], BUFSIZE);

            System.out.println("Server wird getsartet...");
                receiveData();
            }  catch (SocketException ex) {
            ex.printStackTrace();
        }
        catch (IOException exc) {
            exc.printStackTrace();
        }

    }

    private void handshake(int handshakeNumber) {
        try {
            socket.setSoTimeout(0);
            socket.receive(packetIn);
            packetOut.setData(convertDataInt(handshakeNumber));
            packetOut.setLength(convertDataInt(handshakeNumber).length);
            packetOut.setSocketAddress(packetIn.getSocketAddress());
            socket.send(packetOut);
            handshakeSend = true;

        } catch (IOException e) {
            e.printStackTrace();


        }
    }

    private byte[] convertDataInt (int daten) {
        return new String(daten + "").getBytes();
    }

    private void receiveData() throws IOException {

        int handshakeCounter = 0;
        while (true) {
            try {
                if (!handshakeSend) {
                    handshake(handshakeNumber);
                }
                socket.setSoTimeout(TIMEOUT);
                socket.receive(packetIn);
                System.out.println("Empfangen " + packetIn.getLength() + "bytes: " + new String(packetIn.getData()));
                handshakeCounter++;

                if (handshakeNumber == handshakeCounter) {
                    System.out.print("Alle " + handshakeNumber +  " Pakete wurden erfolgreich gesendet");
                    handshakeCounter = 0;
                    handshakeSend = false;
                }




            }catch (SocketTimeoutException e ) { //Sobal der Client keine Daten mehr sendet
                handshakeSend = false;
                float percent =  (handshakeCounter* 100.0f) / handshakeNumber;
                System.out.println("Es sind nur " + handshakeCounter + "/" + handshakeNumber + " Pakete angekommen (" + percent + "%)");

                handshakeCounter =0;
            }
        }
    }





 public static void main(String [] args) {
      Empfänger server = new Empfänger();
      server.start();
 }

}
