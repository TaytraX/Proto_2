package Core.Entities;

import Core.ObjectLoader;

import org.joml.Vector3f;

public class Player {
    private final Vector3f position;
    private final Vector3f velocity;
    private final Model model;

    private Animation idleAnimation;
    private Animation walkRigthAnimation;
    private Animation walkLeftAnimation;
    private Animation jumpAnimation;
    private Animation jumpRigthAnimation;
    private Animation jumpLeftAnimation;
    private Animation currentAnimation;

    // Constantes de gameplay
    private static final float MOVE_SPEED = 0.01f;
    private static final float JUMP_STRENGTH = 0.05f;
    private static final float GRAVITY = -0.0025f;
    private static final float GROUND_LEVEL = -0.4f;

    // États du joueur
    private boolean isOnGround = true;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    private int effectiveDirection = 0;

    private final ObjectLoader loader;

    // Verrou pour synchroniser les mises à jour complexes
    private final Object updateLock = new Object();

    public Player(Model model, ObjectLoader loader) { // ✅ Passer le loader
        this.model = model;
        this.loader = loader;
        this.position = new Vector3f(0.0f, GROUND_LEVEL, 0.0f);
        this.velocity = new Vector3f(0.0f, 0.0f, 0.0f);

        initAnimations();
    }

    // ✅ INITIALISATION CORRIGÉE des animations
    private void initAnimations() {
        try {
            // Créer les différentes animations avec le loader partagé
            idleAnimation = new Animation(4, 8, "player1", loader);
            walkRigthAnimation = new Animation(6, 12, "player_moove", loader);
            jumpAnimation = new Animation(3, 10, "player_jump", loader);
            jumpRigthAnimation = new Animation(4, 16, "player_Rigth_jump", loader);
            jumpLeftAnimation = new Animation(4, 16, "player_Left_jump", loader);
            walkLeftAnimation = new Animation(3, 10, "player_moove_gauche", loader);

            // Animation par défaut
            currentAnimation = idleAnimation;
            currentAnimation.play();

            System.out.println("✅ Animations du joueur initialisées");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation des animations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update() {
        synchronized (updateLock) {
            calculateEffectiveDirection();
            updateAnimationState();

            if (currentAnimation != null) {
                currentAnimation.update();

                Texture currentFrame = currentAnimation.getCurrentFrame();
                if (currentFrame != null && model != null) {
                    model.setTexture(currentFrame);

                    if (Math.random() < 0.001) {
                        System.out.println("🎬 Frame actuelle: " +
                                currentAnimation.getCurrentFrameIndex() + "/" +
                                currentAnimation.getFrameCount());
                    }
                }
            }
        }

        // Logique de physique du joueur
        if (!isOnGround) {
            velocity.y += GRAVITY;
        }

        if (effectiveDirection == -1) {
            velocity.x = -MOVE_SPEED;
        } else if (effectiveDirection == 1) {
            velocity.x = MOVE_SPEED;
        } else {
            velocity.x = 0.0f;
        }

        position.add(velocity);

        if (position.y <= GROUND_LEVEL) {
            position.y = GROUND_LEVEL;
            velocity.y = 0.0f;
            isOnGround = true;
        } else {
            isOnGround = false;
        }

        if (position.x < -1.0f) position.x = -1.0f;
        if (position.x > 1.0f) position.x = 1.0f;
    }

    // Méthode pour calculer la direction effective
    private void calculateEffectiveDirection() {
        if (isMovingLeft && isMovingRight) {
            // ✅ Si les deux touches sont pressées = immobile
            effectiveDirection = 0;
        } else if (isMovingLeft) {
            effectiveDirection = -1;
        } else if (isMovingRight) {
            effectiveDirection = 1;
        } else {
            effectiveDirection = 0;
        }
    }

    private void updateAnimationState() {
        Animation newAnimation;

        if (!isOnGround && effectiveDirection == 1) {
            newAnimation = jumpRigthAnimation;
        } else if (!isOnGround && effectiveDirection == -1) {
            newAnimation = jumpLeftAnimation;
        } else if (!isOnGround) {
            newAnimation = jumpAnimation;
        } else if (effectiveDirection == 1) {
            newAnimation = walkRigthAnimation;
        } else if (effectiveDirection == -1) {
            newAnimation = walkLeftAnimation;
        } else {
            // ✅ Si effectiveDirection == 0 (immobile ou touches simultanées)
            newAnimation = idleAnimation;
        }

        if (newAnimation != currentAnimation && newAnimation != null) {
            if (currentAnimation != null) {
                currentAnimation.stop();
            }
            currentAnimation = newAnimation;
            currentAnimation.play();

            String animName = (newAnimation == idleAnimation) ? "IDLE" :
                    (newAnimation == walkRigthAnimation || newAnimation == walkLeftAnimation) ? "WALK" : "JUMP";
            System.out.println("🎬 Animation changée: " + animName + " (Direction: " + effectiveDirection + ")");
        }
    }
    // ✅ Méthodes d'entrée thread-safe
    public synchronized void jump() {
        if (isOnGround) {
            velocity.y = JUMP_STRENGTH;
            isOnGround = false;
        }
    }

    public synchronized void moveLeft(boolean moving) {
        this.isMovingLeft = moving;
    }

    public synchronized void moveRight(boolean moving) {
        this.isMovingRight = moving;
    }

    // ✅ Getters thread-safe
    public synchronized Vector3f getPosition() {
        return position; // Retourner une copie pour éviter les modifications concurrentes
    }

    public Model getModel() { return model; }
}