package com.chaossmp.shawarma;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ShawarmaPlugin extends JavaPlugin implements Listener {

    private static final String SHAWARMA_NAME = "§6§lШаурма";
    private static final String KETCHUP_NAME = "§c§lКетчуп";

    @Override
    public void onEnable() {
        // Регистрируем оба рецепта
        registerKetchupRecipe();
        registerShawarmaRecipe();
        
        // Регистрируем события для поедания
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("ChaosShawarma (Шаурма + Кетчуп) успешно активирован!");
    }

    // 1. Рецепт Кетчупа: Пляшка в центре, Сахар и Редстоун по бокам (или сверху/снизу)
    private void registerKetchupRecipe() {
        ItemStack ketchup = createKetchupItem();

        NamespacedKey key = new NamespacedKey(this, "craft_ketchup");
        ShapedRecipe recipe = new ShapedRecipe(key, ketchup);

        recipe.shape(
            " S ", 
            " B "  // Здесь B - стеклянная бутылочка, S - сахар, R - редстоун (упростим под 3х3)
        );
        // Сделаем красивую форму крафта для кетчупа (например: Сахар, Бутылочка, Редстоун в ряд)
        // Или вертикально: Сахар сверху, Бутылочка по центру, Редстоун снизу:
        recipe.shape(
            " S ", 
            " B ", 
            " R "  
        );

        recipe.setIngredient('S', Material.SUGAR);
        recipe.setIngredient('B', Material.GLASS_BOTTLE);
        recipe.setIngredient('R', Material.REDSTONE);

        Bukkit.addRecipe(recipe);
    }

    // 2. Рецепт Шаурмы: 3 хлеба сверху и снизу, Кетчуп по центру, Бумага слева и справа
    private void registerShawarmaRecipe() {
        ItemStack shawarma = createShawarmaItem();

        NamespacedKey key = new NamespacedKey(this, "craft_shawarma");
        ShapedRecipe recipe = new ShapedRecipe(key, shawarma);

        recipe.shape(
            "HHH", // 3 хлеба сверху
            "PKP", // Бумага слева и справа, Кетчуп посередине
            "HHH"  // 3 хлеба снизу
        );

        recipe.setIngredient('H', Material.BREAD);
        recipe.setIngredient('P', Material.PAPER);
        // В качестве ингредиента "Кетчуп" используем наш кастомный предмет (бутылочку с редстоуном и сахаром)
        // В Bukkit для точного рецепта кастомного предмета лучше использовать RecipeChoice:
        recipe.setIngredient('K', new org.bukkit.inventory.RecipeChoice.ExactChoice(createKetchupItem()));

        Bukkit.addRecipe(recipe);
    }

    // Создание предмета: Кетчуп
    public static ItemStack createKetchupItem() {
        ItemStack item = new ItemStack(Material.POTION, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(KETCHUP_NAME);
            meta.setLore(List.of(
                "§7Вкусный томатный соус",
                "§eИспользуется для крафта шаурмы"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    // Создание предмета: Шаурма (кроличья лапка)
    public static ItemStack createShawarmaItem() {
        ItemStack item = new ItemStack(Material.RABBIT_FOOT, 1);
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

    // Обработка поедания шаурмы
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        
        if (item.getType() == Material.RABBIT_FOOT && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(SHAWARMA_NAME)) {
                Player player = event.getPlayer();
                
                int currentFood = player.getFoodLevel();
                player.setFoodLevel(Math.min(currentFood + 5, 20));
                
                float currentSaturation = player.getSaturation();
                player.setSaturation(Math.min(currentSaturation + 6.0f, (float) player.getFoodLevel()));
                
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    event.setItem(new ItemStack(Material.GLASS_BOTTLE));
                    return;
                }
                
                player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
            }
        }
    }
}
