/**
 * 懒汉模式
 * @Auther mengchen
 * @Date 2019-09-02 09:34:06
 */
public class Singleton {
    private static Singleton instance;

    private Singleton() {

    }

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}