package net.lee.fnafmod.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public final class ArmorRandomizer {
    private ArmorRandomizer() {}

    public static Item pickArmorForSlot(RandomSource rnd, EquipmentSlot slot) {
        int mat = rnd.nextInt(5); // 0..4 => leather, iron, gold, diamond, netherite
        return pickArmorForSlot(mat, slot);
    }

    public static Item pickArmorForSlot(int materialIndex, EquipmentSlot slot) {
        switch (materialIndex) {
            case 0: // leather
                switch (slot) {
                    case HEAD: return Items.LEATHER_HELMET;
                    case CHEST: return Items.LEATHER_CHESTPLATE;
                    case LEGS: return Items.LEATHER_LEGGINGS;
                    case FEET: return Items.LEATHER_BOOTS;
                }
            case 1: // iron
                switch (slot) {
                    case HEAD: return Items.IRON_HELMET;
                    case CHEST: return Items.IRON_CHESTPLATE;
                    case LEGS: return Items.IRON_LEGGINGS;
                    case FEET: return Items.IRON_BOOTS;
                }
            case 2: // gold
                switch (slot) {
                    case HEAD: return Items.GOLDEN_HELMET;
                    case CHEST: return Items.GOLDEN_CHESTPLATE;
                    case LEGS: return Items.GOLDEN_LEGGINGS;
                    case FEET: return Items.GOLDEN_BOOTS;
                }
            case 3: // diamond
                switch (slot) {
                    case HEAD: return Items.DIAMOND_HELMET;
                    case CHEST: return Items.DIAMOND_CHESTPLATE;
                    case LEGS: return Items.DIAMOND_LEGGINGS;
                    case FEET: return Items.DIAMOND_BOOTS;
                }
            case 4: // netherite
                switch (slot) {
                    case HEAD: return Items.NETHERITE_HELMET;
                    case CHEST: return Items.NETHERITE_CHESTPLATE;
                    case LEGS: return Items.NETHERITE_LEGGINGS;
                    case FEET: return Items.NETHERITE_BOOTS;
                }
            default:
                return Items.AIR;
        }
    }

    public static void equipRandomArmor(Mob mob, RandomSource rnd, boolean uniformSet) {
        EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        if (uniformSet) {
            int mat = rnd.nextInt(5);
            for (EquipmentSlot s : slots) {
                Item item = pickArmorForSlot(mat, s);
                if (item != Items.AIR) mob.setItemSlot(s, new ItemStack(item));
            }
        } else {
            for (EquipmentSlot s : slots) {
                Item item = pickArmorForSlot(rnd, s);
                if (item != Items.AIR) mob.setItemSlot(s, new ItemStack(item));
            }
        }
    }
}
