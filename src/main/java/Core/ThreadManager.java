package Core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Gestionnaire de threads pour séparer la logique de jeu du rendu
 * IMPORTANT: Seul le thread principal peut faire du rendu OpenGL !
 */
public class ThreadManager {

    // Thread pool pour les tâches de logique
    private final ExecutorService logicExecutor;
    private final ExecutorService backgroundExecutor;

    // Verrous pour synchroniser l'accès aux données partagées
    private final ReentrantLock playerLock = new ReentrantLock();
    private final ReentrantLock backgroundLock = new ReentrantLock();

    // Flags pour contrôler l'exécution
    private volatile boolean running = true;

    public ThreadManager() {
        // 1 thread pour la logique du joueur, 1 pour le background
        logicExecutor = Executors.newFixedThreadPool(2);
        backgroundExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Exécute la logique du joueur en parallèle
     */
    public Future<?> updatePlayerLogic(Runnable playerUpdateTask) {
        return logicExecutor.submit(() -> {
            if (running) {
                try {
                    playerLock.lock();
                    playerUpdateTask.run();
                } finally {
                    playerLock.unlock();
                }
            }
        });
    }

    /**
     * Exécute la logique du background en parallèle
     */
    public Future<?> updateBackgroundLogic(Runnable backgroundUpdateTask) {
        return backgroundExecutor.submit(() -> {
            if (running) {
                try {
                    backgroundLock.lock();
                    backgroundUpdateTask.run();
                } finally {
                    backgroundLock.unlock();
                }
            }
        });
    }

    /**
     * Permet au thread de rendu d'accéder aux données du joueur de façon sécurisée
     */
    public void withPlayerLock(Runnable renderTask) {
        try {
            playerLock.lock();
            renderTask.run();
        } finally {
            playerLock.unlock();
        }
    }

    /**
     * Permet au thread de rendu d'accéder aux données du background de façon sécurisée
     */
    public void withBackgroundLock(Runnable renderTask) {
        try {
            backgroundLock.lock();
            renderTask.run();
        } finally {
            backgroundLock.unlock();
        }
    }

    /**
     * Arrête tous les threads proprement
     */
    public void shutdown() {
        running = false;

        logicExecutor.shutdown();
        backgroundExecutor.shutdown();

        try {
            if (!logicExecutor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS)) {
                logicExecutor.shutdownNow();
            }
            if (!backgroundExecutor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS)) {
                backgroundExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logicExecutor.shutdownNow();
            backgroundExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean isRunning() {
        return running;
    }

    // Getters pour les verrous si nécessaire
    public ReentrantLock getPlayerLock() { return playerLock; }
    public ReentrantLock getBackgroundLock() { return backgroundLock; }
}