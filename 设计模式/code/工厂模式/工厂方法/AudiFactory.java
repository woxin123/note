/**
 *
 * @Auther mengchen
 * @Date 2019-09-19 15:12:49
 */
public class AudiFactory implements FunFactory {
    public Car getCar() {
        return new Audi();
    }
}