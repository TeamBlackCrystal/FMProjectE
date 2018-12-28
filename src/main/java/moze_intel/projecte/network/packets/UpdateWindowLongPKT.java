package moze_intel.projecte.network.packets;

import moze_intel.projecte.gameObjs.container.LongContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// Version of SPacketWindowProperty that supports long values
public class UpdateWindowLongPKT implements IMessage
{
    private final short windowId;
    private final short propId;
    private final long propVal;

    public UpdateWindowLongPKT(short windowId, short propId, long propVal)
    {
        this.windowId = windowId;
        this.propId = propId;
        this.propVal = propVal;
    }

    public static void encode(UpdateWindowLongPKT msg, PacketBuffer buf)
    {
        buf.writeShort(msg.windowId);
        buf.writeShort(msg.propId);
        buf.writeLong(msg.propVal);
    }

    public static UpdateWindowLongPKT decode(PacketBuffer buf)
    {
        return new UpdateWindowLongPKT(buf.readShort(), buf.readShort(), buf.readLong());
    }

    public static class Handler
    {
        public static void handle(final UpdateWindowLongPKT msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                EntityPlayer player = Minecraft.getInstance().player;
                if (player.openContainer != null && player.openContainer.windowId == msg.windowId) {
                    //It should always be a LongContainer if it is this type of packet, if not fallback to normal update
                    if (player.openContainer instanceof LongContainer)
                    {
                        ((LongContainer) player.openContainer).updateProgressBarLong(msg.propId, msg.propVal);
                    }
                    else
                    {
                        player.openContainer.updateProgressBar(msg.propId, (int) msg.propVal);
                    }
                }
            });
        }
    }
}