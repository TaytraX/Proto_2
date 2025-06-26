package Core.World;

import org.joml.Vector3f;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// Éviter les créations/destructions fréquentes
public class Vector3fPool {
    private final Queue<Vector3f> pool = new ConcurrentLinkedQueue<>();

    public Vector3f acquire() {
        Vector3f vec = pool.poll();
        return vec != null ? vec.zero() : new Vector3f();
    }

    public void release(Vector3f vec) {
        pool.offer(vec);
    }
}