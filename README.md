# MidnightCore

A core library for Minecraft mods and plugins.

*Note: This library is currently in beta!*

<br>
<br>

## Overview
- MidnightCore is a modular library for Minecraft servers and Proxies. It has a number of APIs and features, and is designed to be expandable.
- As of now, the library has versions for Fabric, Spigot and Velocity servers.
- On Spigot, this library has been tested on 1.8.8, 1.9, 1.9.4, 1.10.2, 1.11.2, 1.12.2, 1.13.2, 1.14.4, 1.15.2, 1.16.5, and 1.17.1

<br>

## Usage
### For Users:
- For users, this library will only be useful if there is another mod/plugin which requires it
- In that case, just drop the .jar file corresponding to your server version in the server's mods or plugins folder.
### For Developers:
- For developers, this mod offers quite a few features to make creating mods and plugins easier.
- To use it, simply add this to your `build.gradle` file:
``` 
    repositories {
        maven { url 'https://mdmc.ddns.net/maven' }
    }
    
    dependencies {
        compileOnly('me.m1dnightninja.midnightcore:api:1.0-latest')

        compileOnly('me.m1dnightninja.midnightcore:fabric:1.0-latest') // For Fabric servers
        compileOnly('me.m1dnightninja.midnightcore:spigot:1.0-latest') // For Spigot servers
        compileOnly('me.m1dnightninja.midnightcore:velocity:1.0-latest') // For Velocity servers
    }
```
- For Fabric mods, there is a custom entrypoint available to use. While not required, using it ensures you do not try to access the MidnightCore API before it is initialized.
    - To use it, add an entrypoint to your `fabric.mod.json` file of type `midnightcore:mod` which points to a class which implements `MidnightCoreModInitializer`
- In order to use all the Library's functions, get the global instance of the API by running:
```
MidnightCoreAPI.getInstance()
```

<br>

## APIs

### Config API:
- The Config API offers a way to easily load and manage configuration files. It all starts with the `ConfigSection` class.
- `ConfigSection` objects contain values associated with String keys. Values can be primitives, Lists, or other `ConfigSection` objects.
    - If you attempt to insert another type of object into a `ConfigSection`, MidnightCore will attempt to serialize it.
- `ConfigSection` objects can be loaded from and serialized into JSON or NBT format (plus YAML on Spigot servers).
- Serialization is done in three passes:
    - First, MidnightCore checks if there is a valid `ConfigSerializer` registered for that object's class, which will convert Objects directly into `ConfigSection` objects.
    - If there is no `ConfigSerializer` registered for that object's class, MidnightCore will look for an `InlineSerializer` instead, which does the same except it converts Objects into Strings instead.
    - If all else fails, the `toString()` method will be called on the Object and the output will be stored.
- MidnightCore comes with a few built-in `ConfigSerializer` and `InlineSerializer` objects, but other mods can add more.
    - To make one, simply create a class that implements either `ConfigSerializer` or `InlineSerializer` and register it into the global `ConfigRegistry`, which can retrieved from the API Instance

### Text API:
- MidnightCore offers a Text API which allows for the creation and serialization of Component-based text.
- Styling is fully supported, including RGB and custom Fonts.
    - *Note: On pre-1.16 servers, RGB colors will be automatically converted to their nearest 4-bit color alternative.*
- Components can be created programatically using the `MComponent.createTextComponent()` function, or be parsed from a String.
    - JSON formatted components can be parsed, as well as Legacy color coded text
    - The following two strings will produce the same result when `MComponent.Serializer.parse()` is used on them:

```
1. {"text":"Hello, ","color":"#6c429b","extra":[{"text":"World","color":"red"}]}
2. #6c429bHello, &cWorld
```
- Components can also be serialized back into JSON Strings or Legacy text as seen above.

### Item API:
- The Item API allows you to easily create Item Stacks with the built-in `MItemStack.Builder` class.
- Item NBT is represented as a `ConfigSection` object, and modified freely.
- `MItemStack` objects have a built-in `ConfigSerializer` registered, so they can be loaded directly from `ConfigSection` objects. Example:
```
MItemStack item = section.get("item", MItemStack.class);
```

### Player API:
- MidnightCore offers a wrapper around Players through the `MPlayer` class. A number of functions are available, including getting their name or teleporting them.
- Players are stored in a `PlayerManager` class, which can be retrieved from the global API instance.

### Event API (Fabric Only):
- MidnightCore offers an Event API for Fabric servers.
- A number of Events have been added, with more planned in the future, and mods can create their own Events as well by extending the `Event` class.
- To listen for an event, simply tell the Event class which event, the listener object, and what function to call when the event is triggered. Example:
```
Event.register(PlayerJoinEvent.class, this, this::onJoin)
```
- To call an event, just run `Event.invoke()` with an instance of the event as an argument.

### Module API:
- MidnightCore is a modular library, and most features that register listeners or commands will be contained in a module.
- Individual modules can be configured and disabled, as to not disrupt player's experiences.
    - *Note: A mod may fail to start or work properly if a module it depends on is disabled!*
- Modules can be created and loaded by other mods, too:
    - Start by simply creating a class that extends `IModule`, and implementing at least `boolean initialize(ConfigSection)`, `MIdentifier getId()`, and `ConfigSection getDefaultConfig()`
    - Register it to load on startup by intercepting the `MidnightCoreLoadModulesEvent` on Spigot, or adding to the return value of `getModules()` on Fabric in the `MidnightCoreModInitializer`.
    - You can also load it at runtime by getting the `ModuleRegistry` from the global API instance and passing it into `loadModule()`
- Modules can be retrieved from the `ModuleRegistry` by either specifying their ID, or passing in a class that extends `IModule`. Examples:
```
IModule module = MidnightCoreAPI.getInstance().getModuleById("midnightcore:skin");
ILangModule langModule = MidnightCoreAPI.getInstance().getModule(ILangModule.class);
```

### Other Useful Classes:
- Titles and ActionBars can be sent to players through the use of `MTitle` and `MActionBar` objects.
- `MInventoryGUI` objects allow for the creation of multi-page inventory GUIs using `MItemStack` objects.
- `MTimer` objects are functional timers that can be shown to players in their Action Bar
- `MScoreboard` objects can be used to create custom scoreboards that only certain players can see, and are easily customizable using `MComponent` objects.

<br>

## Modules

### Lang Module
- ID: "midnightcore:lang" *Enabled by default*
- The Lang module allows you to easily load and display configurable messages to players.
- It also supports translations, and MidnightCore will automatically try to use the server's primary language when loading messages.
- On 1.12 servers and up, player's send their current language when they log in and each time they change it, and the Lang Module will attempt to show them messages in their language if they are present.
- To use it, simply create a `LangProvider` object using `createLangProvider()`, passing in the folder to search for entries in, and a `ConfigSection` containing the default entries.

### Player Data Module
- ID: "midnightcore:player_data" *Enabled by default*
- The Player Data module allows persistent data associated with to be stored.
- To use it, a `IPlayerDataProvider` object must be obtained.
    - `IPlayerDataProvider` objects contain a folder in which to search for player data files.
    - `IPlayerDataProvider` objects can be created using `createProvider()`, passing in the folder to search for player data files.
    - The module also contains a global `IPlayerDataProvider` object, which searches for entries in the `MidnightCore/data` folder by default.

### Save Point Module
- ID: "midnightcore:save_point" *Enabled by default*
- The Save Point module can be used to create named save points which store a player's state, and can be loaded later.
- Save a player's state using `savePlayer()`, passing in the player and the name of the save point to create.
- Load a save point using `loadPlayer()`, passing in the player and the name of the save point to load.

### Skin Module
- ID: "midnightcore:skin" *Enabled by default*
- The Skin module allows you to change player's skins.
- Load a new skin by calling `setSkin()`, passing in the player and the new skin.
- Update a player's appearance by calling `updateSkin()`, passing in the player to update.
- On offline mode servers, the skin module will attempt to find and apply skins for players based on their usernames when they log in. (This can be disabled)

### Vanish Module
- ID: "midnightcore:vanish" *Enabled by default*
- The vanish module allows you to hide players for some or all other players on the server.
- Use `hidePlayer()` or `showPlayer()`, passing in the player in question to hide or show them for everyone.
- Use `hidePlayerFor()` or `showPlayerFor()`, passing in the player in question and the affected players to hide or show them for those players.

### Dimension Module (Fabric Only)
- ID: "midnightcore:dimension" *Enabled by default*
- The Dimension module allows for the dynamic loading and unloading of worlds at runtime on Fabric servers.
- Using this module, the `WorldCreator` class can be used to create worlds in a very similar way to Bukkit's implementation.

### Last Joined Module (Velocity Only)
- ID: "midnightcore:last_joined" *Enabled by default*
- The Last Joined module will log the server a player is on when they log out, and attempt to reconnect them to it when they log back in.

### Plugin Message Module (Velocity Only)
- ID: "midnightcore:plugin_message" *Enabled by default*
- The Plugin Message module allows mods to easily create handlers for specific plugin messages from Spigot/Fabric servers.
- By default, this module registers a handler for "midnightcore:send", which will send the target player the specified server.

<br>