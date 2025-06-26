package Core;

import org.joml.Vector3f;

// Classe utilitaire pour les collisions
public class CollisionUtil {

    // Vérifier collision AABB (Axis-Aligned Bounding Box)
    public static boolean checkAABB(Vector3f pos1, Vector3f size1,
                                    Vector3f pos2, Vector3f size2) {
        return pos1.x < pos2.x + size2.x/2 &&
                pos1.x + size1.x/2 > pos2.x &&
                pos1.y < pos2.y + size2.y/2 &&
                pos1.y + size1.y/2 > pos2.y;
    }

    // Calculer la distance entre deux objets
    public static float getDistance(Vector3f pos1, Vector3f pos2) {
        return pos1.distance(pos2);
    }

    // Vérifier si un point est dans un rectangle
    public static boolean isPointInRect(float x, float y,
                                        Vector3f rectPos, Vector3f rectSize) {
        return x >= rectPos.x - rectSize.x/2 &&
                x <= rectPos.x + rectSize.x/2 &&
                y >= rectPos.y - rectSize.y/2 &&
                y <= rectPos.y + rectSize.y/2;
    }

    // Calculer la résolution de collision (pour pousser les objets)
    public static Vector3f resolveCollision(Vector3f pos1, Vector3f size1,
                                            Vector3f pos2, Vector3f size2) {
        float overlapX = (size1.x + size2.x) / 2 - Math.abs(pos1.x - pos2.x);
        float overlapY = (size1.y + size2.y) / 2 - Math.abs(pos1.y - pos2.y);

        Vector3f resolution = new Vector3f();

        if (overlapX < overlapY) {
            resolution.x = overlapX * (pos1.x < pos2.x ? -1 : 1);
        } else {
            resolution.y = overlapY * (pos1.y < pos2.y ? -1 : 1);
        }

        return resolution;
    }
}