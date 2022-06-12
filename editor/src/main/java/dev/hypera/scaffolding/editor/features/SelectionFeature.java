/*
 * Scaffolding - Schematic library for Minestom
 *  Copyright (c) 2022-latest The Scaffolding Library Authors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the “Software”), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package dev.hypera.scaffolding.editor.features;

import dev.hypera.scaffolding.editor.Feature;
import dev.hypera.scaffolding.editor.ScaffoldingEditor;
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
