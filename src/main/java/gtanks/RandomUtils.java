/*
 * Decompiled with CFR 0.150.
 */
package gtanks;

public class RandomUtils {
    public static float getRandom(float min, float max) {
        if (min == max) {
            return min;
        }
        return (float)((double)min + Math.random() * (double)(max - min + 1.0f));
    }
}

