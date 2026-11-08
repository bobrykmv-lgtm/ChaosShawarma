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
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public final class ShawarmaPlugin extends JavaPlugin implements Listener {

    private static final String SHAWARMA_NAME = "§6§lШаурма";
    private static final String KETCHUP_NAME = "§c§lКетчуп";

    @Override
    public void onEnable() {
        // Регистрируем рецепты
        registerKetchupRecipe();
        registerShawarmaRecipe();
        
        // Регистрируем события
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("ChaosShawarma успешно активирован!");
    }

    // 1. Рецепт Кетчупа: Зілля сцілення як основа, Сахар сверху, Редстоун снизу
    private void registerKetchupRecipe() {
        ItemStack ketchup = createKetchupItem();

        NamespacedKey key = new NamespacedKey(this, "craft_ketchup");
        ShapedRecipe recipe = new ShapedRecipe(key, ketchup);

        recipe.shape(
            " S ", 
            " P ", 
            " R "  
        );

        recipe.setIngredient('S', Material.SUGAR);
        // В качестве бутылочки используем обычное зелье исцеления (или неловкое/водное, но здесь берем зелье)
        recipe.setIngredient('P', Material.POTION);
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
        recipe.setIngredient('K', new org.bukkit.inventory.RecipeChoice.ExactChoice(createKetchupItem()));

        Bukkit.addRecipe(recipe);
    }

    // Создание предмета: Кетчуп (на базе зілля сцілення)
    public static ItemStack createKetchupItem() {
        ItemStack item = new ItemStack(Material.POTION, 1);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(KETCHUP_NAME);
            // Додаємо ефект вилікування, щоб зелля відповідало назві
            meta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0), true);
            meta.setLore(List.of(
                "§7Вкусный томатный соус",
                "§eИспользуется для крафта шаурмы"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    // Создание предмета: Шаурма
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

    // Обработка клика ПКМ для поедания не-съедобного предмета (кроличьей лапки)
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Проверяем, что игрок кликнул ПКМ в воздух или по блоку
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            
            // Если в руке наша шаурма
            if (item.getType() == Material.RABBIT_FOOT && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(SHAWARMA_NAME)) {
                    
                    // Проверяем, не полон ли у игрока голод (опционально, но лучше давать есть только если меньше 20)
                    if (player.getFoodLevel() < 20) {
                        event.setCancelled(true); // Отменяем стандартное действие, чтобы лапа не ставилась никуда
                        
                        // Восстанавливаем 5 единиц голода и сытость
                        int currentFood = player.getFoodLevel();
                        player.setFoodLevel(Math.min(currentFood + 5, 20));
                        
                        float currentSaturation = player.getSaturation();
                        player.setSaturation(Math.min(currentSaturation + 6.0f, (float) player.getFoodLevel()));
                        
                        // Звуки кушанья и отрыжки
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                        
                        // Уменьшаем количество шаурмы в руке на 1
                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        }
                        
                        // Даем пустую стеклянную бутылочку за съеденный кетчуп/упаковку
                        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
                        // Если инвентарь полон, дропаем бутылочку на землю
                        for (ItemStack drop : leftover.values()) {
                            player.getWorld().dropItem(player.getLocation(), drop);
                        }
                    } else {
                        player.sendMessage("§cВы не голодны!");
                    }
                }
            }
        }
    }
}
