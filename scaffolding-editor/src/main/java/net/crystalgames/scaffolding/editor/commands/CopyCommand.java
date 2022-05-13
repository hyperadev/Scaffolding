package net.crystalgames.scaffolding.editor.commands;

import net.crystalgames.scaffolding.editor.Clipboard;
import net.crystalgames.scaffolding.editor.ScaffoldingEditor;
import net.crystalgames.scaffolding.region.Region;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class CopyCommand extends Command {

    public CopyCommand() {
        super("copy");

        setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;
            Clipboard clipboard = ScaffoldingEditor.getClipboard(player);

            Region region = clipboard.createRegionFromSelection();

            if (region == null) {
                player.sendMessage("No region selected");
                return;
            }

            clipboard.getSchematic().copy(region).thenRunAsync(() -> player.sendMessage("Copied region"));
        });
    }
}
