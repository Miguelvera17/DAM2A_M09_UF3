import java.io.ObjectInputStream;
import java.io.IOException; // Importa IOException para manejar excepciones

public class FilServidorXat extends Thread {
    private ObjectInputStream in;
    private String nom;
    private static final String MSG_SORTIR = "sortir";

    // Constructor que recibe ObjectInputStream y el nombre
    public FilServidorXat(ObjectInputStream in, String nom) {
        this.in = in;
        this.nom = nom;
    }

    @Override
    public void run() {
        try {
            String missatge;
            // Método de ejecución que recibe mensajes hasta recibir MSG_SORTIR
            while ((missatge = (String) in.readObject()) != null) {
                if (missatge.equalsIgnoreCase(MSG_SORTIR)) {
                    break; // Salir del bucle si el mensaje es "sortir" (ignorando mayúsculas/minúsculas)
                }
                System.out.println(nom + ": " + missatge);
            }
        } catch (IOException e) {
            System.err.println("Error al llegir missatges del client (IO): " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Error al llegir missatges del client (Class): " + e.getMessage());
        }
    }
}