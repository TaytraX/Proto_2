package Laucher;

import Core.Entities.Model;
import Core.Entities.Player;
import Core.Entities.Texture;
import Core.Ilogic;
import Core.ObjectLoader;
import Core.RenderManager;
import Render.GameBackground;
import Render.Window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class TestGame implements Ilogic {

    private final RenderManager renderer;
    private final ObjectLoader loader;
    private final Window window;
    GameBackground gameBackground;

    private Player player;

    public TestGame() {
        renderer = new RenderManager();
        window = Main.getWindow();
        loader = new ObjectLoader();
        try {
            gameBackground = new GameBackground();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation du fond d'écran", e);
        }
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

        // Saut avec W ou SPACE
        if (window.isKeyPressed(GLFW.GLFW_KEY_W) || window.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            player.jump();
        }

        // Déplacement gauche avec A
        player.moveLeft(window.isKeyPressed(GLFW.GLFW_KEY_A));

        // Déplacement droit avec D
        player.moveRight(window.isKeyPressed(GLFW.GLFW_KEY_D));
    }

    @Override
    public void update() {
        // Mise à jour du fond d'écran
        gameBackground.update(0.016f);

        // Mise à jour du joueur
        if (player != null) {
            player.update();
        }
    }

    @Override
    public void render() {
        // Gérer le redimensionnement de la fenêtre
        if (window.isResize()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResize(false);
        }

        // Rendu du fond d'écran
        gameBackground.render();

        // Rendu du joueur par-dessus le ciel
        if (player != null && player.getModel() != null) {
            if (player.getModel().getTexture() != null) {
                renderer.render(player.getModel(), player.getPosition());

                // Debug: afficher la position du joueur
                if (Math.random() < 0.01) {
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

        if (gameBackground != null) {
            gameBackground.cleanup();
        }

        if (renderer != null) {
            renderer.cleanup();
        }

        if (loader != null) {
            loader.cleanup();
        }
    }
}