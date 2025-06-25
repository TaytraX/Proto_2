package Core.World;

import org.joml.Vector3f;

// Données pure (pas de Model OpenGL)
public class PlatformData {
    private final Vector3f position;
    private final Vector3f size;
    private final int type;

    public PlatformData(Vector3f position, Vector3f size, int type) {
        this.position = new Vector3f(position);
        this.size = new Vector3f(size);
        this.type = type;
    }

    // Getters avec copies défensives
    public Vector3f getPosition() { return new Vector3f(position); }
    public Vector3f getSize() { return new Vector3f(size); }
    public int getType() { return type; }
}