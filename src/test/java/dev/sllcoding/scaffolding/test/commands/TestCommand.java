package dev.sllcoding.scaffolding.test.commands;

import net.crystalgames.scaffolding.schematic.Schematic;
import net.crystalgames.scaffolding.schematic.impl.SpongeSchematic;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.File;
import java.io.IOException;

public class TestCommand extends Command {

    public TestCommand() {
        super("test");

        setDefaultExecutor((sender, context) -> {
            Schematic schematic = new SpongeSchematic(new File("schematic.schematic"));

            Player player = (Player) sender;
            Instance instance = player.getInstance();
            Pos position = player.getPosition();

            try {
                schematic.read();
            } catch (IOException | NBTException e) {
                e.printStackTrace();
            }

            schematic.build(instance, position).thenRun(() -> {
                player.sendMessage("Done!");
            });
        });
    }

}
