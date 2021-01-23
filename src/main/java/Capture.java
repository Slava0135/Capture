import arc.Events;
import arc.util.Align;
import arc.util.Timer;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.mod.Plugin;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.*;

public class Capture extends Plugin {
    @Override
    public void init() {
        Events.on(EventType.BlockDestroyEvent.class, e -> {
            if (e.tile.build instanceof CoreBlock.CoreBuild){
                Tile tile = e.tile;
                Team oldTeam = e.tile.build.team;
                Block block = e.tile.build.block;
                Unit closestEnemy = Units.closestEnemy(oldTeam, tile.worldx(), tile.worldy(), 10000000, u -> true);
                Team newTeam = closestEnemy != null ? closestEnemy.team : Team.derelict;
                Call.effectReliable(Fx.upgradeCore, tile.build.x, tile.build.y, block.size, newTeam.color);
                Call.infoPopup(
                        "Team [#" + newTeam.color.toString() + "]" + newTeam.name
                        + " []captured team [#" + oldTeam.color.toString() + "]"+ oldTeam.name
                        + "[] core at " + tile.x + ", " + tile.y, 10f, Align.center, 0, 0, 0, 0);
                Timer.schedule(() -> tile.setNet(block, newTeam, 0), 0.6f);
            }
        });
    }
}