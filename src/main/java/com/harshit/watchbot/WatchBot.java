package com.harshit.watchbot;

import com.harshit.watchbot.EventListener.EventListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import spark.Spark;

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
        Spark.port(8080); 
        Spark.get("/bot", (req, res) -> "Bot is running");

        System.out.println("Server is running on http://localhost:8080/health");

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
