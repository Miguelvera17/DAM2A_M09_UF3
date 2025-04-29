import java.io.ObjectInputStream;
import java.io.IOException; // Importa IOException

public class FilLectorCX extends Thread {
    private ObjectInputStream in; // Cambiado a ObjectInputStream

    // Constructor con el stream
    public FilLectorCX(ObjectInputStream in) { // Recibe ObjectInputStream
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String missatge;
            // Método de ejecución que recibe los mensajes del Chat
            while ((missatge = (String) in.readObject()) != null) {
                System.out.println("Servidor: " + missatge);
            }
        } catch (IOException e) {
            System.err.println("Error al llegir missatge del servidor (IO): " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Error al llegir missatge del servidor (Class): " + e.getMessage());
        }
    }
}