import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientXat {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connecta() {
        try {
            socket = new Socket("localhost", 9999);
            System.out.println("Client connectat a localhost:9999");
            System.out.println("Flux dentrada i sortida creat");
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error al connectar al servidor: " + e.getMessage());
        }
    }

    public void enviarMissatge(String missatge) {
        try {
            out.writeObject(missatge);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error al enviar missatge: " + e.getMessage());
        }
    }

    public void tancarClient() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();
        } catch (IOException e) {
            System.err.println("Error al tancar client: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ClientXat client = new ClientXat();
        client.connecta();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Missatge ('sortir' per tancar): Fil de lectura iniciat");
        System.out.println("Rebut: Escriu el teu nom:");
        String nom = scanner.nextLine();
        System.out.println(nom); // Print the user input
        client.enviarMissatge(nom);
        System.out.println("Enviant missatge: " + nom); // Print "Enviant missatge:"

        String entrada;
        while (!(entrada = scanner.nextLine()).equalsIgnoreCase("sortir")) {
            System.out.println("Missatge ('sortir' per tancar): " + entrada);  // Print the user input
            client.enviarMissatge(entrada);
            System.out.println("Enviant missatge: " + entrada); // Print "Enviant missatge:"
        }

        System.out.println("sortir"); // Print the user input
        client.enviarMissatge("sortir");
        System.out.println("Enviant missatge: sortir"); // Print "Enviant missatge:"
        client.tancarClient();
        System.out.println("Tancant client...");
        System.out.println("Client tancat.");
        System.out.println("El servidor ha tancat la connexió.");
        scanner.close();
    }
}