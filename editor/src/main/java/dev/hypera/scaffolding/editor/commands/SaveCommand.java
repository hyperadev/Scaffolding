package dev.hypera.scaffolding.editor.commands;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import dev.hypera.scaffolding.Scaffolding;
import dev.hypera.scaffolding.editor.Clipboard;
import dev.hypera.scaffolding.editor.ScaffoldingEditor;
import dev.hypera.scaffolding.schematic.Schematic;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class SaveCommand extends Command {

    public SaveCommand() {
        super("save");

        ArgumentWord nameArgument = ArgumentType.Word("nameArgument");
        nameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            try (Stream<Path> paths = Files.walk(ScaffoldingEditor.SCHEMATICS_PATH)) {
                paths.filter(Files::isRegularFile).forEach(path -> {
                    String file = path.getFileName().toString();
                    suggestion.addEntry(new SuggestionEntry(file, Component.text("Test")));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        this.addSyntax((sender, context) -> {
            if(sender instanceof Player player) {
                String schematicName = context.get(nameArgument);
                Clipboard clipboard = ScaffoldingEditor.getClipboard(player);

                if(!clipboard.hasValidSelection()) {
                    player.sendMessage("No selection to save.");
                    return;
                }

                if(clipboard.getSchematic() == null) {
                    player.sendMessage("Not schematic");
                    return;
                }

                File file = new File(ScaffoldingEditor.SCHEMATICS_PATH + "/" + schematicName);
                if(file.exists()) {
                    player.sendMessage("You cannot override schematics.");
                    return;
                }

                try {
                    file.createNewFile();
                    player.sendMessage(clipboard.getSchematic().toString());
                    FileWriter  writer = new FileWriter(file);
                    writer.write(new Gson().toJson(clipboard.getSchematic()));
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage("Saved to file" + file);
            }

        }, nameArgument);
    }
}
