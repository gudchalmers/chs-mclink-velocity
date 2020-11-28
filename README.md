# CHS MCLink Velocity

This is the plugin that authenticates players as they login against [gudchalmers/chs-mclink-backend][1] and kicks them if they are not linked.

## Requirements

- An instance of [gudchalmers/chs-mclink-backend][1] running
- An instance of [Velocity 1.1.0+][2] to run the plugin on

## Setup

To build the plugin run:

```shell script
# Windows
gradlew.bat shadowJar
```
```shell script
# Linux
./gradlew shadowJar
```

The plugin will be located in `build/libs/` when done building.

Once the plugin have run once a config file have been created in `velocity_install/plugins/mclink/config.toml`.

The default config looks like this:

```toml
mclink_backend = "https://auth.mc.chs.se/"
auth_server = "auth"
permission = "velocity.command.server"
token = ""
```

- `mclink_backend` is the base url to the backend server running [gudchalmers/chs-mclink-backend][3].
- `auth_server` is the server to ignore connections to as it's the one running [gudchalmers/chs-mclink][1] to auth players.
- `permission` if the player have this permission they bypass the auth check.
- `token` is the shared key from [gudchalmers/chs-mclink-backend][3] to auth the api.

## License

[MIT][4]

[1]: https://github.com/gudchalmers/chs-mclink
[2]: https://velocitypowered.com/
[3]: https://github.com/gudchalmers/chs-mclink-backend
[4]: https://choosealicense.com/licenses/mit/
