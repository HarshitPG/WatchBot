package com.harshit.watchbot;

import com.harshit.watchbot.EventListener.EventListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;

public class WatchBot {
    private  final Dotenv config;
    private final ShardManager shardManager;
    public WatchBot() throws LoginException {
        config = Dotenv.configure().load();
        String token = config.get("TOKEN");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES,GatewayIntent.MESSAGE_CONTENT);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.listening("Nee Kavithaigala"));
        shardManager = builder.build();
        String perspectiveApiKey = config.get("PERSPECTIVE_API_KEY");
        shardManager.addEventListener(new EventListener(perspectiveApiKey));

    }

    public Dotenv getConfig(){
        return config;
    }

    public ShardManager getShardManager(){
        return shardManager;
    }

    public static void main(String[] args) {
        try {
            WatchBot bot = new WatchBot();
        }catch (LoginException e){
            System.out.println("ERROR: Invalid token");
        }
    }
}
