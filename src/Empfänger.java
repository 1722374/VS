import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

public class Empfänger {

    private final int PORT = 4495;
    private final int BUFSIZE = 512;
    private final int TIMEOUT = 3000;

    DatagramSocket socket;
    private DatagramPacket packetIn;
    private DatagramPacket packetOut;
    private boolean handshakeSend= false;
    int handshakeNumber = 5; //Anzahl der zu erwartenden Anfragen

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
        int corruptPacket = 0;
        String receivedPackages = "";
        while (true) {
            try {
                if (!handshakeSend) {
                    handshake(handshakeNumber);
                }
                socket.setSoTimeout(TIMEOUT);
                socket.receive(packetIn);

                System.out.println("Empfangen " + packetIn.getLength() + "bytes: " + new String(packetIn.getData()));
                byte[] result= new byte[packetIn.getLength()];

                byte[] clientDataWithoutChecksum= new byte[packetIn.getLength()-9];
                byte[] checksumFromclient= new byte[9];

                for(int i=0; i<packetIn.getLength(); i++) { // Bytearray bis zum Checksum lesen und in Array speichern (Eigentliche Daten)
                    if (i< packetIn.getLength() -9 ) {
                        clientDataWithoutChecksum[i] = packetIn.getData()[i];
                    }
                    else{
                        checksumFromclient [i - packetIn.getLength() +9] = packetIn.getData()[i]; //checksum aus Datenpaket rauslesen (Client)
                    }
                }

               if(compare(checksumFromclient,clientDataWithoutChecksum)) {
                   corruptPacket ++;
               }

                handshakeCounter++;

                if (handshakeNumber == handshakeCounter) {
                    output(handshakeCounter, corruptPacket);
                    handshakeSend = false;
                    corruptPacket = 0;
                    handshakeCounter = 0;
                }




            }catch (SocketTimeoutException e ) { //Sobal der Client keine Daten mehr sendet
                output(handshakeCounter, corruptPacket);
                handshakeSend = false;
                corruptPacket = 0;
                handshakeCounter =0;
            }

        }
    }


   /* private boolean compare(byte checksumClient, byte[] dataWithoutChecksum) {
        //Vergleicht CheckSum mit Sender Checksum
        ByteArrayInputStream byteStream = new ByteArrayInputStream(dataWithoutChecksum);
        CheckedInputStream checkdStream = new CheckedInputStream(byteStream, new CRC32());
        Long checksum = checkdStream.getChecksum().getValue();

        if (checksumClient != checksum.byteValue()) {
            return true;
        }
        return false;
    } */

   private boolean compare(byte[] checksumClient, byte[] dataWithoutChecksum) {
       //Vergleicht CheckSum mit Sender Checksum
       Checksum adler32 = new Adler32();
       adler32.update(dataWithoutChecksum,0, dataWithoutChecksum.length);
       long checksum = adler32.getValue();
       long checksumClientL = Long.parseLong(new String (checksumClient).trim());


       if (checksumClientL != checksum) {
           return true;
       }
       return false;
   }


     private void output (int handshakeCounter, int corruptedPackages) {


        if (handshakeCounter >0){
            float percentReceived =  (handshakeCounter* 100.0f) / handshakeNumber;
            String received = ("Es wurden " + handshakeCounter + "/" + handshakeNumber + " Pakete empfangen (" + percentReceived + "%)");
            System.out.println(received);

        } else {
            String received = ("Es wurden " + handshakeCounter + "/" + handshakeNumber + " Pakete empfangen (0%)");
            System.out.print(received);
        }





        if (corruptedPackages > 0 ) {
            float percentCorrupt =  (corruptedPackages* 100.0f) / handshakeNumber;
            String corrupt = ("Es waren " + handshakeCounter + "/" + handshakeNumber + " Pakete verfälscht (" + percentCorrupt + "%)");
            System.out.println(corrupt);
        } else {
            String corrupt = ("Es waren " + handshakeCounter + "/" + handshakeNumber + " Pakete verfälscht (0%)");
            System.out.println(corrupt);
        }

     }



 public static void main(String [] args) {
      Empfänger server = new Empfänger();
      server.start();
 }

}
