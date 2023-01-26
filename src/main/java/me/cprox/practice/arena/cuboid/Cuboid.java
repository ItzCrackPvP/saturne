package me.cprox.practice.arena.cuboid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

@Data

public class Cuboid implements Iterable<Location> {
  private String worldName;

  private int x1;

  private int y1;

  private int z1;

  private int x2;

  private int y2;

  private int z2;

  public void setWorldName(String worldName) {
    this.worldName = worldName;
  }

  public void setX1(int x1) {
    this.x1 = x1;
  }

  public void setY1(int y1) {
    this.y1 = y1;
  }

  public void setZ1(int z1) {
    this.z1 = z1;
  }

  public void setX2(int x2) {
    this.x2 = x2;
  }

  public void setY2(int y2) {
    this.y2 = y2;
  }

  public void setZ2(int z2) {
    this.z2 = z2;
  }

  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof Cuboid))
      return false;
    Cuboid other = (Cuboid)o;
    if (!other.canEqual(this))
      return false;
    if (getX1() != other.getX1())
      return false;
    if (getY1() != other.getY1())
      return false;
    if (getZ1() != other.getZ1())
      return false;
    if (getX2() != other.getX2())
      return false;
    if (getY2() != other.getY2())
      return false;
    if (getZ2() != other.getZ2())
      return false;
    Object this$worldName = getWorldName(), other$worldName = other.getWorldName();
    return Objects.equals(this$worldName, other$worldName);
  }

  protected boolean canEqual(Object other) {
    return other instanceof Cuboid;
  }

  public int hashCode() {
    int PRIME = 59;
    int result = 1;
    result = result * 59 + getX1();
    result = result * 59 + getY1();
    result = result * 59 + getZ1();
    result = result * 59 + getX2();
    result = result * 59 + getY2();
    result = result * 59 + getZ2();
    Object $worldName = getWorldName();
    return result * 59 + (($worldName == null) ? 43 : $worldName.hashCode());
  }

  public String getWorldName() {
    return this.worldName;
  }

  public int getX1() {
    return this.x1;
  }

  public int getY1() {
    return this.y1;
  }

  public int getZ1() {
    return this.z1;
  }

  public int getX2() {
    return this.x2;
  }

  public int getY2() {
    return this.y2;
  }

  public int getZ2() {
    return this.z2;
  }

  public Cuboid(Location l1, Location l2) {
    this(l1.getWorld().getName(), l1
            .getBlockX(), l1.getBlockY(), l1.getBlockZ(), l2
            .getBlockX(), l2.getBlockY(), l2.getBlockZ());
  }

  public Cuboid(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
    this(world.getName(), x1, y1, z1, x2, y2, z2);
  }

  public Cuboid(String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
    this.worldName = worldName;
    this.x1 = Math.min(x1, x2);
    this.x2 = Math.max(x1, x2);
    this.y1 = Math.min(y1, y2);
    this.y2 = Math.max(y1, y2);
    this.z1 = Math.min(z1, z2);
    this.z2 = Math.max(z1, z2);
  }

  public Location getLowerCorner() {
    return new Location(getWorld(), this.x1, this.y1, this.z1);
  }

  public Location getUpperCorner() {
    return new Location(getWorld(), this.x2, this.y2, this.z2);
  }

  public Location getCenter() {
    return new Location(
            getWorld(), (getLowerX() + (getUpperX() - getLowerX()) / 2), (
            getLowerY() + (getUpperY() - getLowerY()) / 2), (getLowerZ() + (getUpperZ() - getLowerZ()) / 2));
  }

  public World getWorld() {
    World world = Bukkit.getWorld(this.worldName);
    if (world == null)
      throw new IllegalStateException("world '" + this.worldName + "' is not loaded");
    return world;
  }

  public int getSizeX() {
    return this.x2 - this.x1 + 1;
  }

  public int getSizeY() {
    return this.y2 - this.y1 + 1;
  }

  public int getSizeZ() {
    return this.z2 - this.z1 + 1;
  }

  public int getLowerX() {
    return this.x1;
  }

  public int getLowerY() {
    return this.y1;
  }

  public int getLowerZ() {
    return this.z1;
  }

  public int getUpperX() {
    return this.x2;
  }

  public int getUpperY() {
    return this.y2;
  }

  public int getUpperZ() {
    return this.z2;
  }

  public Location[] getCorners() {
    Location[] res = new Location[4];
    World w = getWorld();
    res[0] = new Location(w, this.x1, 0.0D, this.z1);
    res[1] = new Location(w, this.x2, 0.0D, this.z1);
    res[2] = new Location(w, this.x2, 0.0D, this.z2);
    res[3] = new Location(w, this.x1, 0.0D, this.z2);
    return res;
  }

  public Cuboid expand(CuboidDirection dir, int amount) {
    switch (dir) {
      case NORTH:
        return new Cuboid(this.worldName, this.x1 - amount, this.y1, this.z1, this.x2, this.y2, this.z2);
      case SOUTH:
        return new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2 + amount, this.y2, this.z2);
      case EASY:
        return new Cuboid(this.worldName, this.x1, this.y1, this.z1 - amount, this.x2, this.y2, this.z2);
      case WEST:
        return new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2, this.z2 + amount);
      case DOWN:
        return new Cuboid(this.worldName, this.x1, this.y1 - amount, this.z1, this.x2, this.y2, this.z2);
      case UP:
        return new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2 + amount, this.z2);
    }
    throw new IllegalArgumentException("invalid direction " + dir);
  }

  public Cuboid shift(CuboidDirection dir, int amount) {
    return expand(dir, amount).expand(dir.opposite(), -amount);
  }

  public Cuboid outset(CuboidDirection dir, int amount) {
    Cuboid c;
    switch (dir) {
      case HORIZONTAL:
        c = expand(CuboidDirection.NORTH, amount).expand(CuboidDirection.SOUTH, amount).expand(CuboidDirection.EASY, amount).expand(CuboidDirection.WEST, amount);
        return c;
      case VERTICAL:
        c = expand(CuboidDirection.DOWN, amount).expand(CuboidDirection.UP, amount);
        return c;
      case BOTH:
        c = outset(CuboidDirection.HORIZONTAL, amount).outset(CuboidDirection.VERTICAL, amount);
        return c;
    }
    throw new IllegalArgumentException("invalid direction " + dir);
  }

  public Cuboid inset(CuboidDirection dir, int amount) {
    return outset(dir, -amount);
  }

  public boolean contains(int x, int y, int z) {
    return (x >= this.x1 && x <= this.x2 && y >= this.y1 && y <= this.y2 && z >= this.z1 && z <= this.z2);
  }

  public boolean contains(int x, int z) {
    return (x >= this.x1 && x <= this.x2 && z >= this.z1 && z <= this.z2);
  }

  public boolean contains(Location l) {
    if (!this.worldName.equals(l.getWorld().getName()))
      return false;
    return contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
  }

  public boolean contains(Block b) {
    return contains(b.getLocation());
  }

  public int volume() {
    return getSizeX() * getSizeY() * getSizeZ();
  }

  public Cuboid getFace(CuboidDirection dir) {
    switch (dir) {
      case DOWN:
        return new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2, this.y1, this.z2);
      case UP:
        return new Cuboid(this.worldName, this.x1, this.y2, this.z1, this.x2, this.y2, this.z2);
      case NORTH:
        return new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x1, this.y2, this.z2);
      case SOUTH:
        return new Cuboid(this.worldName, this.x2, this.y1, this.z1, this.x2, this.y2, this.z2);
      case EASY:
        return new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2, this.z1);
      case WEST:
        return new Cuboid(this.worldName, this.x1, this.y1, this.z2, this.x2, this.y2, this.z2);
    }
    throw new IllegalArgumentException("Invalid direction " + dir);
  }

  public Cuboid getBoundingCuboid(Cuboid other) {
    if (other == null)
      return this;
    int xMin = Math.min(getLowerX(), other.getLowerX());
    int yMin = Math.min(getLowerY(), other.getLowerY());
    int zMin = Math.min(getLowerZ(), other.getLowerZ());
    int xMax = Math.max(getUpperX(), other.getUpperX());
    int yMax = Math.max(getUpperY(), other.getUpperY());
    int zMax = Math.max(getUpperZ(), other.getUpperZ());
    return new Cuboid(this.worldName, xMin, yMin, zMin, xMax, yMax, zMax);
  }

  public Block getRelativeBlock(int x, int y, int z) {
    return getWorld().getBlockAt(this.x1 + x, this.y1 + y, this.z1 + z);
  }

  public Block getRelativeBlock(World w, int x, int y, int z) {
    return w.getBlockAt(this.x1 + x, this.y1 + y, this.z1 + z);
  }

  public List<Chunk> getChunks() {
    List<Chunk> chunks = new ArrayList<>();
    World w = getWorld();
    int x1 = getLowerX() & 0xFFFFFFF0;
    int x2 = getUpperX() & 0xFFFFFFF0;
    int z1 = getLowerZ() & 0xFFFFFFF0;
    int z2 = getUpperZ() & 0xFFFFFFF0;
    for (int x = x1; x <= x2; x += 16) {
      for (int z = z1; z <= z2; z += 16)
        chunks.add(w.getChunkAt(x >> 4, z >> 4));
    }
    return chunks;
  }

  public Cuboid[] getWalls() {
    return new Cuboid[] { getFace(CuboidDirection.NORTH),
            getFace(CuboidDirection.SOUTH),
            getFace(CuboidDirection.WEST),
            getFace(CuboidDirection.EASY) };
  }

  public Iterator<Location> iterator() {
    return new LocationCuboidIterator(getWorld(), this.x1, this.y1, this.z1, this.x2, this.y2, this.z2);
  }

  public String toString() {
    return "Cuboid: " + this.worldName + "," + this.x1 + "," + this.y1 + "," + this.z1 + "=>" + this.x2 + "," + this.y2 + "," + this.z2;
  }

  public class LocationCuboidIterator implements Iterator<Location> {
    private final World w;

    private final int baseX;

    private final int baseY;

    private final int baseZ;

    private int x;

    private int y;

    private int z;

    private final int sizeX;

    private final int sizeY;

    private final int sizeZ;

    public LocationCuboidIterator(World w, int x1, int y1, int z1, int x2, int y2, int z2) {
      this.w = w;
      this.baseX = x1;
      this.baseY = y1;
      this.baseZ = z1;
      this.sizeX = Math.abs(x2 - x1) + 1;
      this.sizeY = Math.abs(y2 - y1) + 1;
      this.sizeZ = Math.abs(z2 - z1) + 1;
      this.x = this.y = this.z = 0;
    }

    public boolean hasNext() {
      return (this.x < this.sizeX && this.y < this.sizeY && this.z < this.sizeZ);
    }

    public Location next() {
      Location b = new Location(this.w, (this.baseX + this.x), (this.baseY + this.y), (this.baseZ + this.z));
      this.x = 0;
      if (++this.x >= this.sizeX && ++this.y >= this.sizeY) {
        this.y = 0;
        this.z++;
      }
      return b;
    }

    public void remove() {}
  }
}