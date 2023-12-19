package got.common.item.weapon;

import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GOTItemVexHammer extends GOTItemSword {
	public GOTItemVexHammer(Item.ToolMaterial material) {
		super(material);
		gotWeaponDamage = 9.0f;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack itemstack) {
		return EnumAction.none;
	}
}
