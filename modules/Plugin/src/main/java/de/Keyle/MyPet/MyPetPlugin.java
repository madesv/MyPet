/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2018 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet;

import de.Keyle.MyPet.api.*;
import de.Keyle.MyPet.api.entity.EntityRegistry;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.MyPetInfo;
import de.Keyle.MyPet.api.entity.StoredMyPet;
import de.Keyle.MyPet.api.entity.leashing.LeashFlagManager;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import de.Keyle.MyPet.api.repository.*;
import de.Keyle.MyPet.api.skill.SkillManager;
import de.Keyle.MyPet.api.skill.experience.ExperienceCache;
import de.Keyle.MyPet.api.skill.experience.ExperienceCalculatorManager;
import de.Keyle.MyPet.api.skill.skilltree.SkillTreeLoaderJSON;
import de.Keyle.MyPet.api.skill.skilltree.Skilltree;
import de.Keyle.MyPet.api.skill.skilltree.SkilltreeManager;
import de.Keyle.MyPet.api.util.*;
import de.Keyle.MyPet.api.util.Timer;
import de.Keyle.MyPet.api.util.configuration.ConfigurationYAML;
import de.Keyle.MyPet.api.util.hooks.HookHelper;
import de.Keyle.MyPet.api.util.hooks.PluginHookManager;
import de.Keyle.MyPet.api.util.locale.Translation;
import de.Keyle.MyPet.api.util.service.Load;
import de.Keyle.MyPet.api.util.service.ServiceManager;
import de.Keyle.MyPet.commands.*;
import de.Keyle.MyPet.entity.leashing.*;
import de.Keyle.MyPet.listeners.*;
import de.Keyle.MyPet.repository.Converter;
import de.Keyle.MyPet.repository.types.MongoDbRepository;
import de.Keyle.MyPet.repository.types.MySqlRepository;
import de.Keyle.MyPet.repository.types.SqLiteRepository;
import de.Keyle.MyPet.services.RepositoryMyPetConverterService;
import de.Keyle.MyPet.skill.experience.JavaScriptExperienceCalculator;
import de.Keyle.MyPet.skill.skills.*;
import de.Keyle.MyPet.util.ConfigurationLoader;
import de.Keyle.MyPet.util.Updater;
import de.Keyle.MyPet.util.hooks.*;
import de.Keyle.MyPet.util.logger.MyPetLogger;
import de.Keyle.MyPet.util.player.MyPetPlayerImpl;
import de.Keyle.MyPet.util.shop.ShopManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.json.simple.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class MyPetPlugin extends JavaPlugin implements de.Keyle.MyPet.api.plugin.MyPetPlugin {

    private boolean isReady = false;
    private Repository repo;
    private MyPetInfo petInfo;
    private PlatformHelper platformHelper;
    private EntityRegistry entityRegistry;
    private CompatUtil compatUtil;
    private CompatManager compatManager;
    private PlayerManager playerManager;
    private MyPetManager myPetManager;
    private HookHelper hookHelper;
    private PluginHookManager pluginHookManager;
    private ServiceManager serviceManager;

    public void onDisable() {
        if (isReady) {
            for (MyPet myPet : myPetManager.getAllActiveMyPets()) {
                if (myPet.getStatus() == MyPet.PetState.Here) {
                    myPet.removePet(true);
                }
            }
            repo.disable();
            entityRegistry.unregisterEntityTypes();
        }
        Timer.reset();
        Bukkit.getServer().getScheduler().cancelTasks(this);

        if (getLogger() instanceof MyPetLogger) {
            ((MyPetLogger) getLogger()).disableDebugLogger();
        }

        pluginHookManager.disableHooks();
        serviceManager.disableServices();
    }

    public void onLoad() {
        MyPetApi.setPlugin(this);
        replaceLogger();
        getDataFolder().mkdirs();

        // load version from manifest
        MyPetVersion.reset();

        compatUtil = new CompatUtil();

        ConfigurationLoader.upgradeConfig();
        ConfigurationLoader.setDefault();
        ConfigurationLoader.loadConfiguration();

        serviceManager = new ServiceManager();
        pluginHookManager = new PluginHookManager();

        if (compatUtil.getInternalVersion() == null || !MyPetVersion.isValidBukkitPacket(compatUtil.getInternalVersion())) {
            return;
        }

        petInfo = compatUtil.getComapatInstance(MyPetInfo.class, "entity", "MyPetInfo");
        platformHelper = compatUtil.getComapatInstance(PlatformHelper.class, "", "PlatformHelper");
        entityRegistry = compatUtil.getComapatInstance(EntityRegistry.class, "entity", "EntityRegistry");
        myPetManager = new de.Keyle.MyPet.repository.MyPetManager();
        playerManager = new de.Keyle.MyPet.repository.PlayerManager();
        hookHelper = new de.Keyle.MyPet.util.HookHelper();

        ConfigurationLoader.loadCompatConfiguration();

        registerServices();
        compatManager = compatUtil.getComapatInstance(CompatManager.class, "", "CompatManager");
        compatManager.init();
        serviceManager.activate(Load.State.OnLoad);
        registerHooks();
    }

    public void onEnable() {
        this.isReady = false;

        Updater updater = new Updater("MyPet-Premium");
        updater.update();

        if (compatUtil.getInternalVersion() == null || !MyPetVersion.isValidBukkitPacket(compatUtil.getInternalVersion())) {
            getLogger().warning("This version of MyPet is not compatible with \"" + compatUtil.getInternalVersion() + "\". Is MyPet up to date?");
            updater.waitForDownload();
            setEnabled(false);
            return;
        }

        serviceManager.activate(Load.State.OnEnable);

        entityRegistry.registerEntityTypes();

        if (getLogger() instanceof MyPetLogger) {
            ((MyPetLogger) getLogger()).updateDebugLoggerLogLevel();
        }

        compatManager.enable();
        getLogger().info("Compat mode for " + compatUtil.getInternalVersion() + " loaded.");

        //register leash flags
        registerLeashFlags();

        //register exp calculators
        if (!new File(getDataFolder(), "exp.js").exists()) {
            platformHelper.copyResource(this, "exp.js", new File(getDataFolder(), "exp.js"));
        }
        ExperienceCalculatorManager calculatorManager = serviceManager.getService(ExperienceCalculatorManager.class).get();
        calculatorManager.registerCalculator("JS", JavaScriptExperienceCalculator.class);
        calculatorManager.registerCalculator("JavaScript", JavaScriptExperienceCalculator.class);
        calculatorManager.switchCalculator(Configuration.LevelSystem.CALCULATION_MODE.toLowerCase());

        // register event listener
        PlayerListener playerListener = new PlayerListener();
        getServer().getPluginManager().registerEvents(playerListener, this);
        VehicleListener vehicleListener = new VehicleListener();
        getServer().getPluginManager().registerEvents(vehicleListener, this);
        EntityListener entityListener = new EntityListener();
        getServer().getPluginManager().registerEvents(entityListener, this);
        LevelListener levelupListener = new LevelListener();
        getServer().getPluginManager().registerEvents(levelupListener, this);
        WorldListener worldListener = new WorldListener();
        getServer().getPluginManager().registerEvents(worldListener, this);

        // register commands
        getCommand("petname").setExecutor(new CommandName());
        getCommand("petcall").setExecutor(new CommandCall());
        getCommand("petsendaway").setExecutor(new CommandSendAway());
        getCommand("petstop").setExecutor(new CommandStop());
        getCommand("petrelease").setExecutor(new CommandRelease());
        getCommand("mypet").setExecutor(new CommandHelp());
        getCommand("petinventory").setExecutor(new CommandInventory());
        getCommand("petpickup").setExecutor(new CommandPickup());
        getCommand("petbehavior").setExecutor(new CommandBehavior());
        getCommand("petinfo").setExecutor(new CommandInfo());
        getCommand("mypetadmin").setExecutor(new CommandAdmin());
        getCommand("petskill").setExecutor(new CommandSkill());
        getCommand("petskilltree").setExecutor(new CommandShowSkillTree());
        getCommand("petchooseskilltree").setExecutor(new CommandChooseSkilltree());
        getCommand("petbeacon").setExecutor(new CommandBeacon());
        getCommand("petrespawn").setExecutor(new CommandRespawn());
        getCommand("pettype").setExecutor(new CommandPetType());
        getCommand("petcapturehelper").setExecutor(new CommandCaptureHelper());
        getCommand("petsettings").setExecutor(new CommandSettings());
        getCommand("petswitch").setExecutor(new CommandSwitch());
        getCommand("petstore").setExecutor(new CommandStore());
        getCommand("petlist").setExecutor(new CommandList());
        // register premium commands
        getCommand("pettrade").setExecutor(new CommandTrade());
        getCommand("petshop").setExecutor(new CommandShop());

        // register skills
        registerSkills();

        // create folders
        File skilltreeFolder = new File(getDataFolder().getPath(), "skilltrees");
        getDataFolder().mkdirs();
        boolean createDefaultSkilltree = skilltreeFolder.mkdirs();
        boolean createLocaleReadme = new File(getDataFolder(), "locale").mkdirs();
        new File(getDataFolder(), "logs").mkdirs();

        if (!createDefaultSkilltree) {
            File legacyDefaultSkilltree = new File(skilltreeFolder, "default.st");
            createDefaultSkilltree = legacyDefaultSkilltree.exists();
            if (createDefaultSkilltree) {
                if (Util.getSha256FromFile(legacyDefaultSkilltree) == -4323392001800132707L) {
                    legacyDefaultSkilltree.delete();
                }
            }
        }

        if (createDefaultSkilltree) {
            File skilltreeFile = new File(skilltreeFolder, "Combat.st.json");
            if (!skilltreeFile.exists()) {
                platformHelper.copyResource(this, "skilltrees/Combat.st.json", new File(skilltreeFolder, "Combat.st.json"));
            }
            skilltreeFile = new File(skilltreeFolder, "Farm.st.json");
            if (!skilltreeFile.exists()) {
                platformHelper.copyResource(this, "skilltrees/Farm.st.json", new File(skilltreeFolder, "Farm.st.json"));
            }
            skilltreeFile = new File(skilltreeFolder, "PvP.st.json");
            if (!skilltreeFile.exists()) {
                platformHelper.copyResource(this, "skilltrees/PvP.st.json", new File(skilltreeFolder, "PvP.st.json"));
            }
            skilltreeFile = new File(skilltreeFolder, "Ride.st.json");
            if (!skilltreeFile.exists()) {
                platformHelper.copyResource(this, "skilltrees/Ride.st.json", new File(skilltreeFolder, "Ride.st.json"));
            }
            skilltreeFile = new File(skilltreeFolder, "Utility.st.json");
            if (!skilltreeFile.exists()) {
                platformHelper.copyResource(this, "skilltrees/Utility.st.json", new File(skilltreeFolder, "Utility.st.json"));
            }
            MyPetApi.getLogger().info("Default skilltree files created.");
        }

        // load skilltrees
        MyPetApi.getSkilltreeManager().clearSkilltrees();
        SkillTreeLoaderJSON.loadSkilltrees(new File(getDataFolder(), "skilltrees"));
        // register skilltree permissions
        for (Skilltree skilltree : MyPetApi.getSkilltreeManager().getSkilltrees()) {
            try {
                Bukkit.getPluginManager().addPermission(new Permission(skilltree.getFullPermission()));
            } catch (Exception ignored) {
            }
        }

        for (int i = 0; i <= Configuration.Misc.MAX_STORED_PET_COUNT; i++) {
            try {
                Bukkit.getPluginManager().addPermission(new Permission("MyPet.petstorage.limit." + i));
            } catch (Exception ignored) {
            }
        }

        if (createLocaleReadme) {
            platformHelper.copyResource(this, "locale-readme.txt", new File(getDataFolder(), "locale" + File.separator + "readme.txt"));
        }
        Translation.init();

        for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            if (team.getName().startsWith("MyPet-")) {
                team.unregister();
            }
        }

        // init repository
        if (Configuration.Repository.REPOSITORY_TYPE.equalsIgnoreCase("NBT")) {
            Configuration.Repository.REPOSITORY_TYPE = "SQLite";
            Configuration.Repository.CONVERT_FROM = "NBT";
            repo = new SqLiteRepository();
            try {
                repo.init();
            } catch (RepositoryInitException e) {
                e.printStackTrace();
                repo = null;
            }
        } else if (Configuration.Repository.REPOSITORY_TYPE.equalsIgnoreCase("MySQL")) {
            MyPetApi.getLogger().info("Connect to MySQL database...");
            repo = new MySqlRepository();
            try {
                repo.init();
                MyPetApi.getLogger().info("MySQL connection successful.");
            } catch (RepositoryInitException e) {
                MyPetApi.getLogger().warning("MySQL connection failed!");
                e.printStackTrace();
                repo = null;
            }
        } else if (Configuration.Repository.REPOSITORY_TYPE.equalsIgnoreCase("MongoDB")) {
            MyPetApi.getLogger().info("Connect to MongoDB database...");
            repo = new MongoDbRepository();
            try {
                repo.init();
                MyPetApi.getLogger().info("MongoDB connection successful.");
            } catch (RepositoryInitException e) {
                MyPetApi.getLogger().warning("MongoDB connection failed!");
                e.printStackTrace();
                repo = null;
            }
        }

        if (repo == null) {
            MyPetApi.getLogger().info("Connect to SQLite database...");
            repo = new SqLiteRepository();
            try {
                repo.init();
                MyPetApi.getLogger().info("SQLite connection successful.");
            } catch (RepositoryInitException ignored) {
                MyPetApi.getLogger().warning("SQLite connection failed!");
                setEnabled(false);
                return;
            }
        }

        Converter.convert();

        if (repo instanceof Scheduler) {
            Timer.addTask((Scheduler) repo);
        }

        // load worldgroups
        loadGroups(new File(getDataFolder().getPath(), "worldgroups.yml"));

        File shopConfig = new File(getDataFolder(), "pet-shops.yml");
        if (!shopConfig.exists()) {
            platformHelper.copyResource(this, "pet-shops.yml", shopConfig);
        }
        new ShopManager();

        Timer.startTimer();

        // init Metrics
        Metrics metrics = new Metrics(this) {
            public JSONObject getPluginData() {
                JSONObject data = super.getPluginData();
                //noinspection unchecked
                data.put("pluginName", "MyPet-Premium");
                return data;
            }
        };
        metrics.addCustomChart(new Metrics.SingleLineChart("active_pets", () -> myPetManager.countActiveMyPets()));
        metrics.addCustomChart(new Metrics.SimplePie("build", MyPetVersion::getBuild));
        metrics.addCustomChart(new Metrics.SimplePie("update_mode", () -> {
            String mode = "Disabled";
            if (Configuration.Update.CHECK) {
                mode = "Check";
                if (Configuration.Update.DOWNLOAD) {
                    mode += " & Download";
                    if (Configuration.Update.TOKEN.equals("")) {
                        mode += " (Missing Token)";
                    }
                }
            }
            return mode;
        }));

        updater.waitForDownload();

        pluginHookManager.enableHooks();
        serviceManager.activate(Load.State.AfterHooks);

        if (MyPetVersion.isPremium()) {
            getLogger().info("Thank you for buying MyPet-" + ChatColor.YELLOW + "Premium" + ChatColor.RESET + "!");
        }
        getLogger().info("version " + MyPetVersion.getVersion() + "-b" + MyPetVersion.getBuild() + (MyPetVersion.isPremium() ? "P" : "") + ChatColor.GREEN + " ENABLED");
        this.isReady = true;

        serviceManager.activate(Load.State.OnReady);

        // load pets for online players
        new BukkitRunnable() {
            @Override
            public void run() {
                for (final Player player : getServer().getOnlinePlayers()) {
                    repo.getMyPetPlayer(player, new RepositoryCallback<MyPetPlayer>() {
                        @Override
                        public void callback(final MyPetPlayer p) {
                            if (p != null) {
                                final MyPetPlayerImpl onlinePlayer = (MyPetPlayerImpl) p;

                                onlinePlayer.setLastKnownName(player.getName());
                                if (!player.getUniqueId().equals(onlinePlayer.getOfflineUUID())) {
                                    if (onlinePlayer.getMojangUUID() == null) {
                                        onlinePlayer.setMojangUUID(player.getUniqueId());
                                    }
                                    onlinePlayer.setOnlineMode(true);
                                }

                                playerManager.setOnline(onlinePlayer);

                                final WorldGroup joinGroup = WorldGroup.getGroupByWorld(player.getWorld().getName());
                                if (onlinePlayer.hasMyPet()) {
                                    MyPet myPet = onlinePlayer.getMyPet();
                                    if (!myPet.getWorldGroup().equals(joinGroup.getName())) {
                                        myPetManager.deactivateMyPet(onlinePlayer, true);
                                    }
                                }

                                if (joinGroup != null && !onlinePlayer.hasMyPet() && onlinePlayer.hasMyPetInWorldGroup(joinGroup.getName())) {
                                    final UUID petUUID = onlinePlayer.getMyPetForWorldGroup(joinGroup.getName());

                                    MyPetApi.getRepository().getMyPet(petUUID, new RepositoryCallback<StoredMyPet>() {
                                        @Override
                                        public void callback(StoredMyPet storedMyPet) {
                                            myPetManager.activateMyPet(storedMyPet);

                                            if (onlinePlayer.hasMyPet()) {
                                                final MyPet myPet = onlinePlayer.getMyPet();
                                                final MyPetPlayer myPetPlayer = myPet.getOwner();
                                                if (myPet.wantsToRespawn()) {
                                                    if (myPetPlayer.hasMyPet()) {
                                                        MyPet runMyPet = myPetPlayer.getMyPet();
                                                        switch (runMyPet.createEntity()) {
                                                            case Canceled:
                                                                runMyPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Spawn.Prevent", myPet.getOwner()), runMyPet.getPetName()));
                                                                break;
                                                            case NoSpace:
                                                                runMyPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Spawn.NoSpace", myPet.getOwner()), runMyPet.getPetName()));
                                                                break;
                                                            case NotAllowed:
                                                                runMyPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.No.AllowedHere", myPet.getOwner()), myPet.getPetName()));
                                                                break;
                                                            case Dead:
                                                                runMyPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Spawn.Respawn.In", myPet.getOwner()), myPet.getPetName(), myPet.getRespawnTime()));
                                                                break;
                                                            case Flying:
                                                                runMyPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Spawn.Flying", myPet.getOwner()), myPet.getPetName()));
                                                                break;
                                                            case Success:
                                                                runMyPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Command.Call.Success", myPet.getOwner()), runMyPet.getPetName()));
                                                                break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                                onlinePlayer.checkForDonation();
                            }
                        }
                    });
                }
            }
        }.runTaskLater(this, 0);
    }

    private void registerServices() {
        serviceManager.registerService(RepositoryMyPetConverterService.class);
        serviceManager.registerService(ItemDatabase.class);
        serviceManager.registerService(LeashFlagManager.class);
        serviceManager.registerService(ExperienceCache.class);
        serviceManager.registerService(ExperienceCalculatorManager.class);
        serviceManager.registerService(SkillManager.class);
        serviceManager.registerService(SkilltreeManager.class);
        serviceManager.registerService(ShopManager.class);
    }

    private void registerHooks() {
        pluginHookManager.registerHook(AncientHook.class);
        pluginHookManager.registerHook(BattleArenaHook.class);
        pluginHookManager.registerHook(CitizensHook.class);
        pluginHookManager.registerHook(FactionsHook.class);
        pluginHookManager.registerHook(GriefPreventionHook.class);
        pluginHookManager.registerHook(GriefPreventionPlusHook.class);
        pluginHookManager.registerHook(HeroesHook.class);
        pluginHookManager.registerHook(KingdomsHook.class);
        pluginHookManager.registerHook(MagicSpellsHook.class);
        pluginHookManager.registerHook(McMMOHook.class);
        pluginHookManager.registerHook(MiniaturePetsHook.class);
        pluginHookManager.registerHook(MinigamesHook.class);
        pluginHookManager.registerHook(MobArenaHook.class);
        pluginHookManager.registerHook(MythicMobsHook.class);
        pluginHookManager.registerHook(NoCheatPlusHook.class);
        pluginHookManager.registerHook(PlaceholderApiHook.class);
        pluginHookManager.registerHook(PlotSquaredHook.class);
        pluginHookManager.registerHook(PreciousStonesHook.class);
        pluginHookManager.registerHook(PremiumVanish.class);
        pluginHookManager.registerHook(ProtocolLibHook.class);
        pluginHookManager.registerHook(PvPArenaHook.class);
        pluginHookManager.registerHook(PvPDiffTimerHook.class);
        pluginHookManager.registerHook(PvPManagerHook.class);
        pluginHookManager.registerHook(RedProtectHook.class);
        pluginHookManager.registerHook(ResidenceHook.class);
        pluginHookManager.registerHook(SimpleClansHook.class);
        pluginHookManager.registerHook(SkillApiHook.class);
        pluginHookManager.registerHook(SuperVanish.class);
        pluginHookManager.registerHook(SurvivalGamesHook.class);
        pluginHookManager.registerHook(TownyHook.class);
        pluginHookManager.registerHook(UltimateSurvivalGamesHook.class);
        pluginHookManager.registerHook(VaultHook.class);
        pluginHookManager.registerHook(WorldGuardHook.class);
        pluginHookManager.registerHook(WorldGuardCustomFlagsHook.class);
    }

    public static void registerSkills() {
        MyPetApi.getSkillManager().registerSkill(BackpackImpl.class);
        MyPetApi.getSkillManager().registerSkill(HealImpl.class);
        MyPetApi.getSkillManager().registerSkill(PickupImpl.class);
        MyPetApi.getSkillManager().registerSkill(BehaviorImpl.class);
        MyPetApi.getSkillManager().registerSkill(DamageImpl.class);
        MyPetApi.getSkillManager().registerSkill(ControlImpl.class);
        MyPetApi.getSkillManager().registerSkill(LifeImpl.class);
        MyPetApi.getSkillManager().registerSkill(PoisonImpl.class);
        MyPetApi.getSkillManager().registerSkill(RideImpl.class);
        MyPetApi.getSkillManager().registerSkill(ThornsImpl.class);
        MyPetApi.getSkillManager().registerSkill(FireImpl.class);
        MyPetApi.getSkillManager().registerSkill(BeaconImpl.class);
        MyPetApi.getSkillManager().registerSkill(WitherImpl.class);
        MyPetApi.getSkillManager().registerSkill(LightningImpl.class);
        MyPetApi.getSkillManager().registerSkill(SlowImpl.class);
        MyPetApi.getSkillManager().registerSkill(KnockbackImpl.class);
        MyPetApi.getSkillManager().registerSkill(RangedImpl.class);
        MyPetApi.getSkillManager().registerSkill(SprintImpl.class);
        MyPetApi.getSkillManager().registerSkill(StompImpl.class);
        MyPetApi.getSkillManager().registerSkill(ShieldImpl.class);
    }

    public static void registerLeashFlags() {
        MyPetApi.getLeashFlagManager().registerLeashFlag(new AdultFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new AngryFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new BabyFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new BelowHpFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new CanBreedFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new ChanceFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new ImpossibleFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new LowHpFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new SizeFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new TamedFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new UserCreatedFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new WildFlag());
        MyPetApi.getLeashFlagManager().registerLeashFlag(new WorldFlag());
    }

    private int loadGroups(File f) {
        ConfigurationYAML yamlConfiguration = new ConfigurationYAML(f);
        FileConfiguration config = yamlConfiguration.getConfig();

        if (config == null) {
            return 0;
        }

        WorldGroup.clearGroups();

        Set<String> nodes;
        try {
            nodes = config.getConfigurationSection("Groups").getKeys(false);
        } catch (NullPointerException e) {
            nodes = new HashSet<>();
            getLogger().info("No groups found. Everything will be in 'default' group.");
        }

        getLogger().info("--- Loading WorldGroups ---------------------------");
        if (nodes.size() == 0) {
            List<String> worldNames = new ArrayList<>();
            WorldGroup defaultGroup = new WorldGroup("default");
            defaultGroup.registerGroup();
            for (World world : this.getServer().getWorlds()) {
                getLogger().info("added " + ChatColor.GOLD + world.getName() + ChatColor.RESET + " to 'default' group.");
                worldNames.add(world.getName());
                defaultGroup.addWorld(world.getName());
            }
            config.set("Groups.default", worldNames);
            yamlConfiguration.saveConfig();
        } else {
            for (String node : nodes) {
                List<String> worlds = config.getStringList("Groups." + node);
                if (worlds.size() > 0) {
                    WorldGroup newGroup = new WorldGroup(node);
                    for (String world : worlds) {
                        getLogger().info("   added '" + world + "' to '" + newGroup.getName() + "'");
                        newGroup.addWorld(world);
                    }
                    if (newGroup.getWorlds().size() > 0) {
                        newGroup.registerGroup();
                    }
                }
            }

            WorldGroup defaultGroup = WorldGroup.getGroupByName("default");
            if (defaultGroup == null) {
                defaultGroup = new WorldGroup("default");
                defaultGroup.registerGroup();
            }

            boolean saveConfig = false;
            for (World world : getServer().getWorlds()) {
                if (WorldGroup.getGroupByWorld(world.getName()) == null) {
                    getLogger().info("added " + ChatColor.GOLD + world.getName() + ChatColor.RESET + " to 'default' group.");
                    defaultGroup.addWorld(world.getName());
                    saveConfig = true;
                }
            }
            if (saveConfig) {
                config.set("Groups.default", defaultGroup.getWorlds());
                yamlConfiguration.saveConfig();
            }
        }
        getLogger().info("-------------------------------------------------");
        return 0;
    }

    @Override
    public PluginHookManager getPluginHookManager() {
        return pluginHookManager;
    }

    @Override
    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public File getFile() {
        return super.getFile();
    }

    @Override
    public MyPetInfo getMyPetInfo() {
        return petInfo;
    }

    @Override
    public EntityRegistry getEntityRegistry() {
        return entityRegistry;
    }

    @Override
    public CompatUtil getCompatUtil() {
        return compatUtil;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public MyPetManager getMyPetManager() {
        return myPetManager;
    }

    public HookHelper getHookHelper() {
        return hookHelper;
    }

    public Repository getRepository() {
        return repo;
    }

    public PlatformHelper getPlatformHelper() {
        return platformHelper;
    }

    private void replaceLogger() {
        try {
            Field logger = ReflectionUtil.getField(JavaPlugin.class, "logger");
            if (logger != null) {
                logger.set(this, new MyPetLogger(this));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}