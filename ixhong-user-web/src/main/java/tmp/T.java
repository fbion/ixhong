package tmp;

import java.math.BigDecimal;

/**
 * Created by shenhongxi on 2015/11/6.
 */
public class T {

    public static void main(String[] args) throws Exception {
        System.out.println(round(3.2099999D));
    }

    private static double round(double d) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
