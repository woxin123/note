import java.util.*;

public class RemoveKdigits {
    public String removeKdigits(String num, int k) {
        LinkedList<Integer> stack = new LinkedList<>();
        for (int i = 0; i < num.length(); i++) {
            int number = num.charAt(i) - '0';
            while (!stack.isEmpty() && k > 0 && stack.getLast() > number) {
                stack.removeLast();
                k--;
            }
            if (!(stack.isEmpty() && number == 0)) {
                stack.addLast(number);
            }
        }
        while (stack.size() != 0 && k > 0) {
            stack.removeLast();
            k--;
        }
        StringBuilder sb = new StringBuilder();
        for (Integer v : stack) {
            sb.append(v);
        }
        if (sb.length() == 0) {
            return "0";
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        RemoveKdigits r = new RemoveKdigits();
        String res = r.removeKdigits("112", 1);
        System.out.println(res);
    }
}