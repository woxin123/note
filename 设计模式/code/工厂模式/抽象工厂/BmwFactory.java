/**
 *
 * @Auther mengchen
 * @Date 2019-09-19 16:42:58
 */
public class BmwFactory extends AbstractFactory {
    public Car getCar() {
        return new Bmw();
    }
}