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
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class GameBackground {
    private final Window window;
    private ShaderManager backgroundShader;
    private int vaoId;
    private int vboId;
    private int eboId;
    public float time; // ✅ AJOUTÉ: Element Buffer Object


    public GameBackground() {
        window = Main.getWindow();
    }

    public void init() throws Exception {
        System.out.println("🚀 Initialisation du GameBackground...");

        // 1. Créer et compiler les shaders
        backgroundShader = new ShaderManager();

        String vertexCode = Utils.loadRessource("/shaders/background.vs.glsl");
        String fragmentCode = Utils.loadRessource("/shaders/background.fs.glsl");

        System.out.println("📝 Code vertex shader:");
        System.out.println(vertexCode);
        System.out.println("📝 Code fragment shader:");
        System.out.println(fragmentCode);

        backgroundShader.createVertexShader(vertexCode);
        backgroundShader.createFragmentShader(fragmentCode);
        backgroundShader.link();

        // 2. ✅ CORRECTION : Créer les uniforms APRÈS la liaison
        try {
            backgroundShader.createUniform("time");
            backgroundShader.createUniform("resolution");
            System.out.println("✅ Uniforms créés avec succès");
        } catch (Exception e) {
            System.err.println("⚠️ Attention: Uniforms non trouvés dans le shader (peut être normal)");
        }

        // 3. Créer la géométrie pour un quad plein écran
        float[] vertices = {
                -1.0f, -1.0f, -0.9f,  // Bas gauche
                1.0f, -1.0f, -0.9f,  // Bas droit
                1.0f,  1.0f, -0.9f,  // Haut droit
                -1.0f,  1.0f, -0.9f   // Haut gauche
        };

        int[] indices = {
                0, 1, 2,  // Premier triangle
                2, 3, 0   // Deuxième triangle
        };

        // 4. Création et configuration des buffers OpenGL
        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();
        eboId = GL15.glGenBuffers();

        GL30.glBindVertexArray(vaoId);

        // VBO pour les vertices
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        // EBO pour les indices
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);

        // Configuration des attributs de vertex
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);

        // Cleanup
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        // Libération mémoire
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indicesBuffer);

        System.out.println("✅ GameBackground initialisé avec succès");
        System.out.println("   VAO ID: " + vaoId);
        System.out.println("   VBO ID: " + vboId);
        System.out.println("   EBO ID: " + eboId);
    }

    public void update(float delta) {
        time += delta;
    }

    public void render() {
        if (backgroundShader == null || vaoId == 0) {
            System.err.println("❌ GameBackground pas initialisé correctement");
            return;
        }

        // ✅ CORRECTION : Gérer les dimensions de fenêtre invalides
        float windowWidth = (float)window.getWidth();
        float windowHeight = (float)window.getHeight();

        if (windowWidth <= 0 || windowHeight <= 0) {
            System.err.println("❌ Taille de fenêtre invalide: " + windowWidth + "x" + windowHeight);
            return;
        }

        // Sauvegarder l'état OpenGL
        boolean depthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Activer le shader
        backgroundShader.bind();

        try {
            // ✅ CORRECTION : Passer les uniforms de manière sécurisée
            // Utiliser glfwGetTime() pour un temps continu
            float currentTime = (float) glfwGetTime();

            // Debug occasionnel
            if (Math.random() < 0.01) { // 1% de chance d'afficher
                System.out.println("🎨 Rendu background - time: " + currentTime +
                        ", resolution: " + windowWidth + "x" + windowHeight);
            }

            // ✅ MÉTHODE ALTERNATIVE: Passer les uniforms directement via OpenGL
            int timeLocation = GL20.glGetUniformLocation(backgroundShader.getProgramID(), "time");
            int resolutionLocation = GL20.glGetUniformLocation(backgroundShader.getProgramID(), "resolution");

            if (timeLocation >= 0) {
                GL20.glUniform1f(timeLocation, currentTime);
            }
            if (resolutionLocation >= 0) {
                GL20.glUniform2f(resolutionLocation, windowWidth, windowHeight);
            }

            // Rendu du quad
            GL30.glBindVertexArray(vaoId);
            GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
            GL30.glBindVertexArray(0);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du rendu du fond: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Nettoyage
            backgroundShader.unbind();

            // Restaurer l'état OpenGL
            if (depthTestEnabled) {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        }
    }

    public void cleanup() {
        System.out.println("🧹 Nettoyage du GameBackground...");

        if (backgroundShader != null) {
            backgroundShader.cleanup();
            backgroundShader = null;
        }

        if (vaoId != 0) {
            GL30.glDeleteVertexArrays(vaoId);
            vaoId = 0;
        }
        if (vboId != 0) {
            GL15.glDeleteBuffers(vboId);
            vboId = 0;
        }
        if (eboId != 0) {
            GL15.glDeleteBuffers(eboId);
            eboId = 0;
        }

        System.out.println("✅ GameBackground nettoyé");
    }
}