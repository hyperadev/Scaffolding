package net.crystalgames.scaffolding.editor.features;

import net.crystalgames.scaffolding.editor.Feature;
import net.crystalgames.scaffolding.editor.ScaffoldingEditor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class SelectionFeature implements Feature {

    public void hook(@NotNull final EventNode<InstanceEvent> node) {
        node.addListener(EventListener.builder(PlayerBlockBreakEvent.class).handler((this::handleFirstSelection)).filter((this::isValidSelection)).build());
        node.addListener(EventListener.builder(PlayerBlockInteractEvent.class).handler((this::handleSecondSelection)).filter((this::isValidSelection)).build());
    }

    private void handleFirstSelection(PlayerBlockBreakEvent event) {
        event.setCancelled(true);
        ScaffoldingEditor.getClipboard(event.getPlayer()).setFirstPoint(event.getBlockPosition());
    }

    private void handleSecondSelection(PlayerBlockInteractEvent event) {
        if (event.getHand() != Player.Hand.MAIN) return;

        ScaffoldingEditor.getClipboard(event.getPlayer()).setSecondPoint(event.getBlockPosition());
    }

    private boolean isValidSelection(PlayerEvent event) {
        Player player = event.getPlayer();

        return player.getItemInMainHand().material() == Material.WOODEN_AXE;
    }
}
