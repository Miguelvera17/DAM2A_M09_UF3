import java.io.ObjectInputStream;
import java.io.IOException; // Importa IOException

public class FilLectorCX extends Thread {
    private ObjectInputStream in;
    private volatile boolean continuarLeyendo = true; // Bandera de control

    public FilLectorCX(ObjectInputStream in) {
        this.in = in;
    }

    public void detener() {
        continuarLeyendo = false;
    }

    @Override
    public void run() {
        try {
            String missatge;
            while (continuarLeyendo) { // Comprueba la bandera
                try {
                    missatge = (String) in.readObject();
                    if (missatge == null) {
                        break; // Salir si readObject() devuelve null (conexión cerrada)
                    }
                    System.out.println("Servidor: " + missatge);
                } catch (IOException e) {
                    if (continuarLeyendo) { // Imprimir solo si no se detuvo explícitamente
                        System.err.println("Error en FilLectorCX (IO): " + e.getMessage());
                    }
                    break; // Salir del bucle en caso de error de IO
                } catch (ClassNotFoundException e) {
                    System.err.println("Error en FilLectorCX (Class): " + e.getMessage());
                    break;
                }
            }
        } finally {
            try {
                if (in != null) {
                    in.close(); // Asegurar cierre del stream aquí
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar el stream en FilLectorCX: " + e.getMessage());
            }
        }
    }
}