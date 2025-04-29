import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorXat {

    private static final int PORT = 9999;
    private static final String HOST = "localhost"; // Changed to localhost
    private static final String MSG_SORTIR = "sortir";

    private ServerSocket srvSocket;
    private Socket clientSocket;

    public void iniciarServidor() {
        try {
            srvSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciat a " + HOST + ":" + PORT); // Changed output
        } catch (IOException e) {
            System.err.println("No s'ha pogut iniciar el servidor: " + e.getMessage());
        }
    }

    public void pararServidor() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (srvSocket != null && !srvSocket.isClosed()) {
                srvSocket.close();
            }
            System.out.println("Servidor aturat."); // Changed output
        } catch (IOException e) {
            System.err.println("Error en tancar el servidor: " + e.getMessage());
        }
    }

    public String getNom(ObjectInputStream in) {
        try {
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("No s'ha pogut llegir el nom del client: " + e.getMessage());
        }
        return "Client";
    }

    public static void main(String[] args) {
        ServidorXat servidor = new ServidorXat();
        servidor.iniciarServidor();

        try {
            servidor.clientSocket = servidor.srvSocket.accept();
            System.out.println("Client connectat: " + servidor.clientSocket.getInetAddress().getHostAddress()); // Changed output

            ObjectOutputStream out = new ObjectOutputStream(servidor.clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(servidor.clientSocket.getInputStream());

            String nomClient = servidor.getNom(in);
            System.out.println("Nom rebut: " + nomClient);
            System.out.println("Fil de xat creat."); // Added line

            FilServidorXat fil = new FilServidorXat(in, nomClient);
            System.out.println("Fil de " + nomClient + " iniciat"); // Added line
            fil.start();

            BufferedReader teclat = new BufferedReader(new InputStreamReader(System.in));
            String entrada;
            while (!(entrada = teclat.readLine()).equalsIgnoreCase(MSG_SORTIR)) {
                System.out.println("Missatge ('sortir' per tancar): Rebut: " + entrada); // Changed output
                out.writeObject(entrada);
                out.flush();
                System.out.println("Servidor: Enviat: " + entrada); // Added line
            }

            System.out.println("Fil de xat finalitzat."); // Added line
            System.out.println("sortir"); // Added line
            fil.join();
            servidor.pararServidor();

        } catch (IOException | InterruptedException e) {
            System.err.println("Error en la comunicació: " + e.getMessage());
        }
    }
}