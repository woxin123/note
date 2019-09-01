
public class Jump {

    public int jump(int[] nums) {
        if (nums.length == 1) {
            return 0;
        }
        int currentmaxindex = nums[0];
        int premaxmaxindex = nums[0];
        int jumpmin = 1;
        for (int i = 1; i < nums.length; i++) {
            if (currentmaxindex < i) {
                currentmaxindex = premaxmaxindex;
                jumpmin++;
            }
            if (premaxmaxindex < nums[i] + i) {
                premaxmaxindex = nums[i] + i;
            }
        }
        return jumpmin;
    }

    public static void main(String[] args) {
        Jump jump = new Jump();
        System.out.println(jump.jump(new int[] { 2, 1 }));
    }
}