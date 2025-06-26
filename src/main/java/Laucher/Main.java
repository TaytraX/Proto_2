package Laucher;

import Core.EngineManager;
import Core.Utils.Consts;
import Render.GameBackground;
import Render.Window;
import Core.World.PlatformGenerator;
import org.lwjgl.Version;

public class Main {

    private volatile static Window window;
    public static TestGame game;
    private static GameBackground background;
    private static PlatformGenerator platforms;

    public static void main(String[] args) {

        System.out.println("LWJGL Version: " + Version.getVersion());

        window = new Window(Consts.TITLE + "Initializing...", 1200, 800, false);
        game = new TestGame();
        background = new GameBackground();
        platforms = new PlatformGenerator();
        EngineManager engine = new EngineManager();

        try{
            engine.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Window getWindow() {
        return window;
    }

    public static TestGame getGame() {
        return game;
    }

    public static GameBackground getBackground() {
        return background;
    }
}