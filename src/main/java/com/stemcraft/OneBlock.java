package com.stemcraft;

import com.stemcraft.chunkgen.VoidChunkGenerator;
import com.stemcraft.listener.PlayerChangedWorldListener;
import com.stemcraft.listener.PlayerDeathListener;
import com.stemcraft.listener.PlayerQuitListener;
import com.stemcraft.util.SCHologram;
import com.stemcraft.util.SCPlayer;
import com.stemcraft.util.SCString;
import com.stemcraft.util.SCWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class OneBlock extends STEMCraftPlugin {
    private static OneBlock instance;
    private static File configFile;
    private static YamlConfiguration config;
    private static Map<String, OneBlockData> worlds;
    private static final Random random = new Random();
    private static final Map<Integer, List<String>> materials = new HashMap<>();
    private static final Map<Integer, List<String>> mobs = new HashMap<>();

    public static class OneBlockData {
        public World world;
        public Player player;
        public BukkitRunnable task;
        public int nextItemCounter;
        public boolean shouldStop = false;
        public Runnable onStop = null;
        public int level;
        public int nextLevelCounter;

        public OneBlockData(World world, Player player, int level, int nextLevelCounter, int nextItemCounter) {
            this.world = world;
            this.player = player;
            this.level = level;
            this.nextLevelCounter = nextLevelCounter;
            this.nextItemCounter = nextItemCounter;
        }

        public OneBlockData(World world, Player player, int level) {
            this.world = world;
            this.player = player;
            this.level = level;
            this.nextLevelCounter = 10;
            this.nextItemCounter = 20;
        }

        public void dropItem(String name) {
            if(name != null && !name.isEmpty()) {
                ItemStack item = new ItemStack(Material.valueOf(name.toUpperCase()));
                world.dropItem(world.getSpawnLocation(), item);
            }
        }

        public void spawnMob(String name) {
            if(name != null && !name.isEmpty()) {
                EntityType entityType = EntityType.valueOf(name.toUpperCase()); // Replace with desired mob name
                world.spawnEntity(world.getSpawnLocation(), entityType);
            }
        }

        public String getRandomMaterial(int maxLevel) {
            List<String> allMaterials = new ArrayList<>();

            for (int i = 1; i <= maxLevel; i++) {
                if (materials.containsKey(i)) {
                    allMaterials.addAll(materials.get(i));
                }
            }

            if (allMaterials.isEmpty()) {
                return null;
            }

            Random random = new Random();
            return allMaterials.get(random.nextInt(allMaterials.size()));
        }

        public String getRandomMob(int maxLevel) {
            List<String> allMobs = new ArrayList<>();

            for (int i = 1; i <= maxLevel; i++) {
                if (mobs.containsKey(i)) {
                    allMobs.addAll(mobs.get(i));
                }
            }

            if (allMobs.isEmpty()) {
                return null;
            }

            Random random = new Random();
            return allMobs.get(random.nextInt(allMobs.size()));
        }

        public void start() {
            shouldStop = false;
            if(task != null) {
                return;
            }

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (shouldStop) {
                        this.cancel();
                        if(onStop != null) {
                            onStop.run();
                        }

                        return;
                    }

                    nextItemCounter--;
                    if(nextItemCounter <= 0) {
                        nextItemCounter = 20;

                        nextLevelCounter--;
                        if(nextLevelCounter <= 0) {
                            level++;

                            Bukkit.getScheduler().runTaskLater(instance, () -> {
                                save();
                            }, 20L);
                        }

                        if(random.nextDouble() <= 0.05) {
                            spawnMob(getRandomMob(level));
                        } else {
                            dropItem(getRandomMaterial(level));
                        }
                    }
                }
            };

            task.runTaskTimer(instance, 0L, 20L); // 0L delay, 20L period (1 second)
        }

        public void stop(Runnable callback) {
            save();
            onStop = callback;
            shouldStop = true;
        }

        public void save() {
            config.set("one-block.players." + player.getUniqueId() + ".level", level);
            config.set("one-block.players." + player.getUniqueId() + ".items-remaining", nextLevelCounter);

            if(player.getWorld().equals(world)) {
                config.set("one-block.players." + player.getUniqueId() + ".last-location", SCString.locationToString(player.getLocation(), false, true));
            }

            updateScore(false);

            try {
                config.save(configFile);
            } catch (IOException e) {
                STEMCraftLib.log(Level.SEVERE, "Failed to save the oneblock configuration file", e);
            }
        }

        public void updateScore(boolean save) {
            int score = config.getInt("one-block.scores." + player.getUniqueId());
            if(score < level) {
                config.set("one-block.scores." + player.getUniqueId(), level);

                SCHologram.updateAll("oneblock-score");

                if(save) {
                    try {
                        config.save(configFile);
                    } catch (IOException e) {
                        STEMCraftLib.log(Level.SEVERE, "Failed to save the oneblock configuration file", e);
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        instance = this;
        saveDefaultConfig();

        configFile = new File(instance.getDataFolder(), "config.yml");
        if(configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);

            Map<String, Object> rawMaterials = config.getConfigurationSection("one-block.materials").getValues(false);
            for (String key : rawMaterials.keySet()) {
                int level = Integer.parseInt(key);
                List<String> materialList = config.getStringList("one-block.materials." + key);
                materials.put(level, materialList);
            }

            Map<String, Object> rawMobs = config.getConfigurationSection("one-block.mobs").getValues(false);
            for (String key : rawMobs.keySet()) {
                int level = Integer.parseInt(key);
                List<String> mobList = config.getStringList("one-block.mobs." + key);
                mobs.put(level, mobList);
            }
        }

        List<String[]> tabCompletions = new ArrayList<>();
        tabCompletions.add(new String[]{"join"});
        tabCompletions.add(new String[]{"leave"});
        tabCompletions.add(new String[]{"delete"});
        tabCompletions.add(new String[]{"hologram", "create"});
        tabCompletions.add(new String[]{"hologram", "delete"});

        registerCommand(new com.stemcraft.command.OneBlock(instance), "oneblock", null, tabCompletions);

        registerEvents(new PlayerChangedWorldListener());
        registerEvents(new PlayerQuitListener());
        registerEvents(new PlayerDeathListener());

        SCHologram.registerType("oneblock-score", () -> {
            List<String> text = new ArrayList<>();
            Map<UUID, Integer> scores = new HashMap<>();
            ConfigurationSection section = config.getConfigurationSection("one-block.scores");

            text.add("&6-- One Block Champions --");

            if (section != null) {
                for (String key : section.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(key);
                        int level = section.getInt(key);
                        scores.put(uuid, level);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }
            }

            scores = scores.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue())) // Sort descending
                    .limit(10)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new));


            int spaceWidth = SCString.calculatePixelWidth(" ");
            if(!scores.isEmpty()) {
                for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
                    StringBuilder name = new StringBuilder(SCPlayer.name(String.valueOf(entry.getKey()))); // Get player name
                    int score = entry.getValue();

                    // Ensure name width ~120 pixels
                    int currentWidth = SCString.calculatePixelWidth(name.toString());
                    while (currentWidth < 120) { // Add spaces until width is ~120
                        name.append(" ");
                        currentWidth += spaceWidth;
                    }
                    while (currentWidth > 120) { // Trim letters if width exceeds 120
                        name = new StringBuilder(name.substring(0, name.length() - 1));
                        currentWidth = SCString.calculatePixelWidth(name.toString());
                    }

                    // Add formatted entry
                    text.add("&f" + name + "   &2" + score); // White name, dark green score
                }
            } else {
                text.add("&fNo one has played yet!");
            }

            return text;
       });
    }

    @SuppressWarnings("unused")
    public OneBlock getInstance() {
        return instance;
    }

    public static OneBlockData startInstance(Player player) {
        OneBlockData data;
        String worldName = getWorldName(player);

        // has the world data not been loaded yet?
        if(!worlds.containsKey(worldName)) {

            // is the world itself already exist?
            World world = SCWorld.load(worldName);
            if(world == null) {
                world = SCWorld.create(worldName, new VoidChunkGenerator());
                Location spawn = world.getSpawnLocation();
                spawn.subtract(0, 1, 0).getBlock().setType(Material.DIRT);

                config.set("one-block.players." + player.getUniqueId(), null);
            }

            int level = config.getInt("one-block.players." + player.getUniqueId() + ".level", 1);
            int nextLevelCounter = config.getInt("one-block.players." + player.getUniqueId() + ".items-remaining", 10);
            int nextItemCounter = 20;

            data = new OneBlockData(world, player, level, nextLevelCounter, nextItemCounter);
            worlds.put(worldName, data);
        } else {
            data = worlds.get(worldName);
        }

        data.start();
        return data;
    }

    public static void stopInstance(Player player) {
        String worldName = getWorldName(player);

        if(worlds.containsKey(worldName)) {
            worlds.get(worldName).stop(null);
        }
    }

    public static void joinInstance(Player player) {
        OneBlockData data = startInstance(player);

        String lastLocationPath = "one-block.players." + player.getUniqueId() + ".last-location";
        if(config.contains(lastLocationPath)) {
            SCPlayer.teleport(player, SCString.stringToLocation(config.getString(lastLocationPath, ""), data.world));
        } else {
            SCPlayer.teleport(player, data.world.getSpawnLocation());
        }
    }

    public static void deleteInstance(Player player) {
        String worldName = getWorldName(player);

        if(worlds.containsKey(worldName)) {
            worlds.get(worldName).updateScore(true);
        }

        SCWorld.delete(worldName);
        config.set("one-block.players." + player.getUniqueId(), null);

        try {
            config.save(configFile);
        } catch (IOException e) {
            STEMCraftLib.log(Level.SEVERE, "Failed to save the oneblock configuration file", e);
        }
    }

    public static void saveInstance(Player player) {
        String worldName = getWorldName(player);

        if(worlds.containsKey(worldName)) {
            worlds.get(worldName).save();
        }
    }

    public static String getWorldName(Player player) {
        return "oneblock-" + player.getUniqueId();
    }
}
