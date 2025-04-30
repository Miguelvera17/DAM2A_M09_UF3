import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String DIR_ARRIBADA = "/tmp/";
    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    public Socket connectar() throws IOException {
        Socket socket = new Socket(HOST, PORT);
        System.out.println("Connectant a -> " + HOST + ":" + PORT);
        System.out.println("Connexió acceptada: " + socket.getInetAddress());
        return socket;
    }

    public void rebreFitxers(Socket socket) {
        try {
            Scanner scanner = new Scanner(System.in);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
    
            System.out.print("Nom del fitxer a rebre ('sortir' per sortir): ");
            while (true) {
                String nomFitxer = scanner.nextLine();
    
                if (nomFitxer.equalsIgnoreCase("sortir")) {
                    dos.writeUTF(""); 
                    break;
                }
    
                dos.writeUTF(nomFitxer);
                dos.flush();
    
                System.out.println("Nom del fitxer a guardar: " + DIR_ARRIBADA + new File(nomFitxer).getName());
    
                byte[] contingut = (byte[]) ois.readObject();
                if (contingut != null) {
                    FileOutputStream fos = new FileOutputStream(DIR_ARRIBADA + new File(nomFitxer).getName());
                    fos.write(contingut);
                    fos.close();
                    System.out.println("Fitxer rebut i guardat com: " + DIR_ARRIBADA + new File(nomFitxer).getName());
                } else {
                    System.out.println("Fitxer no trobat o buit al servidor.");
                }
            }
    
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void tancarConnexio(Socket socket) {
        try {
            if (socket != null) socket.close();
            System.out.println("Sortint...");
            System.out.println("Connexió tancada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {
            Socket socket = client.connectar();
            client.rebreFitxers(socket);
            client.tancarConnexio(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
