// src/main/java/Core/World/WorldManager.java
package Core.World;

import Core.Entities.Platform;
import Core.RenderManager;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorldManager {
    private final CopyOnWriteArrayList<Platform> platforms;
    private final PlatformGenerator generator;
    private final Object worldLock = new Object();

    // Zone de génération dynamique
    private float lastGeneratedX = 0.0f;
    private static final float GENERATION_DISTANCE = 5.0f;
    private static final int PLATFORMS_PER_CHUNK = 8;

    public WorldManager() {
        this.platforms = new CopyOnWriteArrayList<>();
        this.generator = new PlatformGenerator();

        // Générer les plateformes initiales
        generateInitialPlatforms();
    }

    private void generateInitialPlatforms() {
        // Plateforme de spawn
        Vector3f spawnPos = new Vector3f(-0.5f, -0.2f, 0.0f);
        List<Platform> initialPlatforms = generator.generatePlatforms(PLATFORMS_PER_CHUNK, spawnPos);

        synchronized (worldLock) {
            platforms.addAll(initialPlatforms);
            if (!initialPlatforms.isEmpty()) {
                lastGeneratedX = initialPlatforms.get(initialPlatforms.size() - 1).getPosition().x;
            }
        }

        // Debug détaillé
        System.out.println("✅ " + initialPlatforms.size() + " plateformes générées:");
        for (int i = 0; i < initialPlatforms.size(); i++) {
            Platform p = initialPlatforms.get(i);
            System.out.println("  Plateforme " + i + ": X=" + p.getPosition().x +
                    ", Y=" + p.getPosition().y + ", Largeur=" + p.getSize().x);
        }
    }

    /**
     * Met à jour le monde en fonction de la position du joueur
     */
    public void update(Vector3f playerPosition) {
        // Génération dynamique si le joueur s'approche du bord
        if (playerPosition.x > lastGeneratedX - GENERATION_DISTANCE) {
            generateMorePlatforms();
        }

        // Optionnel: nettoyer les plateformes lointaines pour économiser la mémoire
        cleanupDistantPlatforms(playerPosition);
    }

    private void generateMorePlatforms() {
        synchronized (worldLock) {
            Vector3f startPos = new Vector3f(lastGeneratedX + 1.0f, -0.2f, 0.0f);
            List<Platform> newPlatforms = generator.generatePlatforms(PLATFORMS_PER_CHUNK, startPos);

            platforms.addAll(newPlatforms);
            if (!newPlatforms.isEmpty()) {
                lastGeneratedX = newPlatforms.get(newPlatforms.size() - 1).getPosition().x;
            }

            System.out.println("🔨 " + newPlatforms.size() + " nouvelles plateformes générées");
        }
    }

    private void cleanupDistantPlatforms(Vector3f playerPosition) {
        // Supprimer plateformes trop loin derrière le joueur
        final float CLEANUP_DISTANCE = 15.0f;

        platforms.removeIf(platform ->
                platform.getPosition().x < playerPosition.x - CLEANUP_DISTANCE);
    }

    /**
     * Vérifie les collisions avec toutes les plateformes
     */
    public Platform checkPlatformCollision(Vector3f playerPos, Vector3f playerSize) {
        for (Platform platform : platforms) {
            if (platform.checkCollision(playerPos, playerSize)) {
                return platform;
            }
        }
        return null;
    }

    /**
     * Trouve la plateforme sous le joueur (pour l'atterrissage)
     */
    public Platform findPlatformBelow(Vector3f playerPos, Vector3f playerSize) {
        Platform closestPlatform = null;
        float closestDistance = Float.MAX_VALUE;

        for (Platform platform : platforms) {
            Vector3f platformPos = platform.getPosition();

            // Vérifier si horizontalement aligné
            if (playerPos.x >= platform.getLeft() - playerSize.x/2 &&
                    playerPos.x <= platform.getRight() + playerSize.x/2) {

                // Vérifier si la plateforme est en dessous
                float distance = playerPos.y - platform.getTop();
                if (distance > 0 && distance < closestDistance) {
                    closestDistance = distance;
                    closestPlatform = platform;
                }
            }
        }

        return closestPlatform;
    }

    /**
     * Rendu de toutes les plateformes
     */
    public void render(RenderManager renderer) {
        for (Platform platform : platforms) {
            if (platform.getModel() != null) {
                renderer.render(platform.getModel(), platform.getPosition());
            }
        }
    }

    // Getters
    public List<Platform> getPlatforms() {
        return new ArrayList<>(platforms);
    }

    public int getPlatformCount() {
        return platforms.size();
    }
}