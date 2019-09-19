/**
 *
 * @Auther mengchen
 * @Date 2019-09-19 16:47:46
 */
public class AudiFactory extends AbstractFactory {
    public Car getCar() {
        return new Audi();
    }
}