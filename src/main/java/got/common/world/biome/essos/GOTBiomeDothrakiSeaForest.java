package got.common.world.biome.essos;

import got.common.world.biome.variant.GOTBiomeVariant;

public class GOTBiomeDothrakiSeaForest extends GOTBiomeDothrakiSea {
	public GOTBiomeDothrakiSeaForest(int i, boolean major) {
		super(i, major);
		clearBiomeVariants();
		addBiomeVariant(GOTBiomeVariant.CLEARING, 0.2f);
		addBiomeVariant(GOTBiomeVariant.HILLS);
		decorator.treesPerChunk = 10;
		npcSpawnList.clear();
		invasionSpawns.clearInvasions();
	}

}