package Core.Entities;

import Core.ObjectLoader;

public class Texture {

    private final int id;

    // Constructeur existant
    public Texture(int id) {
        this.id = id;
    }

    // ✅ NOUVEAU: Constructeur pour charger depuis un fichier
    public Texture(String filename) {
        int textureId = 0;
        try {
            ObjectLoader loader = new ObjectLoader();
            textureId = loader.loadTexture(filename);
            System.out.println("✅ Texture chargée: " + filename + " -> ID: " + textureId);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement texture " + filename + ": " + e.getMessage());
            // En cas d'erreur, utiliser une texture par défaut ou ID 0
            textureId = 0;
        }
        this.id = textureId;
    }

    public int getId() {
        return id;
    }
}