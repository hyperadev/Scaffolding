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
package dev.hypera.scaffolding.editor.commands;

import dev.hypera.scaffolding.Scaffolding;
import dev.hypera.scaffolding.editor.Clipboard;
import dev.hypera.scaffolding.editor.ScaffoldingEditor;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
                    }).get(2, TimeUnit.SECONDS);
                } catch (IOException | NBTException | ExecutionException | InterruptedException | TimeoutException e) {
                    player.sendMessage("Failed to load schematic" + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }, nameArgument);
    }

}
