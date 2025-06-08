package Render;

import Core.ShaderManager;
import Core.Utils.Utils;
import Laucher.Main;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class GameBackground {
    private final Window window;
    private ShaderManager backgroundShader;
    private int vaoId;
    private int vboId;
    private float time = 0.0f;

    public GameBackground() throws Exception {
        window = Main.getWindow();
        init();
    }

    private void init() throws Exception {
        // Création du shader
        backgroundShader = new ShaderManager();
        System.out.println("Création des uniforms...");
        backgroundShader.createUniform("time");
        backgroundShader.createUniform("resolution");

        System.out.println("Chargement des shaders...");
        String vertexShader = Utils.loadRessource("/shaders/background.vs.glsl");
        String fragmentShader = Utils.loadRessource("/shaders/background.fs.glsl");

        if (vertexShader == null || vertexShader.isEmpty()) {
            System.err.println("Erreur: Le vertex shader est vide ou n'a pas pu être chargé");
        }
        if (fragmentShader == null || fragmentShader.isEmpty()) {
            System.err.println("Erreur: Le fragment shader est vide ou n'a pas pas pu être chargé");
        }

        backgroundShader.createVertexShader(vertexShader);
        backgroundShader.createFragmentShader(fragmentShader);

        System.out.println("Link des shaders...");
        backgroundShader.link();
        System.out.println("Shaders liés avec succès !");

        // Création d'un simple quad qui couvre tout l'écran
        float[] vertices = {
                -1.0f, -1.0f, 0.0f,  // Bas gauche
                1.0f, -1.0f, 0.0f,  // Bas droit
                1.0f,  1.0f, 0.0f,  // Haut droit
                -1.0f,  1.0f, 0.0f   // Haut gauche
        };

        // Création du VAO et VBO
        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();

        GL30.glBindVertexArray(vaoId);

        // Remplissage du VBO avec les vertices
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        // Configuration des attributs de vertex
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);

        // Nettoyage
        GL30.glBindVertexArray(0);
        MemoryUtil.memFree(buffer);
    }

    public void update(float delta) {
        time += delta;
    }

    public void render() {
        // Désactiver le test de profondeur pour le fond
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        backgroundShader.bind();

        try {
            // Passage des uniformes
            backgroundShader.setUniform("time", time);
            backgroundShader.setUniform("resolution", (float)window.getWidth(), (float)window.getHeight());

            // Rendu du quad
            GL30.glBindVertexArray(vaoId);
            GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, 4);
            GL30.glBindVertexArray(0);
        } catch (Exception e) {
            System.err.println("Erreur lors du rendu du fond: " + e.getMessage());
            e.printStackTrace();
        } finally {
            backgroundShader.unbind();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
    }

    public void cleanup() {
        if (backgroundShader != null) {
            backgroundShader.cleanup();
        }
        GL30.glDeleteVertexArrays(vaoId);
        GL15.glDeleteBuffers(vboId);
    }
}