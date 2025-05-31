package Core;

import Core.Entities.Model;
import Core.Utils.Utils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjectLoader {

    private List<Integer> VAOS = new ArrayList<>();
    private List<Integer> VBOS = new ArrayList<>();
    private List<Integer> textures = new ArrayList<>(); // ✅ CORRIGÉ: nom de variable

    public Model loadModel(float[] vertices, float[] textureCoords, int[] indices) {
        int id = createVAO();
        storeIndicesBuffer(indices);
        storeDataInAttribList(0, 3, vertices);      // Position (x, y, z)
        storeDataInAttribList(1, 2, textureCoords); // Texture (u, v)
        unbind();
        return new Model(id, indices.length);
    }

    public int loadTexture(String filename) throws Exception {
        int width, height;
        ByteBuffer buffer;

        System.out.println("🔍 Tentative de chargement de texture : " + filename);

        // ✅ CORRIGÉ: Vérification de l'existence du fichier
        File textureFile = new File(filename);
        if (!textureFile.exists()) {
            // Essayer le chemin des resources
            String resourcePath = "/textures/player1.png";
            System.out.println("🔄 Fichier non trouvé, essai du chemin resource : " + resourcePath);

            // Vérifier si la resource existe
            if (getClass().getResourceAsStream(resourcePath) == null) {
                throw new Exception("❌ Texture introuvable : " + filename + " et " + resourcePath);
            }
            filename = resourcePath;  // Utiliser le chemin resource
        }

        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);

            // ✅ Important pour OpenGL
            STBImage.stbi_set_flip_vertically_on_load(true);

            // ✅ CORRIGÉ: Gestion des resources et fichiers
            if (filename.startsWith("/")) {
                // Charger depuis les resources
                java.net.URL resourceUrl = getClass().getResource(filename);
                if (resourceUrl != null) {
                    buffer = STBImage.stbi_load(resourceUrl.getPath(), w, h, c, 4);
                } else {
                    throw new Exception("Resource introuvable : " + filename);
                }
            } else {
                // Charger depuis le système de fichiers
                buffer = STBImage.stbi_load(filename, w, h, c, 4);
            }

            if(buffer == null){
                String error = STBImage.stbi_failure_reason();
                System.err.println("❌ Erreur STB chargement : " + filename + " - " + error);
                throw new Exception("Image File " + filename + " not loaded. Reason: " + error);
            }

            width = w.get();
            height = h.get();

            System.out.println("✅ Texture chargée avec succès : " + width + "x" + height + " pixels");
        }

        // ✅ Création de la texture OpenGL
        int textureId = GL11.glGenTextures();
        textures.add(textureId); // ✅ CORRIGÉ: utiliser la bonne liste

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

        // ✅ Libérer la mémoire
        STBImage.stbi_image_free(buffer);

        System.out.println("🎨 Texture OpenGL créée avec ID : " + textureId);
        return textureId;
    }

    // ✅ AJOUT: Méthode pour créer une texture par défaut (blanche)
    public int createDefaultTexture() {
        // Créer une texture blanche 1x1
        ByteBuffer whitePixel = ByteBuffer.allocateDirect(0);
        whitePixel.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 255);
        whitePixel.flip();

        int textureId = GL11.glGenTextures();
        textures.add(textureId);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 1, 1, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, whitePixel);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        System.out.println("⚪ Texture par défaut créée avec ID : " + textureId);
        return textureId;
    }

    private int createVAO() {
        int id = GL30.glGenVertexArrays();
        VAOS.add(id);
        GL30.glBindVertexArray(id);
        return id;
    }

    private void storeIndicesBuffer(int[] indices) {
        int vbo = GL15.glGenBuffers();
        VBOS.add(vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo);
        IntBuffer buffer = Utils.storeDataInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    private void storeDataInAttribList(int attribNo, int vertexCount, float[] data) {
        int VBO = GL15.glGenBuffers();
        VBOS.add(VBO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        FloatBuffer buffer = Utils.storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attribNo, vertexCount, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(attribNo);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void unbind() {
        GL30.glBindVertexArray(0);
    }

    public void cleanup() {
        // ✅ Nettoyage sécurisé
        for(int VAO : VAOS) {
            GL30.glDeleteVertexArrays(VAO);
        }
        for(int VBO : VBOS) {
            GL30.glDeleteBuffers(VBO);
        }
        for(int texture : textures) { // ✅ CORRIGÉ: nom de variable
            GL11.glDeleteTextures(texture);
        }

        VAOS.clear();
        VBOS.clear();
        textures.clear();
    }
}