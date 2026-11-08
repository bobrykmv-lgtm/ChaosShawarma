package com.chaossmp.shawarma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

public final class ShawarmaPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        registerKetchupRecipe();
        registerShawarmaRecipe1();
        registerShawarmaRecipe2(); // Новий додатковий рецепт

        getLogger().info("ChaosSMP Shawarma Plugin увімкнено!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ChaosSMP Shawarma Plugin вимкнено.");
    }

    // 1. Рецепт Кетчупу (CustomModelData: 1001)
    private void registerKetchupRecipe() {
        ItemStack ketchup = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) ketchup.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Кетчуп");
            meta.setBasePotionType(PotionType.HEALING);
            meta.setCustomModelData(1001);
            ketchup.setItemMeta(meta);
        }

        NamespacedKey key = new NamespacedKey(this, "ketchup");
        ShapelessRecipe recipe = new ShapelessRecipe(key, ketchup);
        recipe.addIngredient(Material.REDSTONE);
        recipe.addIngredient(Material.SUGAR);
        recipe.addIngredient(Material.GLASS_BOTTLE);

        Bukkit.addRecipe(recipe);
    }

    // 2. Основний рецепт Шаурми №1 (Велика сітка: Курка навколо, папір по боках, кетчуп в центрі)
    private void registerShawarmaRecipe1() {
        ItemStack shawarma = new ItemStack(Material.COOKED_CHICKEN);
        ItemMeta meta = shawarma.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Соковита Шаурма");
            meta.setCustomModelData(1002);
            shawarma.setItemMeta(meta);
        }

        NamespacedKey key = new NamespacedKey(this, "shawarma_1");
        ShapedRecipe recipe = new ShapedRecipe(key, shawarma);
        recipe.shape(
            "CCC",
            "PKP",
            "CCC"
        );

        recipe.setIngredient('C', Material.COOKED_CHICKEN);
        recipe.setIngredient('K', Material.POTION);
        recipe.setIngredient('P', Material.PAPER);

        Bukkit.addRecipe(recipe);
    }

    // 3. Додатковий альтернативний рецепт Шаурми №2 (Наприклад, швидкий лінійний крафт: Курка + Кетчуп + Папір)
    private void registerShawarmaRecipe2() {
        ItemStack shawarma = new ItemStack(Material.COOKED_CHICKEN);
        ItemMeta meta = shawarma.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Соковита Шаурма");
            meta.setCustomModelData(1002);
            shawarma.setItemMeta(meta);
        }

        NamespacedKey key = new NamespacedKey(this, "shawarma_2");
        ShapedRecipe recipe = new ShapedRecipe(key, shawarma);
        recipe.shape(
            "CKP",
            "   ",
            "   "
        );

        recipe.setIngredient('C', Material.COOKED_CHICKEN);
        recipe.setIngredient('K', Material.POTION);
        recipe.setIngredient('P', Material.PAPER);

        Bukkit.addRecipe(recipe);
    }

    // 4. Відновлення 5 єдиниць голоду при з'їданні Шаурми
    @EventHandler
    public void onEatShawarma(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (item.getType() == Material.COOKED_CHICKEN && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 1002) {
                Player player = event.getPlayer();

                int newFoodLevel = Math.min(20, player.getFoodLevel() + 5);
                player.setFoodLevel(newFoodLevel);
                player.setSaturation(Math.min((float) newFoodLevel, player.getSaturation() + 6.0f));
            }
        }
    }
}
