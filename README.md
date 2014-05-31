# Replant
========================================================

Simple plant replanting mod for Minecraft


This mod listens for when plantable items or blocks are about to expire in the world. When they do, rather than being destroyed they are planted in the ground if they can be. 

This fairly simple mechanic doesn't have a whole lot of use, but this can take some of the headache out of replanting trees when harvesting wood. Instead of breaking the leaves trying to get saplings, just let them fall to the ground, five minutes later they will automatically be planted. No need to collect saplings and manually replant the forest!

All vanilla plants are supported, including double height plants like rose bushes. All rules for planting are preserved, IOW crops must be planted on farmland, netherwart must be on soul sand, etc.

Non-vanilla-y behaviors:

* Pumpkins and melons will be planted as well as their seeds. They will not be placed as blocks as when placing by hand but as stems when planting the seeds.
* Water lillies will float to the surface prior to planting. If there isn't at least one block of air immediately above the water, then it will not be planted.


#### Requires:
- Minecraft 7.2
- Forge 10.12.1.1060 or later
- Java 7 or later (Java 6 is dead people, move on!)


#### Known Issues:
- Apples aren't planted (what tree would they turn into?)


#### Installation:
- Simply drop this in the mods directory along with other Forge mods


#### Configuration:
At the moment this mod has one configuration option for debugging. The config file is saved as config/mod_Replant.cfg. It is recommended to launch Minecraft with the mod installed once, then edit the config file rather than trying to hand craft it.

The following settings are available:

| category | type  | name  | description  |
|:------|:------|:------|:-------------|
| debugging | boolean | tweakTossedItemLifespan | if set to true then items that are tossed by the player will have their lifespan altered, generally so they can expire quicker.|
| debugging | int   | tossedItemLifespan | a number representing the number of ticks tossed items will float around in the world.|


#### A note to other mod developers:
This mod will plant anything that implements the IPlantable interface. If you have added plants/seeds/blocks that can be planted using that interface then this should be able to plant them. I cannot guarantee that any special behaviors will be preserved, especially if the plant wants information about the player when it's planted.


##### This software comes with absolutely no warranty, expressed or implied.

Copyright (c) 2013, 2014, Shaded Reality. All Rights Reserved.
Redistribution rights granted under the terms of the license agreement.

Source code is provided free of charge and royalty-free under the 3 clause BSD license. See Replant.java for the full license text.
