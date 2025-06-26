package Core;

import Core.World.PlatformManager;
import Laucher.Main;
import Laucher.TestGame;
import Render.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EngineManager {

    public static final long NANOSECOND = 1000000000L;
    public static final float FRAMERATE = 60.0f;

    private static int fps;
    private static final float frametime = 1.0f / FRAMERATE;

    private boolean isRunning;

    private volatile Window window;
    private GLFWErrorCallback errorCallback;
    private Ilogic gameLogic, background;
    private static PlatformManager platforms; // ✅ Changé en PlatformManager

    private ThreadManager threadManager;

    private void Init() throws Exception {
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        window = Main.getWindow();
        gameLogic = Main.getGame();
        background = Main.getBackground();

        threadManager = new ThreadManager();
        window.init();

        // ✅ Ordre d'initialisation corrigé
        background.inits();
        gameLogic.inits();                    // TestGame en premier (crée le renderer)
        platforms = new PlatformManager(TestGame.getRenderer()); // Puis PlatformManager
        platforms.inits();

        // Configuration OpenGL
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        System.out.println("✅ Depth test activé: " + GL11.glIsEnabled(GL11.GL_DEPTH_TEST));
    }

    public void start() throws Exception {
        Init();

        if(isRunning) return;

        run();
    }

    public void run() {
        this.isRunning = true;
        int frames = 0;
        long framesCounter = 0;
        long lastTime = System.nanoTime();
        double unprocessedTime = 0;

        while(isRunning){
            boolean render = false;
            long startTime = System.nanoTime();
            long passedTime = startTime - lastTime;
            lastTime = startTime;

            unprocessedTime += passedTime / (double) NANOSECOND;
            framesCounter += passedTime;

            input();

            while(unprocessedTime > frametime){
                render = true;
                unprocessedTime -= frametime;

                if(window.windowShouldClose()) stop();

                if(framesCounter >= NANOSECOND){
                    setFps(frames);
                    window.setTitle("Proto(2) : " + getFps() + " FPS");
                    frames = 0;
                    framesCounter = 0;
                }
            }

            if(render){
                // ✅ Mise à jour parallèle, rendu synchronisé
                updateParallel();
                renderSynchronized();
                frames++;
            }
        }
        cleanup();
    }

    // Dans EngineManager.java - Loop de jeu simplifiée
    private void updateParallel() {
        threadManager.withWriteLock(() -> {
            try {
                gameLogic.update();
                background.update();
            } catch (Exception e) {
                System.err.println("❌ Erreur update: " + e.getMessage());
            }
        });
    }

    private void renderSynchronized() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        threadManager.withReadLock(() -> {
            try {
                background.render();      // Arrière-plan en premier
                platforms.render();       // Plateformes au milieu
                gameLogic.render();
            } catch (Exception e) {
                System.err.println("❌ Erreur render: " + e.getMessage());
            }
        });

        window.update();
    }

    public void stop() {
        if(!isRunning) return;
        isRunning = false;
    }

    private void input() {
        gameLogic.input();
    }

    public void cleanup() {

        window.cleanup();
        background.cleanup();
        gameLogic.cleanup(); // ✅ Ajouté
        errorCallback.free();
        GLFW.glfwTerminate();
    }

    public static int getFps() {
        return fps;
    }

    public static void setFps(int fps) {
        EngineManager.fps = fps;
    }

    // ✅ Getter pour accéder aux plateformes depuis d'autres classes
    public static PlatformManager getPlatforms() {
        return platforms;
    }
}