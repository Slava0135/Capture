import arc.Events;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.Align;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.content.Fx;
import mindustry.entities.Fires;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Call;
import mindustry.gen.Fire;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.mod.Plugin;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.*;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class Capture extends Plugin {

    int messageQueue = 0;
    int messageLimit = 4;

    @Override
    public void init() {
        Events.on(EventType.BlockDestroyEvent.class, e -> {
            if (e.tile.build instanceof CoreBlock.CoreBuild && !Vars.state.gameOver) {

                Tile tile = e.tile;
                Team oldTeam = e.tile.build.team;
                Block block = e.tile.block();

                Unit closestEnemy = Units.closestEnemy(oldTeam, tile.worldx(), tile.worldy(), 10000000, u -> true);
                if (oldTeam == Team.derelict) {
                    for (Teams.TeamData team : Vars.state.teams.active) {
                        if (team.team == Team.derelict) continue;
                        Unit enemy = Units.closest(team.team, tile.worldx(), tile.worldy(), u -> true);
                        if (tile.dst(enemy) < tile.dst(closestEnemy)) {
                            closestEnemy = enemy;
                        }
                    }
                }

                Team newTeam = closestEnemy != null ? closestEnemy.team : Team.derelict;

                Call.effectReliable(Fx.upgradeCore, tile.worldx(), tile.worldy(), block.size, newTeam.color);
                Call.infoPopup(
                        "Team [#" + newTeam.color.toString() + "]" + newTeam.name
                        + " []captured team [#" + oldTeam.color.toString() + "]"+ oldTeam.name
                        + "[] core at " + tile.x + ", " + tile.y, 5f, Align.center, 0, 0, 50 * messageQueue - 50 * messageLimit, 0);
                messageQueue = (messageQueue + 1) % messageLimit;

                Timer.schedule(() -> {
                    tile.setNet(block, newTeam, 0);
                    tile.build.health = Float.POSITIVE_INFINITY;
                    Groups.fire.each(fire -> fire.dst(tile.build) < 4 * block.size, Fire::remove);
                }, 0.5f);
                Timer.schedule(() -> {
                    tile.build.health = tile.block().health;
                    Groups.fire.each(fire -> fire.dst(tile.build) < 4 * block.size, Fire::remove);
                }, 5f);
            }
        });
    }
}