package com.chaossmp.shawarma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public final class ShawarmaPlugin extends JavaPlugin implements Listener {

    private NamespacedKey ketchupRecipeKey;
    private NamespacedKey shawarmaRecipeKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.ketchupRecipeKey = new NamespacedKey(this, "craft_ketchup");
        this.shawarmaRecipeKey = new NamespacedKey(this, "craft_shawarma");

        registerKetchupRecipe();
        registerShawarmaRecipe();

        getServer().getPluginManager().registerEvents(this, this);

        for (Player online : Bukkit.getOnlinePlayers()) {
            unlockRecipes(online);
        }

        getLogger().info("ChaosShawarma успешно активирован!");
    }

    @Override
    public void onDisable() {
        if (ketchupRecipeKey != null) {
            Bukkit.removeRecipe(ketchupRecipeKey);
        }
        if (shawarmaRecipeKey != null) {
            Bukkit.removeRecipe(shawarmaRecipeKey);
        }
    }

    private void unlockRecipes(Player player) {
        player.discoverRecipe(ketchupRecipeKey);
        player.discoverRecipe(shawarmaRecipeKey);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        unlockRecipes(event.getPlayer());
    }

    // 1. Рецепт Кетчупа: сверху 1 редстоун, посередине бутылка с водой, снизу 1 сахар
    private void registerKetchupRecipe() {
        Bukkit.removeRecipe(ketchupRecipeKey);

        ItemStack ketchup = createKetchupItem();
        ShapedRecipe recipe = new ShapedRecipe(ketchupRecipeKey, ketchup);

        recipe.shape(
            " R ",
            " W ",
            " S "
        );

        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('W', Material.POTION);
        recipe.setIngredient('S', Material.SUGAR);

        Bukkit.addRecipe(recipe);
    }

    // 2. Рецепт Шаурмы: сверху и снизу по 3 хлеба, слева и справа курица, посередине кетчуп
    private void registerShawarmaRecipe() {
        Bukkit.removeRecipe(shawarmaRecipeKey);

        ItemStack shawarma = createShawarmaItem();
        ShapedRecipe recipe = new ShapedRecipe(shawarmaRecipeKey, shawarma);

        recipe.shape(
            "HHH",
            "CKC",
            "HHH"
        );

        recipe.setIngredient('H', Material.BREAD);
        recipe.setIngredient('C', Material.COOKED_CHICKEN);
        recipe.setIngredient('K', new RecipeChoice.ExactChoice(createKetchupItem()));

        Bukkit.addRecipe(recipe);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    private List<String> colorList(List<String> lines) {
        List<String> result = new ArrayList<>();
        if (lines == null) {
            return result;
        }
        for (String line : lines) {
            result.add(color(line));
        }
        return result;
    }

    // Кетчуп (текстура glass_bottle в ресурс-паке)
    public ItemStack createKetchupItem() {
        FileConfiguration cfg = getConfig();
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(cfg.getString("items.ketchup.name", "&c&lКетчуп")));
            meta.setLore(colorList(cfg.getStringList("items.ketchup.lore")));
            item.setItemMeta(meta);
        }
        return item;
    }

    // Шаурма (текстура cooked_beef в ресурс-паке)
    public ItemStack createShawarmaItem() {
        FileConfiguration cfg = getConfig();
        ItemStack item = new ItemStack(Material.COOKED_BEEF, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(cfg.getString("items.shawarma.name", "&6&lШаурма")));
            meta.setLore(colorList(cfg.getStringList("items.shawarma.lore")));
            item.setItemMeta(meta);
        }
        return item;
    }

    // Проверка, что посередине крафта кетчупа именно бутылка с водой
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) {
            return;
        }

        if (event.getRecipe() instanceof ShapedRecipe shaped && shaped.getKey().equals(ketchupRecipeKey)) {
            ItemStack[] matrix = event.getInventory().getMatrix();
            ItemStack centerItem = matrix.length > 4 ? matrix[4] : null;

            boolean isWaterBottle = false;
            if (centerItem != null && centerItem.getType() == Material.POTION && centerItem.hasItemMeta()) {
                if (centerItem.getItemMeta() instanceof PotionMeta potionMeta) {
                    isWaterBottle = (potionMeta.getBasePotionType() == PotionType.WATER);
                }
            }

            if (!isWaterBottle) {
                event.getInventory().setResult(null);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        FileConfiguration cfg = getConfig();
        String shawarmaName = color(cfg.getString("items.shawarma.name", "&6&lШаурма"));
        String ketchupName = color(cfg.getString("items.ketchup.name", "&c&lКетчуп"));
        String displayName = meta.getDisplayName();

        // Поедание шаурмы
        if (item.getType() == Material.COOKED_BEEF && displayName.equals(shawarmaName)) {
            if (player.getFoodLevel() < 20) {
                event.setCancelled(true);

                int hungerGain = cfg.getInt("food.shawarma-hunger", 5);
                float saturationGain = (float) cfg.getDouble("food.shawarma-saturation", 6.0);

                int currentFood = player.getFoodLevel();
                player.setFoodLevel(Math.min(currentFood + hungerGain, 20));

                float currentSaturation = player.getSaturation();
                player.setSaturation(Math.min(currentSaturation + saturationGain, (float) player.getFoodLevel()));

                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);

                consumeOneItem(player, item);
            } else {
                player.sendMessage(color(cfg.getString("messages.not-hungry", "&cВы не голодны!")));
            }
            return;
        }

        // Использование кетчупа ПКМ
        if (item.getType() == Material.GLASS_BOTTLE && displayName.equals(ketchupName)) {
            event.setCancelled(true);
            int amplifier = Math.max(0, cfg.getInt("food.ketchup-instant-health", 1) - 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, amplifier, true, false));
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.1f);
            consumeOneItem(player, item);
        }
    }

    private void consumeOneItem(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
    }
}
