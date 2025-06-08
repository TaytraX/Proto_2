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

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class GameBackground {
    private final Window window;
    private ShaderManager backgroundShader;
    private int vaoId;
    private int vboId;
    private float time = 0.0f;

    public GameBackground() {
        window = Main.getWindow();
    }

    public void init() throws Exception {
        // 1. D'abord créer et compiler les shaders
        backgroundShader = new ShaderManager();

        backgroundShader.createVertexShader(Utils.loadRessource("/shaders/background.vs.glsl"));
        backgroundShader.createFragmentShader(Utils.loadRessource("/shaders/background.fs.glsl"));
        backgroundShader.link();

        // 2. ENSUITE créer les uniforms (après link())
        backgroundShader.createUniform("time");
        backgroundShader.createUniform("resolution");

        // 🔧 FIX 1: Vertices avec indices pour éviter les problèmes de rendu
        float[] vertices = {
                -1.0f, -1.0f, -0.9f,  // Bas gauche
                1.0f, -1.0f, -0.9f,  // Bas droit
                1.0f,  1.0f, -0.9f,  // Haut droit
                -1.0f,  1.0f, -0.9f   // Haut gauche
        };

        // 🔧 FIX 2: Ajouter des indices pour un rendu correct
        int[] indices = {
                0, 1, 2,  // Premier triangle
                2, 3, 0   // Deuxième triangle
        };

        // Création du VAO et VBO
        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();
        int eboId = GL15.glGenBuffers(); // 🔧 FIX 3: Ajouter un Element Buffer Object

        GL30.glBindVertexArray(vaoId);

        // Remplissage du VBO avec les vertices
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        // 🔧 FIX 4: Configuration de l'EBO
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        var indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);

        // Configuration des attributs de vertex
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);

        // 🔧 FIX 5: Nettoyage sécurisé
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        // Libération mémoire
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indicesBuffer);

        System.out.println("✅ GameBackground initialisé avec succès");
    }

    public void update(float delta) {
        time += delta;
    }

    public void render() {

        // Dans la méthode render de GameBackground
        int timeLocation = GL20.glGetUniformLocation(backgroundShader.getProgramID(), "time");
        int resolutionLocation = GL20.glGetUniformLocation(backgroundShader.getProgramID(), "resolution");

// Vérifiez si les locations sont valides
        if (timeLocation == -1) {
            System.err.println("❌ Impossible de trouver l'uniform 'time'");
        }
        if (resolutionLocation == -1) {
            System.err.println("❌ Impossible de trouver l'uniform 'resolution'");
        }

// Passez les valeurs aux uniforms
        GL20.glUniform1f(timeLocation, (float) glfwGetTime()); // Pour le temps
        GL20.glUniform2f(resolutionLocation, window.getWidth(), window.getHeight()); // Pour la résolution
        if (backgroundShader == null || vaoId == 0) {
            System.err.println("❌ GameBackground pas initialisé correctement");
            return;
        }

        // Ajouter des logs de debug
        System.out.println("Debug - time: " + time);
        System.out.println("Debug - window dimensions: " + window.getWidth() + "x" + window.getHeight());

        // Sauvegarder l'état actuel
        boolean depthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Nettoyer le framebuffer
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        backgroundShader.bind();

        try {
            float windowWidth = (float)window.getWidth();
            float windowHeight = (float)window.getHeight();

            if (windowWidth <= 0 || windowHeight <= 0) {
                System.err.println("❌ Taille de fenêtre invalide: " + windowWidth + "x" + windowHeight);
                return;
            }

            // Ajouter un log avant de passer les uniforms
            System.out.println("Debug - Setting uniforms - time: " + time + ", resolution: " + windowWidth + "x" + windowHeight);

            backgroundShader.setUniform("time", time);
            backgroundShader.setUniform("resolution", windowWidth, windowHeight);

            GL30.glBindVertexArray(vaoId);
            GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
            GL30.glBindVertexArray(0);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du rendu du fond: " + e.getMessage());
            e.printStackTrace();
        } finally {
            backgroundShader.unbind();

            // Restaurer l'état précédent
            if (depthTestEnabled) {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        }
    }

    public void cleanup() {
        if (backgroundShader != null) {
            backgroundShader.cleanup();
        }

        // 🔧 FIX 11: Nettoyage sécurisé des ressources OpenGL
        if (vaoId != 0) {
            GL30.glDeleteVertexArrays(vaoId);
            vaoId = 0;
        }
        if (vboId != 0) {
            GL15.glDeleteBuffers(vboId);
            vboId = 0;
        }

        System.out.println("✅ GameBackground nettoyé");
    }
}