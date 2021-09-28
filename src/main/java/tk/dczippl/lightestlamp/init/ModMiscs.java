package tk.dczippl.lightestlamp.init;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import tk.dczippl.lightestlamp.potion.BrominePoison;

import java.awt.*;

import static tk.dczippl.lightestlamp.LightestLampsMod.MOD_ID;

public class ModMiscs {
	public static final StatusEffect BROMINE_POISON = new BrominePoison(StatusEffectType.HARMFUL,new Color(102,16,0).getRGB());
	//public static final DamageSource BROMINE = new DamageSource("bromine").setDamageBypassesArmor();

	public static final ItemGroup LAMPS_TAB = FabricItemGroupBuilder.build(new Identifier(MOD_ID,"lamps"),()->new ItemStack(ModBlocks.OMEGA_LAMP,15));
}