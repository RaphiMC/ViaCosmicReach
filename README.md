# ViaCosmicReach
[ViaProxy](https://github.com/RaphiMC/ViaProxy) plugin which allows Minecraft clients to join [CosmicReach](https://finalforeach.itch.io/cosmic-reach) servers.

**Supported Cosmic Reach server versions:** 0.3.6  
**Supported Minecraft client versions:** 1.20-1.21.3

## Features
**This project is mostly for fun and most likely won't ever be fully feature complete.**
- [x] Joining
- [x] Itch.io account authentication
- [x] Chat
- [x] Chunks
- [x] Movement
- [x] Block updates
- [x] Player spawning
- [ ] Entity spawning
- [ ] Items
- [ ] Inventory Containers
- [ ] Block breaking
- [ ] Block placing
- [ ] Block interaction
- [ ] Entity interaction
- [ ] Sounds

## Installation and Usage
1. Download the latest [ViaProxy dev build](https://ci.viaversion.com/job/ViaProxy/lastSuccessfulBuild/) (Get the jar that doesn't have -sources in the name).
2. Download the latest ViaCosmicReach version from [GitHub Actions](https://github.com/RaphiMC/ViaCosmicReach/actions/workflows/build.yml) (Click on the latest entry in the list and download the Artifacts).
3. Put the ViaCosmicReach jar file (Which you get by opening the downloaded zip file) into the plugins folder of ViaProxy (Start ViaProxy once to generate that folder)
4. Run ViaProxy again. You should now be able to select the CosmicReach protocol version in the ViaProxy GUI.

To join online mode servers, you have to add your itch.io account in the Accounts tab of the ViaProxy GUI and switch the authentication mode in the General tab to `Use account`.

For ViaProxy usage instructions, please refer to the [ViaProxy documentation](https://github.com/ViaVersion/ViaProxy?tab=readme-ov-file#usage-for-players-gui).

## Contact
If you encounter any issues, please report them on the
[issue tracker](https://github.com/RaphiMC/ViaCosmicReach/issues).  
If you just want to talk or need help using ViaCosmicReach feel free to join my
[Discord](https://discord.gg/dCzT9XHEWu).
