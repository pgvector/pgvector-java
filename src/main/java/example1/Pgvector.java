package example1;

import java.util.Arrays;

class Pgvector {
    public static String toString(float[] v) {
        return Arrays.toString(v).replace(" ", "");
    }

    public static float[] parse(String v) {
        String[] s = v.substring(1, v.length() - 1).split(",");
        float[] f = new float[s.length];
        for (int i = 0; i < s.length; i++) {
            f[i] = Float.parseFloat(s[i]);
        }
        return f;
    }
}
