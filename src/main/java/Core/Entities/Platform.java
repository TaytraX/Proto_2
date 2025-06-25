// src/main/java/Core/Entities/Platform.java
package Core.Entities;

import Core.Ilogic;
import org.joml.Vector3f;

public class Platform {
    private final Vector3f position;
    private final Vector3f size;
    private final Model model;
    private final int type; // Type de plateforme (normal, fragile, mobile, etc.)

    // Verrou pour thread safety
    private final Object platformLock = new Object();

    public Platform(Vector3f position, Vector3f size, Model model, int type) {
        this.position = new Vector3f(position);
        this.size = new Vector3f(size);
        this.model = model;
        this.type = type;
    }

    // Vérifier si un point est sur la plateforme
    public boolean isPointOnPlatform(float x, float y) {
        synchronized (platformLock) {
            return x >= position.x - size.x/2 &&
                    x <= position.x + size.x/2 &&
                    y >= position.y - size.y/2 &&
                    y <= position.y + size.y/2;
        }
    }

    // Vérifier collision avec le joueur
    public boolean checkCollision(Vector3f playerPos, Vector3f playerSize) {
        synchronized (platformLock) {
            return playerPos.x + playerSize.x/2 >= position.x - size.x/2 &&
                    playerPos.x - playerSize.x/2 <= position.x + size.x/2 &&
                    playerPos.y + playerSize.y/2 >= position.y - size.y/2 &&
                    playerPos.y - playerSize.y/2 <= position.y + size.y/2;
        }
    }

    // Getters thread-safe
    public Vector3f getPosition() {
        synchronized (platformLock) {
            return new Vector3f(position);
        }
    }

    public Vector3f getSize() {
        synchronized (platformLock) {
            return new Vector3f(size);
        }
    }

    public Model getModel() { return model; }
    public int getType() { return type; }

    // Pour le debug
    public float getTop() { return position.y + size.y/2; }
    public float getBottom() { return position.y - size.y/2; }
    public float getLeft() { return position.x - size.x/2; }
    public float getRight() { return position.x + size.x/2; }
}