import java.util.Arrays;

public class FindContentChildren {
    public int findContentChildren(int[] g, int[] s) {
        Arrays.sort(g);
        Arrays.sort(s);
        int child = 0;
        int cookies = 0;
        while (child < g.length && cookies < s.length) {
            if (g[child] <= s[cookies]) {
                child++;
            }
            cookies++;
        }
        return child;
    }

    public static void main(String[] args) {
        FindContentChildren f = new FindContentChildren();
        int children = f.findContentChildren(new int[]{1,2,3}, new int[]{1, 1});
        System.out.println(children);
    }

}