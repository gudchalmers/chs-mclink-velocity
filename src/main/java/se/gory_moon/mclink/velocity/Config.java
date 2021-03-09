package se.gory_moon.mclink.velocity;

import com.google.common.collect.Lists;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {

    private final File path;
    private Toml toml;
    private File f;

    public Config(File path) {
        this.path = path;
        load();
    }

    private void load() {
        f = new File(path,"config.toml");
        if (!f.exists()) {
            try {
                path.mkdirs();
                f.createNewFile();
                save(getDefaultFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        toml = new Toml().read(f);
        save(toml.to(ConfigDefaults.class));
    }

    private void save(Object t) {
        try {
            new TomlWriter().write(t, f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object getDefaultFile() {
        return new ConfigDefaults();
    }

    public String getMcLinkBackend() {
        return toml.getString("mclink_backend");
    }

    public List<String> getIgnoredServers() {
        return toml.getList("ignored_servers");
    }

    public String getToken() {
        return toml.getString("token");
    }

    public String getPermission() {
        return toml.getString("permission");
    }

    public void reload() {
        load();
    }

    @SuppressWarnings("unused")
    static class ConfigDefaults {
        String mclink_backend = "https://auth.mc.chs.se/";
        List<String> ignored_servers = Lists.newArrayList("auth");
        String permission = "velocity.command.server";
        String token = "";
    }

}
