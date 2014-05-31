Replant
=======

Simple plant replanting mod for Minecraft

Works with 1.7.2, Forge 10.12.1.1060.

All this mod does is listen for when plantable items are about to expire in the
world. When they do, rather than being destroyed they are planted in the ground.
This takes some of the headache out of replanting trees when harvesting wood.
Instead of breaking the leaves trying to get saplings, just let them fall to the
ground, five minutes later they will automatically be planted.

All vanilla plants are supported, including double height plants like rose bushes.
All rules for planting are preserved, IOW crops must be planted on farmland,
netherwart must be on soulsand, etc.

Special behaviors:
- Pumpkins and melons will be planted as well as their seeds, so they will not be
placed as blocks but as stems, as real pumpkins and melons would do.
- Water lillies will be floated to the surface prior to planting.


Requires:
- Minecraft 7.2
- Forge 10.12.1.1060 or later
- Java 7 or later (Java 6 is dead people!)


Known Issues:
- None yet


Installation:
- Simply drop this in the mods directory along with other Forge mods


Configuration:
At the moment this mod has one configuration option for debugging. The config
file is saved as config/mod_Replant.cfg.

The following settings are available:
debugging:
boolean tweakTossedItemLifespan - if set to true then items that are tossed by
                                  the player will have their lifespan altered so
                                  they can expire quicker. Not recommended for
                                  production use.

int tossedItemLifespan - a number representing the number of ticks tossed items
                         will float around in the world. The default in Minecraft
                         is 6000, or about five minutes.




This mod come with no warranty, expressed or implied.

Copyright (c) 2013, 2014, Shaded Reality. All Rights Reserved.
Redistribution rights granted under the terms of the license agreement.

Source code is provided free of charge and royalty-free under the 3 clause BSD
license. See Replant.java for the full license text.
