// ClientXat.java (continuació)
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientXat {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean sortir;
    private String nomUsuari; // Guardem el nom de l'usuari un cop connectat

    public ClientXat() {
        this.sortir = false;
    }

    public boolean connecta(String host, int port) {
        try {
            socket = new Socket(host, port);
            System.out.println("Client connectat a " + host + ":" + port);
            // IMPORTANT: OOS primer, flush, després OIS (en el fil receptor)
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush(); // Envia la capçalera de l'stream
            // OIS s'inicialitza en el mètode d'execució (receptor)
            System.out.println("Flux de sortida creat.");
            return true;
        } catch (UnknownHostException e) {
            System.err.println("Host desconegut: " + host);
        } catch (IOException e) {
            System.err.println("No s'ha pogut connectar al servidor " + host + ":" + port + ". Assegura't que el servidor està actiu. " + e.getMessage());
        }
        return false;
    }

    public void enviarMissatge(String missatgeCodificat) {
        if (oos == null) {
            System.out.println("oos null. No es pot enviar missatge. Client no connectat correctament?");
            return;
        }
        try {
            oos.writeObject(missatgeCodificat);
            oos.flush();
            System.out.println("Enviant missatge: " + missatgeCodificat);
        } catch (IOException e) {
            System.err.println("Error enviant missatge: " + e.getMessage());
            sortir = true; // Si hi ha error d'enviament, probablement la connexió s'ha perdut
        }
    }

    public void tancarClient() {
        System.out.println("Tancant client...");
        this.sortir = true; // Assegura que el bucle receptor s'aturi
        try {
            if (ois != null) {
                ois.close();
                System.out.println("Flux d'entrada tancat.");
            }
        } catch (IOException e) {
            System.err.println("Error tancant flux d'entrada: " + e.getMessage());
        }
        try {
            if (oos != null) {
                oos.close();
                System.out.println("Flux de sortida tancat.");
            }
        } catch (IOException e) {
            System.err.println("Error tancant flux de sortida: " + e.getMessage());
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Socket client tancat.");
            }
        } catch (IOException e) {
            System.err.println("Error tancant socket client: " + e.getMessage());
        }
    }

    // Mètode d'execució per rebre missatges (s'executarà en un fil separat)
    public void execucioReceptor() {
        try {
            // OIS s'inicialitza aquí DESPRÉS que OOS s'hagi creat i fet flush
            ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("Flux d'entrada creat.");
            System.out.println("DEBUG: Iniciant rebuda de missatges...");

            while (!sortir) {
                try {
                    String missatgeCru = (String) ois.readObject();
                    if (missatgeCru == null) {
                        System.out.println("Rebut missatge null del servidor. Desconnectant...");
                        sortir = true;
                        break;
                    }

                    String codi = Missatge.getCodiMissatge(missatgeCru);
                    String[] parts = Missatge.getPartsMissatge(missatgeCru);

                    if (codi == null || parts == null) {
                        System.err.println("Error: Missatge mal format rebut del servidor: " + missatgeCru);
                        continue;
                    }

                    switch (codi) {
                        case Missatge.CODI_SORTIR_TOTS:
                            System.out.println("El servidor ha ordenat tancar tots els clients. Missatge: " + (parts.length > 1 ? parts[1] : ""));
                            sortir = true;
                            break;
                        case Missatge.CODI_MSG_PERSONAL:
                            if (parts.length > 2) {
                                String remitent = parts[1];
                                String missatge = parts[2];
                                System.out.println("Missatge de (" + remitent + "): " + missatge);
                            } else {
                                System.err.println("Error: Missatge personal mal format: " + missatgeCru);
                            }
                            break;
                        case Missatge.CODI_MSG_GRUP:
                            if (parts.length > 1) {
                                System.out.println("Grup: " + parts[1]);
                            } else {
                                System.err.println("Error: Missatge de grup mal format: " + missatgeCru);
                            }
                            break;
                        case Missatge.CODI_SORTIR_CLIENT:
                            System.out.println("Servidor confirma desconnexió del client: " + (parts.length > 1 ? parts[1] : ""));
                            sortir = true;
                            break;
                        default:
                            System.err.println("Error: Codi de missatge desconegut (" + codi + ") rebut del servidor: " + missatgeCru);
                            break;
                    }
                } catch (IOException e) {
                    if (!sortir) {
                        System.err.println("Error rebent missatge (IOException): " + e.getMessage());
                        // No sortir immediatament, potser la següent lectura funciona
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Error de format de dades (ClassNotFoundException): " + e.getMessage());
                    // No sortir immediatament
                }
            }
        } catch (EOFException e) {
            System.out.println("El servidor ha tancat la connexió.");
            sortir = true;
        } catch (SocketException e) {
            System.err.println("Error de connexió amb el servidor (SocketException): " + e.getMessage() + ". Sortint...");
            sortir = true;
        } catch (IOException e) {
            if (!sortir) {
                System.err.println("Error inicialitzant streams (IOException): " + e.getMessage() + ". Sortint...");
                sortir = true;
            }
        } finally {
            // El tancament es fa al bucle principal
        }
    }

    public void ajuda() {
        System.out.println("---------------------");
        System.out.println("Comandes disponibles:");
        System.out.println("1.- Connectar al servidor (primer pas obligatori)");
        System.out.println("2.- Enviar missatge personal");
        System.out.println("3.- Enviar missatge al grup");
        System.out.println("4.- (o línia en blanc)-> Sortir del client");
        System.out.println("5.- Finalitzar tothom");
        System.out.println("---------------------");
    }

    public String getLinea(Scanner scanner, String missatge, boolean obligatori) {
        String linia;
        do {
            System.out.print(missatge);
            linia = scanner.nextLine().trim();
        } while (obligatori && linia.isEmpty());
        return linia;
    }

    public static void main(String[] args) {
        ClientXat client = new ClientXat();
        Scanner scanner = new Scanner(System.in);

        if (!client.connecta(ServidorXat.HOST, ServidorXat.PORT)) {
            System.out.println("No s'ha pogut connectar al servidor. Finalitzant client.");
            scanner.close();
            return;
        }

        // Iniciar el fil per rebre missatges
        Thread receptorThread = new Thread(client::execucioReceptor);
        receptorThread.start();

        boolean primerComando = true;

        while (!client.sortir) {
            if (!primerComando) System.out.println("---------------------"); // Separador entre comandes
            client.ajuda();
            primerComando = false;

            String opcio = client.getLinea(scanner, "Escull una opció: ", false);
            String missatgeCodificat = null;

            if (opcio.isEmpty() && client.nomUsuari != null) { // Opció per defecte sortir client si ja connectat
                opcio = "4";
            } else if (opcio.isEmpty() && client.nomUsuari == null) {
                System.out.println("Si us plau, connecta't primer (opció 1).");
                continue;
            }


            switch (opcio) {
                case "1": // Conectar
                    if (client.nomUsuari != null) {
                        System.out.println("Ja estàs connectat com: " + client.nomUsuari);
                        continue;
                    }
                    String nom = client.getLinea(scanner, "Introdueix el nom: ", true);
                    client.nomUsuari = nom; // Guardem el nom
                    missatgeCodificat = Missatge.getMissatgeConectar(nom);
                    break;
                case "2": // Missatge Personal
                    if (client.nomUsuari == null) {
                        System.out.println("Has de connectar-te primer (opció 1).");
                        continue;
                    }
                    String destinatari = client.getLinea(scanner, "Destinatari: ", true);
                    String msgPersonal = client.getLinea(scanner, "Missatge a enviar: ", true);
                    missatgeCodificat = Missatge.getMissatgePersonal(destinatari, msgPersonal);
                    break;
                case "3": // Missatge Grup
                    if (client.nomUsuari == null) {
                        System.out.println("Has de connectar-te primer (opció 1).");
                        continue;
                    }
                    String msgGrup = client.getLinea(scanner, "Missatge al grup: ", true);
                    missatgeCodificat = Missatge.getMissatgeGrup(msgGrup);
                    break;
                case "4": // Sortir Client
                    if (client.nomUsuari == null) { // Si no s'ha connectat, simplement surt
                        System.out.println("Client no estava connectat. Sortint localment.");
                        client.sortir = true;
                        missatgeCodificat = null; // No hi ha res a enviar si no s'ha connectat
                    } else {
                        missatgeCodificat = Missatge.getMissatgeSortirClient(client.nomUsuari);
                        client.sortir = true; // Marcar per sortir després d'enviar el missatge
                    }
                    break;
                case "5": // Finalitzar Tothom
                    missatgeCodificat = Missatge.getMissatgeSortirTots("Adéu");
                    if (client.nomUsuari == null) {
                        System.out.println("Has de connectar-te primer (opció 1) per poder finalitzar tothom.");
                        continue;
                    }
                    client.sortir = true;
                    break;
                default:
                    System.out.println("Opció no vàlida.");
                    break;
            }

            if (missatgeCodificat != null) {
                client.enviarMissatge(missatgeCodificat);
            }

            // Si l'opció era sortir client o sortir tots, i el missatge s'ha enviat (o no calia enviar),
            // el bucle principal s'aturarà a la propera iteració gràcies a client.sortir = true.
            if (client.sortir && (opcio.equals("4") || opcio.equals("5"))) {
                 // Donem un petit temps perquè el missatge de sortida s'enviï abans de tancar tot
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }
        client.tancarClient();
        try {
            receptorThread.join(1000); // Esperar que el fil receptor acabi, amb timeout
            if (receptorThread.isAlive()) {
                System.out.println("Avís: El fil receptor no ha finalitzat netament.");
                receptorThread.interrupt(); // Com a últim recurs
            }
        } catch (InterruptedException e) {
            System.err.println("Interromput mentre s'esperava al fil receptor.");
            Thread.currentThread().interrupt();
        }
        scanner.close();
        System.out.println("Error rebent missatge. Sortint...");
    }
}