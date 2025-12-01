How to use:

Install [ffmpeg](https://www.ffmpeg.org/download.html), I used a package manager called [chocolately](https://chocolatey.org/install#individual) to install it.
JDK 17 Required

Build the fatjar or run the project from IntelliJ. 

Inputs:
-channel `kick channel name`
-dir `directory location`
-count `number of vods to do at the same time`

Example build:
./gradlew fatJar

Example run:
`java -jar KickVODDL.jar -channel xqc -dir C:\vods\xqc -count 3`

It does download the list of .ts files and combine them and then clean up, so it does take 2x the size of a VOD to produce one VOD.