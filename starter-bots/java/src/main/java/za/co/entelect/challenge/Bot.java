package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;
import za.co.entelect.challenge.strategies.*;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);


    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(-1);
        directionList.add(1);
    }

    public Command run() {
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);

        if (myCar.damage >= 5) {
            return new FixCommand();
        }

        if (shouldCarUseEMP()) {
            return EMP;
        }

        if (shouldCarUseBoost()) {
            return BOOST;
        }


        if (blocks.contains(Terrain.MUD)) {
            int i = random.nextInt(directionList.size());
            return new ChangeLaneCommand(directionList.get(i));
        }
        return new AccelerateCommand();
    }

    private boolean isWallInFront() {
        return getBlocksInFront(myCar.position.lane, myCar.position.block).contains(Terrain.WALL);
    }
    
    private int countMudInFront() {
        int count = 0;
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
        for (Object block : blocks) {
            if (block.equals(Terrain.MUD)) {
                count++;
            }
        }
        return count;
    }

    private boolean shouldCarUseBoost() {
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            if (!myCar.boosting && !isWallInFront()) {
                if (gameState.maxRounds - gameState.currentRound <= 50) {
                    return true;
                }
                if (countMudInFront() <= 3) {
                    return true;
                }
                if (myCar.speed <= 3) {
                    return true;
                }
            }
        } 
        return false;
    }

    private boolean isOpponentOnTheSameLane() {
        return myCar.position.lane == opponent.position.lane;
    }

    private boolean shouldCarUseEMP() {
        return hasPowerUp(PowerUps.EMP, myCar.powerups) && isOpponentOnTheSameLane();
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp : available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private List<Object> getBlocksInFront(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

}
