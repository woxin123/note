/**
 *
 * @Auther mengchen
 * @Date 2019-09-19 13:25:02
 */
public class SimpleFactory {
    public Car getCar(String name) {
        if ("Bmw".equalsIgnoreCase(name)) {
            return new Bmw();
        } else if ("Benz".equalsIgnoreCase(name)) {
            return new Benz();
        } else if ("Audi".equalsIgnoreCase(name)) {
            return new Audi();
        } else {
            System.out.println("这款汽车暂时无法生产");
            return null;
        }
    }
}