import java.util.HashMap;
import java.util.Map;

/**
 *
 * @Auther mengchen
 * @Date 2019-09-02 10:24:42
 */
public class SingletonManager {
    private static Map<String, Object> objMap = new HashMap<>();

    private SingletonManager() {

    }

    public static void regsiterService(String key, Object instance) {
        if (!objMap.containsKey(key)) {
            objMap.put(key, instance);
        }
    }

    public static Object service(String key) {
        return objMap.get(key);
    }
}