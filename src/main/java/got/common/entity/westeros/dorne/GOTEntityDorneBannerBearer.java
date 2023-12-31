package got.common.entity.westeros.dorne;

import got.common.entity.other.GOTBannerBearer;
import got.common.item.other.GOTItemBanner;
import net.minecraft.world.World;

public class GOTEntityDorneBannerBearer extends GOTEntityDorneSoldier implements GOTBannerBearer {
	public GOTEntityDorneBannerBearer(World world) {
		super(world);
		canBeMarried = false;
	}

	@Override
	public GOTItemBanner.BannerType getBannerType() {
		return GOTItemBanner.BannerType.MARTELL;
	}
}
