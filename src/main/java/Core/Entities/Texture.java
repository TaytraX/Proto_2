package Core.Entities;

import Core.ObjectLoader;

public class Texture {
    private final int id;

    public Texture(int id) {
        this.id = id;
    }

    // ✅ CORRIGÉ : Utilise le singleton ObjectLoader
    public Texture(String filename) {
        int textureId = 0;
        try {
            ObjectLoader loader = ObjectLoader.getInstance(); // ✅ Singleton
            textureId = loader.loadTexture(filename);
            System.out.println("✅ Texture chargée: " + filename + " -> ID: " + textureId);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement texture " + filename + ": " + e.getMessage());
            try {
                ObjectLoader loader = ObjectLoader.getInstance();
                textureId = loader.createDefaultTexture();
            } catch (Exception ex) {
                System.err.println("❌ Impossible de créer texture par défaut");
                textureId = 0;
            }
        }
        this.id = textureId;
    }

    public int getId() {
        return id;
    }
}