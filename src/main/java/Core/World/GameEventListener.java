package Core.World;

import org.joml.Vector3f;

public interface GameEventListener {
    void onPlayerLanded(Vector3f position);
    void onPlatformGenerated(int count);
}