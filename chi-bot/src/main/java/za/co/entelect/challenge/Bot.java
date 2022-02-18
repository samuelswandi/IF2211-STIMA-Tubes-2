package za.co.entelect.challenge;

import com.sun.org.apache.xpath.internal.operations.Bool;
import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.pow;

public class Bot {

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    public Bot() {}


    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        List<Object> blocks = getBlocksInFront(gameState, myCar.position.lane, myCar.position.block);

        // jika damage dari car lebih dari 2 dan tidak ada wall, mud, dan oil spill, maka akan return FIX
        if (myCar.damage >= 2 && !blocks.contains(Terrain.WALL) && !blocks.contains(Terrain.MUD) && !blocks.contains(Terrain.OIL_SPILL)) {
            return FIX;
        }

        // jika damage lebih dari 4, maka akan return FIX
        if (myCar.damage >= 4) {
            return FIX;
        }

        // jika speed dari mobil adalah 0, maka akan return ACCELERATE
        if (myCar.speed == 0) {
            return ACCELERATE;
        }

        // mengambil lane yang paling menguntungkan
        int lane = checkBestPosition(gameState, myCar);

        // pindah ke lane yang paling menguntungkan
        if (lane != myCar.position.lane) {
            return new ChangeLaneCommand(lane - myCar.position.lane);
        }

        // menggunakan LIZARD jika sesuai syarat
        if (shouldCarUseLizard(gameState, myCar)) {
            return LIZARD;
        }

        // menggunakan EMP jika sesuai syarat
        if (shouldCarUseEMP(gameState, myCar)) {
            return EMP;
        }

        // menggunakan EMP jika sesuai syarat
        Map<Boolean, Command> commands = shouldCarUseBoost(gameState, myCar);
        if (commands.get(true) != null) {
            return commands.get(true);
        }

        // menggunakan TWEET jika sesuai syarat
        if (shouldCarUseTweet(gameState, myCar)) {
            return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 2);
        }

        if (shouldCarUseOil(gameState, myCar)) {
            return OIL;
        }

        return ACCELERATE;
    }

    // cek apakah lawan berada di lane yang sama untuk penggunaan EMP
    private boolean isOpponentOnTheSameLane(GameState gameState, Car myCar) {
        return myCar.position.lane == gameState.opponent.position.lane;
    }

    // cek apakah posisi lawan berada di depan kita 
    private boolean isOpponentInFront(GameState gameState, Car myCar) {
        return getBlocksInFront(gameState, myCar.position.lane, myCar.position.block).contains(gameState.opponent);
    }

    // cek apakah ada dinding di depan sejauh current speed dari car
    private boolean isWallInFrontOf(GameState gameState, Car myCar) {
        boolean isCyberTruckInFront = getCyberTruckInFront(gameState, myCar.position.lane, myCar.position.block);
        return getBlocksInFront(gameState, myCar.position.lane, myCar.position.block).contains(Terrain.WALL) || isCyberTruckInFront;
    }

    // menghitung berapa banyak mud yang ada di depan
    private int countMudInFrontOf(GameState gameState, Car myCar) {
        int count = 0;
        List<Object> blocks = getBlocksInFront(gameState, myCar.position.lane, myCar.position.block);
        for (Object block : blocks) {
            if (block.equals(Terrain.MUD)) {
                count++;
            }
        }
        return count;
    }

    // logic dari penggunaan boost 
    private Map<Boolean, Command> shouldCarUseBoost(GameState gameState, Car myCar) {
        Map<Boolean, Command> commands = new HashMap<>();
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            // jika max speed dan tidak ada wall
            if (myCar.speed == 9 && !isWallInFrontOf(gameState, myCar)) {
                if (myCar.damage >= 2) {
                    commands.put(true, FIX);
                    return commands;
                }

                if (countMudInFrontOf(gameState, myCar) <= 3) {
                    commands.put(true, BOOST);
                    return commands;
                }
            }

            if (myCar.speed <= 3 && !isWallInFrontOf(gameState, myCar)) {
                if (myCar.damage >= 2) {
                    commands.put(true, FIX);
                    return commands;
                }

                commands.put(true, BOOST);
                return commands;
            }
        }
        commands.put(false, null);
        return commands;
    }
    
    // logic dari penggunaan oil
    private boolean shouldCarUseOil(GameState gameState, Car myCar) {
        if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
            if (isOpponentOnTheSameLane(gameState, myCar)) {
                if (myCar.position.lane == 1) {
                    List<Object> blocksInFront = getBlocksInFront(gameState, 2, myCar.position.block);
                    return blocksInFront.contains(Terrain.MUD) || blocksInFront.contains(Terrain.WALL) || blocksInFront.contains(Terrain.OIL_SPILL);
                }
                if (myCar.position.lane == 4) {
                    List<Object> blocksInFront = getBlocksInFront(gameState, 3, myCar.position.block);
                    return blocksInFront.contains(Terrain.MUD) || blocksInFront.contains(Terrain.WALL) || blocksInFront.contains(Terrain.OIL_SPILL);
                }
            }
            return false;
        }
        return false;
    }

    // logic dari penggunaan tweet
    private boolean shouldCarUseTweet(GameState gameState, Car myCar) {
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups) && !isWallInFrontOf(gameState, myCar) && countMudInFrontOf(gameState, myCar) <= 3) {
            return gameState.opponent.speed >= 9;
        }
        return false;
    }

    // logic dari penggunaan lizard
    private boolean shouldCarUseLizard(GameState gameState, Car myCar) {
        List<Object> blocks = getBlocksInFront(gameState, myCar.position.lane, myCar.position.block-1);
        List<Object> nextBlocks = blocks.subList(0,1);
        boolean cyberTruckInFront = getCyberTruckInFront(gameState, myCar.position.lane, myCar.position.block);
        if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
            if (myCar.speed == 15) {
                return isWallInFrontOf(gameState, myCar) || getBlocksInFront(gameState, myCar.position.lane, myCar.position.block).contains(Terrain.OIL_SPILL) || getBlocksInFront(gameState, myCar.position.lane, myCar.position.block).contains(Terrain.MUD) || cyberTruckInFront;
            } else {
                if (nextBlocks.contains(Terrain.WALL)) {
                    return false;
                } else {
                    return isWallInFrontOf(gameState, myCar) || countMudInFrontOf(gameState, myCar) >= 3;
                }
            }
        }
        return false;
    }

    // logic dari penggunaan cyber truck
    private boolean shouldCarUseEMP(GameState gameState, Car myCar) {
        if (hasPowerUp(PowerUps.EMP, myCar.powerups) && !isWallInFrontOf(gameState, myCar)) {
            return gameState.opponent.speed >= 9;
        }
        return false;
    }

    // function untuk mereturn speed state sesuai dengan banyak penurunan state
    private int getPreviousSpeedState(Car myCar, int currSpeed, int decreaseState) {
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

    // function untuk menghitung banyakny pengurangan speed dari car
    private int countSpeedDecrement(GameState gameState, Car myCar, List<Object> blocksInFront) {
        int speedMinus = 0;
        int mud = 0;
        int oil_spill = 0;
        int wall = 0;
        int currSpeed = myCar.speed;
        boolean isCyberTruckInFront = getCyberTruckInFront(gameState, myCar.position.lane, myCar.position.block);

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

        if (isCyberTruckInFront) {
            wall += 3;
        }

        if (wall == 1) {
            speedMinus += wall * (myCar.speed - 3);
            currSpeed = 3;
        } else if (wall > 1){
            speedMinus += currSpeed;
            currSpeed = 0;
        }
        speedMinus += (currSpeed - getPreviousSpeedState(myCar, currSpeed, oil_spill + mud));
        return speedMinus;
    }

    // function untuk menghitung banyaknya damage
    private int countDamageDecrement(GameState gameState, List<Object> blocksInFront) {
        int damage = 0;
        boolean isCyberTruckInFront = getCyberTruckInFront(gameState, gameState.player.position.lane, gameState.player.position.block);

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

            if (isCyberTruckInFront) {
                damage += 2;
            }

        return damage;
    }

    // function untuk menghitung pengurangan score
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

    //
    private int countGetPowerUps(List<Object> blocksInFront) {
        int powerUp = 0;
        for (Object block : blocksInFront) {
            if (block.equals(Terrain.BOOST)) {
                powerUp += 9;
            } else if (block.equals(Terrain.OIL_POWER)) {
                powerUp += 2;
            } else if (block.equals(Terrain.TWEET)) {
                powerUp += 9;
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

    private int checkBestPosition(GameState gameState, Car myCar) {
        String[] priorities = {"speed", "damage", "score", "powerups"};
        List<Object> blocksInFront = getBlocksInFront(gameState, myCar.position.lane, myCar.position.block);

        for (Object priority : priorities) {
            if (priority.equals("speed")) {
                int front = countSpeedDecrement(gameState, myCar, blocksInFront);
                if (myCar.position.lane == 2 || myCar.position.lane == 3) {
                    List<Object> blocksInLeft = getBlocksInFront(gameState, myCar.position.lane - 1, myCar.position.block - 1);
                    List<Object> blocksInRight = getBlocksInFront(gameState, myCar.position.lane + 1, myCar.position.block - 1);

                    int left = countSpeedDecrement(gameState, myCar, blocksInLeft);
                    int right = countSpeedDecrement(gameState, myCar, blocksInRight);

                    if (left < front && left < right) {
                        return myCar.position.lane - 1;
                    }

                    if (front < left && front < right) {
                        return myCar.position.lane;
                    }

                    if (right < left && right < front) {
                        return myCar.position.lane + 1;
                    }

                } else if (myCar.position.lane == 1){
                    List<Object> blocksInRight = getBlocksInFront(gameState, myCar.position.lane + 1, myCar.position.block - 1);
                    int right = countSpeedDecrement(gameState, myCar, blocksInRight);

                    if (right < front) {
                        return myCar.position.lane + 1;
                    } else {
                        return myCar.position.lane;
                    }
                } else {
                    List<Object> blocksInLeft = getBlocksInFront(gameState, myCar.position.lane - 1, myCar.position.block - 1);
                    int left = countSpeedDecrement(gameState, myCar, blocksInLeft);

                    if (left < front) {
                        return myCar.position.lane - 1;
                    } else {
                        return myCar.position.lane;
                    }
                }
            }
            if (priority.equals("damage")) {
                int front = countDamageDecrement(gameState, blocksInFront);
                if (myCar.position.lane == 2 || myCar.position.lane == 3) {
                    List<Object> blocksInLeft = getBlocksInFront(gameState, myCar.position.lane - 1, myCar.position.block - 1);
                    List<Object> blocksInRight = getBlocksInFront(gameState, myCar.position.lane + 1, myCar.position.block - 1);

                    int left = countDamageDecrement(gameState, blocksInLeft);
                    int right = countDamageDecrement(gameState, blocksInRight);

                    if (left < front && left < right) {
                        return myCar.position.lane - 1;
                    }

                    if (front < left && front < right) {
                        return myCar.position.lane;
                    }

                    if (right < left && right < front) {
                        return myCar.position.lane + 1;
                    }

                } else if (myCar.position.lane == 1){
                    List<Object> blocksInRight = getBlocksInFront(gameState, myCar.position.lane + 1, myCar.position.block - 1);
                    int right = countDamageDecrement(gameState, blocksInRight);

                    if (right < front) {
                        return myCar.position.lane + 1;
                    } else {
                        return myCar.position.lane;
                    }
                } else {
                    List<Object> blocksInLeft = getBlocksInFront(gameState, myCar.position.lane - 1, myCar.position.block - 1);
                    int left = countDamageDecrement(gameState, blocksInLeft);

                    if (left < front) {
                        return myCar.position.lane - 1;
                    } else {
                        return myCar.position.lane;
                    }
                }
            }
            if (priority.equals("powerups")) {
                int front = countGetPowerUps(blocksInFront);
                if (myCar.position.lane == 2 || myCar.position.lane == 3) {
                    List<Object> blocksInLeft = getBlocksInFront(gameState, myCar.position.lane - 1, myCar.position.block - 1);
                    List<Object> blocksInRight = getBlocksInFront(gameState, myCar.position.lane + 1, myCar.position.block - 1);

                    int left = countGetPowerUps(blocksInLeft);
                    int right = countGetPowerUps(blocksInRight);

                    if (left > front && left > right) {
                        return myCar.position.lane - 1;
                    }

                    if (front > left && front > right) {
                        return myCar.position.lane;
                    }

                    if (right > left && right > front) {
                        return myCar.position.lane + 1;
                    }

                } else if (myCar.position.lane == 1){
                    List<Object> blocksInRight = getBlocksInFront(gameState, myCar.position.lane + 1, myCar.position.block - 1);
                    int right = countGetPowerUps(blocksInRight);

                    if (right > front) {
                        return myCar.position.lane + 1;
                    } else {
                        return myCar.position.lane;
                    }
                } else {
                    List<Object> blocksInLeft = getBlocksInFront(gameState, myCar.position.lane - 1, myCar.position.block - 1);
                    int left = countGetPowerUps(blocksInLeft);

                    if (left > front) {
                        return myCar.position.lane - 1;
                    } else {
                        return myCar.position.lane;
                    }
                }
            }
            if (priority.equals("score")) {
                int front = countScoreDecrement(blocksInFront);
                if (myCar.position.lane == 2 || myCar.position.lane == 3) {
                    List<Object> blocksInLeft = getBlocksInFront(gameState, myCar.position.lane - 1, myCar.position.block - 1);
                    List<Object> blocksInRight = getBlocksInFront(gameState, myCar.position.lane + 1, myCar.position.block - 1);

                    int left = countScoreDecrement(blocksInLeft);
                    int right = countScoreDecrement(blocksInRight);

                    if (left < front && left < right) {
                        return myCar.position.lane - 1;
                    }

                    if (front < left && front < right) {
                        return myCar.position.lane;
                    }

                    if (right < left && right < front) {
                        return myCar.position.lane + 1;
                    }

                } else if (myCar.position.lane == 1){
                    List<Object> blocksInRight = getBlocksInFront(gameState, myCar.position.lane + 1, myCar.position.block - 1);
                    int right = countScoreDecrement(blocksInRight);

                    if (right < front) {
                        return myCar.position.lane + 1;
                    } else {
                        return myCar.position.lane;
                    }
                } else {
                    List<Object> blocksInLeft = getBlocksInFront(gameState, myCar.position.lane - 1, myCar.position.block - 1);
                    int left = countScoreDecrement(blocksInLeft);

                    if (left < front) {
                        return myCar.position.lane - 1;
                    } else {
                        return myCar.position.lane;
                    }
                }
            }
        }
        return myCar.position.lane;
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp : available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private List<Object> getBlocksInFront(GameState gamestate, int lane, int block) {
        List<Lane[]> map = gamestate.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + gamestate.player.speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    private boolean getCyberTruckInFront(GameState gameState, int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        boolean cyberTruck = false;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + 15; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            if (laneList[i].cyberTruck) {
                cyberTruck = true;
                break;
            }
        }
        return cyberTruck;
    }

}
