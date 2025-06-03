package Core.Entities;

import Core.ObjectLoader;

public class Animation {
    private final Texture[] frames;
    private int pointer;
    private boolean isPlaying;
    private boolean loop;

    private double elapsedTime;
    private double currentTime;
    private double lastTime;
    private double fps;

    // ✅ SOLUTION: Passer l'ObjectLoader en paramètre
    public Animation(int amount, int fps, String filename, ObjectLoader loader) {
        this.pointer = 0;
        this.isPlaying = false;
        this.loop = true;
        this.elapsedTime = 0;
        this.currentTime = 0;
        this.lastTime = System.nanoTime() / 1_000_000_000.0;
        this.fps = 1.0/(double)fps;

        this.frames = new Texture[amount];

        // ✅ CHARGEMENT CORRECT des textures
        for(int i = 0; i < amount; i++){
            try {
                String fullPath = "src/main/resources/textures/" + filename + ".png";
                int textureId = loader.loadTexture(fullPath);
                this.frames[i] = new Texture(textureId);
                System.out.println("🎬 Frame " + i + " chargée: " + fullPath);
            } catch (Exception e) {
                System.err.println("❌ Erreur chargement frame " + i + ": " + e.getMessage());
                // Utiliser une texture par défaut
                try {
                    int defaultId = loader.createDefaultTexture();
                    this.frames[i] = new Texture(defaultId);
                } catch (Exception ex) {
                    System.err.println("❌ Impossible de créer texture par défaut");
                }
            }
        }
    }

    public void play() {
        this.isPlaying = true;
        this.lastTime = System.nanoTime() / 1_000_000_000.0;
    }

    public void stop() {
        this.isPlaying = false;
        this.pointer = 0;
        this.elapsedTime = 0;
    }

    public void pause() {
        this.isPlaying = false;
    }

    public void resume() {
        this.isPlaying = true;
        this.lastTime = System.nanoTime() / 1_000_000_000.0;
    }

    public void reset() {
        this.pointer = 0;
        this.elapsedTime = 0;
    }

    public void update() {
        if (!isPlaying || frames.length == 0) return;

        currentTime = System.nanoTime() / 1_000_000_000.0;
        elapsedTime += (currentTime - lastTime);
        lastTime = currentTime;

        if (elapsedTime >= fps) {
            elapsedTime = 0;
            pointer++;

            if (pointer >= frames.length) {
                if (loop) {
                    pointer = 0;
                } else {
                    pointer = frames.length - 1;
                    isPlaying = false;
                }
            }
        }
    }

    public Texture getCurrentFrame() {
        if (frames.length == 0) return null;
        return frames[pointer];
    }

    // Getters et setters...
    public boolean isPlaying() {
        return isPlaying;
    }
    public void setLoop(boolean loop) {
        this.loop = loop;
    }
    public boolean isLoop() {
        return loop;
    }
    public int getCurrentFrameIndex() {
        return pointer;
    }
    public int getFrameCount() {
        return frames.length;
    }
    public void setFPS(int newFps) {
        this.fps = 1.0 / (double) newFps;
    }
}