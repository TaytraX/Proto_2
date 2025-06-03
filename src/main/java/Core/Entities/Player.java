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
    private boolean wasMoving = false;

    // ✅ SOLUTION: Ajouter référence à ObjectLoader
    private ObjectLoader loader;

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
        updateAnimationState();

        // ✅ CORRECTION: Vérifier si l'animation et la frame existent
        if (currentAnimation != null) {
            currentAnimation.update();

            Texture currentFrame = currentAnimation.getCurrentFrame();
            if (currentFrame != null && model != null) {
                model.setTexture(currentFrame);

                // Debug moins fréquent
                if (Math.random() < 0.001) { // Très occasionnel
                    System.out.println("🎬 Frame actuelle: " +
                            currentAnimation.getCurrentFrameIndex() + "/" +
                            currentAnimation.getFrameCount());
                }
            }
        }

        // Logique de physique (inchangée)
        if (!isOnGround) {
            velocity.y += GRAVITY;
        }

        if (isMovingLeft) {
            velocity.x = -MOVE_SPEED;
        } else if (isMovingRight) {
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
        if (position.x > 1.5f) position.x = 1.5f;

        wasMoving = isMovingLeft || isMovingRight;
    }

    private void updateAnimationState() {
        Animation newAnimation = null;

        if (!isOnGround && isMovingRight){
            newAnimation = jumpRigthAnimation;
        } else if(!isOnGround && isMovingLeft) {
            newAnimation = jumpLeftAnimation;
        } else if (!isOnGround) {
            newAnimation = jumpAnimation;
        }  else if (isMovingRight) {
            newAnimation = walkRigthAnimation;
        } else if (isMovingLeft) {
            newAnimation = walkLeftAnimation;
        } else {
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
            System.out.println("🎬 Animation changée: " + animName);
        }
    }

    // Méthodes existantes...
    public void jump() {
        if (isOnGround) {
            velocity.y = JUMP_STRENGTH;
            isOnGround = false;
            System.out.println("🦘 Saut ! Position: " + position.y);
        }
    }

    public void moveLeft(boolean moving) {
        this.isMovingLeft = moving;
        if (moving && !wasMoving) {
            System.out.println("⬅️ Déplacement à gauche");
        }
    }

    public void moveRight(boolean moving) {
        this.isMovingRight = moving;
        if (moving && !wasMoving) {
            System.out.println("➡️ Déplacement à droite");
        }
    }

    // Getters...
    public Vector3f getPosition() { return position; }
    public Model getModel() { return model; }
    public boolean isOnGround() { return isOnGround; }
    public Vector3f getVelocity() { return velocity; }
    public Animation getCurrentAnimation() { return currentAnimation; }
    public boolean isAnimationPlaying() {
        return currentAnimation != null && currentAnimation.isPlaying();
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }
}