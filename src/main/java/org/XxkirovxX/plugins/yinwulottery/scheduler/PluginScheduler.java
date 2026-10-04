package org.XxkirovxX.plugins.yinwulottery.scheduler;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class PluginScheduler {
    private final Plugin plugin;

    public PluginScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    public TaskHandle runEntityLater(Entity entity, Runnable task, long delayTicks) {
        io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask =
            entity.getScheduler().runDelayed(plugin, ignored -> task.run(), null, safeDelay(delayTicks));
        return scheduledTask::cancel;
    }

    public void runEntity(Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, ignored -> task.run(), null);
    }

    public void runEntity(Entity entity, Runnable task, Runnable retired) {
        entity.getScheduler().run(plugin, ignored -> task.run(), retired);
    }

    private long safeDelay(long delayTicks) {
        return Math.max(1L, delayTicks);
    }

    public interface TaskHandle {
        void cancel();
    }
}
