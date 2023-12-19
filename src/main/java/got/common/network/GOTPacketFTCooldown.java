package got.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import got.common.GOTLevelData;
import io.netty.buffer.ByteBuf;

public class GOTPacketFTCooldown implements IMessage {
    private int cooldownMax;
    private int cooldownMin;

    public GOTPacketFTCooldown() {
    }

    public GOTPacketFTCooldown(int max, int min) {
        this.cooldownMax = max;
        this.cooldownMin = min;
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeInt(this.cooldownMax);
        data.writeInt(this.cooldownMin);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.cooldownMax = data.readInt();
        this.cooldownMin = data.readInt();
    }

    public static class Handler implements IMessageHandler<GOTPacketFTCooldown, IMessage> {
        @Override
        public IMessage onMessage(GOTPacketFTCooldown packet, MessageContext context) {
            GOTLevelData.setWaypointCooldown(packet.cooldownMax, packet.cooldownMin);
            return null;
        }
    }
}