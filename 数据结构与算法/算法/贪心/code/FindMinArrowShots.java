import java.util.Arrays;

public class FindMinArrowShots {
    class Pair implements Comparable {
        int begin;
        int end;

        public Pair(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public int compareTo(Object o) {
            Pair that = (Pair) o;
            return this.begin - that.begin;
        }

    }

    public int findMinArrowShots(int[][] points) {
        if (points.length == 0) {
            return 0;
        }
        Pair[] pairs = new Pair[points.length];
        for (int k = 0; k < points.length; k++) {
            pairs[k] = new Pair(points[k][0], points[k][1]);
        }
        Arrays.sort(pairs);
        int shootNum = 1;
        int shootBegin = pairs[0].begin;
        int shootEnd = pairs[0].end;
        for (int i = 1; i < pairs.length; i++) {
            if (shootEnd >= pairs[i].begin) {
                shootBegin = pairs[i].begin;
                if (shootEnd > pairs[i].end) {
                    shootEnd = pairs[i].end;
                }
            } else {
                shootNum++;
                shootBegin = pairs[i].begin;
                shootEnd = pairs[i].end;
            }
        }
        return shootNum;
    }

    public static void main(String[] args) {
        FindMinArrowShots f = new FindMinArrowShots();
        System.out.println(f.findMinArrowShots(new int[][]{
            {10, 16},
            {2, 8},
            {1, 6},
            {7, 12}
        }));
    }
}