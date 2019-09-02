/**
 * 恶汉模式
 * @Auther mengchen
 * @Date 2019-09-02 09:20:19
 */
public class Singleton {
    private static Singleton instance = new Singleton();

    // 构造器私有
    private Singleton() {

    }

    public static Singleton getInstance() {
        return instance;
    }
}