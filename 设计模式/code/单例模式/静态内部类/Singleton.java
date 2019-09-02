/**
 *
 * @Auther mengchen
 * @Date 2019-09-02 10:16:31
 */
public class Singleton {

    private Singleton() {
    }

    public static Singleton getInstance() {
        return SingletonHolder.singleton;
    }

    private static class SingletonHolder {
        private static final Singleton singleton = new Singleton();
    }
}