// src/main/java/Core/World/PlatformGenerator.java
package Core.World;

import Core.Entities.Model;
import Core.Entities.Platform;
import Core.Entities.Texture;
import Core.ObjectLoader;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlatformGenerator {
    private final Random random;
    private final ObjectLoader loader;

    // Paramètres de génération
    private static final float MIN_PLATFORM_WIDTH = 0.3f;
    private static final float MAX_PLATFORM_WIDTH = 0.8f;
    private static final float PLATFORM_HEIGHT = 0.05f;

    private static final float MIN_JUMP_DISTANCE = 0.4f;
    private static final float MAX_JUMP_DISTANCE = 0.7f;
    private static final float MIN_JUMP_HEIGHT = 0.2f;
    private static final float MAX_JUMP_HEIGHT = 0.5f;

    private static final float WORLD_WIDTH = 10.0f;
    private static final float START_HEIGHT = -0.3f;

    public PlatformGenerator() {
        this.random = new Random();
        this.loader = ObjectLoader.getInstance();
    }

    /**
     * Génère une série de plateformes accessibles
     */
    public List<Platform> generatePlatforms(int count, Vector3f startPosition) {
        List<Platform> platforms = new ArrayList<>();

        Vector3f currentPos = new Vector3f(startPosition);

        for (int i = 0; i < count; i++) {
            // Calculer la prochaine position
            Vector3f nextPos = calculateNextPlatformPosition(currentPos, i == 0);

            // Taille de la plateforme
            float width = MIN_PLATFORM_WIDTH + random.nextFloat() * (MAX_PLATFORM_WIDTH - MIN_PLATFORM_WIDTH);
            Vector3f size = new Vector3f(width, PLATFORM_HEIGHT, 0.1f);

            // Créer le modèle
            Model platformModel = createPlatformModel(size);

            // Créer la plateforme
            Platform platform = new Platform(nextPos, size, platformModel, 0);
            platforms.add(platform);

            currentPos = nextPos;

            System.out.println("🔨 Plateforme " + i + " générée à: " +
                    String.format("X:%.2f Y:%.2f (Largeur: %.2f)",
                            nextPos.x, nextPos.y, width));
        }

        return platforms;
    }

    /**
     * Calcule la position de la prochaine plateforme de manière accessible
     */
    private Vector3f calculateNextPlatformPosition(Vector3f currentPos, boolean isFirst) {
        float nextX, nextY;

        if (isFirst) {
            // Première plateforme proche du spawn
            nextX = currentPos.x + 0.5f + random.nextFloat() * 0.3f;
            nextY = currentPos.y + random.nextFloat() * 0.2f - 0.1f;
        } else {
            // Distance horizontale raisonnable pour le saut
            float jumpDistance = MIN_JUMP_DISTANCE + random.nextFloat() * (MAX_JUMP_DISTANCE - MIN_JUMP_DISTANCE);

            // Direction (droite principalement, parfois gauche)
            float direction = random.nextFloat() < 0.8f ? 1.0f : -1.0f;
            nextX = currentPos.x + jumpDistance * direction;

            // Hauteur accessible (monte parfois, descend parfois)
            float heightVariation = (random.nextFloat() - 0.3f) * (MAX_JUMP_HEIGHT - MIN_JUMP_HEIGHT);
            nextY = currentPos.y + heightVariation;

            // Limites du monde
            nextX = Math.max(-WORLD_WIDTH/2, Math.min(WORLD_WIDTH/2, nextX));
            nextY = Math.max(-1.0f, Math.min(1.0f, nextY));
        }

        return new Vector3f(nextX, nextY, 0.0f);
    }

    /**
     * Crée le modèle 3D pour une plateforme
     */
    private Model createPlatformModel(Vector3f size) {
        float halfWidth = size.x / 2;
        float halfHeight = size.y / 2;

        // Géométrie du quad
        float[] vertices = {
                -halfWidth, -halfHeight, 0.0f,  // Bas gauche
                -halfWidth,  halfHeight, 0.0f,  // Haut gauche
                halfWidth,  halfHeight, 0.0f,  // Haut droit
                halfWidth, -halfHeight, 0.0f   // Bas droit
        };

        int[] indices = {
                0, 1, 3,
                3, 1, 2
        };

        float[] textureCoords = {
                0.0f, 1.0f,  // Bas gauche
                0.0f, 0.0f,  // Haut gauche
                1.0f, 0.0f,  // Haut droit
                1.0f, 1.0f   // Bas droit
        };

        try {
            Model model = loader.loadModel(vertices, textureCoords, indices);

            // ✅ Gestion d'erreur améliorée pour les textures
            try {
                // Essayer de charger une texture spécifique pour les plateformes
                int textureId = loader.loadTexture("src/main/resources/textures/platform.png");
                model.setTexture(new Texture(textureId));
                System.out.println("✅ Texture plateforme chargée");
            } catch (Exception e1) {
                try {
                    // Fallback vers texture par défaut
                    int defaultId = loader.createDefaultTexture();
                    model.setTexture(new Texture(defaultId));
                    System.out.println("⚠️ Plateforme avec texture par défaut");
                } catch (Exception e2) {
                    System.err.println("❌ Impossible de créer texture: " + e2.getMessage());
                    // Le modèle sera rendu sans texture
                }
            }

            return model;
        } catch (Exception e) {
            System.err.println("❌ Erreur création modèle plateforme: " + e.getMessage());
            return null;
        }
    }
}