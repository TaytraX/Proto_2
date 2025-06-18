package Laucher;

import Core.Entities.Model;
import Core.Entities.Player;
import Core.Entities.Texture;
import Core.Ilogic;
import Core.ObjectLoader;
import Core.RenderManager;
import Render.Window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class TestGame implements Ilogic {

    private final RenderManager renderer;
    private final ObjectLoader loader;
    private final Window window;

    private volatile Player player; // ✅ volatile pour visibilité entre threads

    // ✅ Verrou pour les opérations de rendu
    private final Object renderLock = new Object();

    public TestGame() {
        renderer = new RenderManager();
        window = Main.getWindow();
        loader = ObjectLoader.getInstance(); // ✅ Utilise le singleton
    }

    @Override
    public void inits() throws Exception {
        renderer.init();

        // ✅ Géométrie du joueur (quad 2D)
        float[] vertices = {
                -0.4f,  -0.6f,  0.0f,  // Bas gauche
                -0.4f,   0.6f,  0.0f,  // Haut gauche
                0.4f,   0.6f,  0.0f,  // Haut droit
                0.4f,  -0.6f,  0.0f   // Bas droit
        };

        int[] indices = {
                0, 1, 3,  // Premier triangle
                3, 1, 2   // Deuxième triangle
        };

        float[] textureCoords = {
                0.0f, 0.0f,  // Bas gauche
                0.0f, 1.0f,  // Haut gauche
                1.0f, 1.0f,  // Haut droit
                1.0f, 0.0f   // Bas droit
        };

        // ✅ Création du modèle avec le loader singleton
        Model model = loader.loadModel(vertices, textureCoords, indices);
        player = new Player(model); // ✅ Plus besoin de passer le loader

        // ✅ Chargement de la texture initiale avec gestion d'erreur
        initializePlayerTexture(model);

        System.out.println("✅ TestGame initialisé avec succès !");
    }

    // ✅ Méthode séparée pour l'initialisation de la texture
    private void initializePlayerTexture(Model model) {
        try {
            int textureId = loader.loadTexture("src/main/resources/textures/player1.png");
            model.setTexture(new Texture(textureId));
            System.out.println("✅ Texture initiale du joueur chargée ! ID: " + textureId);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement texture initiale : " + e.getMessage());
            try {
                int defaultTextureId = loader.createDefaultTexture();
                model.setTexture(new Texture(defaultTextureId));
                System.out.println("⚠️ Utilisation de la texture par défaut");
            } catch (Exception fallbackError) {
                System.err.println("❌ Impossible de créer une texture par défaut");
            }
        }
    }

    @Override
    public void input() {
        if (player == null) return;

        // ✅ Gestion des entrées avec vérification
        try {
            // Saut avec W ou SPACE
            if (window.isKeyPressed(GLFW.GLFW_KEY_W) || window.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
                player.jump();
            }

            // Déplacement gauche avec A
            player.moveLeft(window.isKeyPressed(GLFW.GLFW_KEY_A));

            // Déplacement droit avec D
            player.moveRight(window.isKeyPressed(GLFW.GLFW_KEY_D));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans input(): " + e.getMessage());
        }
    }

    @Override
    public void update() {
        // ✅ Mise à jour thread-safe du joueur
        if (player != null) {
            try {
                player.update();
            } catch (Exception e) {
                System.err.println("❌ Erreur dans update(): " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void render() {
        try {
            // Gérer le redimensionnement de la fenêtre
            if (window.isResize()) {
                GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
                window.setResize(false);
            }

            // ✅ Rendu thread-safe du joueur
            synchronized (renderLock) {
                renderPlayer();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur dans render(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ Méthode séparée pour le rendu du joueur
    private void renderPlayer() {
        if (player == null) {
            System.err.println("❌ Joueur null !");
            return;
        }

        Model playerModel = player.getModel();
        if (playerModel == null) {
            System.err.println("❌ Modèle du joueur null !");
            return;
        }

        if (playerModel.getTexture() != null) {
            renderer.render(playerModel, player.getPosition());

            // ✅ Debug occasionnel (moins verbeux)
            if (Math.random() < 0.005) { // 0.5% de chance
                System.out.println("🎮 Position joueur: " +
                        String.format("X:%.2f Y:%.2f Z:%.2f",
                                player.getPosition().x,
                                player.getPosition().y,
                                player.getPosition().z));
            }
        } else {
            System.out.println("⚠️ Rendu sans texture");
        }
    }

    @Override
    public void cleanup() {
        try {
            if (renderer != null) {
                renderer.cleanup();
            }

            // ✅ Le singleton s'occupe de son propre cleanup
            if (loader != null) {
                loader.cleanup();
            }

            System.out.println("✅ TestGame nettoyé");

        } catch (Exception e) {
            System.err.println("❌ Erreur dans cleanup(): " + e.getMessage());
        }
    }
}