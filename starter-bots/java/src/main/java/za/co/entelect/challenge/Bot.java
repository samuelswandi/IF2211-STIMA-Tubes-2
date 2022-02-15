package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;
import za.co.entelect.challenge.strategies.*;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.nextAfter;

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

         if (shouldCarUseOil()) {
             return OIL;
         }

//        if (shouldCarUseTweet()) {
//            return new TweetCommand();
//        }


        if (blocks.contains(Terrain.MUD)) {
            int i = random.nextInt(directionList.size());
            return new ChangeLaneCommand(directionList.get(i));
        }
        return new AccelerateCommand();
    }

    private boolean isWallInFront() {
        return getBlocksInFront(myCar.position.lane, myCar.position.block).contains(Terrain.WALL);
    }

    // private boolean isOilInFrontDiagonal(){
    //     // return (myCar.position.lane  == Position.Terrain.MUD AND ) OR myCar.position.lane == Position.Terrain.MUD
    // }

    // private boolean isMudInFrontDiagonal(){

    // }

    // private boolean isTruckInFrontDiagonal(){

    // }

    private boolean isOpponentOnTheSameLane() {
        return myCar.position.lane == opponent.position.lane;
    }

    private boolean isOpponentInFront() {
        return getBlocksInFront(myCar.position.lane, myCar.position.block).contains(opponent);
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

                return myCar.speed <= 3;
            }
        }
        return false;
    }


    private boolean shouldCarUseEMP() {
        return hasPowerUp(PowerUps.EMP, myCar.powerups) && isOpponentOnTheSameLane();
    }

    private boolean shouldCarUseOil() {
        if (hasPowerUp(PowerUps.OIL, myCar.powerups) && !isOpponentInFront()) {
            if (isOpponentOnTheSameLane()) {
                if (myCar.position.lane == 1) {
                    List<Object> blocksInFront = getBlocksInFront(2, myCar.position.block);
                    return blocksInFront.contains(Terrain.MUD) || blocksInFront.contains(Terrain.WALL) || blocksInFront.contains(Terrain.OIL_SPILL);
                }
                if (myCar.position.lane == 4) {
                    List<Object> blocksInFront = getBlocksInFront(3, myCar.position.block);
                    return blocksInFront.contains(Terrain.MUD) || blocksInFront.contains(Terrain.WALL) || blocksInFront.contains(Terrain.OIL_SPILL);
                }
            }
            return false;
        }
        return false;
    }

    private boolean shouldCarUseTweet() {
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            return opponent.speed == 15;
        }
        return false;
    }

    private int getPreviousSpeedState(int currSpeed, int decreaseState) {
        int minimum = 0;
        int state_1 = 3;
        int initial = 5;
        int state_2 = 6;
        int state_3 = 8;
        int max = 9;
        int boost = 15;

        int[] carSpeed = {minimum, state_1, initial, state_2, state_3, max, boost};
        if (currSpeed == minimum) {
            return carSpeed[0];
        } else if (currSpeed == state_1) {
            if (1 - decreaseState <= 0) {
                return carSpeed[0];
            } else {
                return carSpeed[2 - decreaseState];
            }
        } else if (currSpeed == initial) {
            if (2 - decreaseState <= 0) {
                return carSpeed[0];
            } else {
                return carSpeed[2 - decreaseState];
            }
        } else if (currSpeed == state_2) {
            if (2 - decreaseState <= 0) {
                return carSpeed[0];
            } else {
                return carSpeed[3 - decreaseState];
            }
        } else if (currSpeed == state_3) {
            if (2 - decreaseState <= 0) {
                return carSpeed[0];
            } else {
                return carSpeed[4 - decreaseState];
            }
        } else if (currSpeed == max) {
            if (2 - decreaseState <= 0) {
                return carSpeed[0];
            } else {
                return carSpeed[5 - decreaseState];
            }
        } else if (currSpeed == boost) {
            if (2 - decreaseState <= 0) {
                return carSpeed[0];
            } else {
                return carSpeed[6 - decreaseState];
            }
        }
        return myCar.speed;
    }

    private int countSpeedDecrement(List<Object> blocksInFront) {
        int speedMinus = 0;
        int mud = 0;
        int oil_spill = 0;
        int wall = 0;
        int currSpeed = myCar.speed;
        for (Object block : blocksInFront) {
            if (block.equals(Terrain.MUD)) {
                mud += 1;
            } else if (block.equals(Terrain.OIL_SPILL)) {
                oil_spill += 1;
            } else if (block.equals(Terrain.WALL)) {
                wall += 1;
            } else {
                speedMinus += 0;
            }
        }
        if (wall > 0) {
            speedMinus += wall * (myCar.speed - 3);
            currSpeed = 3;
        }
        speedMinus += (currSpeed - getPreviousSpeedState(currSpeed, oil_spill + mud));
        return speedMinus;
    }

    private int countDamageIncrement(List<Object> blocksInFront) {
        int damage = 0;
        for (Object block : blocksInFront) {
            if (block.equals(Terrain.MUD)) {
                damage += 1;
            } else if (block.equals(Terrain.OIL_SPILL)) {
                damage += 1;
            } else if (block.equals(Terrain.WALL)) {
                damage += 2;
            } else {
                damage += 0;
            }
        }
        return damage;
    }

    private int countScoreDecrement(List<Object> blocksInFront) {
        int scoreMinus = 0;
        for (Object block : blocksInFront) {
            if (block.equals(Terrain.MUD)) {
                scoreMinus += 3;
            } else if (block.equals(Terrain.OIL_SPILL)) {
                scoreMinus += 4;
            } else {
                scoreMinus += 0;
            }
        }
        return scoreMinus;
    }

    private int countGetPowerUps(List<Object> blocksInFront) {
        int powerUp = 0;
        for (Object block : blocksInFront) {
            if (block.equals(Terrain.BOOST)) {
                powerUp += 10;
            } else if (block.equals(Terrain.OIL_POWER)) {
                powerUp += 2;
            } else if (block.equals(Terrain.TWEET)) {
                powerUp += 2;
            } else if (block.equals(Terrain.LIZARD)) {
                powerUp += 5;
            } else if (block.equals(Terrain.EMP)) {
                powerUp += 10;
            } else {
                powerUp += 0;
            }
        }
        return powerUp;
    }

    private Position checkBestPosition() {
        int speed;
        int damage;
        int score;
        int powerups;
        // BELOM DIBUAT CORNER CASENYA
        List<Object> blocksInFront = getBlocksInFront(myCar.position.lane, myCar.position.block);
        List<Object> blocksInLeft = getBlocksInFront(myCar.position.lane - 1, myCar.position.block);
        List<Object> blocksInRight = getBlocksInFront(myCar.position.lane + 1, myCar.position.block);
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
