package me.cprox.practice.arena.selection;

import java.util.Arrays;
import java.util.Objects;

import lombok.NonNull;
import me.cprox.practice.Practice;
import me.cprox.practice.arena.cuboid.Cuboid;
import me.cprox.practice.util.external.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class Selection {
	/* 16 */
	public void setPoint1(@NonNull Location point1) {
		if (point1 == null) throw new NullPointerException("point1 is marked non-null but is null");
		this.point1 = point1;
	}

	public void setPoint2(@NonNull Location point2) {
		if (point2 == null) throw new NullPointerException("point2 is marked non-null but is null");
		this.point2 = point2;
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Selection)) return false;
		Selection other = (Selection) o;
		if (!other.canEqual(this)) return false;
		Object this$point1 = getPoint1(), other$point1 = other.getPoint1();
		if (!Objects.equals(this$point1, other$point1)) return false;
		Object this$point2 = getPoint2(), other$point2 = other.getPoint2();
		return Objects.equals(this$point2, other$point2);
	}

	protected boolean canEqual(Object other) {
		return other instanceof Selection;
	}

	public int hashCode() {
		int PRIME = 59;
		int result = 1;
		Object $point1 = getPoint1();
		result = result * 59 + (($point1 == null) ? 43 : $point1.hashCode());
		Object $point2 = getPoint2();
		return result * 59 + (($point2 == null) ? 43 : $point2.hashCode());
	}

	public String toString() {
		return "Selection(point1=" + getPoint1() + ", point2=" + getPoint2() + ")";
	}

	private static final String SELECTION_METADATA_KEY = "CLAIM_SELECTION";
	@NonNull
	private Location point1;
	public static final ItemStack SELECTION_WAND = (new ItemBuilder(Material.GOLD_AXE))
			.name("&6&lSelection Wand")
			.lore(Arrays.asList("" +
							"&eLeft-click to set position 1.",
					"&eRight-click to set position 2."
			)).build();
	@NonNull
	private Location point2;

	@NonNull
	public Location getPoint1() {
		return this.point1;
	}

	@NonNull
	public Location getPoint2() {
		return this.point2;
	}

	public static Selection createOrGetSelection(Player player) {
		if (player.hasMetadata("CLAIM_SELECTION")) {
			return (Selection) (player.getMetadata("CLAIM_SELECTION").get(0)).value();

		}
		Selection selection = new Selection();
		player.setMetadata("CLAIM_SELECTION", new FixedMetadataValue(Practice.get(), selection));
		return selection;

	}

	public Cuboid getCuboid() {
		return new Cuboid(this.point1, this.point2);
	}

	public boolean isFullObject() {
		return (this.point1 != null && this.point2 != null);
	}

	public void clear() {
		this.point1 = null;
		this.point2 = null;

	}
}