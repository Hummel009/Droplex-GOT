package got.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import got.GOT;
import got.common.GOTLevelData;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.EnumDifficulty;

public class GOTPacketLogin implements IMessage {
    public int swordPortalX;
    public int swordPortalY;
    public int swordPortalZ;
    public int ftCooldownMax;
    public int ftCooldownMin;
    public EnumDifficulty difficulty;
    public boolean difficultyLocked;
    public boolean feastMode;
    public boolean fellowshipCreation;
    public boolean enchanting;
    public boolean enchantingGOT;
    public boolean strictFactionTitleRequirements;
    public boolean alignmentZones;
    public int fellowshipMaxSize;
    public int customWaypointMinY;
    public boolean conquestDecay;

    @Override
    public void fromBytes(ByteBuf data) {
        this.swordPortalX = data.readInt();
        this.swordPortalY = data.readInt();
        this.swordPortalZ = data.readInt();
        this.ftCooldownMax = data.readInt();
        this.ftCooldownMin = data.readInt();
        byte diff = data.readByte();
        this.difficulty = diff >= 0 ? EnumDifficulty.getDifficultyEnum(diff) : null;
        this.difficultyLocked = data.readBoolean();
        this.feastMode = data.readBoolean();
        this.fellowshipCreation = data.readBoolean();
        this.enchanting = data.readBoolean();
        this.enchantingGOT = data.readBoolean();
        this.strictFactionTitleRequirements = data.readBoolean();
        this.alignmentZones = data.readBoolean();
        this.fellowshipMaxSize = data.readInt();
        this.conquestDecay = data.readBoolean();
        this.customWaypointMinY = data.readInt();
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeInt(this.fellowshipMaxSize);
        data.writeInt(this.swordPortalX);
        data.writeInt(this.swordPortalY);
        data.writeInt(this.swordPortalZ);
        data.writeInt(this.ftCooldownMax);
        data.writeInt(this.ftCooldownMin);
        int diff = this.difficulty == null ? -1 : this.difficulty.getDifficultyId();
        data.writeByte(diff);
        data.writeBoolean(this.difficultyLocked);
        data.writeBoolean(this.feastMode);
        data.writeBoolean(this.fellowshipCreation);
        data.writeBoolean(this.enchanting);
        data.writeBoolean(this.enchantingGOT);
        data.writeBoolean(this.strictFactionTitleRequirements);
        data.writeBoolean(this.alignmentZones);
        data.writeBoolean(this.conquestDecay);
        data.writeInt(this.customWaypointMinY);
    }

    public static class Handler implements IMessageHandler<GOTPacketLogin, IMessage> {
        @Override
        public IMessage onMessage(GOTPacketLogin packet, MessageContext context) {
            if (!GOT.proxy.isSingleplayer()) {
                GOTLevelData.destroyAllPlayerData();
            }
            GOTLevelData.gameOfThronesPortalX = packet.swordPortalX;
            GOTLevelData.gameOfThronesPortalY = packet.swordPortalY;
            GOTLevelData.gameOfThronesPortalZ = packet.swordPortalZ;
            GOTLevelData.setWaypointCooldown(packet.ftCooldownMax, packet.ftCooldownMin);
            GOTLevelData.clientside_thisServer_fellowshipMaxSize = packet.fellowshipMaxSize;
            EnumDifficulty diff = packet.difficulty;
            if (diff != null) {
                GOTLevelData.setSavedDifficulty(diff);
                GOT.proxy.setClientDifficulty(diff);
            } else {
                GOTLevelData.setSavedDifficulty(null);
            }
            GOTLevelData.setDifficultyLocked(packet.difficultyLocked);
            GOTLevelData.clientside_thisServer_feastMode = packet.feastMode;
            GOTLevelData.clientside_thisServer_fellowshipCreation = packet.fellowshipCreation;
            GOTLevelData.clientside_thisServer_enchanting = packet.enchanting;
            GOTLevelData.clientside_thisServer_enchantingGOT = packet.enchantingGOT;
            GOTLevelData.clientside_thisServer_strictFactionTitleRequirements = packet.strictFactionTitleRequirements;
            return null;
        }
    }

}
