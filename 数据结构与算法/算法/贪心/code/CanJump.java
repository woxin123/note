
public class CanJump {
    public boolean canJump(int[] nums) {
        int[] index = new int[nums.length];
        for (int i = 0; i < nums.length; i++) {
           index[i] = i + nums[i]; 
        }
        int jump = 0;
        int maxIndex = index[0];
        while (jump <= maxIndex && jump < nums.length) {
            if (maxIndex < index[jump]) {
                maxIndex = index[jump];
            }
            jump++;
        }
        if (jump == nums.length) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        CanJump c = new CanJump();
        System.out.println(c.canJump(new int[]{2, 3, 1, 1, 4}));
    }
}