/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.utils;

public class RankUtils {
    private static RankEntity[] rankEntities;
    public static int[] rankupRewards = { 0, 10, 40, 120, 230, 420, 740, 950, 1400, 2000, 2500, 3100, 3900, 4600, 5600,
            6600, 7900, 8900, 10000, 12000, 14000, 16000, 17000, 20000, 22000, 24000, 28000, 31000, 34000, 37000 };

    public static void init() {
        rankEntities = new RankEntity[30];
        RankUtils.rankEntities[0] = new RankEntity(0, 99, "\u041d\u043e\u0432\u043e\u0431\u0440\u0430\u043d\u0435\u0446");
        RankUtils.rankEntities[1] = new RankEntity(100, 499, "\u0420\u044f\u0434\u043e\u0432\u043e\u0439");
        RankUtils.rankEntities[2] = new RankEntity(500, 1499, "\u0415\u0444\u0440\u0435\u0439\u0442\u043e\u0440");
        RankUtils.rankEntities[3] = new RankEntity(1500, 3699, "\u041a\u0430\u043f\u0440\u0430\u043b");
        RankUtils.rankEntities[4] = new RankEntity(3700, 7099,
                "\u041c\u0430\u0441\u0442\u0435\u0440-\u043a\u0430\u043f\u0440\u0430\u043b");
        RankUtils.rankEntities[5] = new RankEntity(7100, 12299, "\u0421\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.rankEntities[6] = new RankEntity(12300, 19999,
                "\u0428\u0442\u0430\u0431-\u0441\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.rankEntities[7] = new RankEntity(20000, 28999,
                "\u041c\u0430\u0441\u0442\u0435\u0440-\u0441\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.rankEntities[8] = new RankEntity(29000, 40999,
                "\u041f\u0435\u0440\u0432\u044b\u0439 \u0441\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.rankEntities[9] = new RankEntity(41000, 56999,
                "\u0421\u0435\u0440\u0436\u0430\u043d\u0442-\u043c\u0430\u0439\u043e\u0440");
        RankUtils.rankEntities[10] = new RankEntity(57000, 75999,
                "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 1");
        RankUtils.rankEntities[11] = new RankEntity(76000, 97999,
                "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 2");
        RankUtils.rankEntities[12] = new RankEntity(98000, 124999,
                "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 3");
        RankUtils.rankEntities[13] = new RankEntity(125000, 155999,
                "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 4");
        RankUtils.rankEntities[14] = new RankEntity(156000, 191999,
                "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 5");
        RankUtils.rankEntities[15] = new RankEntity(192000, 232999,
                "\u041c\u043b\u0430\u0434\u0448\u044b\u0439 \u043b\u0435\u0439\u0442\u0435\u043d\u0430\u043d\u0442");
        RankUtils.rankEntities[16] = new RankEntity(233000, 279999, "\u041b\u0435\u0439\u0442\u0435\u043d\u0430\u043d\u0442");
        RankUtils.rankEntities[17] = new RankEntity(280000, 331999,
                "\u0421\u0442\u0430\u0440\u0448\u0438\u0439 \u043b\u0435\u0439\u0442\u0435\u043d\u0430\u043d\u0442");
        RankUtils.rankEntities[18] = new RankEntity(332000, 389999, "\u041a\u0430\u043f\u0438\u0442\u0430\u043d");
        RankUtils.rankEntities[19] = new RankEntity(390000, 454999, "\u041c\u0430\u0439\u043e\u0440");
        RankUtils.rankEntities[20] = new RankEntity(455000, 526999,
                "\u041f\u043e\u0434\u043f\u043e\u043b\u043a\u043e\u0432\u043d\u0438\u043a");
        RankUtils.rankEntities[21] = new RankEntity(527000, 605999, "\u041f\u043e\u043b\u043a\u043e\u0432\u043d\u0438\u043a");
        RankUtils.rankEntities[22] = new RankEntity(606000, 691999, "\u0411\u0440\u0438\u0433\u0430\u0434\u0438\u0440");
        RankUtils.rankEntities[23] = new RankEntity(692000, 786999,
                "\u0413\u0435\u043d\u0435\u0440\u0430\u043b-\u043c\u0430\u0439\u043e\u0440");
        RankUtils.rankEntities[24] = new RankEntity(787000, 888999,
                "\u0413\u0435\u043d\u0435\u0440\u0430\u043b-\u043b\u0435\u0439\u043d\u0435\u0442\u0430\u043d\u0442");
        RankUtils.rankEntities[25] = new RankEntity(889000, 999999, "\u0413\u0435\u043d\u0435\u0440\u0430\u043b");
        RankUtils.rankEntities[26] = new RankEntity(1000000, 1121999, "\u041c\u0430\u0440\u0448\u0430\u043b");
        RankUtils.rankEntities[27] = new RankEntity(1122000, 1254999, "Field Marshal");
        RankUtils.rankEntities[28] = new RankEntity(1255000, 1399999, "Commander");
        RankUtils.rankEntities[29] = new RankEntity(1400000, 0, "Generalissimo");
    }

    public static int getUpdateNumber(int score) {
        int rangId;
        RankEntity temp = RankUtils.getRankByScore(score);
        int rang = rangId = RankUtils.getNumberRank(temp);
        int result = 0;
        try {
            result = (int) ((double) (score - RankUtils.rankEntities[rang - 1].max) * 1.0
                    / (double) (temp.max - RankUtils.rankEntities[rang - 1].max) * 10000.0);
        } catch (Exception e) {
            result = (int) ((double) (score - 0) * 1.0 / (double) (temp.max - 0) * 10000.0);
        }
        if (score > RankUtils.rankEntities[RankUtils.rankEntities.length - 1].min - 1) {
            result = 10000;
        } else if (score < 0) {
            result = 0;
        }
        return result;
    }

    public static int getNumberRank(RankEntity rankEntity) {
        for (int i = 0; i < rankEntities.length; ++i) {
            if (rankEntities[i] != rankEntity)
                continue;
            return i;
        }
        return -1;
    }

    public static RankEntity getRankByScore(int score) {
        RankEntity temp = rankEntities[0];
        if (score >= RankUtils.rankEntities[29].max) {
            temp = rankEntities[29];
        }
        RankEntity[] arrrank = rankEntities;
        int n = rankEntities.length;
        for (int i = 0; i < n; ++i) {
            RankEntity rankEntity = arrrank[i];
            if (score < rankEntity.min || score > rankEntity.max)
                continue;
            temp = rankEntity;
        }
        return temp;
    }

    public static RankEntity getRankByIndex(int index) {
        return rankEntities[index];
    }

    public static int stringToInt(String src) {
        try {
            int tempelate = Integer.parseInt(src);
            if (tempelate <= 0) {
                tempelate = 5000000;
            }
            return tempelate >= RankUtils.rankEntities[29].min ? RankUtils.rankEntities[29].min : tempelate;
        } catch (Exception ex) {
            return 50000000;
        }
    }
}
