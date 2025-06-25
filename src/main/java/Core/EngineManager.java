package Core;

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
    private static PlatformManager platforms; // ✅ Changé en PlatformManager

    private ThreadManager threadManager;

    private void Init() throws Exception{
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        window = Main.getWindow();
        gameLogic = Main.getGame();
        background = Main.getBackground();
        platforms = new PlatformManager(); // ✅ Initialisation correcte

        // Initialiser le gestionnaire de threads
        threadManager = new ThreadManager();

        window.init();

        // Initialiser les composants dans l'ordre
        platforms.inits(); // ✅ Initialiser les plateformes
        background.inits();
        gameLogic.inits();

        // Configuration OpenGL pour le rendu en couches
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
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

    private void updateParallel() {
        // ✅ Obtenir la position du joueur depuis gameLogic (TestGame)
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
                        // ✅ Pas besoin de position ici, le PlatformManager la récupère
                        platforms.update(null); // ou passer une position par référence
                    } catch (Exception e) {
                        System.err.println("❌ Erreur logique plateformes: " + e.getMessage());
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

    // ✅ Rendu synchronisé (thread principal seulement)
    private void renderSynchronized() {
        // 1. Clear une seule fois au début
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // 2. Rendre le background avec protection
        threadManager.withBackgroundLock(() -> {
            try {
                background.render();
            } catch (Exception e) {
                System.err.println("❌ Erreur rendu background: " + e.getMessage());
            }
        });

        // 3. Rendre les plateformes
        threadManager.withPlatformLock(() -> { // ✅ Nouveau verrou
            try {
                platforms.render();
            } catch (Exception e) {
                System.err.println("❌ Erreur rendu plateformes: " + e.getMessage());
            }
        });

        // 4. Rendre le jeu avec protection
        threadManager.withPlayerLock(() -> {
            try {
                gameLogic.render();
            } catch (Exception e) {
                System.err.println("❌ Erreur rendu joueur: " + e.getMessage());
            }
        });

        // 5. Mettre à jour l'affichage
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
        platforms.cleanup(); // ✅ Ajouté
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