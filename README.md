# Tugas Besar Strategi Algoritma

## Description

Bot that is used to compete on entelect challenge 2020, Overdrive.

## Strategy

Kami menggunakan strategi greedy movement dan powerups. Untuk strategi movement, kita menghitung lane terbaik atas kriteria tertentu
contoh penggunaan :
```java
// mengambil lane yang paling menguntungkan
int lane = checkBestPosition(gameState, myCar);

// pindah ke lane yang paling menguntungkan
// jika lane terbaik bukan merupakan lane tempat bot sekarang, maka akan pindah
if (lane != myCar.position.lane) {
    return new ChangeLaneCommand(lane - myCar.position.lane);
}
```

selain movement, kita juga memiliki algoritma greedy untuk setiap powerups yang harus digunakan oleh bot.
contoh penggunaan :
```java
// menggunakan LIZARD jika sesuai syarat
if (shouldCarUseLizard(gameState, myCar)) {
    return LIZARD;
}

// menggunakan EMP jika sesuai syarat
if (shouldCarUseEMP(gameState, myCar)) {
    return EMP;
}
```
maka bot akan mengecek apakah pada state sekarang, bot perlu menggunakan powerups tertentu atau tidak.


## Dependencies

* Java, JDK
* Linux (Install Make)

## Build program

Untuk melakukan build program, terdapat dua cara yaitu menggunakan vscode atau intellij.
Untuk intellij, kita perlu menambahkan new maven project dan tinggal click install pada project yang sudah ditambahkan.
contoh :


## Executing program

```
Make run (pada linux)
// jika windows, maka tinggal double click pada run.bat
```

## Author
```
13520075 - Samuel Christopher Swandi
13520078 - Grace Claudia
13520132 - Januar Budi Ghifari
```