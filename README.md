# MidnightCore
*A cross-platform library for Minecraft servers, clients, and proxies*
<br/>
Current Version: 2.0.0-pre9

## Overview
- MidnightCore has a number of classes and features to make creating plugins and mods easier.
- The library currently supports Fabric servers and clients, Spigot servers, and Velocity proxies
  - Fabric builds are for the current version (1.20.6), and Spigot supports versions 1.8-1.21.4
- The library is designed to be modular, so anything that a user does not need can be disabled

## Usage
### For Server Owners:
- This library does not offer much on its own for server owners, and should probably only be installed if another plugin
or mod requires it
- Configuration files for the mod can be found in the following locations:
  - `config/MidnightCore` on Fabric servers
  - `plugins/MidnightCore` on Spigot and Velocity servers

### For Players:
- This mod can be installed on clients. Like with servers, it should probably only be installed if another mod depends
on it.
- Configuration files for the mod can be found in the following locations
  - `config/MidnightCore/client` for client-wide configuration
  - `saves/[world]/config/MidnightCore` for single player world configurations

### For Developers:
- This library is most useful for developers.
- To start using it, add the following to your `build.gradle.kts` file:
```
repositories {
    maven("https://maven.wallentines.org/releases")
    maven("https://oss.sonatype.org/content/repositories/snapshots") // For Fabric servers, required for fabric-permissions-api
}
dependencies {
    compileOnly("org.wallentines:midnightcore-common:2.0.0-pre9")
    
    compileOnly("org.wallentines:midnightcore-server:2.0.0-pre9") // For servers
    compileOnly("org.wallentines:midnightcore-client:2.0.0-pre9") // For clients
    compileOnly("org.wallentines:midnightcore-proxy:2.0.0-pre9") // For proxies
    
    compileOnly("org.wallentines:midnightcore-fabric:2.0.0-pre9") // For Fabric servers and clients
    compileOnly("org.wallentines:midnightcore-spigot:2.0.0-pre9") // For Spigot servers
    compileOnly("org.wallentines:midnightcore-velocity:2.0.0-pre9") // For Velocity proxies
}
```
- As shown above, the library is divided into three layers: Common, Environment, and Target
  - The common layer is available in all contexts
  - The environment layer contains three different modules:
    - `client`, which is available when the mod is run on a client
    - `server`, which is available when the mod is run on a dedicated or integrated server
    - `proxy`, which is available when the mod is run on a proxy
  - The target layer, which contains modules implements things from the common and environment layers
    - `fabric`, which is available on Fabric servers and clients
    - `spigot`, which is available on Spigot servers
    - `velocity`, which is available on Velocity servers

## Common
### Configuration
- MidnightCore bundles [MidnightConfig](https://github.com/wallenjos01/MidnightConfig) for configuration

### Text Components
- MidnightCore has a cross-platform text component system which can be used in all contexts
- Text styling is fully supported, including RGB colors, and custom fonts
  - On pre-1.16 servers, RGB colors will be downsampled to their closest equivalent
- Text components can be created using the `Component` class:
  - `Component.text(String)` creates a component with the given literal text
  - `Component.literal(String)` creates a component with the given translation key
  - `Component.keybind(String)` creates a component with the given keybind value
  - `Component.parse(String)` parses a string as a component. 
    - Both raw JSON text and raw text with '&' based color codes are supported
    - In raw text, RGB colors can be specified by writing a '#' and the value after a '&'
      - i.e. `&#35E457Green Text`
- Text components can also be parsed or serialized directly using one of the included serializers:
  - LegacySerializer: Parses plaintext with color codes. Has two variants:
    - `LegacySerializer.INSTANCE`, which parses using '§' codes and doesn't have RGB support 
    - `LegacySerializer.CONFIG_INSTANCE`, which parses using '&' codes and has RGB support
  - ModernSerializer: Parses JSON text as a component
  - PlainSerializer: Creates literal text components using the raw text inputted
  - ConfigSerializer: Capable of parsing both JSON text and legacy text

### Placeholders
- The library has a placeholder system which allows you to insert values into configured text components.
- A `PlaceholderManager` object contains valid placeholders and functions to determine their outputs.
    - There is a global `PlaceholderManager` at `PlaceholderManager.INSTANCE`. It should be used unless you have a good
      reason not to.
- The `PlaceholderSupplier` interface can be extended to create a custom placeholder type.
    - The output of that method can return either a String or a Component.
    - A `PlaceholderSupplier` can be registered to a `PlaceholderManager` using `PlaceholderManager::registerSupplier`
- To parse text with placeholders, use the `UnresolvedComponent` class:
  - The `UnresolvedComponent.parse` function can be used to parse the placeholders from Strings, and the 
`UnresolvedComponent::resolve` method can be used to resolve it to a Component using custom arguments and a
`PlaceholderManager` object as input.
  - The custom arguments are passed to the placeholder suppliers when resolving.
- If you have a one-off placeholder which needs to be present when resolving, use the `CustomPlaceholder` class.

### Skins
- The common module has a data type representing a Player skin
- These can be used to create player heads, or just be saved for later
- Skins can be obtained from Mojang's API using the `MojangUtil` class

### Items
- ItemStacks can be created and manipulated using MidnightCore.
- To create a new ItemStack, use the `ItemStack.Builder` class.
  - Items can be created by ID, using a base type and color, or by specifying a Skin for a Player head
- Item components can be loaded from and saved to MidnightConfig `ConfigObject`s using `ItemStack::loadComponent` and 
`ItemStack::saveComponent`, respectively.

### Events
- MidnightCore bundles [MidnightLib](https://github.com/wallenjos01/MidnightLib) for a few data types and for its event
system.
- Events are used in a number of places throughout the library

### Permissions
- The common module declares a `PermissionHolder` interface, which represents something which can have permissions. On
servers and proxies, this is implemented by players and command sources.


## Server

### Server
- The `Server` interface represents a running server. 
- There is a `Singleton<Server>` at `Server.RUNNING_SERVER`
  - This is populated as soon as the Server starts up, and is reset as soon as it shuts down
  - On Spigot servers, this singleton is populated as soon as MidnightCore is loaded and never reset, as plugins only 
last as long as the server is loaded
  - On Fabric servers, mods are loaded before the server is, so the singleton will not be populated immediately.
  - On Fabric clients, multiple integrated servers can start and stop during the lifetime of the mod, so in order to
cover all use-cases, mods should properly listen to `Server.START_EVENT` and `Server.STOP_EVENT` to determine when
the server is available
- Each server contains some functions for interacting with it and its players, as well as a `ModuleRegistry`

### Player
- The `Player` interface represents an online player
- It contains a number of functions for sending messages, giving items, among other things

### Modules
- Each `Server` has a `ModuleRegistry` containing `ServerModule` instances.
- Additional `ServerModule` types can be created and registered to `ServerModule.REGISTRY`
- Module configuration can be found in a file called `modules.json(.yml)` in the server configuration directory.
- A few modules are enabled by default:
  - `midnightcore:skin`
    - Allows mods to change player skins. 
    - If enabled, it will also automatically download player skins in offline mode according to their username
  - `midnightcore:savepoint`
    - Allows mods to save a user's state and restore it later
  - `midnightcore:session`
    - Allows mods to put users in to temporary "sessions"
    - Player progress is saved when they enter and restored when they leave
    - Player data is saved to disk during a session in case of an untimely server crash
    - Requires `midnightcore:savepoint` to be loaded
  - `midnightcore:messaging`
    - Allows mods to send custom packets to clients and proxies, and listen for custom packets from clients and proxies
  - `midngihtcore:extension`
    - Establishes a standard for querying clients for optional extensions
    - Extensions are implemented on the server using the `ServerExtension` interface
    - Requires `midnightcore:messaging` to be loaded
  - `midnightcore:sql`
    - Allows server admins to specify SQL configurations which other mods can access.
    - Allows mods to easily connect to SQL servers without additional configuration.
    - SQL configurations are stored as "presets," which can be expanded or altered by consumers of the module.
    - Preset configuration options are:
      - `driver`: The JDBC driver to use. Included drivers are: `h2`, `sqlite`, `mysql`, and `mariadb`
      - `url`: The url (or file path) of the database
      - `database`: (Optional) A value to be appended to the end of the url, after a trailing slash. Designed to be set 
in configurations for other mods.
      - `username`: (Optional) The database username.
      - `password`: (Optional) The database username.
      - `table_prefix`: (Optional) A value to prepend to all tables created by the connection. Designed to be set
        in configurations for other mods.
      - `params`: (Optional) A section of key-value pairs which are passed to the database driver when connecting to a
database.

### Inventory GUIs
- Servers can create custom Inventory GUIs and send them to clients
- Use the `InventoryGUI.FACTORY` singleton to create them 

### Custom Scoreboards
- Servers can create custom scoreboards and send them to clients
- Use the `CustomScoreboard.FACTORY` singleton to create them


## Client

### Client
- The `Client` interface represents a running client
- There is a `Singleton<Client>` at `Client.RUNNING_CLIENT`

### Modules
- The client contains a `ModuleRegistry` containing `ClientModule` instances
- Additional `ClientModule` types can be registered at `ClientModule.REGISTRY`
- Two modules are enabled by default:
  - `midnightcore:messaging`
    - Allows clients to send custom packets to the server, and to handle custom packets from servers
  - `midnightcore:extension`
    - Allows clients to communicate with the server `midnightcore:extension` module and declare which extensions they
support
    - Additional `ClientExtension` types can be implemented using the `ClientExtension` interface

## Proxy

### Proxy
- The `Proxy` interface represents a running Proxy
- There is a `Singleton<Proxy>` at `Proxy.RUNNING_PROXY`

### Players
- The `ProxyPlayer` interface represents a player connected to the proxy
- It contains functions to get the username, send messages, or send them to another server, and other things

### Servers
- The `ProxyServer` interface represents a proxied server
- It contains functions to list players on the server, and get its address or name

### Modules
- The proxy contains a `ModuleRegistry` containing `ProxyModule` instances
- Additional `ProxyModule` types can be registered at `ProxyModule.REGISTRY`
- Two modules are enabled by default:
  - `midnightcore:messaging`
    - Allows proxies to send custom packets to servers and clients, and to handle custom packets from servers and 
  clients
  - `midnightcore:extension`
    - Relays extension packets from the server to client and client to server

## Fabric
### Interfaces
- Most interfaces mentioned above (such as `Player` or `Server`) are injected directly into the corresponding classes,
so a `Player` can be obtained from a `ServerPlayer`, by simply casting it.
- To convert in the other direction, (i.e. `Player` to `ServerPlayer`) simply call one of the static `validate`
overloads in `ConversionUtil`

### Text
- Text can be converted as-needed into native Minecraft components using the `WrappedComponent` class

## Spigot
### Interfaces
- The `ItemStack` interface is implemented by `SpigotItem`
- The `Player` interface is implemented by `SpigotPlayer`
- The `Server` interface is implemented by `SpigotServer`
- Each implementation class contains handles to their internal types (i.e. `org.bukkit.inventory.ItemStack` from `SpigotItem`)
- Use the `ConversionUtil.validate()` overloads to get implementation classes from their interfaces

### Commands
- The Spigot module contains a `CommandUtil` class, which contains a function which allows you to dynamically register
commands (rather than registering them in plugin.yml)

### Adapters
- Much of the functionality of this library is implemented through adapters. 
- There is an adapter for each bukkit API revision.
- Adapters can be obtained through the `Adapter.INSTANCE` singleton
- Adapters allow you to do things not available in the regular Bukkit API, including:
  - Getting player skins
  - Querying a player's operator level
  - Getting and setting item NBT
  - Getting the current protocol version
  - Submitting code to be run synchronously on the server thread
  - Running code on the server thread each tick

## Velocity
### Interfaces
- The `Proxy` interface is implemented by `VelocityProxy`
- The `ProxyServer` interface is implemented by `VelocityServer`
- The `ProxyPlayer` interface is implemented by `VelocityPlayer`
- Each implementation class contains handles to their internal types (i.e. `RegisteredServer` from `VelocityServer`) 
- Use the `ConversionUtil.validate()` overloads to get implementation classes from their interfaces


## Compiling
Compiling the library yourself is not recommended for a couple reasons:
- Most Spigot versions since 1.8 need to be compiled using BuildTools, so they will be present in your local maven
repository.
  - This can be disabled by disabling `buildSpigot` in `settings.gradle.kts`
- The gradle plugins [gradle-multi-version](https://github.com/wallenjos01/gradle-multi-version) and 
[gradle-patch](https://github.com/wallenjos01/gradle-patch) need to be available in the local maven repositories.
  - This is so the library can be compiled for multiple Java versions (for legacy spigot support)
  - These libraries are not available in any repositories as of now, so they will need to be compiled and published 
locally