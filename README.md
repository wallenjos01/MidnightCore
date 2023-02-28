# MidnightCore

A core library for Minecraft mods and plugins.

*Note: This library is currently in beta!*

<br>

## Overview
- MidnightCore is a modular library for Minecraft servers and Proxies. It has a number of APIs and features, and is designed to be expandable.
- As of now, the library has versions for Fabric, Spigot and Velocity servers, with support planned for BungeeCord and Forge as well.
- On Spigot, this library has been partially tested on versions 1.8 - 1.19.3. All features appear to be working, but not every feature has been thoroughly tested on every version.

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
    maven { url 'https://maven.wallentines.org/' }
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' } // For Fabric servers, required for fabric-permissions-api
}
dependencies {
    compileOnly('org.wallentines.midnightcore:api:1.0-beta10')
    compileOnly('org.wallentines.midnightcore:fabric:1.0-beta10') // For Fabric servers and clients
    compileOnly('org.wallentines.midnightcore:spigot:1.0-beta10') // For Spigot servers
    compileOnly('org.wallentines.midnightcore:velocity:1.0-beta10') // For Velocity servers
    compileOnly('org.wallentines.midnightcore:client:1.0-beta10') // For clients
}
```
- For Fabric mods, there is a custom entrypoint available to use. While not required, using it ensures you do not try to access the MidnightCoreAPI before it is initialized.
    - To use it, add an entrypoint to your `fabric.mod.json` file of type `midnightcore` which points to a class which implements `ModInitializer`
    - You can also add an entrypoint of type `midnightcore:<version>` which will only load on the specified Minecraft version. (For example, an entry point called `midnightcore:1.19.3` will only load on Minecraft 1.19.3.
- In order to use all the Library's functions, get the global instance of the API by running:
```
MidnightCoreAPI.getInstance()
```
- Much of the API requires a server to be running, which is not always true (Right when mods are loaded or before a world has been selected on Fabric clients)
  - To get around this, use `MidnightCoreAPI.onServerStartup(Consumer<MServer>)`, which will run the supplied code each time a server starts up. (Or immediately if a server is already running)

<br>

## APIs

### Config API:
- Configuration is provided by [MidnightConfig](https://github.com/M1dnightNinja/MidnightConfig)

### Text API:
- MidnightCore offers a Text API which allows for the creation and serialization of Component-based text.
- Styling is fully supported, including RGB and custom Fonts.
    - *Note: On pre-1.16 servers, RGB colors will be automatically converted to their nearest 4-bit color alternative.*
- Components can be created programmatically by instantiating as subclass of `MComponent`, (e.g. `MTextComponent` or `MTranslateComponent`) or be parsed from a String.
    - JSON formatted components can be parsed, as well as Legacy color coded text
    - The following two strings will produce the same result when `MComponent.parse()` is used on them:

```
1. {"text":"Hello, ","color":"#6c429b","extra":[{"text":"World","color":"red"}]}
2. #6c429bHello, &cWorld
```
- Components can also be serialized back into JSON Strings or Legacy text as seen above via `toConfigText()` and `toJSONText()`

### Placeholder API
- The Placeholder API allows text with placeholders to be parsed using a specific context.
- Placeholders can be applied and registered using a `PlaceholderManager` object.
  - A global `PlaceholderManager` object is stored at `PlaceholderManager.INSTANCE`.
- To apply placeholders, run `PlaceholderManager.INSTANCE.parseText(String, Object...)`
  - The object array passed to this function is the context by which things will be parsed.
    - For example, if this contains a player, the `%player_name%` placeholder will return that player's name
- Placeholders can be either inline (Strings) or MComponent-based.
  - The inline placeholder registry can be accessed by calling `PlaceholderManager.INSTANCE.getInlinePlaceholders()`
  - The component placeholder registry can be accessed by calling `PlaceholderManager.INSTANCE.getPlaceholders()`

### Item API:
- The Item API allows you to easily create Item Stacks with the built-in `MItemStack.Builder` class.
- Item NBT is represented as a `ConfigSection` object, and can be modified freely.
- `MItemStack` objects have a built-in `Serializer`, so they can be loaded directly from `ConfigSection` objects. Example:
```
MItemStack item = section.get("item", MItemStack.SERIALIZER);
```

### Server API:
- MidnightCore abstracts servers into a `MServer` class.
- `MServer` objects contain a module registry, and a few functions for interacting with the server.
- The currently running server can be accessed via the global API instance.

### Player API:
- MidnightCore offers a wrapper around Players through the `MPlayer` class. A number of functions are available, including getting their name or teleporting them.
- Players are stored in a `PlayerManager` class, which are contained within `MServer` objects.

### Event API:
- MidnightCore offers an Event API through [MidnightLib](https://github.com/M1dnightNinja/MidnightLib).
- This can be used on any platform, but most builtin events are only available on Fabric, because other platforms come with their own event systems.
- A number of Events have been added, with more planned in the future, and mods can create their own Events as well.
- To listen for an event, simply tell the Event class which event, the listener object, and what function to call when the event is triggered. Example:
```
Event.register(PlayerJoinEvent.class, this, this::onJoin)
```
- To call an event, just run `Event.invoke()` with an instance of the event as an argument.

### Permissions API:
- `MPlayer` objects contain a `hasPermission()` method for checking permissions.
- The Fabric version of MidnightCore contains `fabric-permissions-api` by [Lucko](https://lucko.me/) for permissions compatibility.

### Lang API:
- The Lang API allows you to easily load and display configurable messages to players.
- It also supports translations, and the Lang API will automatically try to use the server's primary language when loading messages.
- On 1.12 servers and up, player's send their current language when they log in and each time they change it, and the Lang API will attempt to show them messages in their language if they are present.
- To use it, simply create a `LangProvider` object, passing in a path to the folder to search for entries in, and a `LangRegistry` or `ConfigSection` containing the default entries.
- When parsing text, placeholders from the global `PlaceholderManager` instance are used.
  - Context for these placeholders can be passed in to the end of the `getMessage()` function.

### Data API:
- The Player Data API allows persistent data to be stored on disk.
- To use it, a `DataProvider` object must be obtained.
  - `DataProvider` objects contain a folder in which to search for data files.
  - `DataProvider` objects can be easily constructed, passing in the folder to search for player data files.

### Module API:
- MidnightCore is a modular library, and most features that register event listeners will be contained in a module.
- Individual modules can be configured and disabled, so modules a server owner doesn't need can be disabled.
    - *Note: A mod may fail to work properly if a module it depends on is disabled!*
- Modules are created and initialized on server startup and destroyed on server shutdown. This applies to integrated servers, too.
- Modules can be created and loaded by other mods, too:
    - Start by simply creating a class that extends `ServerModule`, and implementing at least `boolean initialize(ConfigSection, MServer)`
    - Register it to be loaded by adding it to the `Registries.MODULE_REGISTRY` object.
    - You can also load a module at runtime by getting the `ModuleManager` from the currently running instance and passing a module into `loadModule()`
- Modules can be retrieved from the `ModuleManager` by either specifying their ID, or passing in a class that extends `Module`. Examples:
```
Module module = MidnightCoreAPI.getInstance().getModuleById("midnightcore:skin");
SavepointModule langModule = MidnightCoreAPI.getInstance().getModule(SavepointModule.class);
```
- There are also client modules, which are loaded on client startup and exist for the lifetime of the running application.
  - Client modules should extend `ClientModule` and can be registered through being added to `ClientRegistries.CLIENT_MODULE_REGISTRY`

### Other Useful Classes:
- `InventoryGUI` objects allow for the creation of multi-page inventory GUIs using `MItemStack` objects.
- `CustomScoreboard` objects can be used to create custom scoreboards that only certain players can see, and are easily customizable using `MComponent` objects.

<br>

## Modules

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

### Session Module
- ID: "midnightcore:session" *Enabled by default*
- The Session module offers a way for servers to put players into temporary "sessions".
- This module depends on the Save point module, and player data will automatically be saved when entering a session, and restored when leaving.
- When the server shuts off or a player quits, they are automatically removed from the session.
- Sessions immediately shut down when the last player leaves.
- This module is useful for creating minigames or other systems in which players need to be put into a different state temporarily.

### Plugin Message Module
- ID: "midnightcore:messaging" *Enabled by default*
- The Plugin Message module allows mods to easily send and handle messages between servers and proxies.
- Messages can be sent to the server/proxy (depending on which platform the module is running) by calling the `sendMessage()` method, passing in a `MPlayer` object, a message ID, a `ConfigSection` containing data.
    - *Note: There **must** be at least one player online the server to send or receive messages on that particular server. This is a limitation of how the packet system works. If you need to send messages between proxy and servers when there aren't any players online, you will need to use sockets or something similar.*
- By default, the Velocity version of this module registers a handler for messages with ID "midnightcore:send", which will send the target player a specified server.

### Extension Module
- ID: "midnightcore:extension" *Enabled by default*
- The Extension module is designed to allow servers to recognize and utilize client mods.
- Extensions are entirely optional by default, meaning that players who do not install all or any extensions supported by the server will not be kicked.
- This allows server owners to offer expanded functionality for those who choose to install mods, while still allowing vanilla clients to join.
- Extension mods can be created by creating client and server extensions:
  - Client extensions should extend the `ClientExtension` class, and be added to the `ClientExtension.REGISTRY` registry.
  - Server extensions should extend the `ServerExtension` class, and be added to the `ServerExtension.REGISTRY` registry.
- *Note: For Fabric servers behind Velocity proxies, all supported extensions will need to be added to a list in the Velocity module's config, called "query_extensions". Alternatively, the value "delay_send" can be set to true in the Fabric server module's config*

### Last Joined Module (Velocity Only)
- ID: "midnightcore:last_joined" *Enabled by default*
- The Last Joined module will log the server a player is on when they log out, and attempt to reconnect them to it when they log back in.

### Global Join Messages Module (Velocity Only)
- ID: "midnightcore:global_join_messages" *Disabled by default*
- The Global Join Messages module will send a join message to all players when a player joins the proxy, regardless of which server they join to. It also sends messages when a player changes servers or leaves the proxy.
- It is recommended to use this with a mod/plugin that disables join messages on the individual servers, as well as some sort of global chat implementation, in order to avoid confusing players.
- *Note: The module is disabled by default due to the lack of an implementation of the above recommendations bundled with MidnightCore. This module may end up being moved to another project in the future to avoid confusion, such as MidnightEssentials or MidnightChat*
- *Warning: This module does not have configurable messages yet. Currently, only server names are configurable. There are plans to update this in the future.*

### Regarding the Dynamic Level Module
The dynamic level system has been moved to its own mod: [DynamicLevelLoader](https://github.com/M1dnightNinja/DynamicLevelLoader)

<br>