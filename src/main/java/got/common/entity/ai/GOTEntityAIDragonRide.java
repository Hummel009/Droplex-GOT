package got.common.entity.ai;

import java.util.BitSet;

import got.common.entity.dragon.GOTEntityDragon;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

public abstract class GOTEntityAIDragonRide extends EntityAIBase {

    public GOTEntityDragon dragon;
    public EntityPlayer rider;
    protected int fireballCount = 6;
    protected int timeInterFire = 6 * 20 + 1;

    public GOTEntityAIDragonRide(GOTEntityDragon dragon) {
        this.dragon = dragon;
        this.setMutexBits(0xffffffff);
    }

    public boolean getControlFlag(int index) {
        BitSet controlFlags = this.dragon.getControlFlags();
        return controlFlags == null ? false : controlFlags.get(index);
    }

    public boolean isShootFireball() {
        return this.getControlFlag(2);
    }

    public boolean isFlyDown() {
        return this.getControlFlag(1);
    }

    public boolean isFlyUp() {
        return this.getControlFlag(0);
    }

    @Override
    public boolean shouldExecute() {
        this.rider = this.dragon.getRidingPlayer();
        return this.rider != null;
    }
}
