// ServidorXat.java
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ServidorXat {
    public static final int PORT = 9999;
    public static final String HOST = "localhost";
    public static final String MSG_SORTIR = "sortir"; // Contingut per a CODI_SORTIR_TOTS

    private Hashtable<String, GestorClient> clients;
    private boolean sortir;
    private ServerSocket serverSocket;

    public ServidorXat() {
        this.clients = new Hashtable<>();
        this.sortir = false;
    }

    public void servidorAEscoltar() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciat a " + HOST + ":" + PORT);

            while (!sortir) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connectat: " + clientSocket.getInetAddress());
                    GestorClient gestor = new GestorClient(clientSocket, this);
                    new Thread(gestor).start();
                } catch (IOException e) {
                    
                }
            }
        } catch (IOException e) {
            if (!sortir) {
                System.err.println("No es pot iniciar el servidor al port " + PORT + ": " + e.getMessage());
            }
        } finally {
            pararServidor();
        }
    }

    public synchronized void pararServidor() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error aturant el ServerSocket: " + e.getMessage());
            }
        }
    }

    public synchronized void finalitzarXat() {
        System.out.println("Tancant tots els clients.");
        this.sortir = true; // Atura el bucle d'acceptar nous clients
        // Enviar missatge de sortida a tots els clients connectats
        // Usem una còpia per evitar ConcurrentModificationException si eliminarClient modifica la llista
        List<GestorClient> clientsACopia = new ArrayList<>(clients.values());
        for (GestorClient gestor : clientsACopia) {
            try {
                // El missatge de sortir tots ja porta el seu propi codi.
                // El contingut "MSG_SORTIR" és un text descriptiu.
                gestor.enviarMissatgeAlClient(Missatge.getMissatgeSortirTots(MSG_SORTIR));
            } catch (IOException e) {
                System.err.println("Error enviant missatge de finalització a " + gestor.getNom() + ": " + e.getMessage());
            }
        }
        enviarMissatgeGrupIntern(Missatge.getMissatgeSortirTots(MSG_SORTIR));
        System.out.println("DEBUG: multicast " + MSG_SORTIR);
        clients.clear();
        pararServidor();
    }


    public synchronized void afegirClient(GestorClient gestorClient) {
        if (gestorClient.getNom() == null || gestorClient.getNom().trim().isEmpty()) {
            System.err.println("Intent d'afegir client amb nom buit.");
            return;
        }
        clients.put(gestorClient.getNom(), gestorClient);
        System.out.println(gestorClient.getNom() + " connectat.");
        // Enviar missatge de grup que un nou usuari ha entrat
        String missatgeEntrada = "Entra: " + gestorClient.getNom();
        enviarMissatgeGrup(Missatge.getMissatgeGrup(missatgeEntrada));
        System.out.println("DEBUG: multicast " + missatgeEntrada);
    }

    public synchronized void eliminarClient(String nomClient) {
        if (nomClient != null && clients.containsKey(nomClient)) {
            GestorClient gestor = clients.remove(nomClient);
            if (gestor != null) {
                System.out.println(nomClient + " desconnectat.");
                String missatgeSortida = "Surt: " + nomClient;
                enviarMissatgeGrup(Missatge.getMissatgeGrup(missatgeSortida));
                 System.out.println("DEBUG: multicast " + missatgeSortida);
            }
        }
    }

    public synchronized void enviarMissatgeGrup(String missatgeCompletAmbCodi) {
       // El missatge ja ve codificat des de Missatge.getMissatgeGrup()
        for (GestorClient gestor : clients.values()) {
            try {
                gestor.enviarMissatgeAlClient(missatgeCompletAmbCodi);
            } catch (IOException e) {
                System.err.println("Error enviant missatge de grup a " + gestor.getNom() + ": " + e.getMessage());
                // Considerar eliminar el client si hi ha error d'enviament persistent
            }
        }
    }

    // Mètode intern per enviar missatge de grup sense codificar-lo de nou (per MSG_SORTIR)
    private synchronized void enviarMissatgeGrupIntern(String missatgeCompletAmbCodi) {
        for (GestorClient gestor : clients.values()) {
            try {
                gestor.enviarMissatgeAlClient(missatgeCompletAmbCodi);
            } catch (IOException e) {
                // Silenciós aquí, ja que és part del tancament
            }
        }
    }


    public synchronized void enviarMissatgePersonal(String nomDestinatari, String nomRemitent, String missatge) {
        GestorClient destinatari = clients.get(nomDestinatari);
        if (destinatari != null) {
            try {
                // El remitent és qui envia el missatge, el destinatari és el client actual del gestor
                // El missatge que s'envia al client ha de contenir el nom del remitent original.
                String missatgeCodificat = Missatge.CODI_MSG_PERSONAL + "#" + nomRemitent + "#" + missatge;
                destinatari.enviarMissatgeAlClient(missatgeCodificat);
                System.out.println("Missatge personal per (" + nomDestinatari + ") de (" + nomRemitent + "): " + missatge);
            } catch (IOException e) {

            }
        } else {
            // Podries enviar un missatge d'error al remitent si tinguessis el seu GestorClient
             GestorClient remitentGestor = clients.get(nomRemitent);
             if (remitentGestor != null) {
                 try {
                    remitentGestor.enviarMissatgeAlClient(Missatge.getMissatgeGrup("Servidor: El destinatari '" + nomDestinatari + "' no existeix o no està connectat."));
                 } catch (IOException ex) {
                     // No fem res més
                 }
             }
        }
    }

    public boolean isSortir() {
        return sortir;
    }

    public static void main(String[] args) {
        ServidorXat servidor = new ServidorXat();
        servidor.servidorAEscoltar();
    }
}