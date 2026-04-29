package me.dw1e.axiom.misc.util;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class PlayerUtil {

    public static int getAmplifier(Player player, PotionEffectType effectType) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(effectType)) {
                return effect.getAmplifier() + 1;
            }
        }
        return 0;
    }

}
