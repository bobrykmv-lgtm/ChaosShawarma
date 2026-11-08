package com.example.chaosshawarma;

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

public final class ChaosShawarmaPlugin extends JavaPlugin implements Listener {

    private static final String SHAWARMA_NAME = "§6§lШаурма";

    @Override
    public void onEnable() {
        // Регистрируем крафт
        registerShawarmaRecipe();
        
        // Регистрируем события (чтобы шаурму можно было есть)
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("ChaosShawarma успешно активирован!");
    }

    private void registerShawarmaRecipe() {
        ItemStack shawarma = createShawarmaItem();

        NamespacedKey key = new NamespacedKey(this, "craft_shawarma");
        ShapedRecipe recipe = new ShapedRecipe(key, shawarma);

        // Форма крафта в верстаке: Хлеб сверху, жареная курица посередине, стеклянная бутылочка снизу
        recipe.shape(
            " S ", 
            " C ", 
            " B "  
        );

        recipe.setIngredient('S', Material.BREAD);
        recipe.setIngredient('C', Material.COOKED_CHICKEN);
        recipe.setIngredient('B', Material.GLASS_BOTTLE);

        // Добавляем рецепт на сервер
        Bukkit.addRecipe(recipe);
    }

    // Метод для создания предмета шаурмы
    public static ItemStack createShawarmaItem() {
        ItemStack item = new ItemStack(Material.RABBIT_FOOT, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(SHAWARMA_NAME);
            meta.setLore(List.of(
                "§7Традиционная сочная шаурма",
                "§eВосстанавливает 5 единиц голода!"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    // Обработка поедания предмета
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        
        // Проверяем, что игрок ест именно нашу кроличью лапку с названием "Шаурма"
        if (item.getType() == Material.RABBIT_FOOT && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(SHAWARMA_NAME)) {
                Player player = event.getPlayer();
                
                // Восстанавливаем 5 единиц голода (2.5 куриные ножки)
                int currentFood = player.getFoodLevel();
                player.setFoodLevel(Math.min(currentFood + 5, 20));
                
                // Добавляем насыщение
                float currentSaturation = player.getSaturation();
                player.setSaturation(Math.min(currentSaturation + 6.0f, (float) player.getFoodLevel()));
                
                // Проигрываем звук отрыжки/еды и убираем 1 предмет вручную (так как кроличья лапка ванильно не естся)
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                
                // Уменьшаем количество предметов в руке на 1
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    // Если была одна лапка — заменяем её на пустую стеклянную бутылочку (вместо съеденной)
                    event.setItem(new ItemStack(Material.GLASS_BOTTLE));
                    return;
                }
                
                // Даем игроку пустую бутылочку в инвентарь, если лапок было несколько
                player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
            }
        }
    }
}
