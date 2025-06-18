package Core;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ✅ Gestionnaire de threads amélioré pour séparer la logique de jeu du rendu
 */
public class ThreadManager {

    // ✅ Thread pools avec noms pour le debug
    private final ExecutorService logicExecutor;
    private final ExecutorService backgroundExecutor;

    // Verrous pour synchroniser l'accès aux données partagées
    private final ReentrantLock playerLock = new ReentrantLock();
    private final ReentrantLock backgroundLock = new ReentrantLock();

    // ✅ Timeout pour éviter les blocages
    private static final long TASK_TIMEOUT_MS = 16; // ~60 FPS

    private volatile boolean running = true;

    public ThreadManager() {
        // ✅ Threads avec noms pour le debug
        logicExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "GameLogic-Thread");
            t.setDaemon(true); // ✅ Daemon threads pour fermeture propre
            return t;
        });

        backgroundExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Background-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * ✅ Exécute la logique du joueur avec timeout
     */
    public Future<?> updatePlayerLogic(Runnable playerUpdateTask) {
        return logicExecutor.submit(() -> {
            if (!running) return;

            boolean lockAcquired = false;
            try {
                // ✅ Tentative d'acquisition avec timeout
                lockAcquired = playerLock.tryLock(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (lockAcquired) {
                    playerUpdateTask.run();
                } else {
                    System.out.println("⚠️ Timeout sur playerLock");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ Thread joueur interrompu");
            } catch (Exception e) {
                System.err.println("❌ Erreur dans updatePlayerLogic: " + e.getMessage());
            } finally {
                if (lockAcquired) {
                    playerLock.unlock();
                }
            }
        });
    }

    /**
     * ✅ Exécute la logique du background avec timeout
     */
    public Future<?> updateBackgroundLogic(Runnable backgroundUpdateTask) {
        return backgroundExecutor.submit(() -> {
            if (!running) return;

            boolean lockAcquired = false;
            try {
                lockAcquired = backgroundLock.tryLock(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (lockAcquired) {
                    backgroundUpdateTask.run();
                } else {
                    System.out.println("⚠️ Timeout sur backgroundLock");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ Thread background interrompu");
            } catch (Exception e) {
                System.err.println("❌ Erreur dans updateBackgroundLogic: " + e.getMessage());
            } finally {
                if (lockAcquired) {
                    backgroundLock.unlock();
                }
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

        logicExecutor.shutdown();
        backgroundExecutor.shutdown();

        try {
            if (!logicExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                System.out.println("⚠️ Arrêt forcé du logicExecutor");
                logicExecutor.shutdownNow();
            }
            if (!backgroundExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                System.out.println("⚠️ Arrêt forcé du backgroundExecutor");
                backgroundExecutor.shutdownNow();
            }
            System.out.println("✅ Tous les threads arrêtés");
        } catch (InterruptedException e) {
            System.err.println("❌ Interruption pendant l'arrêt");
            logicExecutor.shutdownNow();
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