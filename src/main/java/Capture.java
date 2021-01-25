import arc.Events;
import arc.math.geom.Point2;
import arc.util.Align;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Fires;
import mindustry.entities.Units;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.mod.Plugin;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.*;

public class Capture extends Plugin {

    int messageQueue = 0;
    int messageLimit = 4;

    @Override
    public void init() {
        Events.on(EventType.BlockDestroyEvent.class, e -> {
            if (e.tile.build instanceof CoreBlock.CoreBuild && !Vars.state.gameOver){
                Tile tile = e.tile;
                Team oldTeam = e.tile.build.team;
                Block block = e.tile.block();
                Unit closestEnemy = Units.closestEnemy(oldTeam, tile.worldx(), tile.worldy(), 10000000, u -> true);
                Team newTeam = closestEnemy != null ? closestEnemy.team : Team.derelict;
                Call.effectReliable(Fx.upgradeCore, tile.build.x, tile.build.y, block.size, newTeam.color);
                Call.infoPopup(
                        "Team [#" + newTeam.color.toString() + "]" + newTeam.name
                        + " []captured team [#" + oldTeam.color.toString() + "]"+ oldTeam.name
                        + "[] core at " + tile.x + ", " + tile.y, 5f, Align.center, 0, 0, 50 * messageQueue - 50 * messageLimit, 0);
                messageQueue = (messageQueue + 1) % messageLimit;
                Timer.schedule(() -> {
                    tile.setNet(block, newTeam, 0);
                    tile.build.health = Float.POSITIVE_INFINITY;
                    Fires.remove(tile);
                    for (int size = 0; size <= block.size; size++) {
                        for (Point2 edge : Edges.getEdges(size)) {
                            Fires.remove(Vars.world.tile(tile.x + edge.x, tile.y + edge.y));
                        }
                    }
                }, 0.5f);
                Timer.schedule(() -> tile.build.health = tile.block().health, 5f);
            }
        });
    }
}