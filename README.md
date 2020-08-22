# SpindleLengthPlugin

This FIJI plugin tracks the spindle length of _S.pombe_ cells dividing. It works on TIFF stacks. You can try it out on the files in the input folder in this repository.

Please note that to use this plugin, you'll need to have FIJI (obviously) and anaconda installed (to run Python). You can download these here:

https://imagej.net/Fiji/Downloads


https://www.anaconda.com/distribution/

Also note that we're working on creating installation instructions for Windows!

To install:
- Download a ZIP of the repository.
<img width="1440" alt="74DF7464-F8FD-41F5-B6A0-3B21709974DA" src="https://user-images.githubusercontent.com/32312683/69989526-c7dd0700-1511-11ea-8399-56c45d12f320.png">
- Unpack the zip and open the folder. All of the files you will need for installation are in the "Installation" folder.
<img width="643" alt="C6A959A4-DE5D-41CA-8366-E78297F0E385" src="https://user-images.githubusercontent.com/32312683/69989592-f22ec480-1511-11ea-9517-5bd38e698ac7.png">
- In your file system, go to "Applications", right-click on "FIJI", select "Show package contents".
<img width="787" alt="Screen Shot 2019-12-02 at 2 33 15 PM" src="https://user-images.githubusercontent.com/32312683/69989626-fe1a8680-1511-11ea-9c1a-10039884a776.png">
- enter the "plugins" folder and copy/paste or drag the .jar file from "Installation" folder here.
<img width="1440" alt="Screen Shot 2019-12-02 at 2 35 26 PM" src="https://user-images.githubusercontent.com/32312683/69989682-23a79000-1512-11ea-9123-d539131856c7.png">
- copy/paste or drag the "Python" folder here in the "Show package contents" folder 
<img width="1440" alt="64B678EF-AC83-4FE0-B700-B100B153A8B7" src="https://user-images.githubusercontent.com/32312683/69989701-3752f680-1512-11ea-9443-6b82c9e9aef2.png">
- the FIJI "Show package contents" folder should look like this:
<img width="795" alt="3AE580C7-9132-4CCF-8871-9157273A8979" src="https://user-images.githubusercontent.com/32312683/69989723-45a11280-1512-11ea-8db2-57238c23ac1e.png">
- restart FIJI and you should see "Spindle Length" in your "Plugins" menu. 
<img width="647" alt="Screen Shot 2019-12-02 at 2 37 22 PM" src="https://user-images.githubusercontent.com/32312683/69989768-5f425a00-1512-11ea-9333-189408334635.png">

To run:
- open your desired TIFF stack in FIJI
- select "Spindle Length" from the plugins menu
- follow the directions from the popup menus to choose where to save the output file and scale the image
- after the program is done running, the output file, called "lengths.csv" will be saved in the folder you chose.
