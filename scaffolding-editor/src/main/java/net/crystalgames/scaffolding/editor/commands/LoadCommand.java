package net.crystalgames.scaffolding.editor.commands;

import net.crystalgames.scaffolding.Scaffolding;
import net.crystalgames.scaffolding.editor.Clipboard;
import net.crystalgames.scaffolding.editor.ScaffoldingEditor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class LoadCommand extends Command {

    public LoadCommand() {
        super("load");

        ArgumentWord nameArgument = ArgumentType.Word("nameArgument");
        nameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            try (Stream<Path> paths = Files.walk(ScaffoldingEditor.SCHEMATICS_PATH)) {
                paths.filter(Files::isRegularFile).forEach(path -> {
                    String name = path.getFileName().toString();
                    suggestion.addEntry(new SuggestionEntry(name));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                try {
                    String schematicName = context.get(nameArgument);

                    Clipboard clipboard = ScaffoldingEditor.getClipboard(player);

                    Scaffolding.fromFile(ScaffoldingEditor.SCHEMATICS_PATH.resolve(schematicName).toFile(), clipboard.getSchematic()).thenRun(() -> {
                        player.sendMessage(Component.text("Loaded schematic " + schematicName, NamedTextColor.GRAY));
                    });
                } catch (IOException | NBTException e) {
                    player.sendMessage("Failed to load schematic");
                }
            }
        }, nameArgument);
    }

}
