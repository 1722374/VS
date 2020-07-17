import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Server {
    public static long lossCounter = 0;
    public static long errorCounter = 0;
    private static final int PORT = 4711;
    private static final int BUFSIZE = 512;
    private static  Checksum checksum = null;


    public static void main(String[] args) {
        try (DatagramSocket s = new DatagramSocket(PORT)) {
            DatagramPacket in = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
            DatagramPacket out = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
            System.out.println("Server wird gestartet.....");
            checksum = new Adler32();
            String sumCheckString = new String();
            String error = "Datenpaket war fehlerhaft ";
            String loss  = "Datenpaket ist nicht angekommen ";
            String correct = "Datenpaket ist korrekt angekommen ";
            long sum = 0;

            while (true) {  //Server läuft immer weiter
                s.receive(in);


                System.out.println("Received: " + in.getLength() + " bytes: " + new String(in.getData()));
                String packetString = new String (in.getData());

                if (packetString.contains("Check")){

                    long sumCheck = Long.parseLong(packetString.split("\\s+")[1].trim()); // Sumcheck aus String auslesen und zu Long konvertieren

                    if (sumCheck == sum){ //Wenn Datenpaket gleich ist
                        out.setData(correct.getBytes());
                        out.setLength(correct.getBytes().length);
                        out.setSocketAddress(in.getSocketAddress());
                    }
                    else { // Wenn Datenpaket nicht gleich anegkommenes Paket ist
                        errorCounter++;
                        out.setData(error.getBytes());
                        out.setLength(error.getBytes().length);
                        out.setSocketAddress(in.getSocketAddress());

                    }
                    System.out.println("Rausgesendet");
                    checksum.reset();
                    s.send(out);

                }

                else {
                    sum = dataCheckSum(in.getData(), in.getLength());
                    System.out.println("Server sum " + sum);


                }



            }


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    private static long dataCheckSum(byte[] message, int length){
        checksum.update (message, 0, length);
        return checksum.getValue();
    }





}


