package Core;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ✅ Gestionnaire de threads amélioré pour séparer la logique de jeu du rendu
 */
public class ThreadManager {

    // Verrous pour synchroniser l'accès aux données partagées
    private final ReentrantLock playerLock = new ReentrantLock();
    private final ReentrantLock backgroundLock = new ReentrantLock();

    // ✅ Timeout pour éviter les blocages
    private static final long TASK_TIMEOUT_MS = 40; // ~60 FPS

    private volatile boolean running = true;

    private final ExecutorService gameExecutor;
    private final ReentrantReadWriteLock dataLock = new ReentrantReadWriteLock();

    public ThreadManager() {
        gameExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "GameLogic");
            t.setDaemon(true);
            return t;
        });
    }

    // ✅ Un seul verrou en lecture/écriture au lieu de multiples verrous
    public void withReadLock(Runnable task) {
        dataLock.readLock().lock();
        try {
            task.run();
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public void withWriteLock(Runnable task) {
        dataLock.writeLock().lock();
        try {
            task.run();
        } finally {
            dataLock.writeLock().unlock();
        }
    }
}