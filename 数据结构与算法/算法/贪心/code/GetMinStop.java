import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;


class Pair {
    int distance;
    int gas;

    public Pair(int distance, int gas) {
        this.distance = distance;
        this.gas = gas;
    }

    @Override
    public String toString() {
        return "Pair [distance=" + distance + ", gas=" + gas + "]";
    }
}

public class GetMinStop {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        ArrayList<Pair> stop = new ArrayList<>();
        while (n-- > 0) {
            int distance = sc.nextInt();
            int gas = sc.nextInt();
            Pair p = new Pair(distance, gas);
            stop.add(p);
        }
        int length = sc.nextInt(), p = sc.nextInt();
        System.out.println(new GetMinStop().getMinStop(length, p, stop));
    }

    public int getMinStop(int length, int p, ArrayList<Pair> stop) {
        PriorityQueue<Integer> queue = new PriorityQueue<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return b - a;
            }
        });
        int result = 0;
        stop.add(new Pair(0, 0));
        stop.sort(new Comparator<Pair>() {
            @Override
            public int compare(Pair a, Pair b) {
                return b.distance - a.distance;
            }
        });
        for (int i = 0; i < stop.size(); i++) {
            int dis = length - stop.get(i).distance;
            while (dis > p && !queue.isEmpty()) {
                p += queue.poll();
                result++;
            }
            if (queue.isEmpty() && p < dis) {
                return -1;
            }
            p = p - dis;
            length = stop.get(i).distance;
            queue.add(stop.get(i).gas);
        }
        return result;
    }
}