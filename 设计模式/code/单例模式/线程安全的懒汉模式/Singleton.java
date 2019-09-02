/**
 *
 * @Auther mengchen
 * @Date 2019-09-02 09:59:49
 */
public class Singleton {
    private static Singleton instance;

    private Singleton() {

    }

    public synchronized static Singleton getInstace() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}