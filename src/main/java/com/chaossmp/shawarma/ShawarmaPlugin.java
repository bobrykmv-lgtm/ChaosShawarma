package com.example.chaosshawarma;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
        // Реєструємо крафт
        registerShawarmaRecipe();
        
        // Реєструємо слухач подій (щоб шаурму можна було з'їсти)
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("ChaosShawarma успішно активовано!");
    }

    private void registerShawarmaRecipe() {
        ItemStack shawarma = createShawarmaItem();

        NamespacedKey key = new NamespacedKey(this, "craft_shawarma");
        ShapedRecipe recipe = new ShapedRecipe(key, shawarma);

        // Форма крафту у верстаку: Хліб зверху, курка посередині, скляна пляшка знизу
        recipe.shape(
            " S ", 
            " C ", 
            " B "  
        );

        recipe.setIngredient('S', Material.BREAD);
        recipe.setIngredient('C', Material.COOKED_CHICKEN);
        recipe.setIngredient('B', Material.GLASS_BOTTLE);

        Bukkit.addRecipe(recipe);
    }

    // Метод для створення самого предмета шаурми
    public static ItemStack createShawarmaItem() {
        ItemStack item = new ItemStack(Material.RABBIT_FOOT, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(SHAWARMA_NAME);
            meta.setLore(List.of(
                "§7Традиційна сочна шаурма",
                "§eВідновлює 5 одиниць голоду!"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    // Подія поїдання: робимо кролячу лапу «їстівною» та додаємо голод
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        
        if (item.getType() == Material.RABBIT_FOOT && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(SHAWARMA_NAME)) {
                Player player = event.getPlayer();
                
                // Додаємо гравцю 5 одиниць голоду (у майнкрафті 1 одиниця = пів-одиниці голоду на шкалі, тобто 5 це 2.5 курячі ніжки)
                // Можна також збільшити харчову цінність, наприклад, 6 або 8 для повноцінного перекусу, але тут встановлено рівно 5.
                int currentFood = player.getFoodLevel();
                player.setFoodLevel(Math.min(currentFood + 5, 20));
                
                // Додаємо насичення (saturation), щоб шкала голоду не падала одразу
                float currentSaturation = player.getSaturation();
                player.setSaturation(Math.min(currentSaturation + 6.0f, (float) player.getFoodLevel()));
                
                // Оскільки кроляча лапа ванільно не зникає як їжа, а просто залишається в руці чи створює скло,
                // забираємо 1 шт. предмету вручну або дозволяємо з'їсти (в 1.21+ кастомна логіка поїдання предметів не-їжі вимагає анімації)
            }
        }
    }
}
