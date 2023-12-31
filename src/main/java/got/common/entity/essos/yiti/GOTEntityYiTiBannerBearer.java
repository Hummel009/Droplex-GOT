package got.common.entity.essos.yiti;

import got.common.entity.other.GOTBannerBearer;
import got.common.item.other.GOTItemBanner;
import net.minecraft.world.World;

public class GOTEntityYiTiBannerBearer extends GOTEntityYiTiSamurai implements GOTBannerBearer {
	public GOTEntityYiTiBannerBearer(World world) {
		super(world);
		canBeMarried = false;
	}

	@Override
	public GOTItemBanner.BannerType getBannerType() {
		return GOTItemBanner.BannerType.YITI;
	}
}
