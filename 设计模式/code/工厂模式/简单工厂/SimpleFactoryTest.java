/**
 *
 * @Auther mengchen
 * @Date 2019-09-19 13:39:03
 */
public class SimpleFactoryTest {
    public static void main(String[] args) {
        // 实现统一管理、专业化管理
        Car car = new SimpleFactory().getCar("Bmw");
        System.out.println("该工厂生产了：" + car.getName());
    }
}