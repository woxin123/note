import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeakReferenceExample {
    public static void main (String[] args) {
       List<Integer> list = new ArrayList<>();
       WeakReference<List<Integer>> weakReference = new WeakReference<>(list);
       for (int i = 0; i < 10; i++) {
           list.add(i);
       }
       System.out.println(weakReference.get());
       list = null;
       while (true) {
           if (weakReference.get() != null) {
               System.gc();
           } else {
               break;
           }
       }
       System.out.println("弱引用已经被回收");
    }
}
