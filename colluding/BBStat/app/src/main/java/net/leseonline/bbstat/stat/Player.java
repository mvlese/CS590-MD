package net.leseonline.bbstat.stat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mvlese on 2/21/2016.
 */
public class Player {

    public Player() {
        id = 0;
        teamIds = new ArrayList<Integer>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return id == player.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public void addTeamId(int teamId) {
        if (!teamIds.contains(teamId)) {
            teamIds.add(teamId);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(List<Integer> teamIds) {
        this.teamIds = teamIds;
    }

    private int id;
    private List<Integer> teamIds;
}
