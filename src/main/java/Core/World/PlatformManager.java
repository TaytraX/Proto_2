package Core.World;

import Core.Entities.Model;
import Core.Entities.Platform;
import Core.Ilogic;
import Core.ObjectLoader;
import org.joml.Vector3f;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlatformManager implements Ilogic {

    private final CopyOnWriteArrayList<Platform> platforms;
    private final PlatformGenerator generator = null;
    private final ObjectLoader loader;

    private float lastGeneratedX = 0.0f;
    private volatile boolean isGenerating = false;

    public void update(Vector3f playerPosition) {
        // ✅ Demander génération si nécessaire
        if (shouldGenerateMore(playerPosition) && !isGenerating) {
            isGenerating = true;
            Vector3f startPos = new Vector3f(lastGeneratedX + 1.0f, -0.2f, 0.0f);
            generator.requestPlatforms(8, startPos);
        }

        // ✅ Traiter les plateformes générées (création modèles OpenGL)
        processGeneratedPlatforms();

        // Cleanup habituel...
        cleanupDistantPlatforms(playerPosition);
    }

    private void processGeneratedPlatforms() {
        List<PlatformData> newPlatformData = generator.pollGeneratedPlatforms();

        if (!newPlatformData.isEmpty()) {
            // ✅ Créer les modèles OpenGL sur le thread principal
            for (PlatformData data : newPlatformData) {
                Model model = createPlatformModel(data.getSize());
                Platform platform = new Platform(data.getPosition(), data.getSize(), model, data.getType());
                platforms.add(platform);

                lastGeneratedX = Math.max(lastGeneratedX, data.getPosition().x);
            }

            isGenerating = false;
            System.out.println("🔨 " + newPlatformData.size() + " plateformes ajoutées");
        }
    }

    private boolean shouldGenerateMore(Vector3f playerPos) {
        return playerPos.x > lastGeneratedX - GENERATION_DISTANCE;
    }
}