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

    private int direction = 0;
    private float colour = 0.0f;

    private final RenderManager renderer;
    private final ObjectLoader loader;
    private final Window window;

    private Player player; // ✅ NOUVEAU: Instance du joueur

    public TestGame() {
        renderer = new RenderManager();
        window = Main.getWindow();
        loader = new ObjectLoader();
    }

    @Override
    public void inits() throws Exception {
        renderer.init();

        float[] vertices = {
                -0.4f, -0.6f,  0.0f,
                -0.4f,  0.6f,  0.0f,
                0.4f,  0.6f,  0.0f,
                0.4f, -0.6f,  0.0f
        };

        int[] indices = { 0, 1, 3, 3, 1, 2 };

        float[] textureCoords = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        Model model = loader.loadModel(vertices, textureCoords, indices);

        // ✅ CORRECTION: Passer le loader au joueur
        player = new Player(model, loader);

        // Charger texture initiale
        try {
            int textureId = loader.loadTexture("src/main/resources/textures/player1.png");
            model.setTexture(new Texture(textureId));
            System.out.println("✅ Texture initiale chargée ! ID: " + textureId);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement texture initiale : " + e.getMessage());
            try {
                int defaultTextureId = loader.createDefaultTexture();
                model.setTexture(new Texture(defaultTextureId));
            } catch (Exception fallbackError) {
                System.err.println("❌ Impossible de créer une texture par défaut");
            }
        }
    }

    @Override
    public void input() {
        // ✅ NOUVEAU: Gestion des inputs pour le joueur

        // Saut avec W
        if (window.isKeyPressed(GLFW.GLFW_KEY_W) || window.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            player.jump();
        }

        // Déplacement gauche avec A
        if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
            player.moveLeft(true);
        } else {
            player.moveLeft(false);
        }

        // Déplacement droite avec D
        if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
            player.moveRight(true);
        } else {
            player.moveRight(false);
        }

        // ✅ CONSERVÉ: Changement de couleur avec les flèches (pour debug)
        if(window.isKeyPressed(GLFW.GLFW_KEY_UP)) {
            direction = 1;
        } else if(window.isKeyPressed(GLFW.GLFW_KEY_DOWN)) {
            direction = -1;
        } else {
            direction = 0;
        }
    }

    @Override
    public void update() {
        // ✅ NOUVEAU: Mise à jour du joueur
        if (player != null) {
            player.update();
        }

        // Changement de couleur de fond avec les flèches (pour debug)
        colour += direction * 0.01f;
        if(colour > 1.0f) {
            colour = 1.0f;
        } else if(colour < 0.0f) {
            colour = 0.0f;
        }
    }

    @Override
    public void render() {
        // Gérer le redimensionnement de la fenêtre
        if (window.isResize()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResize(false);
        }

        // Définir la couleur de fond
        window.setClearColour(colour, colour, colour, 1.0f);

        // ✅ NOUVEAU: Rendu du joueur avec sa position
        if (player != null && player.getModel() != null) {
            if (player.getModel().getTexture() != null) {
                // Rendu avec la position du joueur
                renderer.render(player.getModel(), player.getPosition());

                // Debug: afficher la position du joueur
                if (Math.random() < 0.01) { // Afficher de temps en temps pour ne pas spammer
                    System.out.println("🎮 Position joueur: " +
                            String.format("X:%.2f Y:%.2f Z:%.2f",
                                    player.getPosition().x,
                                    player.getPosition().y,
                                    player.getPosition().z));
                }
            } else {
                System.out.println("⚠️ Rendu sans texture");
            }
        } else {
            System.err.println("❌ Joueur ou modèle null !");
        }
    }

    @Override
    public void cleanup() {
        if (renderer != null) {
            renderer.cleanup();
        }
        if (loader != null) {
            loader.cleanup();
        }
    }
}