package Core.Utils;

import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Utils {

    public static FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip();
        return buffer;
    }

    public static IntBuffer storeDataInIntBuffer(int[] data) {
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        buffer.put(data).flip();
        return buffer;
    }

    public static String loadRessource(String path) throws Exception {
        try (InputStream in = Utils.class.getResourceAsStream(path);
             Scanner scanner = new Scanner(in, "UTF-8")) {
            return scanner.useDelimiter("\\A").next();
        } catch (Exception e) {
            throw new Exception("Erreur lors du chargement de la ressource: " + path, e);
        }
    }

}