/*
 * Decompiled with CFR 0.150.
 */
package gtanks;

import gtanks.Rank;

public class RankUtils {
    private static Rank[] ranks;
    public static int[] rankupRewards = { 0, 10, 40, 120, 230, 420, 740, 950, 1400, 2000, 2500, 3100, 3900, 4600, 5600,
            6600, 7900, 8900, 10000, 12000, 14000, 16000, 17000, 20000, 22000, 24000, 28000, 31000, 34000, 37000 };

    public static void init() {
        ranks = new Rank[30];
        RankUtils.ranks[0] = new Rank(0, 99, "\u041d\u043e\u0432\u043e\u0431\u0440\u0430\u043d\u0435\u0446");
        RankUtils.ranks[1] = new Rank(100, 499, "\u0420\u044f\u0434\u043e\u0432\u043e\u0439");
        RankUtils.ranks[2] = new Rank(500, 1499, "\u0415\u0444\u0440\u0435\u0439\u0442\u043e\u0440");
        RankUtils.ranks[3] = new Rank(1500, 3699, "\u041a\u0430\u043f\u0440\u0430\u043b");
        RankUtils.ranks[4] = new Rank(3700, 7099,
                "\u041c\u0430\u0441\u0442\u0435\u0440-\u043a\u0430\u043f\u0440\u0430\u043b");
        RankUtils.ranks[5] = new Rank(7100, 12299, "\u0421\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.ranks[6] = new Rank(12300, 19999,
                "\u0428\u0442\u0430\u0431-\u0441\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.ranks[7] = new Rank(20000, 28999,
                "\u041c\u0430\u0441\u0442\u0435\u0440-\u0441\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.ranks[8] = new Rank(29000, 40999,
                "\u041f\u0435\u0440\u0432\u044b\u0439 \u0441\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.ranks[9] = new Rank(41000, 56999,
                "\u0421\u0435\u0440\u0436\u0430\u043d\u0442-\u043c\u0430\u0439\u043e\u0440");
        RankUtils.ranks[10] = new Rank(57000, 75999,
                "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 1");
        RankUtils.ranks[11] = new Rank(76000, 97999,
                "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 2");
        RankUtils.ranks[12] = new Rank(98000, 124999,
                "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 3");
        RankUtils.ranks[13] = new Rank(125000, 155999,
                "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 4");
        RankUtils.ranks[14] = new Rank(156000, 191999,
                "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 5");
        RankUtils.ranks[15] = new Rank(192000, 232999,
                "\u041c\u043b\u0430\u0434\u0448\u044b\u0439 \u043b\u0435\u0439\u0442\u0435\u043d\u0430\u043d\u0442");
        RankUtils.ranks[16] = new Rank(233000, 279999, "\u041b\u0435\u0439\u0442\u0435\u043d\u0430\u043d\u0442");
        RankUtils.ranks[17] = new Rank(280000, 331999,
                "\u0421\u0442\u0430\u0440\u0448\u0438\u0439 \u043b\u0435\u0439\u0442\u0435\u043d\u0430\u043d\u0442");
        RankUtils.ranks[18] = new Rank(332000, 389999, "\u041a\u0430\u043f\u0438\u0442\u0430\u043d");
        RankUtils.ranks[19] = new Rank(390000, 454999, "\u041c\u0430\u0439\u043e\u0440");
        RankUtils.ranks[20] = new Rank(455000, 526999,
                "\u041f\u043e\u0434\u043f\u043e\u043b\u043a\u043e\u0432\u043d\u0438\u043a");
        RankUtils.ranks[21] = new Rank(527000, 605999, "\u041f\u043e\u043b\u043a\u043e\u0432\u043d\u0438\u043a");
        RankUtils.ranks[22] = new Rank(606000, 691999, "\u0411\u0440\u0438\u0433\u0430\u0434\u0438\u0440");
        RankUtils.ranks[23] = new Rank(692000, 786999,
                "\u0413\u0435\u043d\u0435\u0440\u0430\u043b-\u043c\u0430\u0439\u043e\u0440");
        RankUtils.ranks[24] = new Rank(787000, 888999,
                "\u0413\u0435\u043d\u0435\u0440\u0430\u043b-\u043b\u0435\u0439\u043d\u0435\u0442\u0430\u043d\u0442");
        RankUtils.ranks[25] = new Rank(889000, 999999, "\u0413\u0435\u043d\u0435\u0440\u0430\u043b");
        RankUtils.ranks[26] = new Rank(1000000, 1121999, "\u041c\u0430\u0440\u0448\u0430\u043b");
        RankUtils.ranks[27] = new Rank(1122000, 1254999, "Field Marshal");
        RankUtils.ranks[28] = new Rank(1255000, 1399999, "Commander");
        RankUtils.ranks[29] = new Rank(1400000, 0, "Generalissimo");
    }

    public static int getUpdateNumber(int score) {
        int rangId;
        Rank temp = RankUtils.getRankByScore(score);
        int rang = rangId = RankUtils.getNumberRank(temp);
        int result = 0;
        try {
            result = (int) ((double) (score - RankUtils.ranks[rang - 1].max) * 1.0
                    / (double) (temp.max - RankUtils.ranks[rang - 1].max) * 10000.0);
        } catch (Exception e) {
            result = (int) ((double) (score - 0) * 1.0 / (double) (temp.max - 0) * 10000.0);
        }
        if (score > RankUtils.ranks[RankUtils.ranks.length - 1].min - 1) {
            result = 10000;
        } else if (score < 0) {
            result = 0;
        }
        return result;
    }

    public static int getNumberRank(Rank rank) {
        for (int i = 0; i < ranks.length; ++i) {
            if (ranks[i] != rank)
                continue;
            return i;
        }
        return -1;
    }

    public static Rank getRankByScore(int score) {
        Rank temp = ranks[0];
        if (score >= RankUtils.ranks[29].max) {
            temp = ranks[29];
        }
        Rank[] arrrank = ranks;
        int n = ranks.length;
        for (int i = 0; i < n; ++i) {
            Rank rank = arrrank[i];
            if (score < rank.min || score > rank.max)
                continue;
            temp = rank;
        }
        return temp;
    }

    public static Rank getRankByIndex(int index) {
        return ranks[index];
    }

    public static int stringToInt(String src) {
        try {
            int tempelate = Integer.parseInt(src);
            if (tempelate <= 0) {
                tempelate = 5000000;
            }
            return tempelate >= RankUtils.ranks[29].min ? RankUtils.ranks[29].min : tempelate;
        } catch (Exception ex) {
            return 50000000;
        }
    }
}
