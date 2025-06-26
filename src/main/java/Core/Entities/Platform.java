// src/main/java/Core/Entities/Platform.java
package Core.Entities;

import org.joml.Random;
import org.joml.Vector3f;

public class Platform {
    private final Vector3f position;
    private final Vector3f size;
    private final Model model;

    // Verrou pour thread safety
    private final Object platformLock = new Object();

    public Platform(Vector3f position, Model model) {
        float length = 0.6f + new Random().nextFloat() * (2.0f - 0.6f);
        float height = 0.3f;
        this.size = new Vector3f(length, height, 0.0f);
        this.position = new Vector3f(position);
        this.model = model;
    }

    // Getters thread-safe
    public Vector3f getPosition() {
        synchronized (platformLock) {
            return new Vector3f(position);
        }
    }

    public Model getModel() { return model; }

    // Pour le debug
    public float getTop() { return position.y + size.y/2; }
}