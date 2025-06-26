package Core.Entities;

import Core.ObjectLoader;
import Core.World.PlatformManager;
import org.joml.Vector3f;
import Core.Entities.Texture;
import Core.Entities.Animation;

public class Player {
    private final Vector3f position;
    private final Vector3f velocity;
    private final Model model;

    // ✅ Animations avec volatile pour visibilité entre threads
    private volatile Animation currentAnimation;
    private Animation idleAnimation;
    private Animation walkRightAnimation;
    private Animation walkLeftAnimation;
    private Animation jumpAnimation;
    private Animation jumpRightAnimation;
    private Animation jumpLeftAnimation;

    // États du joueur avec volatile
    private volatile boolean isOnGround = true;
    private volatile boolean isMovingLeft = false;
    private volatile boolean isMovingRight = false;
    private volatile int effectiveDirection = 0;

    // Constantes
    private static final float MOVE_SPEED = 0.01f;
    private static final float JUMP_STRENGTH = 0.05f;
    private static final float GRAVITY = -0.0025f;
    private static final float GROUND_LEVEL = -0.4f;

    // Verrous pour différentes opérations
    private final Object positionLock = new Object();
    private final Object inputLock = new Object();
    private PlatformManager platforms;

    public Player(Model model) { // ✅ Plus besoin de passer le loader
        this.model = model;
        this.position = new Vector3f(0.0f, GROUND_LEVEL, 0.0f);
        this.velocity = new Vector3f(0.0f, 0.0f, 0.0f);
        initAnimations();
    }

    private void initAnimations() {
        try {
            ObjectLoader loader = ObjectLoader.getInstance(); // ✅ Singleton

            idleAnimation = new Animation(4, 8, "player1", loader);
            walkRightAnimation = new Animation(6, 12, "player_moove", loader);
            jumpAnimation = new Animation(3, 10, "player_jump", loader);
            jumpRightAnimation = new Animation(4, 16, "player_Rigth_jump", loader);
            jumpLeftAnimation = new Animation(4, 16, "player_Left_jump", loader);
            walkLeftAnimation = new Animation(3, 10, "player_moove_gauche", loader);

            currentAnimation = idleAnimation;
            currentAnimation.play();

            System.out.println("✅ Animations du joueur initialisées");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation des animations: " + e.getMessage());
        }
    }

    // ✅ Dans Player.java - optimisation des verrous
    public void update() {
        synchronized (this) {
            Vector3f oldPosition = new Vector3f(position);
            updateMovement();

            // ✅ Debug pour voir les collisions
            if (Math.random() < 0.01) { // 1% du temps
                System.out.println("🎮 Joueur: X=" + String.format("%.2f", position.x) +
                        " Y=" + String.format("%.2f", position.y) +
                        " Au sol: " + isOnGround +
                        " Vélocité Y: " + String.format("%.3f", velocity.y));
            }

            if (!oldPosition.equals(position)) {
                updateAnimations();
            }
        }
    }

    private void updateMovement() {
        calculateEffectiveDirection();

        Vector3f oldPosition = new Vector3f(position);

        // Appliquer la gravité
        if (!isOnGround) {
            velocity.y += GRAVITY;
        }

        // Mouvement horizontal
        if (effectiveDirection == -1) {
            velocity.x = -MOVE_SPEED;
        } else if (effectiveDirection == 1) {
            velocity.x = MOVE_SPEED;
        } else {
            velocity.x = 0.0f;
        }

        // Prédire la nouvelle position
        Vector3f newPosition = new Vector3f(position).add(velocity);

        // Vérifier les collisions avec les plateformes
        handlePlatformCollisions(newPosition);

        // Limites du monde
        clampToWorldBounds();
    }

    private void updateAnimations() {
        updateAnimationState();

        if (currentAnimation != null) {
            currentAnimation.update();
            Texture currentFrame = currentAnimation.getCurrentFrame();
            if (currentFrame != null && model != null) {
                model.setTexture(currentFrame);
            }
        }
    }

    private void clampToWorldBounds() {
        // Limites horizontales (optionnel)
        if (position.x < -10.0f) position.x = -10.0f;
        if (position.x > 50.0f) position.x = 50.0f;
    }

    private void calculateEffectiveDirection() {
        synchronized (inputLock) {
            if (isMovingLeft && isMovingRight) {
                effectiveDirection = 0;
            } else if (isMovingLeft) {
                effectiveDirection = -1;
            } else if (isMovingRight) {
                effectiveDirection = 1;
            } else {
                effectiveDirection = 0;
            }
        }
    }

    // Dans Player.handlePlatformCollisions()
    private void handlePlatformCollisions(Vector3f newPosition) {
        if (platforms == null) {
            position.set(newPosition);
            handleGroundCollision();
            return;
        }

        Vector3f playerSize = new Vector3f(0.8f, 1.2f, 0.1f);

        // ✅ Collision seulement quand on tombe
        if (velocity.y <= 0) {
            Platform platformBelow = platforms.findPlatformBelow(newPosition, playerSize);

            if (platformBelow != null) {
                Vector3f platPos = platformBelow.getPosition();
                Vector3f platSize = platformBelow.getSize();

                float platformTop = platPos.y + platSize.y/2;
                float playerBottom = newPosition.y - playerSize.y/2;

                // ✅ Distance de tolérance plus grande
                float tolerance = 0.1f;

                if (Math.abs(playerBottom - platformTop) <= tolerance) {
                    position.x = newPosition.x;
                    position.y = platformTop + playerSize.y/2;
                    velocity.y = 0.0f;
                    isOnGround = true;

                    System.out.println("🎯 Collision détectée ! Joueur posé sur plateforme");
                    return;
                }
            }
        }

        // Mouvement normal
        position.set(newPosition);
        handleGroundCollision();
    }

    private void updateAnimationState() {
        Animation newAnimation = null;

        // Vérifier d'abord si on est au sol ET sans mouvement
        if (isOnGround && effectiveDirection == 0) {
            // ✅ Animation idle quand on est au sol et qu'on ne bouge pas
            // (y compris quand A et D sont pressés simultanément)
            newAnimation = idleAnimation;
        }
        // Animations de saut
        else if (!isOnGround && effectiveDirection == 1) {
            newAnimation = jumpRightAnimation;
        } else if (!isOnGround && effectiveDirection == -1) {
            newAnimation = jumpLeftAnimation;
        } else if (!isOnGround) {
            newAnimation = jumpAnimation;
        }
        // Animations de marche (seulement si on bouge vraiment)
        else if (isOnGround && effectiveDirection == 1) {
            newAnimation = walkRightAnimation;
        } else if (isOnGround && effectiveDirection == -1) {
            newAnimation = walkLeftAnimation;
        }

        // Changer d'animation si nécessaire
        if (newAnimation != currentAnimation && newAnimation != null) {
            if (currentAnimation != null) {
                currentAnimation.stop();
            }
            currentAnimation = newAnimation;
            currentAnimation.play();

            // ✅ Debug pour vérifier les changements d'animation
            System.out.println("🎬 Animation changée vers: " + getAnimationName(newAnimation) +
                    " (Direction: " + effectiveDirection + ", Au sol: " + isOnGround + ")");
        }
    }

    private void handleGroundCollision() {
        if (position.y <= GROUND_LEVEL) {
            position.y = GROUND_LEVEL;
            velocity.y = 0.0f;
            isOnGround = true;
        } else {
            isOnGround = false;
        }
    }

    // ✅ Méthode helper pour le debug
    private String getAnimationName(Animation animation) {
        if (animation == idleAnimation) return "IDLE";
        if (animation == walkRightAnimation) return "WALK_RIGHT";
        if (animation == walkLeftAnimation) return "WALK_LEFT";
        if (animation == jumpAnimation) return "JUMP";
        if (animation == jumpRightAnimation) return "JUMP_RIGHT";
        if (animation == jumpLeftAnimation) return "JUMP_LEFT";
        return "UNKNOWN";
    }

    // ✅ Méthodes d'entrée de thread safe
    public void jump() {
        synchronized (positionLock) {
            if (isOnGround) {
                velocity.y = JUMP_STRENGTH;
                isOnGround = false;
            }
        }
    }

    public void moveLeft(boolean moving) {
        synchronized (inputLock) {
            this.isMovingLeft = moving;
        }
    }

    public void moveRight(boolean moving) {
        synchronized (inputLock) {
            this.isMovingRight = moving;
        }
    }

    // ✅ Getter thread safe avec copie défensive
    public Vector3f getPosition() {
        synchronized (positionLock) {
            return new Vector3f(position); // ✅ Copie pour éviter les modifications concurrentes
        }
    }

    public Model getModel() {
        return model;
    }

    public void setPlatformManager(PlatformManager platformManager) {
        this.platforms = platformManager;
        System.out.println("✅ PlatformManager lié au joueur");
    }
}