package cromveil.combatnumbers;

import cromveil.combatnumbers.core.Constants;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public final class CombatNumbers {

    public CombatNumbers() {
        var name = Constants.MOD_NAME;
        Constants.LOG.info("{} loaded on Forge!", name);
    }
}
