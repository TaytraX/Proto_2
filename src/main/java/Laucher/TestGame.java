package Laucher;

import Core.Entities.Camera;
import Core.Entities.Model;
import Core.Entities.Player;
import Core.Entities.Texture;
import Core.Ilogic;
import Core.ObjectLoader;
import Core.RenderManager;
import Core.World.PlatformManager;
import Render.Window;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import static Laucher.Main.game;

public class TestGame implements Ilogic {

    private final RenderManager renderer;
    private final ObjectLoader loader;
    private final Window window;
    private volatile PlatformManager platforms;
    private Camera camera;

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
        float aspectRatio = (float) window.getWidth() / window.getHeight();
        camera = new Camera(aspectRatio);

        platforms = new PlatformManager(TestGame.getRenderer());

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
                0.0f, 1.0f,  // Bas gauche -> correspond au haut de l'image
                0.0f, 0.0f,  // Haut gauche -> correspond au bas de l'image
                1.0f, 0.0f,  // Haut droit -> correspond au bas de l'image
                1.0f, 1.0f   // Bas droit -> correspond au haut de l'image
        };

        // ✅ Création du modèle avec le loader singleton
        Model model = loader.loadModel(vertices, textureCoords, indices);
        player = new Player(model); // ✅ Plus besoin de passer le loader
        player.setPlatformManager(platforms);

        // ✅ Chargement de la texture initiale avec gestion d'erreur
        initializePlayerTexture(model);

        System.out.println("✅ TestGame initialisé avec succès !");

        System.out.println("✅ TestGame avec plateformes initialisé !");
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
        if (player != null) {
            try {
                Vector3f playerPos = player.getPosition();

                // ✅ Mettre à jour la caméra pour suivre le joueur
                if (camera != null) {
                    camera.update(playerPos);
                }

                if (platforms != null) {
                    platforms.update(playerPos);
                }

                player.update();
            } catch (Exception e) {
                System.err.println("❌ Erreur dans update(): " + e.getMessage());
            }
        }
    }

    @Override
    public void render() {
        try {
            if (window.isResize()) {
                GL11.glViewport(0, 0, window.getWidth(), window.getHeight());

                // ✅ Mettre à jour l'aspect ratio de la caméra
                float newAspectRatio = (float) window.getWidth() / window.getHeight();
                if (camera != null) {
                    camera.setAspectRatio(newAspectRatio);
                }

                window.setResize(false);
            }

            synchronized (renderLock) {
                // ✅ Passer les matrices de caméra au renderer si nécessaire
                renderWorld();
                renderPlayer();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur dans render(): " + e.getMessage());
        }
    }

    // ✅ Méthode séparée pour le rendu du joueur
    // Dans TestGame.java - Modifier les méthodes de rendu
    private void renderPlayer() {
        if (player == null || camera == null) return;

        Model playerModel = player.getModel();
        if (playerModel == null) return;

        if (playerModel.getTexture() != null) {
            // ✅ Passer la caméra au renderer
            renderer.render(playerModel, player.getPosition(), camera);
        }
    }

    private void renderWorld() {
        if (platforms != null && camera != null) {
            try {
                // ✅ Les plateformes devront aussi recevoir la caméra
                platforms.render(camera);
            } catch (Exception e) {
                System.err.println("❌ Erreur rendu monde: " + e.getMessage());
            }
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

    public static RenderManager getRenderer() {
        return game != null ? game.renderer : null;
    }

    // ✅ Getter pour la caméra
    public Camera getCamera() {
        return camera;
    }
}