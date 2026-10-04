# YinwuLottery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a BreweryX recipe lottery plugin with Vault economy charging and a custom 6x9 chest UI.

**Architecture:** The plugin is split into config, scheduler, economy, BreweryX recipe snapshots, book generation, and lottery UI/session services. Runtime code uses Folia-safe scheduling through reflection where available and Bukkit scheduling as a Paper fallback.

**Tech Stack:** Java 21, Maven, Spigot/Paper API, BreweryX 3.7.0 API, VaultUnlockedAPI 2.19, JUnit 5.

---

### Task 1: Project Skeleton

**Files:**
- Create: `pom.xml`
- Create: `src/main/resources/plugin.yml`
- Create: `src/main/resources/config.yml`
- Create: `src/main/java/org/XxkirovxX/plugins/yinwulottery/YinwuLotteryPlugin.java`

- [x] Create Maven project and Bukkit plugin metadata.
- [x] Generate default config on enable.
- [x] Register `/yw` command and inventory listeners.

### Task 2: Recipe Snapshot and Book Generation

**Files:**
- Create: `recipe/BreweryRecipeService.java`
- Create: `recipe/RecipeSnapshot.java`
- Create: `recipe/RecipeBookFactory.java`

- [x] Read `BRecipe.getAllRecipes()`.
- [x] Snapshot public fields plus reflected detailed fields.
- [x] Generate written books with recipe details.

### Task 3: Economy and Scheduling

**Files:**
- Create: `economy/EconomyService.java`
- Create: `scheduler/PluginScheduler.java`

- [x] Prefer Vault2 economy and fall back to legacy Vault economy.
- [x] Provide Folia global scheduler reflection fallback to Bukkit scheduler.

### Task 4: Lottery UI

**Files:**
- Create: `lottery/LotteryLayout.java`
- Create: `lottery/LotterySession.java`
- Create: `lottery/LotteryService.java`

- [x] Implement image-matched 6x9 layout.
- [x] Animate green glass on black-glass track.
- [x] Charge on start, slow on stop, award recipe book.

### Task 5: Verification

**Files:**
- Create: `src/test/java/org/XxkirovxX/plugins/yinwulottery/lottery/LotteryLayoutTest.java`

- [x] Add layout tests.
- [ ] Run Maven tests and package.
