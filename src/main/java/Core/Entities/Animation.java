package Core.Entities;

import Core.ObjectLoader;

public class Animation {
    private final Texture[] frames;
    private volatile int pointer;
    private volatile boolean isPlaying;
    private volatile boolean loop;

    private volatile double elapsedTime;
    private volatile double currentTime;
    private volatile double lastTime;
    private volatile double fps;

    // ✅ Verrou pour les opérations critiques
    private final Object animationLock = new Object();

    public Animation(int amount, int fps, String filename, ObjectLoader loader) {
        this.pointer = 0;
        this.isPlaying = false;
        this.loop = true;
        this.elapsedTime = 0;
        this.currentTime = 0;
        this.lastTime = System.nanoTime() / 1_000_000_000.0;
        this.fps = 1.0/(double)fps;

        this.frames = new Texture[amount];

        // ✅ Chargement avec gestion d'erreur améliorée
        loadFrames(amount, filename, loader);
    }

    // ✅ Méthode séparée pour le chargement des frames
    private void loadFrames(int amount, String filename, ObjectLoader loader) {
        for(int i = 0; i < amount; i++){
            try {
                // ✅ Format de nom de fichier plus flexible
                String fullPath = String.format("src/main/resources/textures/%s_%d.png", filename, i);
                // Essayer d'abord avec index
                int textureId = tryLoadTexture(fullPath, loader);

                if (textureId == -1) {
                    // Si échec, essayer sans index pour frame 0
                    fullPath = "src/main/resources/textures/" + filename + ".png";
                    textureId = tryLoadTexture(fullPath, loader);
                }

                if (textureId != -1) {
                    this.frames[i] = new Texture(textureId);
                    System.out.println("🎬 Frame " + i + " chargée: " + fullPath);
                } else {
                    // Texture par défaut
                    int defaultId = loader.createDefaultTexture();
                    this.frames[i] = new Texture(defaultId);
                    System.out.println("⚠️ Frame " + i + " utilise texture par défaut");
                }

            } catch (Exception e) {
                System.err.println("❌ Erreur chargement frame " + i + ": " + e.getMessage());
                try {
                    int defaultId = loader.createDefaultTexture();
                    this.frames[i] = new Texture(defaultId);
                } catch (Exception ex) {
                    System.err.println("❌ Impossible de créer texture par défaut pour frame " + i);
                }
            }
        }
    }

    // ✅ Méthode helper pour essayer le chargement
    private int tryLoadTexture(String path, ObjectLoader loader) {
        try {
            return loader.loadTexture(path);
        } catch (Exception e) {
            return -1; // Échec
        }
    }

    public void play() {
        synchronized (animationLock) {
            this.isPlaying = true;
            this.lastTime = System.nanoTime() / 1_000_000_000.0;
        }
    }

    public void stop() {
        synchronized (animationLock) {
            this.isPlaying = false;
            this.pointer = 0;
            this.elapsedTime = 0;
        }
    }

    public void pause() {
        synchronized (animationLock) {
            this.isPlaying = false;
        }
    }

    public void resume() {
        synchronized (animationLock) {
            this.isPlaying = true;
            this.lastTime = System.nanoTime() / 1_000_000_000.0;
        }
    }

    public void reset() {
        synchronized (animationLock) {
            this.pointer = 0;
            this.elapsedTime = 0;
        }
    }

    public void update() {
        if (!isPlaying || frames.length == 0) return;

        synchronized (animationLock) {
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
    }

    public Texture getCurrentFrame() {
        synchronized (animationLock) {
            if (frames.length == 0) return null;
            return frames[pointer];
        }
    }

    // ✅ Getters thread-safe
    public boolean isPlaying() {
        return isPlaying;
    }

    public void setLoop(boolean loop) {
        synchronized (animationLock) {
            this.loop = loop;
        }
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
        synchronized (animationLock) {
            this.fps = 1.0 / (double) newFps;
        }
    }
}