import java.io.ObjectInputStream;

public class FilServidorXat extends Thread {
    private ObjectInputStream in;
    private String nom;
    private static final String MSG_SORTIR = "sortir";

    public FilServidorXat(ObjectInputStream in, String nom) {
        this.in = in;
        this.nom = nom;
    }

    @Override
    public void run() {
        try {
            String missatge;
            while ((missatge = (String) in.readObject()) != null) {
                if (missatge.equalsIgnoreCase(MSG_SORTIR)) break;
                System.out.println(nom + ": " + missatge);
            }
        } catch (Exception e) {
            System.err.println("Error al llegir missatges del client: " + e.getMessage());
        }
    }
}
