package org.XxkirovxX.plugins.yinwulottery.lottery;

import java.util.List;

interface LotteryRandom {
    int nextInt(int bound);

    <T> void shuffle(List<T> values);
}
