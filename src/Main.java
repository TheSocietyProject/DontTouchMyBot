
import com.github.steveice10.mc.protocol.data.message.ChatColor;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.data.message.MessageStyle;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.sasha.eventsys.SimpleEventHandler;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.api.event.EntityInRangeEvent;
import com.sasha.reminecraft.client.ChildReClient;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.LoggerBuilder;
import processing.core.PApplet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Main extends RePlugin implements SimpleListener {
    public Config CFG = new Config();
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

        if(CFG.var_saveLog)
            saveLog(e);

        if(getReMinecraft().areChildrenConnected()) {
            notifyChilds(e.getName());
            return;
        }
        if(takeAction(e.getName())){
            if(CFG.var_Suicide) {
                new Thread(() -> {
                    testTillKilled();
                }).start();
            }

            if(CFG.var_reconnect)
                reconnect();

            if(CFG.var_ShutDown)
                System.exit(0);


        }

    }


    public void saveLog(EntityInRangeEvent.Player e){
        String line = "at (" + System.currentTimeMillis() + ") spotted player (" + e.getName() + ").";
        boolean tA = takeAction(e.getName());
        line += " (" + tA + ") ";
        if(tA)
            line += "took Action ";
        else
            line += "didnt do anything ";

        line += "because ";
        line += "(" + howIsRelationWith(e.getName()) + ")";
        line += " (" + sceptical(e.getName()) + ")";

        save(line);
        // TODO save

    }


    public void save(String data){
        try {
            FileWriter fw = new FileWriter(getFilepath(), true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.newLine();
            bw.write(data);

            bw.flush();
            fw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public String getFilepath(){
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();

        String rV = s + "/data/DontTouchMyBotLog" + LocalDateTime.now().getYear() + "-" + LocalDateTime.now().getMonthValue() + "-" + LocalDateTime.now().getDayOfMonth() + ".txt";

        File f = new File(rV);
        if(!f.exists()){
            new File(s + "\\data").mkdir();//.createNewFile();
        }


        return rV;
    }

    private void reconnect() {
// TODO use reconnect methoid...
        this.getReMinecraft().reLaunch();


    }


    private void testTillKilled() {
        double oldX = ReClient.ReClientCache.INSTANCE.posX;
        double oldZ = ReClient.ReClientCache.INSTANCE.posZ;

        double nevX, nevZ;
        nevX = oldX;
        nevZ = oldZ;

        while(dist(oldX, oldZ, nevX, nevZ) < 32){
            this.getReMinecraft().minecraftClient.getSession().send(new ClientChatPacket("/kill"));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            nevX = ReClient.ReClientCache.INSTANCE.posX;
            nevZ = ReClient.ReClientCache.INSTANCE.posZ;
        }

        LoggieTheLogger.log("Successfully committed suicide");

    }

    public double dist(double x1, double y1, double x2, double y2){
        double x = x2 - x1;
        double y = y2 - y1;

        return Math.sqrt(x * x + y * y);
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
            ChatColor col = ChatColor.DARK_RED;
            switch(howIsRelationWith(intruder)) {
                case -1:
                    msg = "ATTENTION: " + intruder + " has entered your field of vision";
                    break;
                case 0:
                    msg = "neutral " + intruder + " has entered your field of vision";

                    col = ChatColor.BLUE;

                    if (sceptical(intruder)) {
                        msg = "ATTENTION: " + intruder + " has entered your field of vision";
                        col = ChatColor.RED;
                    }
                    break;
                case 1:
                    msg = "friendly " + intruder + " has entered your field of vision";
                    col = ChatColor.GREEN;
                    break;
            }


            Message m = Message.fromString(msg);
            m.setStyle(new MessageStyle().setColor(col));
            ServerChatPacket toSend = new ServerChatPacket(m);

            childClient.getSession().send(toSend);
        }
    }

    @Override
    public void registerConfig() {
        this.getReMinecraft().configurations.add(CFG);
    }
}


class Config extends Configuration {
    @ConfigSetting
    public ArrayList<String> var_CanTouchWhitelist;

    @Configuration.ConfigSetting
    public ArrayList<String> var_DontTouchBlackList;


    @Configuration.ConfigSetting
    public boolean var_Suicide;

    @Configuration.ConfigSetting
    public boolean var_reconnect;

    @Configuration.ConfigSetting
    public boolean var_ShutDown;


    @Configuration.ConfigSetting
    public boolean var_saveLog;

    public Config() {
        super("DontTouchMyBot");
        this.var_CanTouchWhitelist = new ArrayList<>();

        this.var_DontTouchBlackList = new ArrayList<>();
        this.var_CanTouchWhitelist.add("IronException");
        this.var_CanTouchWhitelist.add("The2b2tMossad");

        this.var_Suicide = true;
        this.var_reconnect = false;
        this.var_ShutDown = false;

        this.var_saveLog = true;

    }



}
