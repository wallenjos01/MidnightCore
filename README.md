# MidnightCore

A core library for Minecraft mods and plugins.

*Note: This library is currently in beta!*

<br>

## Overview
- MidnightCore is a modular library for Minecraft servers and Proxies. It has a number of APIs and features, and is designed to be expandable.
- As of now, the library has versions for Fabric, Spigot and Velocity servers, with support planned for BungeeCord and Forge as well.
- On Spigot, this library has only been partially tested on 1.18.2. See the note at the bottom of this README for more information

<br>

## Usage
### For Users:
- For users, this library will only be useful if there is another mod/plugin which requires it
- In that case, just drop the .jar file corresponding to your server version in the server's mods or plugins folder.
### For Developers:
- For developers, this mod offers quite a few features to make creating mods and plugins easier.
- To use it, simply add this to your  `build.gradle` file:
``` 
repositories {
    maven { url 'https://maven.wallentines.org/' }
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' } // For Fabric servers, required for fabric-permissions-api
}
dependencies {
    compileOnly('org.wallentines.midnightcore:api:1.0-SNAPSHOT')
    compileOnly('org.wallentines.midnightcore:fabric:1.0-SNAPSHOT') // For Fabric servers
    compileOnly('org.wallentines.midnightcore:spigot:1.0-SNAPSHOT') // For Spigot servers
    compileOnly('org.wallentines.midnightcore:velocity:1.0-SNAPSHOT') // For Velocity servers
}
```
- For Fabric mods, there is a custom entrypoint available to use. While not required, using it ensures you do not try to access the MidnightCoreAPI before it is initialized.
    - To use it, add an entrypoint to your `fabric.mod.json` file of type `midnightcore` which points to a class which implements `ModInitializer`
    - You can also add an entrypoint of type `midnightcore:<version>` which will only load on the specified Minecraft version. (For example, an entry point called `midnightcore:1.18.2` will only load on Minecraft 1.18.2.
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
- Components can be created programatically by instantiating as subclass of `MComponent`, (e.g. `MTextComponent` or `MTranslateComponent`) or be parsed from a String.
    - JSON formatted components can be parsed, as well as Legacy color coded text
    - The following two strings will produce the same result when `MComponent.parse()` is used on them:

```
1. {"text":"Hello, ","color":"#6c429b","extra":[{"text":"World","color":"red"}]}
2. #6c429bHello, &cWorld
```
- Components can also be serialized back into JSON Strings or Legacy text as seen above.

### Item API:
- The Item API allows you to easily create Item Stacks with the built-in `MItemStack.Builder` class.
- Item NBT is represented as a `ConfigSection` object, and can be modified freely.
- `MItemStack` objects have a built-in `ConfigSerializer` registered, so they can be loaded directly from `ConfigSection` objects. Example:
```
MItemStack item = section.get("item", MItemStack.class);
```

### Player API:
- MidnightCore offers a wrapper around Players through the `MPlayer` class. A number of functions are available, including getting their name or teleporting them.
- Players are stored in a `PlayerManager` class, which can be retrieved from the global API instance through the `getPlayerManager()` function.

### Event API:
- MidnightCore offers an Event API through MidnightLib.
- This can be used on any platform, but most builtin events are only available on Fabric, because other platforms come with their own event systems.
- A number of Events have been added, with more planned in the future, and mods can create their own Events as well by extending the `Event` class.
- To listen for an event, simply tell the Event class which event, the listener object, and what function to call when the event is triggered. Example:
```
Event.register(PlayerJoinEvent.class, this, this::onJoin)
```
- To call an event, just run `Event.invoke()` with an instance of the event as an argument.

### Permissions API:
- `MPlayer` objects contain a `hasPermission()` method for checking permissions.
- The Fabric version of MidnightCore contains `fabric-permissions-api` by Lucko for permissions compatibility.

### Module API:
- MidnightCore is a modular library, and most features that register listeners or commands will be contained in a module.
- Individual modules can be configured and disabled, as to not disrupt player's experiences.
    - *Note: A mod may fail to start or work properly if a module it depends on is disabled!*
- Modules can be created and loaded by other mods, too:
    - Start by simply creating a class that extends `Module<MidnightCoreAPI>`, and implementing at least `boolean initialize(ConfigSection)`
    - Register it to load on startup by intercepting the `MidnightCoreLoadModulesEvent`, and adding a `ModuleInfo<MidnightCoreAPI>` instance to the return value of `getModuleRegistry()`
    - You can also load a module at runtime by getting the `ModuleManager` from the global API instance and passing it into `loadModule()`
- Modules can be retrieved from the `ModuleManager` by either specifying their ID, or passing in a class that extends `Module`. Examples:
```
Module module = MidnightCoreAPI.getInstance().getModuleById("midnightcore:skin");
LangModule langModule = MidnightCoreAPI.getInstance().getModule(LangModule.class);
```

### Other Useful Classes:
- `InventoryGUI` objects allow for the creation of multi-page inventory GUIs using `MItemStack` objects.
- `CustomScoreboard` objects can be used to create custom scoreboards that only certain players can see, and are easily customizable using `MComponent` objects.

<br>

## Modules

### Lang Module
- ID: "midnightcore:lang" *Enabled by default*
- The Lang module allows you to easily load and display configurable messages to players.
- It also supports translations, and MidnightCore will automatically try to use the server's primary language when loading messages.
- On 1.12 servers and up, player's send their current language when they log in and each time they change it, and the Lang Module will attempt to show them messages in their language if they are present.
- To use it, simply create a `LangProvider` object using `createProvider()`, passing in a path to the folder to search for entries in, and a `ConfigSection` containing the default entries.

### Data Module
- ID: "midnightcore:data" *Enabled by default*
- The Player Data module allows persistent data to be stored on disk.
- To use it, a `DataProvider` object must be obtained.
    - `DataProvider` objects contain a folder in which to search for data files.
    - `DataProvider` objects can be created using `getOrCreateProvider()`, passing in the folder to search for player data files.
    - The module also contains a global `DataProvider` object, accessed using `getGlobalProvider()`, which searches for entries in the `MidnightCore/data` folder by default.

### Save Point Module
- ID: "midnightcore:savepoint" *Enabled by default*
- The Save Point module can be used to create named save points which store a player's state, and can be loaded later.
- Save a player's state using `savePlayer()`, passing in the player and the name of the save point to create.
- Load a save point using `loadPlayer()`, passing in the player and the name of the save point to load.
- Delete a save point using `removeSavePoint()`, passing in the player and the name of the save point to delete.
- *Note: Save points are not persistent by default! Mods that require persistent storage will need to combine this module with the Data module or something to that effect*

### Skin Module
- ID: "midnightcore:skin" *Enabled by default*
- The Skin module allows you to change player's skins.
- Load a new skin by calling `setSkin()`, passing in the player and the new skin.
- Update a player's appearance by calling `updateSkin()`, passing in the player to update.
- On offline mode servers, the skin module will attempt to find and apply skins for players based on their usernames when they log in. (This can be disabled)
- The Skin module also offers a `Skinnable` interface which can be implemented by custom entites or any class that receive skins.
    - The interface contains `getSkin()`, `setSkin()`, and `resetSkin()` methods.
    - By default, the `MPlayer` class implements the `Skinnable` interface
    - On Fabric servers, a Mixin is applied to the `ServerPlayer` class, which also implements the `Skinnable` interface 
    - *Note: The Skin Module **must** be enabled for the default implementations of `Skinnable` to function!*

### Vanish Module
- ID: "midnightcore:vanish" *Enabled by default*
- The vanish module allows you to hide players for some or all other players on the server.
- Use `vanishPlayer()` or `revealPlayer()`, passing in the player in question to hide or show them for everyone.
- Use `vanishPlayerFor()` or `revealPlayerFor()`, passing in the player in question and the affected players to hide or show them for those players.

### Plugin Message Module
- ID: "midnightcore:messaging" *Enabled by default*
- The Plugin Message module allows mods to easily send and handle messages between servers and proxies.
- Messages can be sent to the server/proxy (depending on which platform the module is running) by calling the `sendMessage()` method, passing in a `MPlayer` object, a message ID, a `ConfigSection` containing data.
    - *Note: There **must** be at least one player online the server to send or receive messages on that particular server. This is a limitation of how the packet system works. If you need to send messages between proxy and servers when there aren't any players online, you will need to use sockets or somehing similar.*
- By default, the Velocity version of this module module registers a handler for messages with ID "midnightcore:send", which will send the target player the specified server.

### Dimension Module (Fabric Only)
- ID: "midnightcore:dimension" *Enabled by default*
- The Dimension module allows for the dynamic loading and unloading of worlds at runtime on Fabric servers.
- Using this module, the `WorldCreator` class can be used to create worlds in a very similar way to Bukkit's implementation.

### Last Joined Module (Velocity Only)
- ID: "midnightcore:last_joined" *Enabled by default*
- The Last Joined module will log the server a player is on when they log out, and attempt to reconnect them to it when they log back in.

### Global Join Messages Module (Velocity Only)
- ID: "midnightcore:global_join_messages" *Disabled by default*
- The Global Join Messages module will send a join message to all players when a player joins the proxy, regardless of which server they join to. It also sends messages when a player changes servers or leaves the proxy.
- It is recommended to use this with a mod/plugin that disables join messages on the individual servers, as well as some sort of global chat implementation, in order to avoid confusing players.
- *Note: The module is disabled by default due to the lack of an implementation of the above recommendations bundled with MidnightCore. This module may end up being moved to another project in the future to avoid confusion, such as MidnightEssentials or MidnightChat*
- *Warning: This module does not have configurable messages yet. Currently, only server names are configurable. This are plans to update this in the future.*

<br>

## A Note on Spigot Support

Spigot support is currently a work in progress. Nearly all functions work on Spigot 1.18.2, but advanced features that require packets such as changing Skins or sending RGB messages will be disabled on earlier versions. There are plans to update to support all versions down to 1.8 in the near future, but as of now those functions remain unimplemented. Spigot is not my highest priority, as I have switched to using Fabric for all my personal servers, but I do intend on having stable Spigot support in MidnightCore as soon as possible. 

<br>