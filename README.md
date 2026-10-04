# YinwuLottery（Yinwu抽奖）

面向 Minecraft Java 版folia服务端的 **BreweryX 酒方抽奖**插件：玩家花费货币转动转盘，抽中 BreweryX 酒方，并获得一本配方详情成书。

- 平台：Paper 1.21.4（`api-version: '1.21'`），声明 `folia-supported: true`
- 命令：`/yw lottery`、`/yw reload`（别名 `/yinwulottery`）

[![Build](https://github.com/XxkirovxX/YinwuLottery/actions/workflows/build.yml/badge.svg)](https://github.com/XxkirovxX/YinwuLottery/actions/workflows/build.yml)

## 功能

- **箱子转盘 UI**：54 格（6 行 × 9 列）界面，18 个酒方展示位围成一圈，黑玻璃板构成跑道，绿色玻璃板为移动指针。
- **扣费抽取**：点击营火，通过 Vault / VaultUnlocked 扣费。
- **减速停止**：再次点击（灵魂营火）触发停止，停在最终酒方处。
- **酒方成书**：抽中的酒方生成一本成书（标题前缀、作者、每页行数可配置），含配方名与耗材说明。
- **发放兜底**：点击结果槽领取；关闭界面时自动发放，背包满则掉落在脚边。

## 环境要求

| 项目 | 要求 |
|---|---|
| 服务端 | Paper 1.21.4+（仅兼容 Folia） |
| Java | JDK 21+ |
| 必需插件 | [BreweryX](https://github.com/BreweryTeam/BreweryX) 3.7.0+（酒方数据来源）、Vault |
| 经济实现 | VaultUnlocked（`net.milkbowl.vault2`，优先）或任意旧版 Vault 经济插件（`net.milkbowl.vault`，回退） |

> 硬依赖：服务器缺少 BreweryX 或 Vault 时插件不会加载。

## 安装

1. 确认服务端已安装 **BreweryX 3.7.0+** 与 **Vault（或 VaultUnlocked）+ 一个经济实现**（如 EssentialsX Economy、CMI 等）。
2. 把 `target/YinwuLottery-<版本>.jar` 放进服务端 `plugins/` 目录。
3. 启动服务端，生成 `plugins/YinwuLottery/config.yml`。
4. 按需修改配置后执行 `/yw reload`（或重启）。

## 使用

```
/yw lottery   # 打开抽奖界面（权限 yinwulottery.command.lottery，默认所有人）
/yw reload    # 重载配置（权限 yinwulottery.admin.reload，默认 OP）
```

界面内操作：

1. 点击中间的**营火**按钮 → 扣费并开始转动。
2. 再点一次（按钮变为**灵魂营火**）→ 请求停止，转盘减速。
3. 停止后结果槽出现**成书** → 点击领取；若直接关闭界面且 `auto-deliver-on-close` 为 `true`，会自动发放。
4. 同一玩家同一时间只能有一个进行中的抽奖；未结算就关界面会立即结算并发放。

## 权限

| 权限 | 默认值 | 说明 |
|---|---|---|
| `yinwulottery.command.lottery` | `true` | 允许打开抽奖界面 |
| `yinwulottery.admin.reload` | `op` | 允许重载配置 |

## 配置说明

`plugins/YinwuLottery/config.yml`：

```yaml
lottery:
  cost: 100.0                      # 每次抽奖扣费金额
  initial-tick-period: 3           # 转动起始 tick 周期（最小 1）
  stop-min-full-cycles: 0          # 停止后至少再转满几圈
  stop-extra-random-cycles: 0      # 停止后额外随机圈数
  max-tick-period: 18              # 减速后的最大 tick 周期（自动不小于 initial-tick-period）
  auto-deliver-on-close: true      # 关闭界面时自动发放奖品

book:
  title-prefix: "酒方："            # 成书标题前缀
  author: "YinwuLottery"           # 成书作者
  max-lines-per-page: 12           # 成书每页最大行数（最小 6）

messages:                          # 所有提示文本，支持 & 颜色代码
  prefix: "&6[Yinwu抽奖]&r "
  # 其余键：player-only / no-permission / usage / reloaded / no-recipes /
  # economy-unavailable / not-enough-money / charge-failed / charged /
  # already-running / click-stop / stopping / won / delivered / inventory-full-drop
```

注意：

- `lottery.stopping-increments`（默认 `14`）代码里会读取，但默认 `config.yml` 未写出，需要时自行添加。
- 占位符：`{cost}`（金额）、`{recipe}`（酒方名）、`{reason}`（失败原因）；`messages` 中缺失的键会回落到代码内置中文默认值。

## 从源码构建

需要 **JDK 21+** 与 **Maven 3.9+**：

```powershell
mvn clean package          # 产物：target/YinwuLottery-<版本>.jar
mvn test                   
```

本仓库**未附带 Maven Wrapper**（没有 `mvnw` / `mvnw.cmd`），请自行安装 Maven 并确保 `mvn` 在 `PATH` 中。


## 项目结构

```
pom.xml                     Maven 构建（Java 21 / paper-api 1.21.4 / surefire 3.2.5 / JUnit 5）
folia.md                    Folia 区域线程安全开发规范（改动代码前请先读）
docs/superpowers/plans/     开发计划与演进记录
src/main/resources/         plugin.yml、config.yml
src/main/java/org/XxkirovxX/plugins/yinwulottery/
  YinwuLotteryPlugin.java   插件入口：装配服务、注册命令与监听器、reload
  command/                  /yw 命令与 Tab 补全
  config/                   PluginConfig：配置读取、消息与颜色处理
  economy/                  EconomyService：VaultUnlocked 优先，旧版 Vault 回退
  lottery/                  LotteryService / LotterySession / LotterySpinState / LotteryLayout
                            / LotteryTextFormatter：界面、动画与状态机
  recipe/                   BreweryRecipeService（配方快照）、RecipeBookFactory（成书）
  scheduler/                PluginScheduler：Folia 实体调度封装
  util/                     ItemBuilder、Materials 兼容工具
src/test/java/...           单元测试与 Folia 合规测试
```

## 开发约定

- 只用 `EntityScheduler` / `RegionScheduler` / `GlobalRegionScheduler`，禁用 Bukkit 传统调度器；`runDelayed` / `runAtFixedRate` 初始延迟不得 ≤ 0。
- 操作玩家、背包、实体一律通过 `PluginScheduler` 投递到实体所在线程。
- 不要引入 NMS、CraftBukkit 或对私有字段的反射访问，否则 `FoliaComplianceTest` 会失败。
- 依赖版本变更请同步更新 `pom.xml` 与本文档的「环境要求」。

## 相关文档

- [LICENSE](LICENSE) —— GNU General Public License v3.0 全文与本项目版权声明
- 依赖源码参考：`.upstream-BreweryX/`、`.upstream-VaultUnlocked/`、`.upstream-VaultUnlockedAPI/`

## 许可证

本项目以 **GNU General Public License v3.0 或更新版本（GPL-3.0-or-later）** 发布。

这意味着你可以自由使用、修改、分发本插件，甚至可以收费；但如果你分发修改后的版本，必须同样以 GPL 开源完整源码，并保留版权声明。本插件依赖的许可证如下，均为运行时以独立插件形式加载，本插件**不包含也不打包**它们的代码。

## 第三方依赖

| 依赖 | 版本 | 许可证 | 用途 |
|---|---|---|---|
| [Paper API](https://papermc.io/) | 1.21.4-R0.1-SNAPSHOT | GPL-3.0（含特例） | 编译期 API（`provided`） |
| [BreweryX](https://github.com/BreweryTeam/BreweryX) | 3.7.0 | GPL-3.0 | 酒方数据来源（`provided`，运行时必需） |
| [VaultUnlockedAPI](https://github.com/TheNewEconomy/VaultUnlockedAPI) | 2.19 | LGPL-3.0 | 经济 API（`provided`，运行时必需） |
| [JUnit 5](https://junit.org/junit5/) | 5.10.2 | EPL-2.0 | 单元测试（`test`） |

其中 BreweryX 采用 GPL-3.0，本项目同样采用 GPL-3.0，因此不存在许可证兼容性上的争议。

## 作者
该插件由YinwuRealm正版服务器运营团队[Xx_kirov_xX](https://namemc.com/profile/Xx_Kirov_xX.1)开发
