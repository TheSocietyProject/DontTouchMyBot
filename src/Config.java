import com.sasha.reminecraft.Configuration;

import java.util.ArrayList;

public class Config extends Configuration {
    @ConfigSetting
    public ArrayList<String> var_DontTouchWhitelist = new ArrayList<>();

    {
        var_DontTouchWhitelist.add("Friedolin2000");
        var_DontTouchWhitelist.add("The2b2tMossad");
    }

    public Config(String configName) {
        super(configName);
    }
}
