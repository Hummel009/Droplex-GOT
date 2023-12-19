package got.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import got.GOT;
import got.common.GOTLevelData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class GOTPacketFireballTimer implements IMessage {
    public int timer;

    public GOTPacketFireballTimer() {
    }

    public GOTPacketFireballTimer(int i) {
        this.timer = i;
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.timer = data.readInt();
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeInt(this.timer);
    }

    public static class Handler implements IMessageHandler<GOTPacketFireballTimer, IMessage> {
        @Override
        public IMessage onMessage(GOTPacketFireballTimer packet, MessageContext context) {
            EntityPlayer entityplayer = GOT.proxy.getClientPlayer();
            GOTLevelData.getData(entityplayer).setDragonFireballCooldown(packet.timer);
            return null;
        }
    }

}