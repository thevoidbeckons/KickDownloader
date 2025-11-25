How to use:

Install [ffmpeg](https://www.ffmpeg.org/download.html), I used a package manager called [chocolately](https://chocolatey.org/install#individual) to install it.
JDK 17 Required

Build the fatjar or run the project from IntelliJ. 

Inputs:
kick channel name
vod file location (no sapces)
number of vods to get, between 1 and 10.

Example build:
./gradlew fatJar

Example run:
`java -jar KickVODDL.jar xqc C:\vods\xqc 3`

It does download the list of .ts files and combine them and then clean up, so it does take 2x the size of a VOD to produce one VOD.