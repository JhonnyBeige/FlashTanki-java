package flashtanki.discord.commands.Bot;

import flashtanki.discord.commands.Permissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Locale;

public class WriteCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (event.getMember() != null && !Permissions.hasPermission(event.getMember())) {
            return;
        }

        String content = event.getMessage().getContentRaw().trim();
        String lowerContent = content.toLowerCase(Locale.ROOT);

        if (lowerContent.startsWith("en?write-publish") || lowerContent.startsWith("ru?write-publish")) {
            handlePublishCommand(event, content);
        } else if (lowerContent.startsWith("en?write") || lowerContent.startsWith("ru?write")) {
            handleWriteCommand(event, content);
        }
    }

    private void handleWriteCommand(MessageReceivedEvent event, String content) {
        String message = content.contains(" ") ? content.substring(content.indexOf(" ") + 1) : "";

        if (!message.isEmpty()) {
            event.getMessage().delete().queue(success -> {
                event.getChannel().sendMessage(message).queue();
            }, failure -> {
                event.getChannel().sendMessage("Failed to delete the message.").queue();
            });
        } else {
            String response = content.toLowerCase(Locale.ROOT).startsWith("en?write") ? "Please provide a message to write." : "Пожалуйста, укажите текст для отправки.";
            event.getChannel().sendMessage(response).queue();
        }
    }

    private void handlePublishCommand(MessageReceivedEvent event, String content) {
        String message = content.contains(" ") ? content.substring(content.indexOf(" ") + 1) : "";

        if (!message.isEmpty()) {
            event.getMessage().delete().queue(success -> {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                String title;
                if (content.toLowerCase(Locale.ROOT).startsWith("ru?write-publish")) {
                    title = "Внимание Новости!";
                } else {
                    title = "Attention News!";
                }

                String mention = "||@everyone||";

                embedBuilder.setTitle(title)
                        .setDescription(mention + "\n\n" + message)
                        .setColor(Color.RED);

                event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            }, failure -> {
                event.getChannel().sendMessage("Failed to delete the message.").queue();
            });
        } else {
            String response = content.toLowerCase(Locale.ROOT).startsWith("en?write-publish") ? "Please provide a message to publish." : "Пожалуйста, укажите текст для публикации.";
            event.getChannel().sendMessage(response).queue();
        }
    }
}
