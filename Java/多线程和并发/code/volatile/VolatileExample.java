/**
 *
 * @Auther mengchen
 * @Date 2019-09-20 18:01:17
 */
public class VolatileExample {

    private boolean stop = false;

    public boolean getState() {
        return stop;
    }

    public void setState(boolean state) {
        this.stop = state;
    }

    class Thread1 extends Thread {
        @Override
        public void run() {
            while ()
        }
    }
    public static void main(String[] args) {
        
    }
}