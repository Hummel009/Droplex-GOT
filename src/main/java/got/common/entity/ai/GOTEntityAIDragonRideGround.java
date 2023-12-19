package got.common.entity.ai;

import got.common.GOTConfig;
import got.common.GOTLevelData;
import got.common.GOTPlayerData;
import got.common.entity.dragon.GOTDragonFireball;
import got.common.entity.dragon.GOTEntityDragon;
import net.minecraft.init.Items;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;

public class GOTEntityAIDragonRideGround extends GOTEntityAIDragonRide {

    public static float PLAYER_SPEED = 0.98f;
    public double speed;

    public GOTEntityAIDragonRideGround(GOTEntityDragon dragon, double speed) {
        super(dragon);
        this.speed = speed;
    }

    @Override
    public void startExecuting() {
        this.dragon.getNavigator().clearPathEntity();
    }

    @Override
    public void updateTask() {
        super.updateTask();

        float speedX = this.rider.moveForward / PLAYER_SPEED;
        float speedY = this.rider.moveStrafing / PLAYER_SPEED;

        if (GOTEntityDragon.hasEquipped(this.rider, Items.carrot_on_a_stick)) {
            speedX = 1;
        }

        float speedPlayer = Math.max(Math.abs(speedX), Math.abs(speedY));

        Vec3 look = this.rider.getLookVec();
        float dir = Math.min(speedX, 0) * -1;
        dir += speedY / (speedX * 2 + (speedX < 0 ? -2 : 2));
        if (dir != 0) {
            look.rotateAroundY((float) Math.PI * dir);
        }

        if (speedPlayer > 0) {
            this.dragon.getMoveHelper().setMoveTo(this.dragon.posX + look.xCoord, this.dragon.posY, this.dragon.posZ + look.zCoord, this.speed * speedPlayer);
        }

        if (this.isFlyUp()) {
            this.dragon.liftOff();
        }
        GOTPlayerData playerData = GOTLevelData.getData(this.rider);

        if(this.isShootFireball()) {
            if(playerData.getDragonFireballTime() >= GOTConfig.getDragonFireballCooldown * 20) {
                Vec3 vecPlayer = this.rider.getLookVec();
                Vec3 vec3 = this.dragon.getLookVec()
                        //                        .addVector(vecPlayer.xCoord, vecPlayer.yCoord, vecPlayer.zCoord)
                        ;
                GOTDragonFireball ball = new GOTDragonFireball(this.dragon.worldObj, this.rider, vec3.xCoord, vec3.yCoord, vec3.zCoord);
                ball.setPlayer(this.rider);
                ball.field_92057_e = 2;
                double d8 = 4.0D;
                ball.posX = this.rider.posX + vecPlayer.xCoord * d8;
                ball.posY = this.rider.posY + this.rider.height / 2.0F + 0.5D;
                ball.posZ = this.rider.posZ + vecPlayer.zCoord * d8;
                ball.attackEntityFrom(DamageSource.causePlayerDamage(this.rider), 1.0F);
                //                this.dragon.worldObj.newExplosion((Entity)null, ball.posX, ball.posY, ball.posZ, (float)1, true, true);
                this.dragon.worldObj.spawnEntityInWorld(ball);
                this.fireballCount--;
                if(this.fireballCount == 0) {
                    playerData.setDragonFireballCooldown(0);
                    this.fireballCount = 6;
                }
            } else {
                this.rider.addChatMessage(new ChatComponentTranslation("got.chat.fireballDisabled", GOTConfig.getDragonFireballCooldown - Math.round(playerData.getDragonFireballTime() / 20)));
            }
        }
    }
}
