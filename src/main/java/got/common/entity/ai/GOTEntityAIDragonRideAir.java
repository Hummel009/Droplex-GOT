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

public class GOTEntityAIDragonRideAir extends GOTEntityAIDragonRide {

    public GOTEntityAIDragonRideAir(GOTEntityDragon dragon) {
        super(dragon);
    }

    @Override
    public void updateTask() {
        super.updateTask();

        double dist = 100;

        if (GOTEntityDragon.hasEquipped(this.rider, Items.carrot_on_a_stick)) {
            Vec3 wp = this.rider.getLookVec();

            wp.xCoord *= dist;
            wp.yCoord *= dist;
            wp.zCoord *= dist;

            wp.xCoord += this.dragon.posX;
            wp.yCoord += this.dragon.posY;
            wp.zCoord += this.dragon.posZ;

            this.dragon.getWaypoint().setVector(wp);

            this.dragon.setMoveSpeedAirHoriz(1);
            this.dragon.setMoveSpeedAirVert(0);
        } else {
            Vec3 wp = this.dragon.getLookVec();

            wp.xCoord *= dist;
            wp.yCoord *= dist;
            wp.zCoord *= dist;

            wp.xCoord += this.dragon.posX;
            wp.yCoord += this.dragon.posY;
            wp.zCoord += this.dragon.posZ;

            this.dragon.getWaypoint().setVector(wp);

            double speedAir = 0;

            if (this.rider.moveForward != 0) {
                speedAir = 1;

                if (this.rider.moveForward < 0) {
                    speedAir *= 0.5;
                }

                speedAir *= this.rider.moveForward;
            }

            this.dragon.setMoveSpeedAirHoriz(speedAir);

            if (this.rider.moveStrafing != 0) {
                this.dragon.rotationYaw -= this.rider.moveStrafing * 6;
            }

            double verticalSpeed = 0;

            if (this.isFlyUp()) {
                verticalSpeed = 0.5f;
            } else if (this.isFlyDown()) {
                verticalSpeed = -0.5f;
            }
            GOTPlayerData playerData = GOTLevelData.getData(this.rider);

            if(this.isShootFireball()) {
                if((this.timeInterFire / 20) == 0 || (this.timeInterFire / 20) == 1 || (this.timeInterFire / 20) == 2
                        || (this.timeInterFire / 20) == 3 || (this.timeInterFire / 20) == 4 || (this.timeInterFire / 20) == 5
                        || (this.timeInterFire / 20) == 6) {
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
                this.timeInterFire--;
            }

            this.dragon.setMoveSpeedAirVert(verticalSpeed);
        }
    }
}
