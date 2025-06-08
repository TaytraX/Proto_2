// ✅ CORRECTIONS PRINCIPALES POUR L'AFFICHAGE DU BACKGROUND :
//
// 1. Vertices Z=0.0 au lieu de -0.9 (problème de profondeur)
// 2. Désactivation du depth test pour le background
// 3. Vérification que les attributs vertex sont bien activés
// 4. Gestion robuste des uniforms même s'ils ne sont pas trouvés
// 5. Restauration correcte de l'état OpenGL

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
    public float time;

    public GameBackground() {
        window = Main.getWindow();
    }

    public void init() throws Exception {
        System.out.println("🚀 Initialisation du GameBackground...");

        // 1. ✅ CORRECTION: Désactiver le depth test pour le background
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // 2. Créer et compiler les shaders
        backgroundShader = new ShaderManager();
    
    String vertexShader = Utils.loadRessource("/shaders/background.vs.glsl");
    String fragmentShader = Utils.loadRessource("/shaders/background.fs.glsl");
    
    System.out.println("Debug - Vertex Shader content: " + vertexShader.substring(0, Math.min(100, vertexShader.length())));
    System.out.println("Debug - Fragment Shader content: " + fragmentShader.substring(0, Math.min(100, fragmentShader.length())));
    
    backgroundShader.createVertexShader(vertexShader);
    backgroundShader.createFragmentShader(fragmentShader);
    backgroundShader.link();
    
    // Vérification du statut après link
    System.out.println("Debug - Shader Program ID: " + backgroundShader.getProgramID());

        String vertexCode = Utils.loadRessource("/shaders/background.vs.glsl");
        String fragmentCode = Utils.loadRessource("/shaders/background.fs.glsl");

        System.out.println("📝 Shader codes chargés");

        backgroundShader.createVertexShader(vertexCode);
        backgroundShader.createFragmentShader(fragmentCode);
        backgroundShader.link();

        // 3. Créer les uniforms APRÈS la liaison
        try {
            backgroundShader.createUniform("time");
            backgroundShader.createUniform("resolution");
            System.out.println("✅ Uniforms créés avec succès");
        } catch (Exception e) {
            System.err.println("⚠️ Attention: Uniforms non trouvés dans le shader");
        }

        // 4. ✅ CORRECTION: Créer la géométrie pour un quad plein écran (Z=0.0 au lieu de -0.9)
        float[] vertices = {
                -1.0f, -1.0f,  0.0f,  // Bas gauche
                1.0f, -1.0f,  0.0f,  // Bas droit
                1.0f,  1.0f,  0.0f,  // Haut droit
                -1.0f,  1.0f,  0.0f   // Haut gauche
        };

        int[] indices = {
                0, 1, 2,  // Premier triangle
                2, 3, 0   // Deuxième triangle
        };

        // 5. Création et configuration des buffers OpenGL
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

        // ✅ CORRECTION: Gérer les dimensions de fenêtre invalides
        float windowWidth = (float)window.getWidth();
        float windowHeight = (float)window.getHeight();

        if (windowWidth <= 0 || windowHeight <= 0) {
            System.err.println("❌ Taille de fenêtre invalide: " + windowWidth + "x" + windowHeight);
            return;
        }

        // ✅ IMPORTANT: Désactiver le depth test pour le background
        boolean depthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // ✅ IMPORTANT: Désactiver le blending si activé
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_BLEND);

        // Activer le shader
        backgroundShader.bind();

        try {
            // Utiliser glfwGetTime() pour un temps continu
            float currentTime = (float) glfwGetTime();

            // Debug occasionnel
            if (Math.random() < 0.005) { // Réduire les logs
                System.out.println("🎨 Rendu background - time: " + String.format("%.2f", currentTime) +
                        ", resolution: " + (int)windowWidth + "x" + (int)windowHeight);
            }

            // ✅ CORRECTION: Passer les uniforms directement via OpenGL
            int timeLocation = GL20.glGetUniformLocation(backgroundShader.getProgramID(), "time");
            int resolutionLocation = GL20.glGetUniformLocation(backgroundShader.getProgramID(), "resolution");

            if (timeLocation >= 0) {
                GL20.glUniform1f(timeLocation, currentTime);
            } else {
                System.out.println("⚠️ Uniform 'time' non trouvé");
            }

            if (resolutionLocation >= 0) {
                GL20.glUniform2f(resolutionLocation, windowWidth, windowHeight);
            } else {
                System.out.println("⚠️ Uniform 'resolution' non trouvé");
            }

            // ✅ CORRECTION: S'assurer que le VAO est bien bindé
            GL30.glBindVertexArray(vaoId);

            // ✅ CORRECTION: Vérifier que l'attribut 0 est activé
            GL20.glEnableVertexAttribArray(0);

            // Rendu du quad (6 indices pour 2 triangles)
            GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);

            // Cleanup des attributs
            GL20.glDisableVertexAttribArray(0);
            GL30.glBindVertexArray(0);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du rendu du fond: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Nettoyage
            backgroundShader.unbind();

            // ✅ CORRECTION: Restaurer l'état OpenGL
            if (depthTestEnabled) {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
            if (blendEnabled) {
                GL11.glEnable(GL11.GL_BLEND);
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