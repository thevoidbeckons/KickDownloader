How to use:

Install [ffmpeg](https://www.ffmpeg.org/download.html), I used a package manager called [chocolately](https://chocolatey.org/install#individual) to install it.

Build the fatjar or run the project from IntelliJ. 

Inputs:
kick channel name
vod file location (no sapces)
number of vods to get

Example run:
`java -jar KickVODDL.jar xqc C:\vods\xqc 3`

It does take a while to actually get a video, but it can get multiple VODs at the same time concurrently. 
