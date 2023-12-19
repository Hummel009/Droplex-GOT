package got.common.network;

import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import got.common.fellowship.GOTFellowship;
import got.common.fellowship.GOTFellowshipClient;
import got.common.fellowship.GOTFellowshipData;
import io.netty.buffer.ByteBuf;

public abstract class GOTPacketFellowshipDo implements IMessage {
	public UUID fellowshipID;

	public GOTPacketFellowshipDo() {
	}

	public GOTPacketFellowshipDo(GOTFellowshipClient fsClient) {
		fellowshipID = fsClient.getFellowshipID();
	}

	@Override
	public void fromBytes(ByteBuf data) {
		fellowshipID = new UUID(data.readLong(), data.readLong());
	}

	protected GOTFellowship getActiveFellowship() {
		return GOTFellowshipData.getActiveFellowship(fellowshipID);
	}

	protected GOTFellowship getActiveOrDisbandedFellowship() {
		return GOTFellowshipData.getFellowship(fellowshipID);
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeLong(fellowshipID.getMostSignificantBits());
		data.writeLong(fellowshipID.getLeastSignificantBits());
	}
}
