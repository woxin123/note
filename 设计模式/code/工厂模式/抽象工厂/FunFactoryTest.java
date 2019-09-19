/**
 *
 * @Auther mengchen
 * @Date 2019-09-19 15:20:28
 */
public class FunFactoryTest {
    public static void main(String[] args) {
        FunFactory factory = new AudiFactory();
        System.out.println(factory.getCar().getName());
        factory = new BmwFactory();
        System.out.println(factory.getCar().getName());
        factory = new BenzFactory();
        System.out.println(factory.getCar().getName());
    }
}