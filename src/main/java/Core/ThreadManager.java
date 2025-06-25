package Core;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ✅ Gestionnaire de threads amélioré pour séparer la logique de jeu du rendu
 */
public class ThreadManager {
    // ✅ Un seul thread pour la logique de jeu
    private final ExecutorService gameLogicExecutor;
    private final ExecutorService backgroundExecutor;
    private final ExecutorService platformExecutor;

    // Verrous pour synchroniser l'accès aux données partagées
    private final ReentrantLock playerLock = new ReentrantLock();
    private final ReentrantLock backgroundLock = new ReentrantLock();
    private final ReentrantLock platformLock = new ReentrantLock();

    // ✅ Timeout pour éviter les blocages
    private static final long TASK_TIMEOUT_MS = 40; // ~60 FPS

    private volatile boolean running = true;

    public ThreadManager() {

        gameLogicExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "GameLogic-Thread");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY + 1); // Priorité légèrement plus élevée
            return t;
        });

        backgroundExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Background-Thread");
            t.setDaemon(true);
            return t;
        });

        platformExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Platform-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    public Future<?> updateAllLogic(Runnable playerTask, Runnable backgroundTask, Runnable platformTask) {
        return gameLogicExecutor.submit(() -> {
            if (!running) return;

            try {
                // Exécuter les deux tâches séquentiellement dans le même thread
                if (playerLock.tryLock(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    try {
                        playerTask.run();
                    } finally {
                        playerLock.unlock();
                    }
                }

                if (platformLock.tryLock(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    try {
                        platformTask.run();
                    } finally {
                        platformLock.unlock();
                    }
                }

                if (backgroundLock.tryLock(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    try {
                        backgroundTask.run();
                    } finally {
                        backgroundLock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }


    /**
     * ✅ Accès sécurisé aux données du joueur pour le rendu
     */
    public void withPlayerLock(Runnable renderTask) {
        boolean lockAcquired = false;
        try {
            lockAcquired = playerLock.tryLock(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (lockAcquired) {
                renderTask.run();
            } else {
                System.out.println("⚠️ Timeout sur playerLock (rendu)");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("❌ Erreur dans withPlayerLock: " + e.getMessage());
        } finally {
            if (lockAcquired) {
                playerLock.unlock();
            }
        }
    }

    /**
     * ✅ Accès sécurisé aux données du background pour le rendu
     */
    public void withBackgroundLock(Runnable renderTask) {
        boolean lockAcquired = false;
        try {
            lockAcquired = backgroundLock.tryLock(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (lockAcquired) {
                renderTask.run();
            } else {
                System.out.println("⚠️ Timeout sur backgroundLock (rendu)");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("❌ Erreur dans withBackgroundLock: " + e.getMessage());
        } finally {
            if (lockAcquired) {
                backgroundLock.unlock();
            }
        }
    }

    /**
     * ✅ Arrêt propre avec timeout
     */
    public void shutdown() {
        running = false;

        System.out.println("🔄 Arrêt des threads...");

        gameLogicExecutor.shutdown();
        backgroundExecutor.shutdown();

        try {
            if (!gameLogicExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                System.out.println("⚠️ Arrêt forcé du logicExecutor");
                gameLogicExecutor.shutdownNow();
            }
            if (!backgroundExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                System.out.println("⚠️ Arrêt forcé du backgroundExecutor");
                backgroundExecutor.shutdownNow();
            }
            System.out.println("✅ Tous les threads arrêtés");
        } catch (InterruptedException e) {
            System.err.println("❌ Interruption pendant l'arrêt");
            gameLogicExecutor.shutdownNow();
            backgroundExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ✅ Méthodes de diagnostic
    public boolean isRunning() {
        return running;
    }

    public boolean isPlayerLockHeld() {
        return playerLock.isLocked();
    }

    public boolean isBackgroundLockHeld() {
        return backgroundLock.isLocked();
    }

    // Getters pour les verrous (si nécessaire pour debug)
    public ReentrantLock getPlayerLock() { return playerLock; }
    public ReentrantLock getBackgroundLock() { return backgroundLock; }
}