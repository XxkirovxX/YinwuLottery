# BreweryX VaultUnlockedAPI Folia Adapter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep YinwuLottery compatible with BreweryX and VaultUnlockedAPI while enforcing Folia-safe scheduling and dependency metadata.

**Architecture:** BreweryX access stays in `BreweryRecipeService` as immutable recipe snapshots. Economy charging stays in `EconomyService`, preferring VaultUnlockedAPI's `net.milkbowl.vault2.economy.Economy` and falling back to legacy Vault economy. Folia rules are guarded by focused JUnit tests that scan production code and plugin metadata.

**Tech Stack:** Java 21, Maven, Paper API 1.21.4, BreweryX 3.7.0 API, VaultUnlockedAPI 2.19, JUnit 5.

---

### Task 1: Folia and Dependency Guard Tests

**Files:**
- Modify: `src/test/java/org/XxkirovxX/plugins/yinwulottery/folia/FoliaComplianceTest.java`

- [x] Add tests asserting production code does not call Bukkit scheduler APIs, NMS, CraftBukkit, reflection, or unsafe async scheduling.
- [x] Add tests asserting `PluginScheduler` uses `EntityScheduler` and clamps delayed tasks to at least `1L`.
- [x] Add tests asserting `plugin.yml` declares `folia-supported: true`, depends on `BreweryX`, and depends on `Vault`.

### Task 2: VaultUnlockedAPI Behavior Tests

**Files:**
- Create: `src/test/java/org/XxkirovxX/plugins/yinwulottery/economy/EconomyServiceTest.java`

- [x] Add a test that registers both VaultUnlockedAPI `vault2` and legacy Vault economy providers and verifies `vault2` is used first.
- [x] Add a test that verifies legacy Vault is used when no `vault2` provider is present.
- [x] Add a test that verifies `canWithdraw` returning `NOT_IMPLEMENTED` falls back to `has` before withdrawing.

### Task 3: Production Adjustments

**Files:**
- Modify: `src/main/java/org/XxkirovxX/plugins/yinwulottery/economy/EconomyService.java`
- Modify: `src/main/java/org/XxkirovxX/plugins/yinwulottery/scheduler/PluginScheduler.java`
- Modify: `src/main/resources/plugin.yml`

- [x] Keep `EconomyService` on public VaultUnlockedAPI/Vault APIs only.
- [x] Keep `PluginScheduler` on Paper/Folia scheduler APIs only and ensure delayed scheduling never receives `0L`.
- [x] Keep runtime dependency metadata aligned with BreweryX and VaultUnlocked's documented `Vault` service name.

### Task 4: Verification

**Files:**
- Maven project root

- [x] Run targeted tests for `EconomyServiceTest` and `FoliaComplianceTest`.
- [x] Run the full Maven test suite.
- [x] Run `mvn package` or equivalent compile/package verification.
