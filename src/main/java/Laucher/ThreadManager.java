package Laucher;

import Core.EngineManager;
import Render.BackgroundManager;

public class ThreadManager implements Runnable {

    EngineManager engine = new EngineManager();
    BackgroundManager background = new BackgroundManager();

    @Override
    public void run() {
        try {
            engine.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}