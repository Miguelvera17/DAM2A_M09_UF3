import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 7777;
    private static ServerSocket srvSocket;
    private static Socket clientSocket;

    public static void connecta() {
        try {
            System.out.println("Esperant connexions a localhost: " + PORT + "...");
            clientSocket = srvSocket.accept();
            System.out.println("Client connectat: " + HOST);
        } catch (IOException ex) {
            System.err.println("Error al acceptar la connexió: " + ex.getMessage());
        }
    }

    public static void repDades() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Rebut: " + message);
            }
        } catch (IOException ex) {
            System.err.println("Error al rebre dades: " + ex.getMessage());
        }
    }

    public static void tanca() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (srvSocket != null && !srvSocket.isClosed()) {
                srvSocket.close();
            }
            System.out.println("Servidor tancat.");
        } catch (IOException ex) {
            System.err.println("Error al tancar el servidor: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            srvSocket = new ServerSocket(PORT);
            System.out.println("Servidor en marxa a localhost: " + PORT);
            connecta();
            repDades();
            tanca();
        } catch (IOException ex) {
            System.err.println("Error al iniciar el servidor: " + ex.getMessage());
        }
    }
}
