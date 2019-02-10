import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.sasha.eventsys.SimpleEventHandler;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.api.event.EntityInRangeEvent;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.LoggerBuilder;

import java.util.ArrayList;

public class Main extends RePlugin implements SimpleListener {
    public Config CFG = new Config("DontTouchWhitelist");
    ILogger LoggieTheLogger = LoggerBuilder.buildProperLogger("DontTouchMyBot");
    @Override
    public void onPluginInit() {

        LoggieTheLogger.log("Thanks for using DontTouchMyBot!");
    }

    @Override
    public void onPluginEnable() {
        this.getReMinecraft().EVENT_BUS.registerListener(this);
    }

    @Override
    public void onPluginDisable() {
        this.getReMinecraft().EVENT_BUS.deregisterListener(this);
    }

    @Override
    public void onPluginShutdown() {

    }

    @Override
    public void registerCommands() {

    }
    @SimpleEventHandler
    public void onEvent(EntityInRangeEvent.Player e){
        LoggieTheLogger.log("Spotted player "+ e.getName());
        if(!getReMinecraft().areChildrenConnected()&&!CFG.var_DontTouchWhitelist.contains(e.getName())){
            this.getReMinecraft().minecraftClient.getSession().send(new ClientChatPacket("/kill"));}

    }
    @Override
    public void registerConfig() {
        this.getReMinecraft().configurations.add(new Configuration("DontTouchMyBot"));
    }
}

