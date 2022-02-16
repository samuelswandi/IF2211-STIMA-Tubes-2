function Run(GameState gameState)  -> Command
{ Menjalankan public/method yang mengemabilkan command langkah yang dipilih berdasarkan algoritma yang telah didesain }

KAMUS LOKAL
    gameState = GameState { Berisi keadaan game saat round tertentu }
    ACCELERATE = AccelerateCommand() { Command untuk menjalankan akselerasi }
    LIZARD = LizardCommand() { Command untuk menjalankan lizard }
    OIL = OilCommand() { Command untuk menjalankan oil }
    BOOST = BoostCommand() { Command untuk menjalankan boost }
    EMP = EMPCommand() { Command untuk menjalankan EMP }
    FIX = FixCommand() { Command untuk menjalankan FIX }

ALGORITMA
    { assign player ke variable myCar }
    myCar <- gameState.player;

    { assign opponent ke variable opponent }
    opponent <- gameState.opponent;

    { assign list of blocks di depan myCar }
    blocks <- getBlocksInFront(gameState, myCar.position.lane, myCar.position.block)

    { jika damage dari myCar lebih dari 2, dan di depan myCar tidak ada wall, mud, dan oil spill maka akan FIX }
    if (myCar.damage >= 2 && !blocks.contains(WALL) && !blocks.contains(MUD) && !blocks.contains(OIL_SPILL) then
        return FIX

    { jika damage dari myCar lebih dari 4, maka akan return FIX }
    if (myCar.damage >= 4) then
        return FIX

    { jika speed dari myCar 0, maka harus dijalankan ACCELERATE untuk menjalankan mobil kembali }
    if (myCar.speed == 0) then
        return ACCELERATE

    { untuk mengambil lane yang paling menguntungkan }
    lane <- checkBestPosition(gameState, myCar)

    { jika lane paling menguntunkan bukan merupakan lane bot saat ini }
    { maka bot akan berubah arah menjadi lane yang paling menguntungkan }
    if (lane != myCar.lane) then
        return ChangeLaneCommand(lane)

    if (shouldCarUseLizard(gameState, myCar) then
        return LIZARD

    if (shouldCarUseEMP(gameState, myCar) then
        return EMP

    if (shouldCarUseBoost(gameState,myCar) then
        return BOOST

    if (shouldCarUseTweet(gameState, myCar) then
        return TweetCommand(opponent.lane, opponent.block + opponent.speed + 2)

    if (shouldCarUseOil(gameState, myCar) then
        return OIL

    return ACCELERATE



function CheckBestPosition(GameState gameState, Car myCar) -> integer
{ fungsi untuk menentukan lane terbaik yang akan dipilih oleh bot berdasarkan konsiderasi tertentu }

KAMUS LOKAL
    priorities = Array of string
    blocksInFront = Array of Object
    blocksInLeft = Array of Object
    blocksInRight = Array of Object
    left = integer
    right = integer
    front = integer

ALGORITMA
    priorities <- ["damage", "speed", "score", "powerups"]
    blocksInFront <- getBlocksInFront(gameState, myCar.lane, myCar.blocks)

    { looping untuk setiap elemen pada array dan menamai tiap elemen = priority }
    for (Object priority : priorities) do
        if (priority == "speed") then

            { jika bot ada di lane 2 atau 4, yang artinya bot dapat bergerak ke kanan dan kiri }
            if (myCar.lane == 2 || myCar.lane == 4) then

                { untuk mendapatkan object blocks di kanan dan kiri bot }
                blocksInLeft <- getBlocksInFront(gameState, myCar.lane-1, myCar.block-1)
                blocksInRight <- getBlocksInFront(gameState, myCar.lane+1, myCar.block-1)

                { menghitung nilai pengurangan speed untuk lane kiri, kanan, dan depan }
                front <- countSpeedDecrement(gameState, myCar, blocksInFront)
                left <- countSpeedDecrement(gameState, myCar, blocksInLeft)
                right <- countSpeedDecrement(gameState, myCar, blocksInRight)
                
                { perbandingan antara lane kiri, kanan, dan depan }
                { untuk menentukan lane terbaik }
                if (left < front && left < right) then
                    return myCar.lane - 1

                if (front < left && front < right) then
                    return myCar.lane

                if (right < left && right < front) then
                    return myCar.lane

            else if (myCar.lane == 1) then
                blocksInRight <- getBlocksInFront(gameState, myCar.lane+1, myCar.block-1)
                right <- countSpeedDecrement(gameState, myCar, blocksInRight)
                front <- countSpeedDecrement(gameState, myCar, blocksInFront)

                if (right < front) then
                    return myCar.lane + 1
                else 
                    return myCar.lane

            else 
                blocksInLeft <- getBlocksInFront(gameState, myCar.lane-1, myCar.block-1)
                front <- countSpeedDecrement(gameState, myCar, blocksInFront)
                left <- countSpeedDecrement(gameState, myCar, blocksInLeft)

                if (left < front) then
                    return myCar.lane - 1
                else 
                    return myCar.lane

        if (priority == "damage")

            { jika bot ada di lane 2 atau 4, yang artinya bot dapat bergerak ke kanan dan kiri }
            if (myCar.lane == 2 || myCar.lane == 4) then

                { untuk mendapatkan object blocks di kanan dan kiri bot }
                blocksInLeft <- getBlocksInFront(gameState, myCar.lane-1, myCar.block-1)
                blocksInRight <- getBlocksInFront(gameState, myCar.lane+1, myCar.block-1)

                { menghitung nilai pengurangan damage untuk lane kiri, kanan, dan depan }
                front <- countDamageDecrement(gameState, myCar, blocksInFront)
                left <- countDamageDecrement(gameState, myCar, blocksInLeft)
                right <- countDamageDecrement(gameState, myCar, blocksInRight)
                
                { perbandingan antara lane kiri, kanan, dan depan }
                { untuk menentukan lane terbaik }
                if (left < front && left < right) then
                    return myCar.lane - 1

                if (front < left && front < right) then
                    return myCar.lane

                if (right < left && right < front) then
                    return myCar.lane

            else if (myCar.lane == 1) then
                blocksInRight <- getBlocksInFront(gameState, myCar.lane+1, myCar.block-1)
                right <- countDamageDecrement(gameState, myCar, blocksInRight)
                front <- countDamageDecrement(gameState, myCar, blocksInFront)

                if (right < front) then
                    return myCar.lane + 1
                else 
                    return myCar.lane

            else 
                blocksInLeft <- getBlocksInFront(gameState, myCar.lane-1, myCar.block-1)
                front <- countDamageDecrement(gameState, myCar, blocksInFront)
                left <- countDamageDecrement(gameState, myCar, blocksInLeft)

                if (left < front) then
                    return myCar.lane - 1
                else 
                    return myCar.lane

        if (priority == "score")
                
            { jika bot ada di lane 2 atau 4, yang artinya bot dapat bergerak ke kanan dan kiri }
            if (myCar.lane == 2 || myCar.lane == 4) then

                { untuk mendapatkan object blocks di kanan dan kiri bot }
                blocksInLeft <- getBlocksInFront(gameState, myCar.lane-1, myCar.block-1)
                blocksInRight <- getBlocksInFront(gameState, myCar.lane+1, myCar.block-1)

                { menghitung nilai pengurangan score untuk lane kiri, kanan, dan depan }
                front <- countScoreDecrement(gameState, myCar, blocksInFront)
                left <- countScoreDecrement(gameState, myCar, blocksInLeft)
                right <- countScoreDecrement(gameState, myCar, blocksInRight)
                
                { perbandingan antara lane kiri, kanan, dan depan }
                { untuk menentukan lane terbaik }
                if (left < front && left < right) then
                    return myCar.lane - 1

                if (front < left && front < right) then
                    return myCar.lane

                if (right < left && right < front) then
                    return myCar.lane

            else if (myCar.lane == 1) then
                blocksInRight <- getBlocksInFront(gameState, myCar.lane+1, myCar.block-1)
                right <- countScoreDecrement(gameState, myCar, blocksInRight)
                front <- countScoreDecrement(gameState, myCar, blocksInFront)

                if (right < front) then
                    return myCar.lane + 1
                else 
                    return myCar.lane

            else 
                blocksInLeft <- getBlocksInFront(gameState, myCar.lane-1, myCar.block-1)
                front <- countScoreDecrement(gameState, myCar, blocksInFront)
                left <- countScoreDecrement(gameState, myCar, blocksInLeft)

                if (left < front) then
                    return myCar.lane - 1
                else 
                    return myCar.lane

        if (priority == "powerups")

            { jika bot ada di lane 2 atau 4, yang artinya bot dapat bergerak ke kanan dan kiri }
            if (myCar.lane == 2 || myCar.lane == 4) then

                { untuk mendapatkan object blocks di kanan dan kiri bot }
                blocksInLeft <- getBlocksInFront(gameState, myCar.lane-1, myCar.block-1)
                blocksInRight <- getBlocksInFront(gameState, myCar.lane+1, myCar.block-1)

                { menghitung nilai penambahan powerups untuk lane kiri, kanan, dan depan }
                front <- countGetPowerUps(gameState, myCar, blocksInFront)
                left <- countGetPowerUps(gameState, myCar, blocksInLeft)
                right <- countGetPowerUps(gameState, myCar, blocksInRight)
                
                { perbandingan antara lane kiri, kanan, dan depan }
                { untuk menentukan lane terbaik }
                if (left > front && left > right) then
                    return myCar.lane - 1

                if (front > left && front > right) then
                    return myCar.lane

                if (right > left && right > front) then
                    return myCar.lane

            else if (myCar.lane == 1) then
                blocksInRight <- getBlocksInFront(gameState, myCar.lane+1, myCar.block-1)
                right <- countGetPowerUps(gameState, myCar, blocksInRight)
                front <- countGetPowerUps(gameState, myCar, blocksInFront)

                if (right > front) then
                    return myCar.lane + 1
                else 
                    return myCar.lane

            else 
                blocksInLeft <- getBlocksInFront(gameState, myCar.lane-1, myCar.block-1)
                front <- countGetPowerUps(gameState, myCar, blocksInFront)
                left <- countGetPowerUps(gameState, myCar, blocksInLeft)

                if (left > front) then
                    return myCar.lane - 1
                else 
                    return myCar.lane

    return myCar.lane


function shouldCarUseLizard(GameState gameState, Car myCar) -> boolean
{ function untuk menentukan apakah bot akan menggunakan command LIZARD atau tidak }

KAMUS LOKAL
    blocks = Array of object
    cyberTruckInFront = boolean

ALGORITMA
    { assign block di depan bot }
    blocks <- getBlocksInFront(gameState, myCar.lane, myCar.blocks)

    { mengecek apakah ada cybertruck di didepan bot}
    cyberTruckInFront <- getCyberTruckInFront(gameState, myCar.lane, myCar.blocks)

    { mengecek apakah myCar memiliki LIZARD POWERUP }
    if (hasPowerUp(LIZARD, myCar.powerups)) then

        { jika kecepatannya 15 maka akan mengembalikan command LIZARD berdasarkan }
        { jika tidak ada wall, mud, oil_spill, dan cybertruck}
        if (myCar.speed == 15) then
            return isWallInFrontOf(myCar) || blocks,contains(MUD) || blocks.contains(OIL_SPILL) || cyberTruckInFront
        else 
            { pakai LIZARD jika ada wall dan jumlah mud di depan myCar lebih dari 3 }
            return isWallInFrontOf(myCar)|| countMudInFrontOf(myCar) >= 3

function shouldCarUseEMP(GameState gameState, Car myCar) -> boolean
{ function untuk menentukan apakah bot akan menggunakan command EMP atau tidak }

ALGORITMA
    { mengecek apakah myCar memiliki EMP POWERUP }
    if (hasPowerUp(EMP, myCar.powerups)) then

        { jika tidak ada wall dan kecepatan musuh lebih dari 9, maka akan langsung mengguunakan EMP }
        return !isWallInFrontOf(myCar) && opponent.speed >= 9
    
    return false

