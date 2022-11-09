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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class Clipboard {

    public static final Component FIRST_POINT_COMPONENT = Component.text("First point", NamedTextColor.GOLD);
    public static final Component SECOND_POINT_COMPONENT = Component.text("Second point", NamedTextColor.AQUA);

    private final Player player;

    private final Task drawParticlesTask;

    private final Schematic schematic = new Schematic();

    private Point firstPoint, secondPoint, firstPointSet, secondPointSet;
    private Hologram firstPointHologram, secondPointHologram;

    public Clipboard(Player player) {
        this.player = player;
        this.drawParticlesTask = MinecraftServer.getSchedulerManager().buildTask(this::drawSelection).repeat(150, TimeUnit.MILLISECOND).schedule();
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

        if (region == null) {
            return;
        }

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

        Particle p = Particle.REVERSE_PORTAL;


        drawLine(player, p, p1, p2);
        drawLine(player, p, p2, p3);
        drawLine(player, p, p3, p4);
        drawLine(player, p, p4, p1);

        drawLine(player, p, p5, p6);
        drawLine(player, p, p6, p7);
        drawLine(player, p, p7, p8);
        drawLine(player, p, p8, p5);

        drawLine(player, p, p1, p5);
        drawLine(player, p, p2, p6);
        drawLine(player, p, p3, p7);
        drawLine(player, p, p4, p8);
    }

    private void drawLine(Player player, Particle particle, Point p1, Point p2) {
        final Vec v1 = Vec.fromPoint(p1);
        final Vec v2 = Vec.fromPoint(p2);

        Vec direction = v2.sub(v1).normalize();
        double repeat = v1.distance(v2) / 4;

        for (Vec position = v1; position.sub(v2).dot(direction) < 0; position = position.add(direction.mul(repeat))) {
            ParticlePacket packet = ParticleCreator.createParticlePacket(particle, true, position.x(), position.y(), position.z(), 0, 0, 0, 0, 1, null);
            player.sendPacket(packet);
        }
    }

    public Point getFirstPoint() {
        return firstPoint;
    }

    public void setFirstPoint(Point firstPoint) {
        boolean isOverride = this.firstPointSet != null && this.firstPointSet.samePoint(firstPoint);
        this.firstPointSet = firstPoint;

        //Setting
        if(this.firstPoint == null) {
            //set
            this.firstPoint = firstPoint;

            //Creating hologram
            if (firstPointHologram != null) firstPointHologram.remove();
            firstPointHologram = createHologram(firstPoint, FIRST_POINT_COMPONENT);

            //Starting animation
            this.drawParticlesTask.unpark();


            player.sendMessage("Setzen");
            return;
        }
        if(this.firstPoint.samePoint(firstPoint)) {
            //Entfernen
            if (firstPointHologram != null) firstPointHologram.remove();
            this.firstPoint = null;

            player.sendMessage("entfernen");
            return;
        }
        //Replace
        if(this.firstPoint != null) {
            player.sendMessage("Double click the same location for override");

            //Double clicked location
            if(isOverride) {
                player.sendMessage("Overriden!");
                this.firstPoint = firstPoint;

                //Create hologram
                if (firstPointHologram != null) firstPointHologram.remove();
                firstPointHologram = createHologram(firstPoint, FIRST_POINT_COMPONENT);

                //Start animation
                this.drawParticlesTask.unpark();
            }
        }
    }

    public void setSecondPoint(Point secondPoint) {
        boolean isOverride = this.secondPointSet != null && this.secondPointSet.samePoint(secondPoint);
        this.secondPointSet = secondPoint;

        //Setting
        if(this.secondPoint == null) {
            //set
            this.secondPoint = secondPoint;

            //Creating hologram
            if (secondPointHologram != null) secondPointHologram.remove();
            secondPointHologram = createHologram(secondPoint, SECOND_POINT_COMPONENT);

            //Starting animation
            if(this.drawParticlesTask != null) {
                this.drawParticlesTask.unpark();
            }

            player.sendMessage("Setzen");
            return;
        }
        if(this.secondPoint.samePoint(secondPoint)) {
            //Entfernen
            if (secondPointHologram != null) secondPointHologram.remove();
            this.secondPoint = null;

            player.sendMessage("entfernen");
            return;
        }
        //Replace
        if(this.secondPoint != null) {
            player.sendMessage("Double click the same location for override");

            //Double clicked location
            if(isOverride) {
                player.sendMessage("Overriden!");
                this.secondPoint = secondPoint;

                //Create hologram
                if (secondPointHologram != null) secondPointHologram.remove();
                secondPointHologram = createHologram(secondPoint, SECOND_POINT_COMPONENT);

                //Start animation
                this.drawParticlesTask.unpark();
            }
        }
    }

    private Hologram createHologram(Point position, Component text) {
        Hologram hologram = new Hologram(player.getInstance(), Pos.fromPoint(position.add(0.5d, 1.5d, 0.5d)), text, false, true);
        hologram.addViewer(player);

        return hologram;
    }

    public Point getSecondPoint() {
        return secondPoint;
    }

    public Schematic getSchematic() {
        return schematic;
    }

    public void cleanup() {
        this.drawParticlesTask.cancel();
    }
}
