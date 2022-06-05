package dev.sllcoding.scaffolding.test.commands;

import java.nio.file.Path;
import dev.hypera.scaffolding.Scaffolding;
import dev.hypera.scaffolding.schematic.Schematic;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public class TestCommand extends Command {

    public TestCommand() {
        super("test");

        setDefaultExecutor((sender, context) -> {
            try {
                Schematic schematic = Scaffolding.fromPath(Path.of("schematic.schematic"));

                Player player = (Player) sender;
                Instance instance = player.getInstance();
                Pos position = player.getPosition();

                schematic.build(instance, position).thenRun(() -> player.sendMessage("Done!"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
