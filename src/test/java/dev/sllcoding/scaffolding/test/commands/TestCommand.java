package dev.sllcoding.scaffolding.test.commands;

import net.crystalgames.scaffolding.Scaffolding;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.io.File;

public class TestCommand extends Command {

    public TestCommand() {
        super("test");

        setDefaultExecutor((sender, context) -> {
            try {
                Schematic schematic = Scaffolding.fromFile(new File("schematic.schematic"));

                Player player = (Player) sender;
                Instance instance = player.getInstance();
                Pos position = player.getPosition();

                schematic.read();

                schematic.build(instance, position).thenRun(() -> player.sendMessage("Done!"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
