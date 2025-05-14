// GestorClient.java
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class GestorClient implements Runnable {
    private Socket clientSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private ServidorXat servidor;
    private String nom;
    private boolean sortir;

    public GestorClient(Socket socket, ServidorXat servidorXat) {
        this.clientSocket = socket;
        this.servidor = servidorXat;
        this.sortir = false;
        try {
            // IMPORTANT: Crear ObjectOutputStream PRIMER i fer flush abans de crear ObjectInputStream
            this.oos = new ObjectOutputStream(clientSocket.getOutputStream());
            this.oos.flush(); // Assegura que la capçalera de l'stream s'envia
            this.ois = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error inicialitzant streams per client " + socket.getInetAddress() + ": " + e.getMessage());
            this.sortir = true; // No podem operar sense streams
        }
    }

    public String getNom() {
        return nom;
    }

    @Override
    public void run() { // Mètode d'execució
        String missatgeCru;
        try {
            while (!sortir && !servidor.isSortir()) {
                missatgeCru = (String) ois.readObject();
                if (missatgeCru != null) {
                    processaMissatge(missatgeCru);
                } else {
                    // Normalment no hauria de passar amb readObject si el client es desconnecta bé
                    System.out.println("Rebut missatge null de " + (nom != null ? nom : clientSocket.getInetAddress()) + ". Possible desconnexió abrupta.");
                    sortir = true;
                }
            }
        } catch (EOFException e) {
            System.out.println("Client " + (nom != null ? nom : clientSocket.getInetAddress()) + " ha tancat la connexió.");
            sortir = true;
        } catch (SocketException e) {
            System.out.println("Error de socket amb " + (nom != null ? nom : clientSocket.getInetAddress()) + ": " + e.getMessage() + ". Possible desconnexió.");
            sortir = true;
        } catch (IOException e) {
            if (!sortir) { // Només si no és una sortida esperada
                System.err.println("Error llegint missatge de " + (nom != null ? nom : clientSocket.getInetAddress()) + ": " + e.getMessage());
            }
            sortir = true;
        } catch (ClassNotFoundException e) {
            System.err.println("Error de format de missatge (ClassNotFoundException) de " + (nom != null ? nom : clientSocket.getInetAddress()) + ": " + e.getMessage());
            sortir = true;
        } finally {
            if (nom != null) { // Si el client s'havia connectat amb nom
                servidor.eliminarClient(nom);
            }
            tancarRecursos();
        }
    }

    // Aquest mètode s'utilitza per enviar missatges DES DEL SERVIDOR CAP A AQUEST CLIENT
    public void enviarMissatgeAlClient(String missatgeComplet) throws IOException {
        if (oos != null && !clientSocket.isOutputShutdown()) {
            oos.writeObject(missatgeComplet);
            oos.flush();
        } else {
            throw new IOException("ObjectOutputStream no està disponible o tancat.");
        }
    }


    private void processaMissatge(String missatgeCru) {
        String codi = Missatge.getCodiMissatge(missatgeCru);
        String[] parts = Missatge.getPartsMissatge(missatgeCru);

        if (codi == null || parts == null) {
            System.err.println("Error processant missatge rebut (codi o parts null): " + missatgeCru);
            try {
                enviarMissatgeAlClient(Missatge.getMissatgeGrup("Servidor: Missatge mal format rebut."));
            } catch (IOException e) { /* Ignorar si no es pot notificar */ }
            return;
        }

        switch (codi) {
            case Missatge.CODI_CONECTAR:
                if (parts.length > 1) {
                    this.nom = parts[1];
                    servidor.afegirClient(this);
                } else {
                     System.err.println("Missatge CODI_CONECTAR sense nom: " + missatgeCru);
                }
                break;
            case Missatge.CODI_SORTIR_CLIENT:
                // El client demana sortir. El nom ja hauria d'estar establert.
                // La part [1] del missatge de sortir client conté el nom, per si es vol verificar.
                System.out.println("Client " + this.nom + " demana sortir.");
                sortir = true; // Això aturarà el bucle run() i es cridarà eliminarClient al finally.
                break;
            case Missatge.CODI_SORTIR_TOTS:
                this.sortir = true; // Atura aquest gestor
                servidor.finalitzarXat(); // Demana al servidor que finalitzi tot
                break;
            case Missatge.CODI_MSG_PERSONAL:
                if (parts.length > 2) {
                    String destinatari = parts[1];
                    String missatge = parts[2];
                    // El remitent és this.nom
                    servidor.enviarMissatgePersonal(destinatari, this.nom, missatge);
                } else {
                    System.err.println("Missatge CODI_MSG_PERSONAL mal format: " + missatgeCru);
                }
                break;
            case Missatge.CODI_MSG_GRUP:
                 if (parts.length > 1) {
                    String missatgeText = parts[1];
                    // El remitent és this.nom, el servidor afegirà "nom: missatge"
                    servidor.enviarMissatgeGrup(Missatge.getMissatgeGrup(this.nom + ": " + missatgeText));
                } else {
                    System.err.println("Missatge CODI_MSG_GRUP mal format: " + missatgeCru);
                }
                break;
            default:
                System.err.println("Codi de missatge desconegut ("+codi+") rebut de " + (this.nom != null ? this.nom : "client desconegut") + ": " + missatgeCru);
                try {
                    enviarMissatgeAlClient(Missatge.getMissatgeGrup("Servidor: Codi d'operació desconegut."));
                } catch (IOException e) { /* Ignorar */ }
                break;
        }
    }

    private void tancarRecursos() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error tancant recursos per al client " + (nom != null ? nom : "") + ": " + e.getMessage());
        }
    }
}