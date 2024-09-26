import processing.core.PApplet;
import processing.core.PImage;

/**
* In this program, a game is generated where the player has to escape the maze by collecting keys and avoiding falling rocks and zombies using the WASD keys.
* @author: Ethan Rodrigues and Aurora Chen
* 
*/
public class Sketch extends PApplet {

  // declare gameplay images
  PImage imgKey;
  PImage imgSmallKey;
  PImage imgFlashlightCircle;
  PImage imgBackground;
  PImage imgHeart;
  PImage imgEmptyHeart;

  // declare player images
  PImage imgRobot;
  PImage imgRobotStill;
  PImage imgRobotWalkingSheet;
  PImage [] robotFrames;
  PImage imgRobotFall;

  // declare player variables
  int intWalkingRobotFrames = 8;
  int intWalkingFrameWidth = 192 / 3;

  float playerX = -22;
  float playerY = 316;
  float playerSpeed = 5;
  int robotHeight = 60;
  int robotWidth = 50;

  float playerImageX = playerX - 7;
  float playerImageY = playerY - 26;

  int playerLives = 5;

  // declare zombie images
  PImage imgZombie;
  PImage imgZombieStill;
  PImage imgZombieAttack;
  PImage imgZombieWalkingSheet;
  PImage [] zombieFrames;

  // declare zombie variables
  int intWalkingZombieFrames = 3;
  int intZombieHeight = 60;
  int intZombieWidth = 50;

  float zombie1X = 150;
  float zombie1Y = 420;
  float zombie1ImageX = zombie1X - 7;
  float zombie1ImageY = zombie1Y - 26;

  float zombie2X = 520;
  float zombie2Y = 320;
  float zombie2ImageX = zombie2X - 7;
  float zombie2ImageY = zombie2Y - 26;

  float zombie3X = 620;
  float zombie3Y = 720;
  float zombie3ImageX = zombie3X - 7;
  float zombie3ImageY = zombie3Y - 26;

  float zombie4X = 920;
  float zombie4Y = 20;
  float zombie4ImageX = zombie4X - 7;
  float zombie4ImageY = zombie4Y - 26;

  float zombie5X = 1220;
  float zombie5Y = 420;
  float zombie5ImageX = zombie5X - 7;
  float zombie5ImageY = zombie5Y - 26;

  float zombie1Speed = 3;
  float zombie2Speed = 3;
  float zombie3Speed = 3;
  float zombie4Speed = 3;
  float zombie5Speed = 3;
  
  // declare meny screen images and menu screen vairables
  PImage imgInstructionScreen;
  PImage imgLooseScreen;
  PImage imgMenuScreen;
  PImage imgWinScreen;

  boolean isShowingMenu = false;

  // arrays for the falling rocks
  float[] circleY = new float[2];
  float[] circleX = new float[2];
  int intCircleSpeed = 2;

  // key location variables
  int firstKeyX = 120;
  int firstKeyY = 720;

  int secondKeyX = 420;
  int secondKeyY = 720;

  int thirdKeyX = 600;
  int thirdKeyY = 120;

  int fourthKeyX = 1200;
  int fourthKeyY = 120;

  // key collection variables
  int keysCollected = 0;
  int keyHeight = 50;
  int keyWidth = 85;
  boolean showKey1 = true;
  boolean showKey2 = true;
  boolean showKey3 = true;
  boolean showKey4 = true;

  public void settings() {

	// screen size
    size(1640,840);
  }

  public void setup() {

   // background colour and image
   background(139, 163, 155);
   imgBackground = loadImage("Background.png");

   // set menu screen images
   imgInstructionScreen = loadImage("InstructionScreen.png");
   imgLooseScreen = loadImage("LooseScreen.png");
   imgMenuScreen = loadImage("MenuScreen.png");
   imgWinScreen = loadImage("WinScreen.png");

   // set key images
   imgKey = loadImage("keyImage.png");
   imgKey.resize(100,100);

   imgSmallKey =loadImage("keyImage.png");
   imgSmallKey.resize(75,50);

   // set heart images
   imgHeart = loadImage("heart.png");
   imgHeart.resize(30,30);
   imgEmptyHeart = loadImage("EmptyHeart.png");
   imgEmptyHeart.resize(30,30);

   // set flashlight circle images
   imgFlashlightCircle = loadImage("flashlightCircle2.png");
   imgFlashlightCircle.resize(imgFlashlightCircle.width * 2, imgFlashlightCircle.height * 2);

   // set robot images
   imgRobot = loadImage("RobotSpriteSheet.png");

   imgRobotStill = imgRobot.get(0,0,192,256);
   imgRobotStill.resize(imgRobotStill.width/3, imgRobotStill.height/3);

   imgRobotWalkingSheet = imgRobot.get(0,1024,1536,256);
   imgRobotWalkingSheet.resize(imgRobotWalkingSheet.width/3, imgRobotWalkingSheet.height/3);

   imgRobotFall = imgRobot.get(768,768,192,256);
   imgRobotFall.resize(imgRobotFall.width/3,imgRobotFall.height/3);

   // set zombie images
   imgZombie = loadImage("ZombieSpriteSheet.png");

   imgZombieStill = imgZombie.get(0,0,192,256);
   imgZombieStill.resize(imgZombieStill.width/3, imgZombieStill.height/3);

   imgZombieAttack = imgZombie.get(384,0,192,256);
   imgZombieAttack.resize(imgZombieAttack.width/3, imgZombieAttack.height/3);

   imgZombieWalkingSheet = imgZombie.get(0,768,576,256);
   imgZombieWalkingSheet.resize(imgZombieWalkingSheet.width/3, imgZombieWalkingSheet.height/3);

   // robot walking sprite sheet setup
   robotFrames = new PImage[intWalkingRobotFrames];
    for(int frameNum = 0; frameNum < robotFrames.length; frameNum++ ){
      robotFrames[frameNum] = imgRobotWalkingSheet.get(intWalkingFrameWidth*frameNum, 0, intWalkingFrameWidth, imgRobotWalkingSheet.height );
    }

   // zombie walking sprite sheet setup
   zombieFrames = new PImage[intWalkingZombieFrames];
   for(int frameNum = 0; frameNum < zombieFrames.length; frameNum++ ){
     zombieFrames[frameNum] = imgZombieWalkingSheet.get(intWalkingFrameWidth*frameNum, 0, intWalkingFrameWidth, imgZombieWalkingSheet.height );
   }

   // loop for locations of falling rocks
   for (int i = 0; i < circleX.length; i++){
     circleY[i] = random(height);
     circleX[i] = random(120, width);
    }  
  }

  public void draw() {

	  // draw background by repeating the background over the width
    int intX = 0;
    int intY = 0;
    for(int intRow = 0; intRow < width; intRow+=(imgBackground.width)){
      for(int intColumn = 0; intColumn < height; intColumn+=(imgBackground.height)){
        intX = intRow; 
        intY = intColumn; 
        image(imgBackground, intX, intY);
      }
    }

    // draw maze outlines
    noStroke();
    fill(40, 66, 36);
    rect(20,20,1600,10); //top line
    rect(20,20,10,300); // left side
    rect(20,420,10,400);
    rect(20,810,1600,10); // bottom line
    rect(1610,20,10,600); // right side
    rect(1610,720,10,100);

    // draw maze lines
    rect(120, 120,10,600);
    rect(220, 20, 10, 400);
    rect(220, 520, 10, 300);
    rect(220,420,600,10);
    rect(220,520,700,10);
    rect(320,120,400,10);
    rect(420,220,300,10);
    rect(320,320,500,10);
    rect(420,220,300,10);
    rect(220,520,700,10);
    rect(320,620,700,10);
    rect(220,720,700,10);
    rect(320,120,10,100);
    rect(820,120,10,210);
    rect(920,120,10,410);
    rect(1020,120,10,200);
    rect(1020, 520,10,200);
    rect(1120, 620,10,200);
    rect(1020, 120, 500, 10);
    rect(720, 120, 10, 110);
    rect(1120, 220, 400, 10);
    rect(1020, 320, 400, 10);
    rect(1520, 220, 10, 200);
    rect(1020, 420, 510, 10);
    rect(1020, 520, 600, 10);
    rect(1120, 620,300,10);
    rect(1220, 720,300,10);
    rect(1520, 520,10,210);

    // final gate that opens if keys 4 keys are collected
    fill(122, 81, 13);
    if(!(keysCollected == 4)){
      rect(1610,620,10,100);
    }

    // opening gate that closes once player walks in the maze
    if(playerX > 20){ 
    rect(20,320,10,100);
    }
    
    // player image changes if movement keys are pressed
    if(keyPressed && (key == 'w' || key == 'a' || key == 's' || key == 'd')){
      image(robotFrames[(frameCount/3)%intWalkingRobotFrames], playerX, playerY);
    }
    else{
      image(imgRobotStill, playerX, playerY);
    }

    // player movement based on which key is pressed and if a wall is blocking the player
    if(keyPressed){
      if(key == 'w' && canMoveUP(playerX, playerY) == true){
        playerY -= playerSpeed;
      }
      else if(key == 'a'&& canMoveLEFT(playerX, playerY) == true){
        playerX -= playerSpeed;
      }
      else if(key == 's' && canMoveDOWN(playerX, playerY) == true){
        playerY += playerSpeed;
      }
      else if(key == 'd' && canMoveRIGHT(playerX, playerY) == true){
        playerX += playerSpeed;
      }
    }

    // zombie 1 movement controls
    image(zombieFrames[(frameCount/10)%intWalkingZombieFrames], zombie1X, zombie1Y);
    zombie1Y += zombie1Speed;

    // prevent zombie 1 from exiting the desired area
    if (zombie1X < 120 || zombie1X > 220){
     zombie1Speed *= -1;
    }
    if (zombie1Y < 30 || zombie1Y > 720){
     zombie1Speed *= -1;
    }

    // zombie 2 movement controls
    image(zombieFrames[(frameCount/10)%intWalkingZombieFrames], zombie2X, zombie2Y);
    zombie2X += zombie2Speed;

    // prevent zombie 2 from exiting the desired area
    if (zombie2X > 860 || zombie2X < 240){
     zombie2Speed *= -1;
    }
    if (zombie2Y < 320 || zombie2Y > 450){
     zombie2Speed *= -1;
    }

    // zombie 3 movement controls
    image(zombieFrames[(frameCount/10)%intWalkingZombieFrames], zombie3X, zombie3Y);
    zombie3X += zombie3Speed;

    // prevent zombie 3 from exiting the screen area
    if (zombie3X > 1040 || zombie3X < 240){
      zombie3Speed *= -1;
    }
    if (zombie3Y < 700 || zombie3Y > 840){
      zombie3Speed *= -1;
    }

    // zombie 4 movement controls
    image(zombieFrames[(frameCount/10)%intWalkingZombieFrames], zombie4X, zombie4Y);
    zombie4X += zombie4Speed;

    // prevent zombie 4 from exiting the desired area
    if (zombie4X > 1556 || zombie4X < 240){
      zombie4Speed *= -1;
    }
    if (zombie4Y < 10 || zombie4Y > 140){
      zombie4Speed *= -1;
    }

    // zombie 5 movement controls
    image(zombieFrames[(frameCount/10)%intWalkingZombieFrames], zombie5X, zombie5Y);
    zombie5X += zombie5Speed;

    // prevent zombie 5 from exiting the desired area
    if (zombie5X > 1556 || zombie5X < 940){
      zombie5Speed *= -1;
    }
    if (zombie5Y < 400 || zombie5Y > 600){
      zombie5Speed *= -1;
    }
 
    // draw keys if uncollected
    if (showKey1 == true){
      image(imgKey,firstKeyX,firstKeyY);
    }
    if (showKey2 == true){
      image(imgKey,secondKeyX,secondKeyY); 
    }
    if (showKey3 == true){
      image(imgKey,thirdKeyX,thirdKeyY); 
    }
    if (showKey4 == true){
      image(imgKey,fourthKeyX,fourthKeyY);
    }

    // draw circle for rocks
    stroke(0);
    for (int i = 0; i < circleY.length; i++){
      fill(156, 104, 50);
      ellipse(circleX[i], circleY[i], 40,40);
      circleY[i] = circleY[i] + intCircleSpeed;

      // redraw rocks at the top of the screen 
      if (circleY[i] >= height + 50 ){
        circleY[i] = 0 - random(0,1000);
        circleX[i] = random(0, width);
      }
    }

    // re-stating calculating image variables
    playerImageX = playerX - 7;
    playerImageY = playerY - 26;
    zombie1ImageX = zombie1X - 7;
    zombie1ImageY = zombie1Y - 26;
    zombie2ImageX = zombie2X - 7;
    zombie2ImageY = zombie2Y - 26;
    zombie3ImageX = zombie3X - 7;
    zombie3ImageY = zombie3Y - 26;
    zombie4ImageX = zombie4X - 7;
    zombie4ImageY = zombie4Y - 26;
    zombie5ImageX = zombie5X - 7;
    zombie5ImageY = zombie5Y - 26;

    // Zombie 1 colission detection
    if ((playerImageX > zombie1ImageX && playerImageX < zombie1ImageX + intZombieWidth && playerImageY > zombie1ImageY && playerImageY < zombie1ImageY + intZombieHeight) || 
    (playerImageX + robotWidth > zombie1ImageX && playerImageX  + robotWidth < zombie1ImageX + intZombieWidth && playerImageY  + robotHeight> zombie1ImageY && playerImageY + robotHeight < zombie1ImageY + intZombieHeight) || 
    (playerImageX > zombie1ImageX + intZombieWidth && playerImageX < zombie1ImageX + intZombieWidth && playerImageY > zombie1ImageY + intZombieHeight && playerImageY < zombie1ImageY + intZombieHeight) || 
    (playerImageX + robotWidth > zombie1ImageX + intZombieWidth && playerImageX  + robotWidth  < zombie1ImageX + intZombieWidth && playerImageY  + robotHeight> zombie1ImageY + intZombieHeight && playerImageY + robotHeight < zombie1ImageY + intZombieHeight)){
      playerLifeLost();
    }

    // Zombie 2 colission detection
    if ((playerImageX > zombie2ImageX && playerImageX < zombie2ImageX + intZombieWidth && playerImageY > zombie2ImageY && playerImageY < zombie2ImageY + intZombieHeight) || 
    (playerImageX + robotWidth > zombie2ImageX && playerImageX  + robotWidth < zombie2ImageX + intZombieWidth && playerImageY  + robotHeight> zombie2ImageY && playerImageY + robotHeight < zombie2ImageY + intZombieHeight) || 
    (playerImageX > zombie2ImageX + intZombieWidth && playerImageX < zombie2ImageX + intZombieWidth && playerImageY > zombie2ImageY + intZombieHeight && playerImageY < zombie2ImageY + intZombieHeight) || 
    (playerImageX + robotWidth > zombie2ImageX + intZombieWidth && playerImageX  + robotWidth  < zombie2ImageX + intZombieWidth && playerImageY  + robotHeight> zombie2ImageY + intZombieHeight && playerImageY + robotHeight < zombie2ImageY + intZombieHeight)){
      playerLifeLost();
    }

    // Zombie 3 colission detection
    if ((playerImageX > zombie3ImageX && playerImageX < zombie3ImageX + intZombieWidth && playerImageY > zombie3ImageY && playerImageY < zombie3ImageY + intZombieHeight) || 
    (playerImageX + robotWidth > zombie3ImageX && playerImageX  + robotWidth < zombie3ImageX + intZombieWidth && playerImageY  + robotHeight> zombie3ImageY && playerImageY + robotHeight < zombie3ImageY + intZombieHeight) || 
    (playerImageX > zombie3ImageX + intZombieWidth && playerImageX < zombie3ImageX + intZombieWidth && playerImageY > zombie3ImageY + intZombieHeight && playerImageY < zombie3ImageY + intZombieHeight) || 
    (playerImageX + robotWidth > zombie3ImageX + intZombieWidth && playerImageX  + robotWidth  < zombie3ImageX + intZombieWidth && playerImageY  + robotHeight> zombie3ImageY + intZombieHeight && playerImageY + robotHeight < zombie3ImageY + intZombieHeight)){
      playerLifeLost();
    }

    // Zombie 4 colission detection
    if ((playerImageX > zombie4ImageX && playerImageX < zombie4ImageX + intZombieWidth && playerImageY > zombie4ImageY && playerImageY < zombie4ImageY + intZombieHeight) || 
    (playerImageX + robotWidth > zombie4ImageX && playerImageX  + robotWidth < zombie4ImageX + intZombieWidth && playerImageY  + robotHeight> zombie4ImageY && playerImageY + robotHeight < zombie4ImageY + intZombieHeight) || 
    (playerImageX > zombie4ImageX + intZombieWidth && playerImageX < zombie4ImageX + intZombieWidth && playerImageY > zombie4ImageY + intZombieHeight && playerImageY < zombie4ImageY + intZombieHeight) || 
    (playerImageX + robotWidth > zombie4ImageX + intZombieWidth && playerImageX  + robotWidth  < zombie4ImageX + intZombieWidth && playerImageY  + robotHeight> zombie4ImageY + intZombieHeight && playerImageY + robotHeight < zombie4ImageY + intZombieHeight)){
      playerLifeLost();
    }

    // Zombie 5 colission detection
    if ((playerImageX > zombie5ImageX && playerImageX < zombie5ImageX + intZombieWidth && playerImageY > zombie5ImageY && playerImageY < zombie5ImageY + intZombieHeight) || 
    (playerImageX + robotWidth > zombie5ImageX && playerImageX  + robotWidth < zombie5ImageX + intZombieWidth && playerImageY  + robotHeight> zombie5ImageY && playerImageY + robotHeight < zombie5ImageY + intZombieHeight) || 
    (playerImageX > zombie5ImageX + intZombieWidth && playerImageX < zombie5ImageX + intZombieWidth && playerImageY > zombie5ImageY + intZombieHeight && playerImageY < zombie5ImageY + intZombieHeight) || 
    (playerImageX + robotWidth > zombie5ImageX + intZombieWidth && playerImageX  + robotWidth  < zombie5ImageX + intZombieWidth && playerImageY  + robotHeight> zombie5ImageY + intZombieHeight && playerImageY + robotHeight < zombie5ImageY + intZombieHeight)){
      playerLifeLost();
    }

    // Rock colission detection
    for (int i = 0; i < circleY.length; i++){
      if((playerImageX < (circleX[i] + 40)) && playerImageX > (circleX[i] - 40) && playerImageY > (circleY[i] - 40) && playerImageY < (circleY[i] + 40)){
        playerLifeLost();
      }
    }

    // key 1 colission detection
    if(showKey1 == true){
      if (playerX > 73 && playerX < 165 && playerY > 660 && playerY < 800){
        keysCollected = keysCollected + 1;
        showKey1 = false;
      } 
    }

    // key 2 colission detection
    if(showKey2 == true){
      if (playerX > 370 && playerX < 505 && playerY > 680 && playerY < 750){
        keysCollected = keysCollected + 1;
        showKey2 = false;
      } 
    }
    
    // key 3 colission detection
    if(showKey3 == true){
      if (playerX > 550 && playerX < 650 && playerY > 100 && playerY < 140){
        keysCollected = keysCollected + 1;
        showKey3 = false;
      } 
    }

    // key 4 colission detection
    if(showKey4 == true){
      if (playerX > 1150 && playerX < 1280 && playerY > 100 && playerY < 130){
        keysCollected = keysCollected + 1;
        showKey4 = false;
      } 
    }

    // flashlight movement
    image(imgFlashlightCircle, ((playerX - imgFlashlightCircle.width/2 ) + 35), ((playerY - imgFlashlightCircle.height/2) + 50 ));

    // draw keys collected
    if(keysCollected == 1){
      image(imgSmallKey,1555,780);
    }
    else if(keysCollected == 2){
      image(imgSmallKey,1555,780);
      image(imgSmallKey,1480,780);
    }
    else if(keysCollected == 3){
      image(imgSmallKey,1555,780);
      image(imgSmallKey,1480,780);
      image(imgSmallKey,1405,780);
    }
    else if(keysCollected == 4){
      image(imgSmallKey,1555,780);
      image(imgSmallKey,1480,780);
      image(imgSmallKey,1405,780);
      image(imgSmallKey,1330,780);
    }

    // draw lives
    if(playerLives == 5){
      image(imgHeart,10,10);
      image(imgHeart,40,10);
      image(imgHeart,70,10);
      image(imgHeart,100,10);
      image(imgHeart,130,10);
    }
    else if(playerLives == 4){
      image(imgHeart,10,10);
      image(imgHeart,40,10);
      image(imgHeart,70,10);
      image(imgHeart,100,10);
      image(imgEmptyHeart,130,10);
    }
    else if(playerLives == 3){
      image(imgHeart,10,10);
      image(imgHeart,40,10);
      image(imgHeart,70,10);
      image(imgEmptyHeart,100,10);
      image(imgEmptyHeart,130,10);
    }
    else if(playerLives == 2){
      image(imgHeart,10,10);
      image(imgHeart,40,10);
      image(imgEmptyHeart,70,10);
      image(imgEmptyHeart,100,10);
      image(imgEmptyHeart,130,10);
    }
    else if(playerLives == 1){
      image(imgHeart,10,10);
      image(imgEmptyHeart,40,10);
      image(imgEmptyHeart,70,10);
      image(imgEmptyHeart,100,10);
      image(imgEmptyHeart,130,10);
    }
    else if(playerLives == 0){
      fill(139, 163, 155);
      rect(0,0,1640,840);
      image(imgLooseScreen,0,0);
      if(mousePressed && playerLives == 0){
       keysCollected = 0;
       playerLives = 5;
       playerX = -22;
       playerY = 316;
       showKey1 = true;
       showKey2 = true;
       showKey3 = true;
       showKey4 = true;
      }
    }

    // win screen 
    if(playerX >= 1560){
      displayWinScreen();
    }

    // menu screen 
    if(playerX == -22 && playerY == 316){
      image(imgMenuScreen,0,0);
      isShowingMenu = true;
      image(zombieFrames[(frameCount/10)%intWalkingZombieFrames], 720, 360);
      image(robotFrames[(frameCount/3)%intWalkingRobotFrames], 850, 360);

      if(mousePressed){
        if(mouseX > 470 && mouseX < 1170 && mouseY > 600 && mouseY < 710){
          image(imgInstructionScreen,0,0);
        }
      }
    }
  }

 /**
  * Determines if a player can move upwards or not, based on the player's X and Y locations on the screen. 
  * 
  * @param playerX  the horizontal location of the player
  * @param playerY  the vertical location of the player
  * @return a  boolean value, determining if the player can move up or not. If false, the player can't move up, if true, the player can move up.
  * 
  */
  public boolean canMoveUP(float playerX, float playerY){
    if (playerY <= 20){
      return false;
    }
    else if(playerX > 70 && playerX < 110 && playerY > 690 && playerY < 695){
      return false;
    }
    else if(playerX > 183 && playerX < 790 && playerY > 405 && playerY < 410){
      return false;
    }
    else if(playerX > 270 && playerX < 810 && playerY > 310 && playerY < 315){
      return false;
    }
    else if(playerX > 310 && playerX < 700 && playerY > 80 && playerY < 115){
      return false;
    }
    else if(playerX > 365 && playerX < 715 && playerY > 190 && playerY < 205 ){
      return false;
    }
    else if(playerX > 1020 && playerX < 1500 && playerY > 105 && playerY < 115){
      return false;
    }
    else if(playerX > 1070 && playerX < 1500 && playerY > 195 && playerY < 210){
      return false;
    }
    else if(playerX > 975 && playerX < 1405 && playerY > 290 && playerY < 310){
      return false;
    }
    else if(playerX > 970 && playerX < 1505 && playerY > 390 && playerY < 410){
      return false;
    }
    else if(playerX > 180 && playerX < 915 && playerY > 495 && playerY < 510){
      return false;
    }
    else if(playerX > 270 && playerX < 1000 && playerY > 590 && playerY < 610){
      return false;
    }
    else if(playerX > 205 && playerX < 900 && playerY > 685 && playerY < 700){
      return false;
    }
    else if(playerX > 1005 && playerX < 1560 && playerY > 500 && playerY < 510){
      return false;
    }
    else if(playerX > 1100 && playerX < 1400 && playerY > 560 && playerY < 610 ){
      return false;
    }
    else if(playerX > 1173 && playerX < 1515 && playerY > 690 && playerY < 710){
      return false;
    }
    else if(playerX > -30 && playerX < 20 && playerY > 280 && playerY < 340){
      return false;
    }
    else{
      return true;
    }
  }

  /**
  * Determines if a player can move downward or not, based on the player's X and Y locations on the screen. 
  * 
  * @param playerX  the horizontal location of the player
  * @param playerY  the vertical location of the player
  * @return  a boolean value, determining if the player can move down or not. If false, the player can't move down, if true, the player can move down.
  * 
  */
  public boolean canMoveDOWN(float playerX, float playerY){
    if (playerY >= 724){
      return false;
    }
    else if (playerX > 75 && playerX < 110 && playerY > 30 && playerY < 35){
      return false;
    }
    else if (playerX > 183 && playerX < 866 && playerY > 430 && playerY < 435){
      return false;
    }
    else if (playerX > 225 && playerX < 790 && playerY > 330 && playerY < 335){
      return false;
    }
    else if(playerX > 265 && playerX < 720 && playerY > 30 && playerY < 40){
      return false;
    }
    else if(playerX > 270 && playerX < 795 && playerY > 230 && playerY < 260){
      return false;
    }
    else if(playerX > 375 && playerX < 670 && playerY > 125 && playerY < 155){
      return false;
    }
    else if(playerX > 970 && playerX < 1500 && playerY > 30 && playerY < 45){
      return false;
    }
    else if( playerX > 1060 && playerX < 1520 && playerY > 130 && playerY < 145){
      return false;
    }
    else if(playerX > 1020 && playerX < 1420 && playerY > 220 && playerY < 240){
      return false;
    }
    else if(playerX > 970 && playerX < 1504 && playerY > 330 && playerY < 350){
      return false;
    }
    else if(playerX > 270 && playerX < 980 && playerY > 525 && playerY < 535){
      return false;
    }
    else if(playerX > 205 && playerX < 900 && playerY > 625 && playerY < 645){
      return false;
    }
    else if(playerX > 970 && playerX < 1560 && playerY > 430 && playerY < 440){
      return false;
    }
    else if(playerX > 1070 && playerX < 1400 && playerY > 530 && playerY < 545){
      return false;
    }
    else if(playerX > 1170 && playerX < 1480 && playerY > 630 && playerY < 645){
      return false; 
    }
    else if(playerX > -30 && playerX < 20 && playerY > 280 && playerY < 340){
      return false;
    }
    else{
      return true;
    }
  }

  /**
  * Determines if a player can move right or not, based on the player's X and Y locations on the screen. 
  * 
  * @param playerX  the horizontal location of the player
  * @param playerY  the vertical location of the player
  * @return  a boolean value, determining if the player can move right or not. If false, the player can't move right, if true, the player can move right.
  * 
  */
  public boolean canMoveRIGHT(float playerX, float playerY){
    if (playerX > 60 && playerX < 70 && playerY < 690 && playerY > 50){
      return false;
    }
    else if (playerX > 65 && playerX < 70 && playerY > 50 && playerY < 680 ){
      return false;
    }
    else if(playerX > 160 && playerX < 165 && playerY > -20 && playerY < 390){
      return false;
    }
    else if(playerX > 265 && playerX < 270 && playerY > 40 && playerY < 200){
      return false;
    }
    else if(playerX > 260 && playerX < 270 && playerY > 240 && playerY < 300){
      return false;
    }
    else if(playerX > 160 && playerX < 170 && playerY > 430 && playerY < 740){
      return false;
    }
    else if(playerX > 765 && playerX < 780 && playerY > 50 && playerY < 240){
      return false;
    }
    else if(playerX > 660 && playerX < 670 && playerY > 105 && playerY < 160){
      return false;
    }
    else if(playerX > 850 && playerX < 890 && playerY > 45 && playerY < 440){
      return false;
    }
    else if(playerX > 960 && playerX < 970 && playerY > 45 && playerY < 300){
      return false;
    }
    else if(playerX > 1465 && playerX < 1480 && playerY > 160 && playerY < 390){
      return false;
    }
    else if(playerX > 964 && playerX < 980 && playerY > 430 && playerY < 690){
      return false;
    }
    else if(playerX > 1060 && playerX < 1085 && playerY > 550 && playerY < 760){
      return false;
    }
    else if(playerX > 1460 && playerX < 1470 && playerY > 470 && playerY < 670){
      return false;
    }
    else if(playerX > 1550 && playerX < 1560 && playerY > 0 && playerY < 575){
      return false;
    }
    else if(playerX > 1550 && playerX < 1560 && playerY > 640 && playerY < 790){
      return false;
    }
    else if(playerX > 1550 && playerX < 1560 && playerY > 590 && playerY < 640 && keysCollected < 4){
     return false;
    }
    else{
      return true;
    }
  }

  /**
  * Determines if a player can move left or not, based on the player's X and Y locations on the screen. 
  * 
  * @param playerX  the horizontal location of the player
  * @param playerY  the vertical location of the player
  * @return  a boolean value, determining if the player can move right or not. If false, the player can't move right, if true, the player can move right.
  * 
  */
  public boolean canMoveLEFT(float playerX, float playerY){
    if (playerX <= 30){
      return false;
    }
    else if (playerX < 20 && playerX > 25 && playerY > 0 && playerY < 730){
      return false;
    }
    else if (playerX > 125 && playerX < 130 && playerY > 40 && playerY < 690){
      return false;
    }
    else if (playerX < 830 && playerX > 825 && playerY > 40 && playerY < 300){
      return false;
    }
    else if (playerX < 815 && playerX > 810 && playerY > 325 && playerY < 405){
      return false;
    }
    else if(playerX < 320 && playerX > 315 && playerY > 85 && playerY < 175){
      return false;
    }
    else if(playerX < 725 && playerX > 715 && playerY > 40 && playerY < 195){
      return false;
    }
    else if(playerX < 930 && playerX > 920 && playerY > 40 && playerY < 505){
      return false;
    }
    else if(playerX < 1030 && playerX > 1015 && playerY > 65 && playerY < 270){
      return false;
    }
    else if(playerX < 1515 && playerX > 1500 && playerY > 35 && playerY < 100){
      return false;
    }
    else if(playerX < 1530 && playerX > 1505 && playerY > 130 && playerY < 400){
      return false;
    }
    else if(playerX < 225 && playerX > 215 && playerY > 470 && playerY < 656){
      return false;
    }
    else if(playerX < 1020 && playerX > 1005 && playerY > 460 && playerY < 700 ){
      return false;
    }
    else if(playerX < 1130 && playerX > 1110 && playerY > 580 && playerY < 730){
      return false;
    }
    else if(playerX < 1520 && playerX > 1510 && playerY > 490 && playerY < 700){
      return false;
    }
    else if(playerX < 230 && playerX > 220 && playerY >660 && playerY < 760){
      return false;
    }
    else if(playerX < 1640 && playerX > 1560){
      return false;
    }
    else{
      return true;
    }
  }

  /**
  * When all the keys are collected, the win screen is displayed.  If the screen is pressed and the keys are collected, the player's location, keys and lives are reset.
  * 
  */  
  public void displayWinScreen(){
    image(imgWinScreen,0,0);
    if(mousePressed && keysCollected == 4){
      keysCollected = 0;
      playerLives = 5;
      playerX = -22;
      playerY = 316;
      showKey1 = true;
      showKey2 = true;
      showKey3 = true;
      showKey4 = true;
    }
  }
  
  /**
  * When the player life is lost, the playerlives integer is decreased and the location is reset. If the mouse is pressed and all keys are collected, the game is reset.
  * 
  */    
  public void playerLifeLost(){
    playerLives = playerLives - 1;
    playerX = 22;
    playerY = 315;
    if(mousePressed && keysCollected == 4){
      keysCollected = 0;
      playerLives = 5;
      playerX = -22;
      playerY = 316;
      showKey1 = true;
      showKey2 = true;
      showKey3 = true;
      showKey4 = true;
    }
  }
}