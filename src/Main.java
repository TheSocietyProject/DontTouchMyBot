
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.sasha.eventsys.SimpleEventHandler;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.api.event.EntityInRangeEvent;
import com.sasha.reminecraft.client.ChildReClient;
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
        if(getReMinecraft().areChildrenConnected()) {
            notifyChilds(e.getName());
            return;
        }
        if(takeAction(e.getName())){
            if(CFG.var_Suicide)
                this.getReMinecraft().minecraftClient.getSession().send(new ClientChatPacket("/kill"));
            if(CFG.var_ShutDown)
                System.exit(0);
        }

    }

    // 0 = neutral, 1 = friend, -1 = hate
    public int howIsRelationWith(String name){
        if(CFG.var_CanTouchWhitelist.contains(name))
            return 1;

        if(CFG.var_DontTouchBlackList.contains(name))
            return -1;

        if(CFG.var_CanTouchWhitelist.size() == 0)
            return 0;

        if(CFG.var_DontTouchBlackList.size() == 0)
            return 0;

        return 0;
    }

    /*
        can del, was just to think bout when to return what..
            h n f
          blacklist && whitelist eZ: h n f skept?
          blacklist h n !skeptl
          whitelist f n skepticl
          nothing n skeptly

        */

    public boolean sceptical(String name){
        if(CFG.var_CanTouchWhitelist.contains(name))
            return false;

        if(CFG.var_DontTouchBlackList.contains(name))
            return true;

        // if both is 0 it returns now
        if(CFG.var_DontTouchBlackList.size() == 0)
            return true;

        if(CFG.var_CanTouchWhitelist.size() == 0)
            return false;

        // cant decide wether its sceptical in nature. like if there r ppl u hate and like what do u do with neutrals?
        return false;
    }

    public boolean takeAction(String name){
        int relation = howIsRelationWith(name);

        if(relation == -1)
            return true;

        if(relation == 1)
            return false;

        // only neutrals r left now
        // and depending on wether u r sceptical u take action
        return sceptical(name);
    }

    private void notifyChilds(String intruder) {
        for(ChildReClient childClient : ReMinecraft.INSTANCE.childClients) {
            if(!childClient.isPlaying()) continue;
            String msg = "";
            switch(howIsRelationWith(intruder)){
                case -1:
                    msg = "ATTENTION: " + intruder + " has entered your field of vision";
                    break;
                case 0:
                    msg = "neutral " + intruder + " has entered your field of vision";
                    if(sceptical(intruder))
                        msg = "ATTENTION: " + intruder + " has entered your field of vision";
                    break;
                case 1:
                    msg = "friendly " + intruder + " has entered your field of vision";
                    break;
            }

            childClient.getSession().send(new ServerChatPacket(msg));
        }
    }

    @Override
    public void registerConfig() {
        this.getReMinecraft().configurations.add(new Configuration("DontTouchMyBot"));
    }
}

class Config extends Configuration {
    @ConfigSetting
    public ArrayList<String> var_CanTouchWhitelist = new ArrayList<>();

    public ArrayList<String> var_DontTouchBlackList = new ArrayList<>();

    {
        var_CanTouchWhitelist.add("Friedolin2000");
        var_CanTouchWhitelist.add("IronException");
        var_CanTouchWhitelist.add("The2b2tMossad");
        var_CanTouchWhitelist.add("SonEasterZombie");
    }

    public boolean var_Suicide = true;

    public boolean var_ShutDown = false;

    public Config(String configName) {
        super(configName);
    }
}
