package Core;

import Laucher.Main;
import Render.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

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
        background.inits();
        gameLogic.inits();
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
                    // Mise à jour du titre avec les FPS
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

        background.render();

        // Appeler le rendu de la logique de jeu
        gameLogic.render();
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