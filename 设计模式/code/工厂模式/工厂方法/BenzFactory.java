
public class BenzFactory implements FunFactory {
    public Car getCar() {
        return new Benz();
    }
}