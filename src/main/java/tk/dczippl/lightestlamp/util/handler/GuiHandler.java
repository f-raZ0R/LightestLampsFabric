package tk.dczippl.lightestlamp.util.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.FMLPlayMessages;

public class GuiHandler
{
    /*public static GuiScreen getClientGuiElement(FMLPlayMessages.OpenContainer message)
    {
        PacketBuffer buf = message.getAdditionalData();
        BlockPos pos = buf.readBlockPos();
        Main.LOGGER.info("getClientGuiElement: " + message.getId() + " " + pos);

        World world = Minecraft.getInstance().world;
        PlayerEntity entityPlayer = Minecraft.getInstance().player;
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof GasExtractorTileEntity)
            return new GasExtractorGui((GasExtractorTileEntity) te, new GasExtractorContainer(entityPlayer.inventory, (GasExtractorTileEntity) te));
        else
            return null;
    }*/
}