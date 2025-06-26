package Core.World;

import Core.Entities.Model;
import Core.Entities.Platform;
import Core.Entities.Texture;
import Core.ObjectLoader;
import Core.RenderManager;
import org.joml.Vector3f;

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

    // Dans PlatformManager.inits()
    public void inits() {
        createInitialPlatforms();

        // ✅ FORCER une génération immédiate pour test
        System.out.println("🔨 Génération forcée pour test...");
        Vector3f testPos = new Vector3f(8.0f, 0.0f, 0.0f);
        generator.requestPlatforms(3, testPos);

        // Traiter immédiatement (pour le test)
        try {
            Thread.sleep(100); // Laisser le temps à la génération
            processGeneratedPlatforms();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ✅ Amélioration dans PlatformManager.update()
    public void update(Vector3f playerPosition) {
        try {
            System.out.println("🎮 Player à X=" + String.format("%.2f", playerPosition.x) +
                    ", lastGeneratedX=" + String.format("%.2f", lastGeneratedX));

            if (shouldGenerateMore(playerPosition) && !isGenerating) {
                System.out.println("🔨 GÉNÉRATION DÉCLENCHÉE !");
                isGenerating = true;
                Vector3f startPos = new Vector3f(lastGeneratedX + 1.0f, -0.2f, 0.0f);
                generator.requestPlatforms(5, startPos); // ✅ Moins de plateformes mais plus souvent
            }

            processGeneratedPlatforms();
            cleanupDistantPlatforms(playerPosition);

        } catch (Exception e) {
            System.err.println("❌ Erreur critique dans PlatformManager: " + e.getMessage());
            e.printStackTrace();
            isGenerating = false;
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

    // Dans PlatformManager.createInitialPlatforms()
    private void createInitialPlatforms() {
        // ✅ Plateformes SOUS le niveau du sol (-0.4f)
        Vector3f pos1 = new Vector3f(1.5f, -0.6f, 0.0f);  // Plus basse
        Vector3f size1 = new Vector3f(1.5f, 0.3f, 0.1f);
        Model model1 = createPlatformModel(size1);
        platforms.add(new Platform(pos1, model1));

        Vector3f pos2 = new Vector3f(3.5f, -0.5f, 0.0f);  // Plus basse
        Vector3f size2 = new Vector3f(1.2f, 0.3f, 0.1f);
        Model model2 = createPlatformModel(size2);
        platforms.add(new Platform(pos2, model2));

        Vector3f pos3 = new Vector3f(6.0f, -0.7f, 0.0f);  // Plus basse
        Vector3f size3 = new Vector3f(1.0f, 0.3f, 0.1f);
        Model model3 = createPlatformModel(size3);
        platforms.add(new Platform(pos3, model3));

        lastGeneratedX = 7.0f;
        System.out.println("✅ " + platforms.size() + " plateformes initiales créées");
    }

    private Model createPlatformModel(Vector3f size) {
        float halfX = size.x / 2;
        float halfY = size.y / 2;

        float[] vertices = {
                -halfX, -halfY, -0.1f,
                halfX, -halfY, -0.1f,
                halfX,  halfY, -0.1f,
                -halfX,  halfY, -0.1f
        };

        int[] indices = {0, 1, 2, 2, 3, 0};
        float[] texCoords = {0, 0, 1, 0, 1, 1, 0, 1};

        Model model = loader.loadModel(vertices, texCoords, indices);

        // ✅ Ajouter une texture ou couleur simple
        try {
            int textureId = loader.loadTexture("src/main/resources/textures/platform.png");
            model.setTexture(new Texture(textureId));
        } catch (Exception e) {
            // Texture par défaut (couleur unie)
            int defaultTextureId = loader.createDefaultTexture();
            model.setTexture(new Texture(defaultTextureId));
        }

        return model;
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

    public void render() {
        if (platforms.isEmpty()) {
            System.err.println("❌ Aucune plateforme à rendre !");
            return;
        }

        System.out.println("🔨 Rendu de " + platforms.size() + " plateformes");

        for (Platform platform : platforms) {
            Vector3f position = platform.getPosition();

            // ✅ Vérifier si la plateforme est dans une zone visible
            if (position.x < -5.0f || position.x > 20.0f) {
                System.out.println("⚠️ Plateforme hors champ visuel: X=" + position.x);
                continue;
            }

            Model model = platform.getModel();
            if (model != null && renderer != null) {
                System.out.println("🎯 Rendu plateforme visible à: X=" +
                        String.format("%.2f", position.x) + " Y=" +
                        String.format("%.2f", position.y));
                renderer.render(model, position);
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
        boolean should = playerPos.x > lastGeneratedX - GENERATION_DISTANCE;
        System.out.println("🤔 Doit générer ? " + should +
                " (Player: " + String.format("%.2f", playerPos.x) +
                " vs Limite: " + String.format("%.2f", lastGeneratedX - GENERATION_DISTANCE) + ")");
        return should;
    }
}