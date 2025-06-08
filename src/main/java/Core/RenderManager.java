package Core;

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

    private final Window window;
    private ShaderManager shader;

    public RenderManager() {
        window = Main.getWindow();
    }

    public void init() throws Exception {
        shader = new ShaderManager();
        shader.createVertexShader(Utils.loadRessource("/shaders/vertex.vs.glsl"));
        shader.createFragmentShader(Utils.loadRessource("/shaders/fragment.fs.glsl"));
        shader.link();

        // ✅ AJOUT: Créer les uniforms nécessaires
        shader.createUniform("textureSample");// ✅ NOUVEAU: Pour les transformations
    }

    // ✅ NOUVEAU: Méthode de rendu avec position
    public void render(Model model, Vector3f position) {
        if (model == null) {
            System.err.println("❌ Tentative de rendu d'un modèle null !");
            return;
        }

        clear();
        shader.bind();

        // ✅ NOUVEAU: Créer et appliquer la matrice de transformation
        Matrix4f transformationMatrix = new Matrix4f().identity();
        transformationMatrix.translate(position); // Appliquer la translation
        shader.setUniform("transformationMatrix", transformationMatrix);

        // Bind du VAO
        GL30.glBindVertexArray(model.getId());
        GL20.glEnableVertexAttribArray(0); // Position
        GL20.glEnableVertexAttribArray(1); // Texture coordinates

        // Gestion de la texture
        if (model.getTexture() != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getId());

            // Paramètres de texture
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

            shader.setUniform("textureSample", window.getWidth(), 0);
        } else {
            System.out.println("⚠️ Pas de texture - rendu avec couleur par défaut");
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }

        // Rendu des triangles
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

        // Cleanup
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        shader.unbind();
    }

    // ✅ CONSERVÉ: Méthode de rendu sans position (pour compatibilité)
    public void render(Model model) {
        render(model, new Vector3f(0.0f, 0.0f, 0.0f));
    }

    // ✅ CONSERVÉ: Méthode pour rendu wireframe
    public void renderWireframe(Model model) {
        if (model == null) return;

        clear();

        // Rendu en wireframe
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        GL30.glBindVertexArray(model.getId());
        GL20.glEnableVertexAttribArray(0);

        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);

        // Revenir au mode normal
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
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