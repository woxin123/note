
/**
 * testGC() 方法执行后，objA 和 objB 会不会被 GC
 */
public class ReferenceCountingGC {
    
    public Object instance = null;
    /**
     * 这个成员的唯一意义就是占用一点内存，以便能在 GC 日志中请求地看到是否被回收过
     */
    private static int _1MB = 1024 * 1024;

    public static void testGC() {
        ReferenceCountingGC objA = new ReferenceCountingGC();
        ReferenceCountingGC objB = new ReferenceCountingGC();

        objA.instance = objB;
        objB.instance = objB;

        objA = null;
        objB = null;

        // 假设在这发生 GC，objA 和 objB 是否会被收回
        System.gc();
    }
}
