package net.crystalgames.scaffolding.editor.commands;

import net.crystalgames.scaffolding.editor.Clipboard;
import net.crystalgames.scaffolding.editor.ScaffoldingEditor;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public class PasteCommand extends Command {

    public PasteCommand() {
        super("paste");

        setDefaultExecutor((sender, context) -> {
            if((sender instanceof Player player)) {
                Clipboard clipboard = ScaffoldingEditor.getClipboard(player);
                Schematic schematic = clipboard.getSchematic();

                Instance instance = player.getInstance();
                Pos placementPosition = player.getPosition();

                if (instance == null) {
                    player.sendMessage("You are not in an instance. This should probably not happen...");
                    return;
                }

                if (schematic == null) {
                    player.sendMessage("No schematic in clipboard");
                    return;
                }

                if(!schematic.isPlaceable(instance, placementPosition)) {
                    player.sendMessage("Schematic would not fit within the world boundaries at this position");
                    return;
                }

                schematic.build(instance, placementPosition).thenRunAsync(() -> player.sendMessage("Schematic pasted"));
            }
        });
    }
}
