
public class Money{

    final static int[] RMB = new int[]{200, 100, 20,10, 5, 1};

    public static void main(String[] args) {
        int x = 1024;
        int count = 0;
        for (int i = 0; i < RMB.length; i++) {
            int use = x / RMB[i];
            count += use;
            if (use == 0) {
                continue;
            }
            x = x % RMB[i];
            System.out.printf("需要使用面额为 %d 的钞票 %d 张，", RMB[i], use);
            System.out.printf("剩余需要支持的金额：%d 。\n", x);
            if (x == 0) {
                break;
            }
        }
        System.out.printf("总共需要支付 %d 张。\n", count);
    }
}