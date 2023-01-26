package me.cprox.practice.util.chat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
public class Format {

    private String message;
    private List<String> messages;
    private HashMap<String, String> variables = Maps.newHashMap();

    public Format(Object object) {
        if (object instanceof List) messages = (List<String>) object;
        else message = (String) object;
    }

    public void setMessage(Object object) {
        if (object instanceof List) messages = (List<String>) object;
        else message = (String) object;
    }

    public Format add(String variable, String value){
        variables.put(variable.toLowerCase(), value);
        return this;
    }

    public void send(CommandSender sender){
        if (messages != null) {
            messages.forEach(formatted -> {
                for (Map.Entry<String, String> entry : variables.entrySet()){
                    String variable = entry.getKey();
                    String value = entry.getValue();

                    formatted = formatted.replace(variable, value);
                }

                sender.sendMessage(CC.translate(formatted));
            });
        }
        else if (message != null) {
            String formatted = message;

            for (Map.Entry<String, String> entry : variables.entrySet()){
                String variable = entry.getKey();
                String value = entry.getValue();

                formatted = formatted
                        .replace(variable, value);
            }

            sender.sendMessage(CC.translate(formatted));
        }
    }

    public void broadcast(){
        if(message == null && messages != null){
            messages.forEach(formatted -> {
                for (Map.Entry<String, String> entry : variables.entrySet()){
                    String variable = entry.getKey();
                    String value = entry.getValue();

                    formatted = formatted
                            .replace(variable, value);
                }
                Bukkit.broadcastMessage(CC.translate(formatted));
            });
            return;
        }
        if(message != null){
            String formatted = message;
            for (Map.Entry<String, String> entry : variables.entrySet()){
                String variable = entry.getKey();
                String value = entry.getValue();

                formatted = formatted
                        .replace(variable, value);
            }
            Bukkit.broadcastMessage(CC.translate(formatted));
        }
    }

    public List<String> toList() {
        List<String> lines = Lists.newArrayList();
        if(messages != null){
            messages.forEach(formatted -> {
                for (Map.Entry<String, String> entry : variables.entrySet()){
                    String variable = entry.getKey();
                    String value = entry.getValue();

                    formatted = formatted
                            .replace(variable, value);
                }
                lines.add(CC.translate(formatted));
            });
        }
        return lines;
    }

    @Override
    public String toString() {
        if(message != null){
            String formatted = message;
            for (Map.Entry<String, String> entry : variables.entrySet()){
                String variable = entry.getKey();
                String value = entry.getValue();

                formatted = formatted
                        .replace(variable, value);
            }
            return CC.translate(formatted);
        }
        return "";
    }

    private String getVariable(String string){
        if (string == null) return  "";
        if (string.contains("{")) {
            StringBuilder variable = new StringBuilder();
            boolean add = false;
            for (char s : string.toCharArray()){
                if(s == '{') add = true;
                if(s == '}') {
                    variable.append(s);
                    break;
                }
                if (add) variable.append(s);
            }
            return variable.toString();
        }
        return string;
    }
}