package me.m1dnightninja.midnightcore.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonWrapper {

    private File file;
    private JsonObject root;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public JsonWrapper() {
        this.root = new JsonObject();
    }

    public JsonWrapper(JsonObject obj) {
        this.root = obj;
    }

    public JsonWrapper(JsonObject obj, File file) {
        this.file = file;
        this.root = obj;
    }

    public JsonWrapper(File file) {
        this.file = file;
        this.root = new JsonObject();
    }

    public JsonObject getRoot() {
        return root;
    }

    public void setRoot(JsonObject obj) {
        this.root = obj;
    }

    public boolean save() {
        if(file == null) {
            return false;
        }
        return save(file);
    }

    public boolean load() {
        if(file == null) {
            return false;
        }
        return load(file);
    }

    public boolean save(File file) {

        if(file == null || file.isDirectory()) {
            MidnightCoreAPI.getLogger().warn("Unable to create file to save JSON file!");
            return false;
        }

        if(!file.exists()) {
            try {
                if(!(file.createNewFile() && file.setWritable(true) && file.setReadable(true))) {
                    MidnightCoreAPI.getLogger().warn("Unable to create " + file.getName() + "!");
                }
            } catch (IOException ex) {
                MidnightCoreAPI.getLogger().warn("An exception occurred while trying to create file " + file.getName() + "!");
                ex.printStackTrace();
                return false;
            }
        }

        if(root == null) {
            return false;
        }

        try {

            OutputStream stream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);

            GSON.toJson(root, writer);

            writer.close();
            stream.close();

            return true;

        } catch(IOException ex) {
            MidnightCoreAPI.getLogger().warn("An exception occurred while trying to write to file " + file.getName() + "!");
            ex.printStackTrace();
            return false;
        }

    }

    public boolean load(File file) {
        if(file == null || file.isDirectory() || !file.exists()) {
            MidnightCoreAPI.getLogger().warn("Unable to locate JSON file!");
            return false;
        }

        try {

            InputStream stream = new FileInputStream(file);
            boolean out = load(stream);

            stream.close();
            return out;

        } catch (IOException e) {
            MidnightCoreAPI.getLogger().warn("An exception occurred while trying to read file " + file.getName() + "!");
            e.printStackTrace();
            return false;
        }

    }

    public boolean load(InputStream stream) {

        try {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            root = GSON.fromJson(reader, JsonObject.class);

            reader.close();
            return true;

        } catch(IOException ex) {
            MidnightCoreAPI.getLogger().warn("An exception occurred while trying to read JSON from stream!");
            ex.printStackTrace();

            return false;
        }
    }

    public static JsonWrapper loadFromFile(File file) {

        if(!file.exists()) {
            MidnightCoreAPI.getLogger().warn("Unable to load JSON from file " + file.getName() + "!");
            return null;
        }

        JsonWrapper out = new JsonWrapper(file);
        if(!out.load()) {
            MidnightCoreAPI.getLogger().warn("Unable to load JSON from file " + file.getName() + "!");
            return null;
        }

        return out;
    }

}
