package me.cprox.practice.arena.selection;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import java.io.File;
import java.io.IOException;
import org.bukkit.World;

public class Schematic {
    private CuboidClipboard clipBoard;

    public CuboidClipboard getClipBoard() {
        return this.clipBoard;
    }

    public Schematic(File file) throws IOException {
        SchematicFormat format = SchematicFormat.MCEDIT;
        try {
            this.clipBoard = format.load(file);
        } catch (DataException e) {
            e.printStackTrace();
        }
    }

    public void pasteSchematic(World world, int x, int y, int z) {
        Vector pastePos = new Vector(x, y, z);
        EditSession editSession = new EditSession(BukkitUtil.getLocalWorld(world), 999999);
        try {
            this.clipBoard.place(editSession, pastePos, true);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }
}
