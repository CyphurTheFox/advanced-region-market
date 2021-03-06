package net.alex9849.arm.handler.listener;

import net.alex9849.arm.AdvancedRegionMarket;
import net.alex9849.arm.Messages;
import net.alex9849.arm.Permission;
import net.alex9849.arm.exceptions.InputException;
import net.alex9849.arm.regions.Region;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

public class BlockModifyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void addBuiltBlock(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (AdvancedRegionMarket.getInstance().getRegionManager() == null) {
            return;
        }

        List<Region> locRegions = AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByLocation(event.getBlock().getLocation());

        for (Region region : locRegions) {
            if (region.isHotel()) {
                if (region.isSold()) {
                    region.addBuiltBlock(event.getBlock().getLocation());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void breakBlock(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        this.breakblockCheckHotel(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void blockPhysicsHotel(EntityChangeBlockEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Entity s = event.getEntity();
        if (!(s instanceof FallingBlock)) {
            return;
        }
        Location blockLocation = event.getBlock().getLocation();
        List<Region> regions = AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByLocation(blockLocation);
        for(Region region : regions) {
            if(region.isHotel() && region.isSold()) {
                region.addBuiltBlock(blockLocation);
            }
        }

    }

    private void breakblockCheckHotel(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getPlayer().hasPermission(Permission.ADMIN_BUILDEVERYWHERE)) {
            return;
        }

        if (AdvancedRegionMarket.getInstance().getRegionManager() == null) {
            return;
        }

        try {
            List<Region> locRegions = AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByLocation(event.getBlock().getLocation());

            for (Region region : locRegions) {
                if (region.isHotel()) {
                    if (!region.allowBlockBreak(event.getBlock().getLocation())) {
                        event.setCancelled(true);
                        throw new InputException(event.getPlayer(), Messages.REGION_ERROR_CAN_NOT_BUILD_HERE);
                    }
                }
            }
        } catch (InputException inputException) {
            inputException.sendMessages(Messages.PREFIX);
        }
    }

}
