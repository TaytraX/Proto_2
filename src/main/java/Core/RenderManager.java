package Core;

import Core.Entities.Camera;
import Core.Entities.Model;
import Core.Utils.Utils;
import Laucher.Main;
import Render.Window;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class RenderManager {

    public final Window window;
    private ShaderManager shader;

    public RenderManager() {
        window = Main.getWindow();
    }

    public void init() throws Exception {
        shader = new ShaderManager();

        shader.createVertexShader(Utils.loadRessource("/shaders/vertex.vs.glsl"));
        shader.createFragmentShader(Utils.loadRessource("/shaders/fragment.fs.glsl"));
        shader.link();

        shader.createUniform("textureSample");
        shader.createUniform("transformationMatrix");
        // ✅ AJOUTER ces uniforms pour la caméra
        shader.createUniform("viewMatrix");
        shader.createUniform("projectionMatrix");
    }

    public void render(Model model, Vector3f position, Camera camera) {
        if (model == null) {
            System.err.println("❌ Tentative de rendu d'un modèle null !");
            return;
        }

        shader.bind();

        // ✅ Utiliser les matrices de la caméra
        if (camera != null) {
            shader.setUniform("viewMatrix", camera.getViewMatrix());
            shader.setUniform("projectionMatrix", camera.getProjectionMatrix());
        }

        // Matrice de transformation locale
        Matrix4f transformationMatrix = new Matrix4f().identity();
        transformationMatrix.translate(position);
        shader.setUniform("transformationMatrix", transformationMatrix);

        // Bind du VAO
        GL30.glBindVertexArray(model.getId());
        GL20.glEnableVertexAttribArray(0); // Position
        GL20.glEnableVertexAttribArray(1); // Texture coordinates

        // Gestion de la texture
        if (model.getTexture() != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getId());

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

            shader.setUniform("textureSample", 0);
        } else {
            System.out.println("⚠️ Pas de texture - rendu avec couleur par défaut");
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }

        // Activer le blending pour la transparence
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Rendu des triangles
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

        // Cleanup
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        shader.unbind();
    }

    public void cleanup() {
        if (shader != null) {
            shader.cleanup();
        }
    }
}