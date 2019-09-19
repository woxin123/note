/**
 *
 * @Auther mengchen
 * @Date 2019-09-19 15:10:23
 */
public class BmwFactory implements FunFactory {
    public Car getCar() {
        return new Bmw();
    }
}