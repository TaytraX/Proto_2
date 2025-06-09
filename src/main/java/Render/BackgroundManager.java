package Render;

import Core.Entities.Model;
import Core.ShaderManager;
import Core.Utils.Utils;
import Laucher.Main;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class BackgroundManager {

    private ShaderManager shader;

    public BackgroundManager() {
        Window window = Main.getWindow();
    }

    public void init() throws Exception {
        shader = new ShaderManager();

        shader.createVertexShader(Utils.loadRessource("/shaders/background.vs.glsl"));
        shader.createFragmentShader(Utils.loadRessource("/shaders/background.fs.glsl"));
        shader.link();

        // Créer TOUS les uniforms nécessaires
        shader.createUniform("time");
        shader.createUniform("resolution");
    }

    // Méthode de rendu avec position
    public void render(Model model, Vector3f position) {
        if (model == null) {
            System.err.println("❌ Tentative de rendu d'un modèle null !");
            return;
        }

        clear();
        shader.bind();

        // Créer et appliquer la matrice de transformation
        Matrix4f resolution = new Matrix4f().identity();
        resolution.translate(position); // Appliquer la translation
        shader.setUniform("resolution", resolution);

        // Bind du VAO
        GL30.glBindVertexArray(model.getId());
        GL20.glEnableVertexAttribArray(0); // Position
        GL20.glEnableVertexAttribArray(1); // Texture coordinates

        // Rendu des triangles
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

        // Cleanup
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
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