package Render;

import Core.Entities.Player;
import Core.Ilogic;
import Core.ObjectLoader;
import Core.RenderManager;
import Laucher.Main;
import org.lwjgl.opengl.GL11;

public class GameBackground implements Ilogic {

    private final RenderManager renderer;
    private final ObjectLoader loader;
    private final Window window;

    public GameBackground() {
        renderer = new RenderManager();
        window = Main.getWindow();
        loader = new ObjectLoader();
    }

    @Override
    public void inits() throws Exception {
        renderer.init();

        float[] vertices = {
               -1.0f, -1.0f,  -0.9f,  // Bas gauche
                1.0f, -1.0f,  -0.9f,  // Bas droit
                1.0f,  1.0f,  -0.9f,  // Haut droit
               -1.0f,  1.0f,  -0.9f   // Haut gauche
        };

        int[] indices = {
                0, 1, 2,
                2, 3, 0
            };
    }

    public void update() {
    }

    public void input() {

    }

    @Override
    public void render() {
        // Gérer le redimensionnement de la fenêtre
        if (window.isResize()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResize(false);
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