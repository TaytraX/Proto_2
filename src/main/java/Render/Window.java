package Render;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL.*;

public class Window {

    public static final float FOV = (float) Math.toRadians(60);
    public static final float Z_NEAR = 0.01F;
    public static final float Z_FAR = 1000f;

    private String title;

    private int width, height;
    private long window;

    private boolean resize, vSync;

    private final Matrix4f projectionMatrix;

    public Window(String title, int width, int height, boolean vSync) {
        this.vSync = vSync;
        this.height = height;
        this.width = width;
        this.title = title;
        projectionMatrix = new Matrix4f();
    }

    public void init(){
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        boolean maximised = false;
        if(width == 0 || height == 0){
            width = 100;
            height = 100;
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            maximised = true;
        }

        window = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if(window == MemoryUtil.NULL)
            throw new RuntimeException("Failed to create GLFW window");

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResize(true);
        });

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        if(maximised){
            glfwMaximizeWindow(window);
        }else{
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidMode.width() - width) / 2,
                    (vidMode.height() - height) / 2);
        }

        glfwMakeContextCurrent(window);

        // CORRIGÉ: Créer les capacités OpenGL - TRÈS IMPORTANT
        createCapabilities();

        if(isvSync())
            glfwSwapInterval(1);

        glfwShowWindow(window);

        // Définir la couleur de fond
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // CORRIGÉ: Activer GL_DEPTH_TEST pour le rendu 3D
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // CORRIGÉ: Désactiver temporairement le culling pour debug
        // glEnable(GL_CULL_FACE);
        // glCullFace(GL_BACK);
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    public void update() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public void cleanup(){
        glfwDestroyWindow(window);
    }

    public void setClearColour(float r, float g, float b, float a){
        glClearColor(r, g, b, a);
    }

    public boolean isKeyPressed(int keycode){
        return glfwGetKey(window, keycode) == GLFW_PRESS;
    }

    public boolean windowShouldClose(){
        return glfwWindowShouldClose(window);
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String newTitle){
        this.title = newTitle;
        glfwSetWindowTitle(window, newTitle);
    }

    private boolean isvSync() {
        return vSync;
    }

    public boolean isResize(){
        return resize;
    }

    public void setResize(boolean resize) {
        this.resize = resize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getWindow() {
        return window;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    // CORRIGÉ: Pour un rendu 2D simple, utiliser une matrice identité ou orthographique simple
    public Matrix4f updateProjectionMatrix() {
        // Option 1: Matrice identité pour coordonnées OpenGL directes (-1 à 1)
        return projectionMatrix.identity();

        // Option 2: Si vous voulez utiliser des coordonnées pixels, décommentez la ligne suivante:
        // return projectionMatrix.setOrtho(0, width, 0, height, -1, 1);
    }

    public Matrix4f updateProjectionMatrix(Matrix4f matrix, int width, int height) {
        float aspectRatio = (float) width / height;
        return matrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }
}