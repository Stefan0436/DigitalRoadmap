# DigitalRoadmap 
DigitalRoadmap is a simple java program for project roadmaps, inspired by Scrum.


# Building
You can build the project by using the following commands depending on your operating system.

For Linux:

```bash
chmod +x gradlew
./gradlew build
```

For Windows:

```batch
.\gradlew.bat build
```

Both Windows and Linux output in `build/libs`, the `all` jar is runnable.


# Installing into a Linux desktop environment
On linux, you can install the program to the application menu (and associate the drf extension) by using the following commands:

```bash
chmod +x gradlew
./gradlew build

sudo cp build/libs/DigitalRoadmap-1.0.0.A1-all.jar /usr/lib/digitalroadmap.jar

cp digitalroadmap-exec.desktop digitalroadmap.desktop ~/.local/share/applications
xdg-mime install asf-digitalroadmap.xml
xdg-mime default digitalroadmap-exec.desktop application/x-digitalroadmap
```
