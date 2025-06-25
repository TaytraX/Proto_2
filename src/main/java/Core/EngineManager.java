package Core;

import Core.World.PlatformGenerator;
import Core.World.PlatformManager;
import Laucher.Main;
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
    private static PlatformManager platforms;

    private ThreadManager threadManager;

    private void Init() throws Exception{
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        window = Main.getWindow();
        gameLogic = Main.getGame();
        background = Main.getBackground();
        platforms = Main.getPlatforms();

        // Initialiser le gestionnaire de threads
        threadManager = new ThreadManager();

        window.init();

        // Initialiser le background AVANT le jeu
        background.inits();
        gameLogic.inits();

        // Configuration OpenGL pour le rendu en couches
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Noir par défaut
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
                // Logique en parallèle, rendu en série
                updateParallel();
                renderSynchronized();
                frames++;
            }
        }
        cleanup();
    }

    private void updateParallel() {
        // ✅ Une seule tâche pour toute la logique
        Future<?> logicTask = threadManager.updateAllLogic(
                () -> {
                    try {
                        gameLogic.update();
                    } catch (Exception e) {
                        System.err.println("❌ Erreur logique joueur: " + e.getMessage());
                    }
                },
                () -> {
                    try {
                        platforms.update(player.getPosition());
                    } catch (Exception e) {
                        System.err.println("❌ Erreur logique background: " + e.getMessage());
                    }
                },
                () -> {
                    try {
                        background.update();
                    } catch (Exception e) {
                        System.err.println("❌ Erreur logique background: " + e.getMessage());
                    }
                }
        );

        // Attendre que la logique soit terminée
        try {
            logicTask.get(16, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.err.println("⚠️ Timeout sur la logique de jeu");
            logicTask.cancel(true);
        } catch (Exception e) {
            System.err.println("❌ Erreur dans le thread de logique: " + e.getMessage());
        }
    }

     // NOUVEAU : Rendu synchronisé (reste dans le thread principal)
    private void renderSynchronized() {
        // LE RENDU DOIT RESTER DANS LE THREAD PRINCIPAL !
        // Mais on utilise les verrous pour accéder aux données en sécurité

        // 1. Clear une seule fois au début
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // 2. Rendre le background avec protection
        threadManager.withBackgroundLock(() -> {
            background.render();
        });

        // 3. Rendre le jeu avec protection
        threadManager.withPlayerLock(() -> {
            gameLogic.render();
        });

        // 4. Mettre à jour l'affichage
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

        // Arrêter les threads AVANT le cleanup OpenGL
        if (threadManager != null) {
            threadManager.shutdown();
        }

        window.cleanup();
        background.cleanup();
        gameLogic.cleanup();
        errorCallback.free();
        GLFW.glfwTerminate();
    }

    public static int getFps() {
        return fps;
    }

    public static void setFps(int fps) {
        EngineManager.fps = fps;
    }
}