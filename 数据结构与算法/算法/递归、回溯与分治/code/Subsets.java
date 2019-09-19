import java.util.ArrayList;
import java.util.List;

/**
 *
 * @Auther mengchen
 * @Date 2019-09-18 14:17:15
 */
public class Subsets {
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        help(nums, 0, new ArrayList<>(), res);
        return res;
    }

    public void help(int[] nums, int index, List<Integer> list, List<List<Integer>> res) {
        res.add(new ArrayList<>(list));
        for (int i = index; i < nums.length; i++) {
            list.add(nums[i]);
            help(nums, i + 1, list, res);
            list.remove(Integer.valueOf(nums[i]));
        }
    }

    public static void main(String[] args) {
        System.out.println(new Subsets().subsets(new int[]{1,2,3}));
    }
}