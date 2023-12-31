package got.common.entity.westeros.ironborn;

import got.common.entity.other.GOTBannerBearer;
import got.common.item.other.GOTItemBanner;
import net.minecraft.world.World;

public class GOTEntityIronbornBannerBearer extends GOTEntityIronbornSoldier implements GOTBannerBearer {
	public GOTEntityIronbornBannerBearer(World world) {
		super(world);
		canBeMarried = false;
	}

	@Override
	public GOTItemBanner.BannerType getBannerType() {
		return GOTItemBanner.BannerType.GREYJOY;
	}
}
