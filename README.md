# Hexcode

Hexcode is a component-based spellcasting system. 

If you are looking for how to play hexcode, look to https://docs.hexcodec.com/ for the full documentation with guides, videos, and lists



## Setting Up The Dev Environment

Required:
- Java 25
- Hytale

Edit `./gradle.properties` and set `hytale.install_dir` to the location of where Hytale is installed

On windows, do `./gradlew.bat` - on Linux/Mac do `./gradlew` for running commands. Alternatively, from an IDE, run the gradle commands via an extension. 

1. Run `./gradlew init` to set up the gradle project
2. Optionally run `./gradlew decompileServer` to get editor autocomplete support
3. Run `./gradlew runServer` to begin the dev server
4. Connect to `localhost` in hytale to join the dev server

Reach out to me on discord if you encounter any issues at `Riprod` or join the discord at https://discord.hexcodec.com

## Notes

Hexcode has one major split between `./core` and `./builtin`

Anything in `./core` should be "pure" or engine-level setup. Ideally, it wont hold any implementation code.
Anything in `./builtin` is the implementation of the stuff setup in `./core`. You shouldn't import anything from Builtin -> Core. This is a one-direction dependency


More will be added here later as the public api is developed. As hexcode is in beta, writing much more about integration here will result in stale documentation down the road. 
