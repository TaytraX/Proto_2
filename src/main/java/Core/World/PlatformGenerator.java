// src/main/java/Core/World/PlatformGenerator.java
package Core.World;

import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class PlatformGenerator {
    private final ExecutorService generatorThread;
    private final BlockingQueue<PlatformData> generatedPlatforms;
    private final Random random;

    public PlatformGenerator() {
        this.generatorThread = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "PlatformGenerator");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });
        this.generatedPlatforms = new LinkedBlockingQueue<>();
        this.random = new Random();
    }

    // ✅ Demande de génération asynchrone
    public void requestPlatforms(int count, Vector3f startPosition) {
        generatorThread.submit(() -> {
            List<PlatformData> newPlatforms = generatePlatformData(count, startPosition);
            generatedPlatforms.addAll(newPlatforms);
        });
    }

    // ✅ Récupération des plateformes générées (thread principal)
    public List<PlatformData> pollGeneratedPlatforms() {
        List<PlatformData> result = new ArrayList<>();
        PlatformData platform;
        while ((platform = generatedPlatforms.poll()) != null) {
            result.add(platform);
        }
        return result;
    }

    // ✅ Génération pure des données (pas de OpenGL)
    private List<PlatformData> generatePlatformData(int count, Vector3f startPosition) {
        List<PlatformData> platforms = new ArrayList<>();
        Vector3f currentPos = new Vector3f(startPosition);

        for (int i = 0; i < count; i++) {
            // Calculs de position (peut être complexe)
            Vector3f nextPos = calculateNextPosition(currentPos);
            Vector3f size = calculatePlatformSize();
            int type = random.nextInt(3); // Types de plateformes

            platforms.add(new PlatformData(nextPos, size, type));
            currentPos = nextPos;
        }

        return platforms;
    }

    // Dans PlatformGenerator.calculateNextPosition()
    private Vector3f calculateNextPosition(Vector3f currentPos) {
        // ✅ Espacement plus raisonnable et positions visibles
        float nextX = currentPos.x + 1.5f + random.nextFloat() * 1.0f; // Entre 1.5 et 2.5 unités
        float nextY = -0.4f + random.nextFloat() * 0.6f; // Entre -0.4 et 0.2
        return new Vector3f(nextX, nextY, 0.0f);
    }

    private Vector3f calculatePlatformSize() {
        float width = 1.0f + random.nextFloat() * 0.8f; // 1.0 à 1.8 (plus grandes)
        float height = 0.2f + random.nextFloat() * 0.1f; // 0.2 à 0.3
        return new Vector3f(width, height, 0.1f);
    }

    public void shutdown() {
        generatorThread.shutdown();
    }
}