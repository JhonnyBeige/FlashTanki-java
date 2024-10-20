package flashtanki.discord;

import flashtanki.discord.commands.Moderator.ClearChatCommand;
import flashtanki.discord.commands.Default.DiscordLinkCommand;
import flashtanki.discord.commands.Owner.HelpCommand;
import flashtanki.discord.commands.Default.OnlineCommand;
import flashtanki.discord.commands.Default.PingCommand;
import flashtanki.discord.commands.Owner.RestartCommand;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class JdaBot {

    private static final Logger logger = LoggerFactory.getLogger(JdaBot.class);

    private static final String TOKEN = "MTI0OTI4Mjc2ODc4NjAzMDY1Mw.GNhwjn.tGmHM8L0VZjvtZamOrSmWZ690hA0g2ZkTQqkaA";

    public static void initialize() throws InterruptedException, LoginException {
        JDABuilder builder = JDABuilder.createDefault(TOKEN)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new BotEventListener())
                .addEventListeners(new PingCommand())
                .addEventListeners(new DiscordLinkCommand())
                .addEventListeners(new HelpCommand())
                .addEventListeners(new OnlineCommand())
                .addEventListeners(new RestartCommand())
                .addEventListeners(new ClearChatCommand())
                .setActivity(Activity.playing("FlashTanki"));

        builder.build().awaitReady();
        logger.info("Bot is ready.");
    }

    private static class BotEventListener extends ListenerAdapter {
        @Override
        public void onReady(@NotNull ReadyEvent event) {
            logger.info("Bot started!");
        }
    }
}
