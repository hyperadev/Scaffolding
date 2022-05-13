package net.crystalgames.scaffolding.editor;

import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Feature {

    void hook(@NotNull final EventNode<InstanceEvent> node);
}
