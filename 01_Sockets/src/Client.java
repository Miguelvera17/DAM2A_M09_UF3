import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String HOST = "localhost"; // Dirección del servidor
    private static final int PORT = 7777; // Puerto del servidor
    private Socket socket;
    private PrintWriter out;

    public void connect() {
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            System.out.println("Conectado al servidor en " + HOST + ":" + PORT);
        } catch (IOException ex) {
            System.err.println("Error al conectar con el servidor: " + ex.getMessage());
        }
    }

    public void envia(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("Enviado al servidor: " + message);
        }
    }

    public void tanca() {
        try {
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Cliente desconectado.");
        } catch (IOException ex) {
            System.err.println("Error al cerrar el cliente: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.connect();

        client.envia("Prova d'enviament 1");
        client.envia("Prova d'enviament 2");
        client.envia("Adéu!");
        
        System.out.println("Presiona ENTER para salir...");
        Scanner sc = new Scanner(System.in);
        String mensaje = sc.nextLine();
        while (mensaje != null && !mensaje.isEmpty()){
            client.envia(mensaje);
            mensaje = sc.nextLine(); 
        }
        client.tanca();
        sc.close();
    }
}
