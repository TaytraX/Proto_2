package Core.World;

import Core.Entities.Model;
import Core.Entities.Platform;
import Laucher.TestGame;
import Core.ObjectLoader;
import Core.RenderManager;
import org.joml.Vector3f;
import Core.Entities.Platform;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlatformManager {

    private final CopyOnWriteArrayList<Platform> platforms;
    private final PlatformGenerator generator;
    private final ObjectLoader loader;
    private RenderManager renderer;

    private float lastGeneratedX = 0.0f;
    private volatile boolean isGenerating = false;
    private static final float GENERATION_DISTANCE = 10.0f;
    private final int GRID_SIZE = 2; // Taille des cellules
    private final Map<String, List<Platform>> spatialGrid = new ConcurrentHashMap<>();


    public PlatformManager(RenderManager renderer) {
        this.renderer = renderer;
        this.platforms = new CopyOnWriteArrayList<>();
        this.loader = ObjectLoader.getInstance();
        this.generator = new PlatformGenerator();
    }

    public void inits() {
        // Créer quelques plateformes initiales
        createInitialPlatforms();
    }

    // ✅ Amélioration dans PlatformManager.update()
    public void update(Vector3f playerPosition) {
        try {
            if (shouldGenerateMore(playerPosition) && !isGenerating) {
                isGenerating = true;
                Vector3f startPos = new Vector3f(lastGeneratedX + 1.0f, -0.2f, 0.0f);
                generator.requestPlatforms(8, startPos);
            }

            processGeneratedPlatforms();
            cleanupDistantPlatforms(playerPosition);

        } catch (Exception e) {
            System.err.println("❌ Erreur critique dans PlatformManager: " + e.getMessage());
            e.printStackTrace();
            isGenerating = false; // Reset du flag en cas d'erreur
        }
    }

    private void processGeneratedPlatforms() {
        List<PlatformData> newPlatformData = generator.pollGeneratedPlatforms();

        if (!newPlatformData.isEmpty()) {
            // ✅ Créer les modèles OpenGL sur le thread principal
            for (PlatformData data : newPlatformData) {
                Model model = createPlatformModel(data.getSize());
                Platform platform = new Platform(data.getPosition(), model);
                platforms.add(platform);

                lastGeneratedX = Math.max(lastGeneratedX, data.getPosition().x);
            }

            isGenerating = false;
            System.out.println("🔨 " + newPlatformData.size() + " plateformes ajoutées");
        }
    }

    private String getGridKey(Vector3f position) {
        int x = (int) (position.x / GRID_SIZE);
        int y = (int) (position.y / GRID_SIZE);
        return x + "," + y;
    }

    private void createInitialPlatforms() {
        // Ajouter des plateformes de base pour tester
        Vector3f pos1 = new Vector3f(2.0f, -0.2f, 0.0f);
        Vector3f size1 = new Vector3f(1.0f, 0.2f, 0.1f);
        Model model1 = createPlatformModel(size1);
        platforms.add(new Platform(pos1, model1));

        Vector3f pos2 = new Vector3f(4.0f, 0.1f, 0.0f);
        Vector3f size2 = new Vector3f(1.2f, 0.2f, 0.1f);
        Model model2 = createPlatformModel(size2);
        platforms.add(new Platform(pos2, model2));

        lastGeneratedX = 5.0f;
    }

    // Dans PlatformManager.createPlatformModel()
    private Model createPlatformModel(Vector3f size) {
        float halfX = size.x / 2;
        float halfY = size.y / 2;

        float[] vertices = {
                -halfX, -halfY, 0.1f,  // ✅ Z=0.1 (derrière le joueur)
                halfX, -halfY, 0.1f,
                halfX,  halfY, 0.1f,
                -halfX,  halfY, 0.1f
        };

        int[] indices = {0, 1, 2, 2, 3, 0};
        float[] texCoords = {0, 0, 1, 0, 1, 1, 0, 1};

        return loader.loadModel(vertices, texCoords, indices);
    }

    // ✅ Méthode cruciale manquante
    public Platform findPlatformBelow(Vector3f playerPos, Vector3f playerSize) {
        Platform closestPlatform = null;
        float closestDistance = Float.MAX_VALUE;

        for (Platform platform : platforms) {
            Vector3f platPos = platform.getPosition();
            Vector3f platSize = platform.getSize(); // ✅ Maintenant disponible

            // Vérifier si le joueur est horizontalement au-dessus de la plateforme
            boolean horizontallyAligned =
                    playerPos.x + playerSize.x/2 > platPos.x - platSize.x/2 &&
                            playerPos.x - playerSize.x/2 < platPos.x + platSize.x/2;

            // La plateforme doit être en dessous du joueur
            if (horizontallyAligned && platPos.y < playerPos.y) {
                float distance = playerPos.y - platPos.y;
                if (distance < closestDistance && distance < 1.0f) { // ✅ Distance max
                    closestDistance = distance;
                    closestPlatform = platform;
                }
            }
        }

        return closestPlatform;
    }

    // Dans PlatformManager.render()
    public void render() {
        System.out.println("🔨 Rendu de " + platforms.size() + " plateformes");

        for (Platform platform : platforms) {
            try {
                Vector3f position = platform.getPosition();
                Model model = platform.getModel();

                if (model != null && renderer != null) {
                    System.out.println("🎯 Rendu plateforme à: " + position);
                    renderer.render(model, position);
                } else {
                    System.err.println("❌ Model ou renderer null");
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur rendu plateforme: " + e.getMessage());
                e.printStackTrace(); // ✅ Ajouter la stack trace
            }
        }
    }

    private void cleanupDistantPlatforms(Vector3f playerPos) {
        platforms.removeIf(platform -> {
            float distance = Math.abs(platform.getPosition().x - playerPos.x);
            return distance > 20.0f; // Supprimer si trop loin
        });
    }

    private boolean shouldGenerateMore(Vector3f playerPos) {
        return playerPos.x > lastGeneratedX - GENERATION_DISTANCE;
    }
}