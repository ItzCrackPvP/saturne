package me.cprox.practice.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.kit.KitLeaderboards;
import me.cprox.practice.profile.Profile;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {
    public String getIdentifier() {
        return "Practice";
    }

    public String getAuthor() {
        return "cprox13";
    }

    public String getVersion() {
        return "1.0";
    }

  public String onPlaceholderRequest(Player player, String identifier) {
    if (player == null)
      return "&7"; 
    if (identifier.contains("global")) {
      KitLeaderboards kitLeaderboards;
      String[] arguments = identifier.split("_");
      int number = Integer.parseInt(arguments[1]) - 1;
      try {
        kitLeaderboards = Profile.getGlobalEloLeaderboards().get(number);
      } catch (Exception e) {
        return "&4&l#" + (number + 1) + " &8- &7N/A &8- &41000";
      } 
      if (kitLeaderboards == null)
        return "&4&l#" + (number + 1) + " &8- &7N/A &8- &41000";
      return "&4&l#" + (number + 1) + " &8- &7" + kitLeaderboards.getName() + " &8- &4" + kitLeaderboards.getElo();
    } 
    if (identifier.contains("lb")) {
      KitLeaderboards kitLeaderboards;
      String[] splitString = identifier.split("_");
      String kitString = splitString[1];
      int number = Integer.parseInt(splitString[2]) - 1;
      Kit kit = Kit.getByName(kitString);
      if (kit == null)
        return "&4&l#" + (number + 1) + " &8- &7N/A &8- &41000"; 
      try {
        kitLeaderboards = kit.getRankedEloLeaderboards().get(number);
      } catch (Exception e) {
        return "&4&l#" + (number + 1) + " &8- &7N/A &8- &41000";
      } 
      if (kitLeaderboards == null)
        return "&4&l#" + (number + 1) + " &8- &7N/A &8- &41000"; 
      return "&4&l#" + (number + 1) + " &8- &7" + kitLeaderboards.getName() + " &8- &4" + kitLeaderboards.getElo();
    } 
    if (identifier.contains("switch")) {
      KitLeaderboards kitLeaderboards;
      String[] splitArgs = identifier.split("_");
      int number = Integer.parseInt(splitArgs[2]) - 1;
      Kit kit = Kit.getByName(getNextLadder());
      if (kit == null)
        return "&7"; 
      try {
        kitLeaderboards = kit.getRankedEloLeaderboards().get(number);
      } catch (Exception e) {
        return "&7";
      } 
      if (kitLeaderboards == null)
        return "&7"; 
      return "&4&l#" + (number + 1) + " &8- &7" + kitLeaderboards.getName() + " &8- &4" + kitLeaderboards.getElo();
    } 
    return null;
  }
  
  public String getNextLadder() {
    if (getIdentifier().contains("switch")) {
      String[] splitString = getIdentifier().split("_");
      String kitString = splitString[1];
      switch (kitString) {
        case "NoDebuff":
          return "Debuff";
        case "Debuff":
          return "Kohi";
        case "Kohi":
          return "BuildUHC";
        case "BuildUHC":
          return "Combo";
        case "Combo":
          return "Gapple";
        case "Gapple":
          return "Sumo";
        case "Sumo":
          return "Archer";
        case "Archer":
          return "Classic";
        case "Classic":
          return "Axe";
        case "Axe":
          return "NoDebuff";
      } 
      return "";
    } 
    return null;
  }
}