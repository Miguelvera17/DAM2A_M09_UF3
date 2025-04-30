import java.io.*;
import java.net.*;

public class Servidor {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private ServerSocket serverSocket;

    public Socket connectar() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Acceptant connexions en -> " + HOST + ":" + PORT);
        System.out.println("Esperant connexió...");
        Socket socket = serverSocket.accept();
        System.out.println("Connexió acceptada: " + socket.getInetAddress());
        return socket;
    }

    public void enviarFitxers(Socket socket) {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Esperant el nom del fitxer del client...");
            while (true) {
                String nomFitxer;
                try {
                    nomFitxer = dis.readUTF();
                } catch (EOFException e) {
                    nomFitxer = null;
                }
    
                if (nomFitxer == null || nomFitxer.trim().isEmpty()) {
                    System.out.println("Error llegint el fitxer del client: null");
                    System.out.println("Nom del fitxer buit o nul. Sortint...");
                    break;
                }
    
                System.out.println("NomFitxer rebut: " + nomFitxer);
    
                Fitxer fitxer = new Fitxer(nomFitxer);
                byte[] dades = fitxer.getContingut();
    
                if (dades == null) {
                    System.out.println("Error llegint el fitxer del client: null");
                    oos.writeObject(null);
                } else {
                    System.out.println("Contingut del fitxer a enviar: " + dades.length + " bytes");
                    oos.writeObject(dades);
                    System.out.println("Fitxer enviat al client: " + nomFitxer);
                }
            }
    
        } catch (IOException e) {
            System.out.println("Error llegint el fitxer del client: null");
            System.out.println("Nom del fitxer buit o nul. Sortint...");
        }
    }
    
    

    public void tancarConnexio(Socket socket) {
        try {
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
            System.out.println("Tancant connexió amb el client " + socket.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        try {
            Socket socket = servidor.connectar();
            servidor.enviarFitxers(socket);
            servidor.tancarConnexio(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}