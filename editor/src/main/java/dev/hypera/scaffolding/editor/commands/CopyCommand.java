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

import dev.hypera.scaffolding.editor.Clipboard;
import dev.hypera.scaffolding.editor.ScaffoldingEditor;
import dev.hypera.scaffolding.region.Region;
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
