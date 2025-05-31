package Core.Entities;

import org.joml.Vector3f;

public class Player {

    private Vector3f position;
    private Vector3f velocity;
    private Model model;

    // Constantes de gameplay
    private static final float MOVE_SPEED = 0.01f;      // Vitesse de déplacement horizontal
    private static final float JUMP_STRENGTH = 0.05f;   // Force du saut
    private static final float GRAVITY = -0.0025f;       // Gravité
    private static final float GROUND_LEVEL = -0.4f;    // Niveau du sol

    // États du joueur
    private boolean isOnGround = true;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    public Player(Model model) {
        this.model = model;
        this.position = new Vector3f(0.0f, GROUND_LEVEL, 0.0f);
        this.velocity = new Vector3f(0.0f, 0.0f, 0.0f);
    }

    public void update() {
        // Appliquer la gravité
        if (!isOnGround) {
            velocity.y += GRAVITY;
        }

        // Déplacement horizontal
        if (isMovingLeft) {
            velocity.x = -MOVE_SPEED;
        } else if (isMovingRight) {
            velocity.x = MOVE_SPEED;
        } else {
            velocity.x = 0.0f; // Arrêt immédiat (ou vous pouvez ajouter de l'inertie)
        }

        // Appliquer la vélocité à la position
        position.add(velocity);

        // Vérifier les collisions avec le sol
        if (position.y <= GROUND_LEVEL) {
            position.y = GROUND_LEVEL;
            velocity.y = 0.0f;
            isOnGround = true;
        } else {
            isOnGround = false;
        }

        // Limiter les mouvements horizontaux (optionnel)
        if (position.x < -1.0f) position.x = -1.0f;
        if (position.x > 1.5f) position.x = 1.5f;
    }

    public void jump() {
        if (isOnGround) {
            velocity.y = JUMP_STRENGTH;
            isOnGround = false;
            System.out.println("🦘 Saut ! Position: " + position.y);
        }
    }

    public void moveLeft(boolean moving) {
        this.isMovingLeft = moving;
        if (moving) {
            System.out.println("⬅️ Déplacement à gauche");
        }
    }

    public void moveRight(boolean moving) {
        this.isMovingRight = moving;
        if (moving) {
            System.out.println("➡️ Déplacement à droite");
        }
    }

    // Getters
    public Vector3f getPosition() {
        return position;
    }

    public Model getModel() {
        return model;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    // Setters pour debugging
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }
}