import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Fitxer {
    
    private String nom;
    private byte[] contingut;

    public Fitxer(String nom) {
        this.nom = nom;
    }

    public byte[] getContingut() {
        File file = new File(nom);
        if (file.exists() && file.isFile()) {
            try {
                contingut = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contingut;
    }

    public void setContingut(byte[] contingut) {
        this.contingut = contingut;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public byte[] getBytes() {
        return contingut;
    }
}