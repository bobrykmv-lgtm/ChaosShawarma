package com.chaossmp.shawarma;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.List;

public final class ShawarmaPlugin extends JavaPlugin implements Listener {

    private static final String SHAWARMA_NAME = "§6§lШаурма";
    private static final String KETCHUP_NAME = "§c§lКетчуп";

    @Override
    public void onEnable() {
        registerKetchupRecipe();
        registerShawarmaRecipe();

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("ChaosShawarma успешно активирован!");
    }

    // 1. Рецепт Кетчупа: Зелье исцеления как основа, Сахар сверху, Редстоун снизу
    private void registerKetchupRecipe() {
        ItemStack ketchup = createKetchupItem();

        NamespacedKey key = new NamespacedKey(this, "craft_ketchup");
        ShapedRecipe recipe = new ShapedRecipe(key, ketchup);

        recipe.shape(
            " S ",
            " P ",
            " R "
        );

        ItemStack healingPotion = new ItemStack(Material.POTION, 1);
        PotionMeta potionMeta = (PotionMeta) healingPotion.getItemMeta();
        if (potionMeta != null) {
            potionMeta.setBasePotionType(PotionType.HEALING);
            healingPotion.setItemMeta(potionMeta);
        }

        recipe.setIngredient('S', Material.SUGAR);
        recipe.setIngredient('P', new RecipeChoice.ExactChoice(healingPotion));
        recipe.setIngredient('R', Material.REDSTONE);

        Bukkit.addRecipe(recipe);
    }

    // 2. Рецепт Шаурмы: 3 хлеба сверху/снизу, Кетчуп по центру, Бумага по бокам
    private void registerShawarmaRecipe() {
        ItemStack shawarma = createShawarmaItem();

        NamespacedKey key = new NamespacedKey(this, "craft_shawarma");
        ShapedRecipe recipe = new ShapedRecipe(key, shawarma);

        recipe.shape(
            "HHH",
            "PKP",
            "HHH"
        );

        recipe.setIngredient('H', Material.BREAD);
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('K', new RecipeChoice.ExactChoice(createKetchupItem()));

        Bukkit.addRecipe(recipe);
    }

    // Создание предмета: Кетчуп (под текстуру glass_bottle в ресурс-паке)
    public static ItemStack createKetchupItem() {
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(KETCHUP_NAME);
            meta.setLore(List.of(
                "§7Вкусный томатный соус",
                "§eПКМ: восстановить здоровье или скрафтить шаурму"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    // Создание предмета: Шаурма (под текстуру cooked_beef в ресурс-паке)
    public static ItemStack createShawarmaItem() {
        ItemStack item = new ItemStack(Material.COOKED_BEEF, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(SHAWARMA_NAME);
            meta.setLore(List.of(
                "§7Традиционная сочная шаурма с кетчупом",
                "§eВосстанавливает 5 единиц голода!"
            ));
            item.setItemMeta(meta);
        }
        return item;
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

        String displayName = meta.getDisplayName();

        // Поедание шаурмы
        if (item.getType() == Material.COOKED_BEEF && displayName.equals(SHAWARMA_NAME)) {
            if (player.getFoodLevel() < 20) {
                event.setCancelled(true);

                int currentFood = player.getFoodLevel();
                player.setFoodLevel(Math.min(currentFood + 5, 20));

                float currentSaturation = player.getSaturation();
                player.setSaturation(Math.min(currentSaturation + 6.0f, (float) player.getFoodLevel()));

                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);

                consumeOneItem(player, item);
            } else {
                player.sendMessage("§cВы не голодны!");
            }
            return;
        }

        // Использование кетчупа ПКМ (быстрый томатный буст)
        if (item.getType() == Material.GLASS_BOTTLE && displayName.equals(KETCHUP_NAME)) {
            event.setCancelled(true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0, true, false));
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
