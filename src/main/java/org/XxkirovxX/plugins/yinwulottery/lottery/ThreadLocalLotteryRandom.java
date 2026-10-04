package org.XxkirovxX.plugins.yinwulottery.lottery;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class ThreadLocalLotteryRandom implements LotteryRandom {
    @Override
    public int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    @Override
    public <T> void shuffle(List<T> values) {
        Collections.shuffle(values, ThreadLocalRandom.current());
    }
}
