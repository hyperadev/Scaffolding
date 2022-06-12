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
package dev.hypera.scaffolding.editor;

import dev.hypera.scaffolding.region.Region;
import dev.hypera.scaffolding.schematic.Schematic;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.hologram.Hologram;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.Nullable;

public class Clipboard {

    public static final Component FIRST_POINT_COMPONENT = Component.text("First point", NamedTextColor.GOLD);
    public static final Component SECOND_POINT_COMPONENT = Component.text("Second point", NamedTextColor.AQUA);

    private final Player player;
    private final Task drawParticlesTask;

    private final Schematic schematic = new Schematic();

    private Point firstPoint, secondPoint;
    private Hologram firstPointHologram, secondPointHologram;

    public Clipboard(Player player) {
        this.player = player;
        drawParticlesTask = MinecraftServer.getSchedulerManager().buildTask(this::drawSelection).repeat(50, TimeUnit.MILLISECOND).schedule();
    }

    public boolean hasValidSelection() {
        return firstPoint != null && secondPoint != null && player.getInstance() != null;
    }

    public @Nullable Region createRegionFromSelection() {
        Instance playerInstance = player.getInstance();
        if (playerInstance == null || !hasValidSelection()) return null;

        return new Region(playerInstance, firstPoint, secondPoint);
    }

    public void drawSelection() {
        Region region = createRegionFromSelection();

        if (region == null) return;

        Point lower = region.getLower();
        Point upper = region.getUpper().add(1);

        Vec p1 = new Vec(lower.x(), lower.y(), lower.z());
        Vec p2 = new Vec(upper.x(), lower.y(), lower.z());
        Vec p3 = new Vec(upper.x(), lower.y(), upper.z());
        Vec p4 = new Vec(lower.x(), lower.y(), upper.z());

        Vec p5 = new Vec(lower.x(), upper.y(), lower.z());
        Vec p6 = new Vec(upper.x(), upper.y(), lower.z());
        Vec p7 = new Vec(upper.x(), upper.y(), upper.z());
        Vec p8 = new Vec(lower.x(), upper.y(), upper.z());

        drawLine(player, Particle.CRIT, p1, p2);
        drawLine(player, Particle.CRIT, p2, p3);
        drawLine(player, Particle.CRIT, p3, p4);
        drawLine(player, Particle.CRIT, p4, p1);

        drawLine(player, Particle.CRIT, p5, p6);
        drawLine(player, Particle.CRIT, p6, p7);
        drawLine(player, Particle.CRIT, p7, p8);
        drawLine(player, Particle.CRIT, p8, p5);

        drawLine(player, Particle.CRIT, p1, p5);
        drawLine(player, Particle.CRIT, p2, p6);
        drawLine(player, Particle.CRIT, p3, p7);
        drawLine(player, Particle.CRIT, p4, p8);
    }

    private void drawLine(Player player, Particle particle, Point p1, Point p2) {
        final Vec v1 = Vec.fromPoint(p1);
        final Vec v2 = Vec.fromPoint(p2);

        Vec direction = v2.sub(v1).normalize();

        for (Vec position = v1; position.sub(v2).dot(direction) < 0; position = position.add(direction.mul(0.2d))) {
            ParticlePacket packet = ParticleCreator.createParticlePacket(particle, true, position.x(), position.y(), position.z(), 0, 0, 0, 0, 1, null);
            player.sendPacket(packet);
        }
    }

    public Point getFirstPoint() {
        return firstPoint;
    }

    public void setFirstPoint(Point firstPoint) {
        this.firstPoint = firstPoint;

        if (firstPointHologram != null) firstPointHologram.remove();
        firstPointHologram = createHologram(firstPoint, FIRST_POINT_COMPONENT);

        player.sendMessage(Component.text("Set ", NamedTextColor.GRAY).append(FIRST_POINT_COMPONENT));
    }

    private Hologram createHologram(Point position, Component text) {
        Hologram hologram = new Hologram(player.getInstance(), Pos.fromPoint(position.add(0.5d, 1.5d, 0.5d)), text, false, true);
        hologram.addViewer(player);

        return hologram;
    }

    public Point getSecondPoint() {
        return secondPoint;
    }

    public void setSecondPoint(Point secondPoint) {
        this.secondPoint = secondPoint;

        if (secondPointHologram != null) secondPointHologram.remove();
        secondPointHologram = createHologram(secondPoint, SECOND_POINT_COMPONENT);

        player.sendMessage(Component.text("Set ", NamedTextColor.GRAY).append(SECOND_POINT_COMPONENT));
    }

    public Schematic getSchematic() {
        return schematic;
    }

    public void cleanup() {
        drawParticlesTask.cancel();
    }
}
