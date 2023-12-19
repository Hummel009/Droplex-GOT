package got.common.entity.dragon;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class GOTDragonFireball extends EntityLargeFireball {
    private int field_145795_e = -1;
    private int field_145793_f = -1;
    private int field_145794_g = -1;
    private Block field_145796_h;
    private boolean inGround;
    public EntityLivingBase shootingEntity;
    private int ticksAlive;
    private int ticksInAir;
    private EntityPlayer player;

    public GOTDragonFireball(World world) {
        super(world);
    }

    @SideOnly(Side.CLIENT)
    public GOTDragonFireball(World world, double posX, double posY, double posZ, double accelerationX, double accelerationY, double accelerationZ) {
        super(world, posX, posY, posZ, accelerationX, accelerationY, accelerationZ);
        this.setSize(1.0F, 1.0F);
        this.setLocationAndAngles(posX, posY, posZ, this.rotationYaw, this.rotationPitch);
        this.setPosition(posX, posY, posZ);
        double d6 = MathHelper.sqrt_double(accelerationX * accelerationX + accelerationY * accelerationY + accelerationZ * accelerationZ);
        this.accelerationX = accelerationX / d6 * 0.1D;
        this.accelerationY = accelerationY / d6 * 0.1D;
        this.accelerationZ = accelerationZ / d6 * 0.1D;
    }

    public GOTDragonFireball(World world, EntityLivingBase entity, double posX, double posY, double posZ) {
        super(world, entity, posX, posY, posZ);
        this.shootingEntity = entity;
        this.setSize(1.0F, 1.0F);
        this.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        this.motionX = this.motionY = this.motionZ = 0.0D;
        posX += this.rand.nextGaussian() * 0.4D;
        posY += this.rand.nextGaussian() * 0.4D;
        posZ += this.rand.nextGaussian() * 0.4D;
        double d3 = MathHelper.sqrt_double(posX * posX + posY * posY + posZ * posZ);
        this.accelerationX = posX / d3 * 0.1D;
        this.accelerationY = posY / d3 * 0.1D;
        this.accelerationZ = posZ / d3 * 0.1D;
    }

    @Override
    protected void onImpact(MovingObjectPosition rayTrace) {
        if(rayTrace.entityHit != null && rayTrace.entityHit instanceof GOTEntityDragon) return;
        if (!this.worldObj.isRemote) {
            if (rayTrace.entityHit == null || (!(rayTrace.entityHit instanceof GOTEntityDragon) && (this.player == null || !rayTrace.entityHit.equals(this.player)))) {
                if (rayTrace.entityHit != null) {
                    rayTrace.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), 6.0F);
                    //                    this.worldObj.setBlock((int)rayTrace.entityHit.posX, (int)rayTrace.entityHit.posY - 1, (int)rayTrace.entityHit.posZ, Blocks.fire);
                    //                    List<EntityLivingBase> list = this.worldObj.selectEntitiesWithinAABB(EntityLivingBase.class, rayTrace.entityHit.boundingBox.expand(2.0, 0, 2.0), new IEntitySelector() {
                    //
                    //                        @Override
                    //                        public boolean isEntityApplicable(Entity entity) {
                    //                            return entity != rayTrace.entityHit && !(entity instanceof GOTEntityDragon);
                    //                        }
                    //
                    //                    });
                    //                    for(EntityLivingBase living : list) {
                    //                        living.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), 6.0F);
                    //                    }
                }
                this.worldObj.newExplosion(null, this.posX, this.posY, this.posZ, this.field_92057_e, true, false);
                this.setDead();
            }
        }
    }

    @Override
    public void onUpdate() {
        if (!this.worldObj.isRemote && (this.shootingEntity != null && this.shootingEntity.isDead || !this.worldObj.blockExists((int)this.posX, (int)this.posY, (int)this.posZ))) {
            this.setDead();
        } else {
            super.onUpdate();
            this.setFire(1);

            if (this.inGround) {
                if (this.worldObj.getBlock(this.field_145795_e, this.field_145793_f, this.field_145794_g) == this.field_145796_h) {
                    ++this.ticksAlive;

                    if (this.ticksAlive == 600) {
                        this.setDead();
                    }

                    return;
                }

                this.inGround = false;
                this.motionX *= this.rand.nextFloat() * 0.2F;
                this.motionY *= this.rand.nextFloat() * 0.2F;
                this.motionZ *= this.rand.nextFloat() * 0.2F;
                this.ticksAlive = 0;
                this.ticksInAir = 0;
            } else {
                ++this.ticksInAir;
            }

            Vec3 vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            Vec3 vec31 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec3, vec31);
            vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            vec31 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null) {
                vec31 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;

            for (Object element : list) {
                Entity entity1 = (Entity)element;

                if (entity1.canBeCollidedWith() && (!entity1.isEntityEqual(this.shootingEntity) || !(entity1 instanceof GOTEntityDragon) || this.ticksInAir >= 25)) {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.boundingBox.expand(f, f, f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

                    if (movingobjectposition1 != null) {
                        double d1 = vec3.distanceTo(movingobjectposition1.hitVec);

                        if (d1 < d0 || d0 == 0.0D) {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null) {
                this.onImpact(movingobjectposition);
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;

            for (this.rotationPitch = (float)(Math.atan2(f1, this.motionY) * 180.0D / Math.PI) - 90.0F; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
                ;
            }

            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
                this.prevRotationYaw -= 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
            float f2 = this.getMotionFactor();

            if (this.isInWater()) {
                for (int j = 0; j < 4; ++j) {
                    float f3 = 0.25F;
                    this.worldObj.spawnParticle("bubble", this.posX - this.motionX * f3, this.posY - this.motionY * f3, this.posZ - this.motionZ * f3, this.motionX, this.motionY, this.motionZ);
                }

                f2 = 0.8F;
            }

            this.motionX += this.accelerationX;
            this.motionY += this.accelerationY;
            this.motionZ += this.accelerationZ;
            this.motionX *= f2;
            this.motionY *= f2;
            this.motionZ *= f2;
            this.worldObj.spawnParticle("smoke", this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
            this.setPosition(this.posX, this.posY, this.posZ);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound p_70014_1_) {
        p_70014_1_.setShort("xTile", (short)this.field_145795_e);
        p_70014_1_.setShort("yTile", (short)this.field_145793_f);
        p_70014_1_.setShort("zTile", (short)this.field_145794_g);
        p_70014_1_.setByte("inTile", (byte)Block.getIdFromBlock(this.field_145796_h));
        p_70014_1_.setByte("inGround", (byte)(this.inGround ? 1 : 0));
        p_70014_1_.setTag("direction", this.newDoubleNBTList(new double[] {this.motionX, this.motionY, this.motionZ}));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound p_70037_1_) {
        this.field_145795_e = p_70037_1_.getShort("xTile");
        this.field_145793_f = p_70037_1_.getShort("yTile");
        this.field_145794_g = p_70037_1_.getShort("zTile");
        this.field_145796_h = Block.getBlockById(p_70037_1_.getByte("inTile") & 255);
        this.inGround = p_70037_1_.getByte("inGround") == 1;

        if (p_70037_1_.hasKey("direction", 9)) {
            NBTTagList nbttaglist = p_70037_1_.getTagList("direction", 6);
            this.motionX = nbttaglist.func_150309_d(0);
            this.motionY = nbttaglist.func_150309_d(1);
            this.motionZ = nbttaglist.func_150309_d(2);
        } else {
            this.setDead();
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float f) {
        if(source.getEntity() instanceof GOTDragonFireball) return false;
        return super.attackEntityFrom(source, f);
    }

    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }
}