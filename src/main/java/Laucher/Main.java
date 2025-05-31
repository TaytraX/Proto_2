package Laucher;

import Core.EngineManager;
import Core.Utils.Consts;
import Render.Window;
import org.lwjgl.Version;

public class Main {

    private static Window window;
    private static TestGame game;

    public static void main(String[] args) {

        System.out.println("LWJGL Version: " + Version.getVersion());

        window = new Window(Consts.TITLE + "Initializing...", 1200, 800, false);
        game = new TestGame();
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
}