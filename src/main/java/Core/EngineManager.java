package Core;

import Laucher.Main;
import Render.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;

public class EngineManager {

    public static final long NANOSECOND = 1000000000L;
    public static final float FRAMERATE = 60.0f;

    private static int fps;
    private static final float frametime = 1.0f / FRAMERATE;

    private boolean isRunning;

    private Window window;
    private GLFWErrorCallback errorCallback;
    private Ilogic gameLogic, background;

    private void Init() throws Exception{
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        window = Main.getWindow();
        gameLogic = Main.getGame();
        background = Main.getBackground();

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
                update();
                render();
                frames++;
            }
        }
        cleanup();
    }

    public void stop() {
        if(!isRunning) return;
        isRunning = false;
    }

    private void input() {
        gameLogic.input();
    }

    private void render() {
        // Ordre de rendu crucial
        // 1. Clear une seule fois au début
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // 2. Rendre le background EN PREMIER (arrière-plan)
        background.render();

        // 3. Rendre le jeu PAR-DESSUS (premier plan)
        gameLogic.render();

        // 4. Mettre à jour l'affichage
        window.update();
    }

    private void update() {
        gameLogic.update();
    }

    public void cleanup() {
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