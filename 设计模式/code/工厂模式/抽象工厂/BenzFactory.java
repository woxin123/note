/**
 *
 * @Auther mengchen
 * @Date 2019-09-19 16:38:53
 */
public class BenzFactory extends AbstractFactory {
    public Car getCar(String name) {
        return new Benz();
    }
}