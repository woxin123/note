/**
 * 双重检测 DCL
 * @Auther mengchen
 * @Date 2019-09-02 10:07:34
 */
public class Singleton {
    private volatile static Singleton instance;

    private Singleton() {

    }

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}