// Remplacer le contenu de Core/Entities/Camera.java
package Core.Entities;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f target;
    private Vector3f up;

    // Matrices de transformation
    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;

    // Paramètres de caméra
    private float fov = 45.0f;
    private float aspectRatio;
    private float nearPlane = 0.1f;
    private float farPlane = 100.0f;

    // Pour suivre le joueur
    private Vector3f offset;
    private float smoothing = 0.1f;

    public Camera(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        this.position = new Vector3f(0.0f, 0.0f, 5.0f);
        this.target = new Vector3f(0.0f, 0.0f, 0.0f);
        this.up = new Vector3f(0.0f, 1.0f, 0.0f);
        this.offset = new Vector3f(0.0f, 1.0f, 5.0f); // Caméra derrière et au-dessus

        this.viewMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();

        updateProjectionMatrix();
    }

    public void followTarget(Vector3f targetPosition) {
        // Position désirée de la caméra
        Vector3f desiredPosition = new Vector3f(targetPosition).add(offset);

        // Interpolation lisse
        position.lerp(desiredPosition, smoothing);
        target.lerp(targetPosition, smoothing);

        updateViewMatrix();
    }

    public void update(Vector3f playerPosition) {
        followTarget(playerPosition);
    }

    private void updateViewMatrix() {
        viewMatrix.setLookAt(position, target, up);
    }

    private void updateProjectionMatrix() {
        projectionMatrix.setPerspective((float)Math.toRadians(fov), aspectRatio, nearPlane, farPlane);
    }

    // Getters
    public Matrix4f getViewMatrix() { return new Matrix4f(viewMatrix); }
    public Matrix4f getProjectionMatrix() { return new Matrix4f(projectionMatrix); }
    public Vector3f getPosition() { return new Vector3f(position); }

    // Setters
    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        updateProjectionMatrix();
    }

    public void setOffset(Vector3f offset) {
        this.offset.set(offset);
    }
}