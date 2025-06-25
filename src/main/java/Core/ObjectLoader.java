package Core;

import Core.Entities.Model;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjectLoader {

    private static volatile ObjectLoader instance;
    private final Object loadLock = new Object(); // Verrou pour le chargement

    // Liste des ressources pour le cleanup
    private final List<Integer> vaos = new ArrayList<>();
    private final List<Integer> vbos = new ArrayList<>();
    private final List<Integer> textures = new ArrayList<>();

    public ObjectLoader() {} // Constructeur privé

    // ✅ Singleton thread-safe avec double-checked locking
    public static synchronized ObjectLoader getInstance() {
        if (instance == null) {
            instance = new ObjectLoader();
        }
        return instance;
    }

    // ✅ Méthodes synchronized pour éviter les conflits OpenGL
    public synchronized Model loadModel(float[] vertices, float[] textureCoords, int[] indices) {
        int vao = GL30.glGenVertexArrays();
        vaos.add(vao);
        GL30.glBindVertexArray(vao);

        // Position buffer
        int posVBO = GL15.glGenBuffers();
        vbos.add(posVBO);
        FloatBuffer posBuffer = org.lwjgl.BufferUtils.createFloatBuffer(vertices.length);
        posBuffer.put(vertices).flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        // Texture coordinates buffer
        int texVBO = GL15.glGenBuffers();
        vbos.add(texVBO);
        FloatBuffer texBuffer = org.lwjgl.BufferUtils.createFloatBuffer(textureCoords.length);
        texBuffer.put(textureCoords).flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);

        // Index buffer
        int ibo = GL15.glGenBuffers();
        vbos.add(ibo);
        IntBuffer indexBuffer = org.lwjgl.BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

        GL30.glBindVertexArray(0);
        return new Model(vao, indices.length);
    }

    public synchronized int loadTexture(String filename) throws Exception {
        synchronized (loadLock) {
            int width, height;
            ByteBuffer buffer;

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);

                buffer = STBImage.stbi_load(filename, w, h, comp, 4);
                if (buffer == null) {
                    throw new Exception("Could not load file " + filename + " " + STBImage.stbi_failure_reason());
                }

                width = w.get();
                height = h.get();
            }

            int textureID = GL11.glGenTextures();
            textures.add(textureID);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            // Paramètres de filtrage
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            STBImage.stbi_image_free(buffer);
            return textureID;
        }
    }

    public synchronized int createDefaultTexture() {
        // Texture 2x2 pixels blancs
        ByteBuffer data = org.lwjgl.BufferUtils.createByteBuffer(16);
        for (int i = 0; i < 16; i++) {
            data.put((byte) 255); // Blanc opaque
        }
        data.flip();

        int textureID = GL11.glGenTextures();
        textures.add(textureID);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        return textureID;
    }

    public synchronized void cleanup() {
        vaos.forEach(GL30::glDeleteVertexArrays);
        vbos.forEach(GL15::glDeleteBuffers);
        textures.forEach(GL11::glDeleteTextures);
        vaos.clear();
        vbos.clear();
        textures.clear();
    }
}