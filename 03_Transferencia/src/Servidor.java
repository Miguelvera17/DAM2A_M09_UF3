import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader entradaCliente;
    private PrintWriter salidaCliente;
    private DataOutputStream dosCliente;
    private BufferedReader entradaServidorConsola = new BufferedReader(new InputStreamReader(System.in));
    private volatile boolean running = true;

    public void iniciarServidor() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor acceptant connexions en -> " + HOST + ":" + PORT);

            while (running) {
                System.out.println("Esperant connexió...");
                clientSocket = serverSocket.accept();
                System.out.println("Connexió acceptada: " + clientSocket.getInetAddress().getHostAddress());

                entradaCliente = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                salidaCliente = new PrintWriter(clientSocket.getOutputStream(), true);
                dosCliente = new DataOutputStream(clientSocket.getOutputStream());

                // Iniciar hilo para leer comandos del servidor desde la consola
                Thread leerConsolaServidor = new Thread(this::leerComandosServidor);
                leerConsolaServidor.start();

                // Bucle para manejar la comunicación con el cliente conectado
                while (running && clientSocket.isConnected()) {
                    System.out.println("Esperant el nom del fitxer del client...");
                    String nomFitxerClient = entradaCliente.readLine();

                    if (nomFitxerClient == null || nomFitxerClient.equalsIgnoreCase("sortir")) {
                        System.out.println("El client ha tancat la connexió o ha sol·licitat sortir.");
                        break;
                    }

                    System.out.println("Nomfitxer rebut del client: " + nomFitxerClient);

                    Fitxer fitxerAEnviar = new Fitxer(nomFitxerClient);
                    byte[] contingutFitxer = fitxerAEnviar.getContingut();

                    if (contingutFitxer != null) {
                        System.out.println("Contingut del fitxer a enviar: " + contingutFitxer.length + " bytes");
                        salidaCliente.println("FICHERO"); // Indicar al cliente que se va a enviar un fichero
                        salidaCliente.println(fitxerAEnviar.getNom());
                        dosCliente.writeLong(contingutFitxer.length);
                        dosCliente.write(contingutFitxer);
                        dosCliente.flush();
                        System.out.println("Fitxer enviat al client: " + fitxerAEnviar.getNom());
                    } else {
                        System.err.println("Error llegint el fitxer del client: null");
                        salidaCliente.println("ERROR_FICHERO"); // Indicar al cliente que hubo un error
                    }
                }
                tancarConnexioClient(); // Cerrar la conexión con el cliente actual
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tancarServidor();
        }
    }

    private void leerComandosServidor() {
        try {
            while (running) {
                System.out.println("Introduce un comando ('sortir' para cerrar el servidor):");
                String comandoServidor = entradaServidorConsola.readLine();
                if (comandoServidor != null && comandoServidor.equalsIgnoreCase("sortir")) {
                    running = false;
                    if (salidaCliente != null) {
                        salidaCliente.println("sortir"); // Informar al cliente para que cierre
                    }
                    break;
                } else if (comandoServidor != null && !comandoServidor.isEmpty()) {
                    // Aquí puedes agregar lógica para otros comandos del servidor si los necesitas
                    System.out.println("Comando del servidor rebut: " + comandoServidor);
                    if (salidaCliente != null) {
                        salidaCliente.println("SERVIDOR_OK"); // Enviar una respuesta genérica al cliente
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
                System.err.println("Error al leer comandos del servidor.");
            }
        } finally {
            tancarServidor();
        }
    }

    public void tancarConnexioClient() {
        try {
            if (salidaCliente != null) salidaCliente.close();
            if (entradaCliente != null) entradaCliente.close();
            if (dosCliente != null) dosCliente.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                System.out.println("Tancant connexió amb el client: " + clientSocket.getInetAddress().getHostAddress());
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tancarServidor() {
        try {
            running = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                System.out.println("Tancant el servidor.");
                serverSocket.close();
            }
            if (entradaServidorConsola != null) entradaServidorConsola.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciarServidor();
    }
}