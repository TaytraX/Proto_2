package Render;

import Core.Entities.Model;
import Core.ShaderManager;
import Core.Utils.Utils;
import Laucher.Main;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class BackgroundManager {

    private final Window window;
    private ShaderManager shader;

    public BackgroundManager() {
        window = Main.getWindow();
    }

    public void init() throws Exception {
        shader = new ShaderManager();

        shader.createVertexShader(Utils.loadRessource("/shaders/background.vs.glsl"));
        shader.createFragmentShader(Utils.loadRessource("/shaders/background.fs.glsl"));
        shader.link();

        // Créer les uniforms nécessaires
        shader.createUniform("time");
        shader.createUniform("resolution");

        System.out.println("✅ BackgroundManager initialisé avec succès !");
    }

    public void render(Model model, Vector3f position) {
        if (model == null) {
            System.err.println("❌ Tentative de rendu d'un modèle null !");
            return;
        }

        shader.bind();

        // Définir les uniforms avec des valeurs appropriées
        float currentTime = System.nanoTime() / 1_000_000_000.0f; // Temps en secondes
        shader.setUniform("time", currentTime);

        // Passer la résolution de la fenêtre
        shader.setUniform("resolution", (float)window.getWidth(), (float)window.getHeight());

        // Bind du VAO
        GL30.glBindVertexArray(model.getId());
        GL20.glEnableVertexAttribArray(0); // Position

        // Le background n'a pas besoin de coordonnées de texture
        // mais, on les active quand même si elles existent
        GL20.glEnableVertexAttribArray(1); // Texture coordinates (optionnel)

        // Rendu des triangles
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

        // Cleanup
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
        shader.unbind();
    }

    public void clear(){
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void cleanup() {
        if (shader != null) {
            shader.cleanup();
        }
    }
}