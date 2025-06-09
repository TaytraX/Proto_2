package Render;

import Core.Entities.Model;
import Core.Ilogic;
import Core.ObjectLoader;
import Laucher.Main;
import org.joml.Vector3f;

public class GameBackground implements Ilogic {

    private final BackgroundManager renderer; // Utiliser BackgroundManager au lieu de RenderManager
    private final ObjectLoader loader;
    public final Window window;

    private Model backgroundModel; // Modèle pour le background

    public GameBackground() {
        renderer = new BackgroundManager(); // Utiliser le bon renderer
        window = Main.getWindow();
        loader = new ObjectLoader();
    }

    @Override
    public void inits() throws Exception {
        renderer.init(); // Initialiser le renderer

        // Créer un quad plein écran pour le background
        float[] vertices = {
                -1.0f, -1.0f,  -0.9f,  // Bas gauche
                1.0f, -1.0f,  -0.9f,  // Bas droit
                1.0f,  1.0f,  -0.9f,  // Haut droit
                -1.0f,  1.0f,  -0.9f   // Haut gauche
        };

        int[] indices = {
                0, 1, 2,  // Premier triangle
                2, 3, 0   // Deuxième triangle
        };

        // Coordonnées de texture (même si pas utilisées pour le background)
        float[] textureCoords = {
                0.0f, 0.0f,  // Bas gauche
                1.0f, 0.0f,  // Bas droit
                1.0f, 1.0f,  // Haut droit
                0.0f, 1.0f   // Haut gauche
        };

        // Créer le modèle
        backgroundModel = loader.loadModel(vertices, textureCoords, indices);

        System.out.println("✅ Background model créé avec succès !");
    }

    @Override
    public void update() {
        // Le background peut avoir des animations (temps qui passe, etc.)
    }

    @Override
    public void input() {
        // Le background ne réagit généralement pas aux inputs
    }

    @Override
    public void render() {
        // ✅ CORRECTION: Effectuer le rendu du background
        if (backgroundModel != null) {
            // Position fixe pour le background (il couvre tout l'écran)
            Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
            renderer.render(backgroundModel, position);
        } else {
            System.err.println("❌ Background model est null !");
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