---
trigger: always_on
---

# Folia 区域线程安全开发规范

> **重要提醒**：违反以下规范可能导致服务器崩溃、数据损坏或不可预测的行为！

## 📋 核心规则

### 1. 禁止使用的调度方式
- ❌ 禁止使用 `Bukkit.getScheduler()`
- ❌ 禁止使用 `runTask()`、`runTaskLater()`、`runTaskTimer()`
- ❌ 禁止使用 `runTaskAsynchronously()` 等传统 Bukkit 调度方法

### 2. 必须使用的调度方式
- ✅ 所有调度必须使用以下 Folia API：
    - `Bukkit.getRegionScheduler()` - 区域线程调度
    - `Bukkit.getGlobalRegionScheduler()` - 全局区域线程调度
    - `Bukkit.getEntityScheduler()` - 实体专属线程调度
    - `io.papermc.paper.threadedregions.RegionizedTask` - 区域化任务

### 3. 区域线程执行规则
- ✅ 操作实体必须在实体所在区域线程中执行
- ✅ 操作方块必须在方块所在区域线程中执行
- ✅ 操作世界/区块必须在对应区域线程中执行

**示例：**
```java
// ✅ 正确：在区域线程中操作实体
Bukkit.getRegionScheduler().run(plugin, entity.getLocation(), (task) -> {
    entity.teleport(location);
    entity.setHealth(20.0);
});

// ❌ 错误：直接在主线程操作
entity.teleport(location); // 可能导致线程安全问题
```

### 4. 事件监听规范
- ✅ 事件监听器可以正常编写（`@EventHandler`）
- ❌ 不要在事件处理方法中直接执行跨区域异步操作
- ✅ 如果需要在事件中操作其他区域的实体/方块，必须使用调度器包裹

**示例：**
```java
@EventHandler
public void onPlayerInteract(PlayerInteractEvent event) {
    Location targetLoc = event.getClickedBlock().getLocation();
    
    // ✅ 正确：使用调度器在目标区域执行
    Bukkit.getRegionScheduler().run(plugin, targetLoc, (task) -> {
        // 在目标区域线程中执行操作
        targetLoc.getBlock().setType(Material.STONE);
    });
}
```

### 5. 命令执行规范
- ✅ 命令中涉及世界/实体/方块操作时，必须使用调度器包裹

**示例：**
```java
@Override
public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) return false;
    
    Location targetLoc = player.getLocation();
    
    // ✅ 正确：使用调度器执行世界操作
    Bukkit.getRegionScheduler().run(plugin, targetLoc, (task) -> {
        targetLoc.getWorld().spawnEntity(targetLoc, EntityType.ZOMBIE);
    });
    
    return true;
}
```

### 6. 代码质量标准
- ✅ 代码结构清晰，符合 Java 编码规范
- ✅ 必须可以直接编译通过，无编译错误
- ✅ 无线程安全问题（所有共享数据使用线程安全的集合或同步机制）
- ✅ 推荐使用 `ConcurrentHashMap`、`CopyOnWriteArrayList` 等线程安全集合

### 7. 依赖限制
- ✅ 仅使用 Paper API 或 Folia API
- ❌ 禁止使用 NMS (net.minecraft.server) 直接调用
- ❌ 禁止使用不安全的反射操作访问私有字段/方法
- ✅ 如需高级功能，优先使用 Paper API 提供的扩展方法

## 💡 常见场景示例

### 场景 1：生成实体
```java
// ✅ 正确
Bukkit.getRegionScheduler().run(plugin, spawnLocation, (task) -> {
    LivingEntity entity = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);
    entity.setCustomName("测试");
});
```

### 场景 2：修改方块
```java
// ✅ 正确
Bukkit.getRegionScheduler().run(plugin, blockLocation, (task) -> {
    blockLocation.getBlock().setType(Material.DIAMOND_BLOCK);
});
```

### 场景 3：延迟执行
```java
// ✅ 正确：延迟 20 tick（1秒）执行
Bukkit.getRegionScheduler().runDelayed(plugin, location, (task) -> {
    // 执行操作
}, 20L);
```

### 场景 4：周期性任务
```java
// ✅ 正确：每 60 tick（3秒）执行一次，初始延迟 1 tick
Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (task) -> {
    // 全局周期性任务
}, 1L, 60L);
```

### 8. ⚠️ Folia 调度器初始延迟限制（重要）

**Folia 不允许 `runAtFixedRate` 和 `runDelayed` 的初始延迟为 0 或负数！**

- ❌ **禁止**：`runAtFixedRate(plugin, task -> {...}, 0L, interval)` - 会抛出 `IllegalArgumentException`
- ❌ **禁止**：`runDelayed(plugin, location, task -> {...}, 0L)` - 会抛出 `IllegalArgumentException`
- ✅ **必须**：初始延迟至少为 `1L` tick

**错误示例：**
```java
// ❌ 错误：初始延迟为 0，Folia 会抛出异常
Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (task) -> {
    // 周期性任务
}, 0L, 60L);  // IllegalArgumentException: Initial delay ticks may not be <= 0

Bukkit.getRegionScheduler().runDelayed(plugin, location, (task) -> {
    // 延迟任务
}, 0L);  // IllegalArgumentException: Initial delay ticks may not be <= 0
```

**正确示例：**
```java
// ✅ 正确：初始延迟至少为 1 tick
Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (task) -> {
    // 周期性任务
}, 1L, 60L);  // 1 tick 后开始，每 60 tick 执行一次

Bukkit.getRegionScheduler().runDelayed(plugin, location, (task) -> {
    // 延迟任务
}, 20L);  // 20 tick（1秒）后执行
```

**常见场景：**
```java
// ✅ 灾厄效果检测任务：1 tick 后开始，每 40 tick 检测一次
Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (task) -> {
    // 检测逻辑
}, 1L, 40L);

// ✅ 倒计时任务：20 tick 后开始，每 20 tick 执行一次
Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (task) -> {
    // 倒计时逻辑
}, 20L, 20L);

// ✅ 袭击调度器：使用配置值（默认 60 tick）
long interval = configManager.getRaidPerformanceConfig().getRaidSchedulerInterval();
Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (task) -> {
    // 袭击调度逻辑
}, interval, interval);
```

---

## 🔍 调试建议

- 遇到线程安全问题时，检查是否所有世界/实体/方块操作都在正确的区域线程中
- 使用日志记录调度器的执行情况，确保任务在预期的线程中运行
- 测试时启用 Folia 的线程安全检查选项
- **如果遇到 `IllegalArgumentException: Initial delay ticks may not be <= 0`，检查所有 `runAtFixedRate` 和 `runDelayed` 调用的初始延迟参数**

---

## 📦 编译指南

### 使用本机 Maven 编译

```powershell
# 在项目根目录执行
mvn clean package
```

**编译步骤：**
1. 打开终端，切换到项目根目录
2. 执行上述命令
3. 编译成功后，JAR 文件位于 `target/YinwuLottery-<版本>.jar`（当前为 `target/YinwuLottery-1.1.0.jar`）

**编译参数说明：**
- `clean` - 清理之前的编译产物
- `package` - 编译并打包 JAR 文件

**环境要求：** JDK 21+ 与 Maven 3.9+（已在 Maven 3.9.16 + Zulu JDK 25 上验证通过，产物字节码为 Java 21）。

**预期输出：**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  41.240 s
```

**输出文件：**
- `target/YinwuLottery-1.1.0.jar` - 最终插件文件
