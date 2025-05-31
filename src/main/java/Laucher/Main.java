package Core;

import Core.Utils.Consts;
import Render.Window;
import org.lwjgl.Version;

import static Core.EngineManager.getFps;
import static Core.EngineManager.setFps;

public class Main {

    private static Window window;
    private static EngineManager engine;

    public static void main(String[] args) {

        System.out.printf(Version.getVersion());

        window = new Window(Consts.TITLE + getFps(), 1200, 800, false);
        engine = new EngineManager();

        try{
            engine.start();
        }catch(Exception e){e.printStackTrace();}

    }

    public static Window getWindow() {
        return window;
    }

}