//Sarah Bellaire
//SpaceInvaders.java
//This is a slightly less professional version of the popular 80s shooter arcade game, Space Invaders,
//which features the player as a space cannon that has to shoot enemy aliens that want to destroy
//you and your defenses. Using arroy keys to move left and right and space to shoot, every time the
//fleet of aliens are destroyed, a new and faster fleet comes along. Each hit adds a range of 10 - 30 
//points, depending on the alien shot, which add to your high score

import javax.swing.*;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SpaceInvaders extends JFrame implements ActionListener{
     Timer myTimer;
     gamePanel game;
    
    public SpaceInvaders(){
        super("Space Invaders");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(920, 742);
        
        myTimer = new Timer(10, this);
        
        game = new gamePanel(this);
        add(game);
        
        setLocationRelativeTo(null); //opens program in the middle of the screen
        setVisible(true); //makes the window visible
    }
    
    public void start(){
        myTimer.start();
    }

    public void actionPerformed(ActionEvent evnt){
        game.move();
        game.repaint(); 
    }
    
    public static void main(String[]args){
        SpaceInvaders frame = new SpaceInvaders();
    } 
}


class gamePanel extends JPanel implements KeyListener{
    private final int RIGHT = 1;
    private final int LEFT = 2;
    
    String screen = "GAME"; //screen that is displayed on screen
    
    private boolean wallTouch = false; //if aliens hit right or left wall
    private int direction = RIGHT; //direction aliens are moving
    private int score = 0; //players's high score

    
    private boolean []keys;
    Font fontLocal = null;
    private SpaceInvaders mainFrame;
    private ArrayList <Invaders> masterAliens = new ArrayList <Invaders>(); //masterlist of aliens used for calculations with their positions 
    private ArrayList <Invaders> aliens = new ArrayList <Invaders>(); //in-game list of aliens
    private ArrayList <Bullet> alienBombs = new ArrayList <Bullet>(); //list of alien's bullets
     private ArrayList <Shields> shields = new ArrayList <Shields>(); //list of plyer's shields
    
    Player ship = new Player(460, 625, 30, 50);
    Bullet bullet = new Bullet(485, 650, 15, 10);
    Invaders UFO = new Invaders(-50, 75, 40, 40, 0, 0);
    
    public gamePanel(SpaceInvaders m){
        keys = new boolean[KeyEvent.KEY_LAST + 1];
        
        String fName = "SpaceInvaders.ttf";
        InputStream is = gamePanel.class.getResourceAsStream(fName);
        
        try{
            fontLocal = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(32f);
        }
        catch(IOException ex){
            System.out.println(ex); 
        }
        catch(FontFormatException ex){
            System.out.println(ex); 
        }

        mainFrame = m;
        addKeyListener(this);
        
        initAliens();
        initShields(150, 475);
        initShields(350, 475);
        initShields(550, 475);
        initShields(750, 475);
    }
    
    public void addNotify(){
        super.addNotify();
        requestFocus();
        mainFrame.start();
    }
    
    public void initAliens(){
        //creating alien objects to a list to draw them
        int spaceX = 0;
        int spaceY = 75;
        for(int x = 0; x < 5; x++){
            int alienScore = 0;
            int type = 0;
            if(x == 0){ //depending on the row, the alien is a different type with a different score
                alienScore = 40;
                type = 1;
            }
            else if(x < 3){
                alienScore = 20;
                type = 2;
            }
            else{
                alienScore = 10;
                type = 3;
            }
            spaceX = 0;
            spaceY = spaceY + 45; 
            for(int y = 0; y < 11; y++){
                Invaders alien = new Invaders(75 + spaceX, spaceY, 40, 40, type, alienScore);
                spaceX += alien.getLength() + 18;
                aliens.add(alien); 
                masterAliens.add(alien); //add to a masterlist to keep their original positions
                if(x == 4){ //if in the last row, set it to lowest alien so it can shoot
                    alien.setLowestAlien(true);
                }
            }
        }
    }   
    
    //draw 10x10 boxes that make up the shields
    public void initShields(int startX, int startY){
        int x = 0;
        int y = 0;

        //creates the top square on the shields
        for (y = 0; y < 3; y++) {
            for (x = 0; x < 3; x++) {
                Shields shield = new Shields(startX + (x * Shields.getWidth()), (startY + y * Shields.getHeight()));
                shields.add(shield);
            }
        }
        
        //creates the 3 squares in the middle of the shields
        startX = startX - 30;
        startY = startY + Shields.getHeight() * 3;
        for (y = 0; y < 3; y++) {
            for (x = 0; x < 9; x++) {
                Shields shield = new Shields(startX + (x * Shields.getWidth()), (startY + y * Shields.getHeight()));
                shields.add(shield);
            }
        }
        
        //creates the square on the bottom left
        startY = startY + Shields.getHeight() * 3;
        for (y = 0; y < 3; y++) {
            for (x = 0; x < 3; x++) {
                Shields shield = new Shields(startX + (x * Shields.getWidth()), (startY + y * Shields.getHeight()));
                shields.add(shield);
            }
        }
        
        //creates the square on the bottom right
        startX = startX + 60;
        for (y = 0; y < 3; y++) {
            for (x = 0; x < 3; x++) {
                Shields shield = new Shields(startX + (x * Shields.getWidth()), (startY + y * Shields.getHeight()));
                shields.add(shield);
            }
        }
    }   
    
    //handles all movement in the gamePanel
    public void move(){
        checkGameOver();
        checkNextRound();
        setUpNextRound();
        moveShip();        
        moveAlien();
        shipShoot();
        alienShoot();
    }
    
    //moves x coordinates of player's ship
    public void moveShip(){
        //sets the x boundaries for the ship
        if((ship.getShipX() + ship.getLength()) < 890){ //right wall
            if(keys[KeyEvent.VK_RIGHT]){
                ship.setShipX(ship.getShipX() + ship.getSpeed());
            }
        }
        if(ship.getShipX() > 5){ //left wall
            if(keys[KeyEvent.VK_LEFT]){
                ship.setShipX(ship.getShipX() - ship.getSpeed());
            }
        }
    }
    
    //moves player's bullets y-values
    public void shipShoot(){       
        if(keys[KeyEvent.VK_SPACE]){
            if(ship.getShot() == false){ //makes it so only one shot can be out at a time
             ship.setShot(true);
             bullet.setBulletX(ship.getShipX() + (ship.getLength() / 2));
             bullet.setBulletY(ship.getShipY());
            }
        }
        
        if(ship.getShot() == true){
            if(bullet.getBulletY() + bullet.getLength() > 0){ //boundary set a top of screen
                bullet.setBulletY(bullet.getBulletY() - 10); //move bullet up
            }
            else{
                ship.setShot(false); //remove bullet once no longer in use
            }
        }
        
        Invaders alienHit = bullet.checkAlienHit(bullet, ship, aliens, masterAliens);
        
        if(alienHit != null){ //if hit is true, alien is removed and bullet is reset
            score += alienHit.getScore();
            ship.setShot(false);
            bullet.setBulletX(ship.getShipX() + (ship.getLength() / 2)); //reset bullet's position to the original pos in the cannon's barrel
            bullet.setBulletY(ship.getShipY());
        } 
        
        for(Shields s: shields){ // if hit is true, shield is removed, shot is reset and bullet is reset
            if(s.checkShieldHit(bullet) == true){
                shields.remove(s);
                ship.setShot(false);
                bullet.setBulletX(ship.getShipX() + (ship.getLength() / 2));
                bullet.setBulletY(ship.getShipY());
                break;
            }
        }
    }
    
    //enemy attack randomizer
    public void bulletRandomizer(){
        Random rand = new Random();
        Bullet eBullet;
        
        int randomizer;
        
        for(Invaders i: aliens){
            if(i.getLowestAlien() == true){
                randomizer = rand.nextInt(10000);
                if(randomizer < 8){ //randomize the amount of times aliens shoot(1 in 1,250 chance)
                    eBullet = new Bullet(i.getInvaderX() + (i.getWidth() / 2), i.getInvaderY(), 15, 10);
                    alienBombs.add(eBullet);
                }         
            }
        }   
    }
    
    //moves npc alien's x and y coordinates depending on their directions
    public void moveAlien(){
        Random rand = new Random();
        for(Invaders alien : aliens){ 
            alien.animateTick();
            
            if(alien.getInvaderY() >= 590){
                screen = "GAMEOVER";
            }
            if(direction == RIGHT){
                if(alien.getInvaderX() + alien.getWidth() == 920){ //hit right wall
                    wallTouch = true;
                } 
                else{
                    wallTouch = false;
                    alien.setInvaderX(alien.getInvaderX() + alien.getSpeed()); //move left
                }
            }
            else if(direction == LEFT){
                if(alien.getInvaderX() == 0){ //hit left wall
                    wallTouch = true;
                }
                else{
                    wallTouch = false;
                    alien.setInvaderX(alien.getInvaderX() - alien.getSpeed()); //move right
                }
            }
            //if aliens touch the wall, shift downwards
            if(wallTouch == true){
               for(Invaders i : aliens){
                    i.setInvaderY(i.getInvaderY() + 5); //move down
               }
               if(direction == RIGHT){
                   direction = LEFT;
                   wallTouch = false;
                }
               else{
                   direction = RIGHT;
                   wallTouch = false;
                }   
            }
        }
    }
    
    public void alienShoot(){
        bulletRandomizer();
        
        Iterator <Bullet> i = alienBombs.iterator();
        while(i.hasNext()){           
            Bullet b = i.next();
            if(bullet.checkPlayerHit(b, ship) == true){
            }
            for(Shields s: shields) {
                if (s.checkShieldHit(b) == true){
                    shields.remove(s);
                    i.remove();
                    break;
                }
            }
            if(b.getBulletY() + bullet.getWidth() < 742){
                b.setBulletY(b.getBulletY() + 8);
            }
            else{ //hit a boundary, delete it
                i.remove();
            }
        }
    }
    
    public boolean checkNextRound(){
        if(aliens.size() == 0){
            screen = "NEXT ROUND";
            return true;
        }
        return false;
    }
     
    public boolean setUpNextRound(){
        if(checkNextRound() == true){
            masterAliens.clear();
            initAliens();
            for(Invaders i: aliens){
                i.setSpeed(i.getSpeed() + 1);
            }
            screen = "GAME";
            return true;
        }
        else{
            return false;
        }
    }
    
    public void checkGameOver(){
        if(ship.getLives() == 0){
            screen = "GAMEOVER";
        }
    }

    public void keyTyped(KeyEvent evt) {}

    public void keyPressed(KeyEvent evt) {
        keys[evt.getKeyCode()] = true;
    }
    
    public void keyReleased(KeyEvent evt) {
        keys[evt.getKeyCode()] = false;
    }
    
    public void paint(Graphics g){
        g.setFont(fontLocal);
        if(screen == "GAME"){
            //background
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 920, 742);
            
            g.setColor(Color.WHITE);
            g.drawString("SCORE: " + score, 25, 50);
            g.drawString("LIVES: " + ship.getLives(), 740, 50);

            //player bullets
            if(ship.getShot() == true){
               g.setColor(Color.WHITE); 
               g.fillRect(bullet.getBulletX(), bullet.getBulletY(), bullet.getWidth(), bullet.getLength());
            }

            //alien bullets
            g.setColor(Color.WHITE);
            for(Bullet b: alienBombs){
                g.fillRect(b.getBulletX(), b.getBulletY(), b.getWidth(), b.getLength());
            }
            
            //player
            ship.draw(g);

            //invaders 
            for(Invaders i: aliens){
                i.draw(g, i);
            } 
            
            //shields
            for(Shields s: shields) {
                s.draw(g);
            }
        }
        else if(screen == "NEXT ROUND"){
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 920, 742);
            
            g.setColor(Color.WHITE);
            g.drawString("NEXT ROUND!", 400, 500);
        }
        
        else if(screen == "GAMEOVER"){
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 920, 742);
            
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER", 400, 500);
        }
    }
}

//the player class sets up the player's cannon that they shoot from
class Player{
    private int shipX, shipY;
    private int length, width;
    private boolean shot;
    private int lives = 3;
    private int speed = 4;
    private Image pic;
    
    public Player(int shipX, int shipY, int length, int width){
        this.shipX = shipX;
        this.shipY = shipY;
        this.length = length;
        this.width = width;
        
        pic = new ImageIcon("SpaceCannon.png").getImage();
        pic = pic.getScaledInstance(50, 30,Image.SCALE_SMOOTH);
    }
   
    public int getShipX(){
        return shipX;
    }
    
    public int getShipY(){
        return shipY;
    }
    
    public void setShipX(int posX){
        shipX = posX;
    }
    
    public void setShipY(int posY){
        shipY = posY;
    }
    
    public int getLength(){
        return length;
    }
    
    public int getWidth(){
        return width;
    }
    
    public boolean getShot(){
        return shot;
    }
    
    public void setShot(boolean condition){
        shot = condition;
    }
    
    public int getLives(){
        return lives;
    }
    
    public void setLives(int num){
        lives = num;
    }
    
    public int getSpeed(){
        return speed;
    }
    
    public void draw(Graphics g){
        g.drawImage(pic, shipX, shipY, null);
    }
}


//the invaders class sets up the aliens that the player has to shoot
class Invaders{
    private int invaderX, invaderY;
    private int length, width;
    private int speed = 1;
    private int type; //1 of 3 alien shapes
    private int frame;
    private int score; //3 different types of aliens have different score values
    private boolean lowestAlien;
    private int animateCnt = 0;
    
    private ImageIcon[] aliensStop;
    private ImageIcon[] aliensMove;
    
    public Invaders(int invaderX, int invaderY, int length, int width, int type, int score){
        this.invaderX = invaderX;
        this.invaderY = invaderY;
        this.length = length;
        this.width = width;
        this.type = type;
        this.score = score;
        
        frame = 0;
        aliensStop = new ImageIcon[3];
        aliensMove = new ImageIcon[3];
        for(int i = 0; i < 3; i++){ //stopping alien sprites
            aliensStop[i] = new ImageIcon("alien"+(i+1) + ".png");
            Image image = aliensStop[i].getImage().getScaledInstance(60,30,Image.SCALE_SMOOTH);
            aliensStop[i] = new ImageIcon(image);
        }
        for(int i = 0; i < 3; i++){ //moving alien sprites
            aliensMove[i] = new ImageIcon("alien"+(i+1) + "2.png");
            Image image = aliensMove[i].getImage().getScaledInstance(60,30,Image.SCALE_SMOOTH);
            aliensMove[i] = new ImageIcon(image);
        }
    }
    
    public int getInvaderX(){
        return invaderX;
    }
    
    public int getInvaderY(){
        return invaderY;
    }
    
    public void setInvaderX(int posX){
        invaderX = posX;
    }
    public void setInvaderY(int posY){
        invaderY = posY;
    }
    
    public int getWidth(){
        return width;
    }
    
    public int getLength(){
        return length;
    }
    
    public int getSpeed(){
        return speed;
    }
    
    public void setSpeed(int speed){
        this.speed = speed;
    }
    
    public int getType(){
        return type;
    }
    
    public int getScore(){
        return score;
    }
    
    public boolean getLowestAlien(){
        return lowestAlien;
    }
    
    public void setLowestAlien(boolean condition){
        lowestAlien = condition;
    }
    
    public void animateTick(){
        animateCnt += 1;
        
        if(animateCnt > 50){
            animateCnt = 0;
        }
    }
    
    public void draw(Graphics g, Invaders i){
        if(animateCnt < 25){
            if(i.getType() == 1){
                g.drawImage(aliensMove[frame].getImage(), invaderX, invaderY, null);
            }
            else if(i.getType() == 2){
                g.drawImage(aliensMove[frame + 1].getImage(), invaderX, invaderY, null);
            }
            else if(i.getType() == 3){
                g.drawImage(aliensMove[frame + 2].getImage(), invaderX, invaderY, null);
                
            }
        }
        else{
            if(i.getType() == 1){
                g.drawImage(aliensStop[frame].getImage(), invaderX, invaderY, null);
            }
            else if(i.getType() == 2){
                g.drawImage(aliensStop[frame + 1].getImage(), invaderX, invaderY, null);
            }
            else if(i.getType() == 3){
                g.drawImage(aliensStop[frame + 2].getImage(), invaderX, invaderY, null);
            }
        }  
    }
}

//the bullet class sets up the  bullets that both the player and the invaders shoot
class Bullet{
    private int bulletX, bulletY;
    private int length, width;
    private int speed = 4;
    
    public Bullet(int bulletX, int bulletY, int length, int width){
        this.bulletX = bulletX;
        this.bulletY = bulletY;
        this.length = length;
        this.width = width;
    }
    
    public int getBulletX(){
        return bulletX;
    }
    
    public int getBulletY(){
        return bulletY;
    }
    
    public void setBulletX(int posX){
        bulletX = posX;
    }
    
    public void setBulletY(int posY){
        bulletY = posY;
    } 
    
    public int getLength(){
        return length;
    }
    
    public int getWidth(){
        return width;
    }
        
    public Invaders checkAlienHit(Bullet bullet, Player ship, ArrayList <Invaders> aliens, ArrayList <Invaders> masterAliens){ //checks to see if the bullet collided with the aliens
        if(bullet.getBulletX() == (ship.getShipX() + (ship.getLength() / 2)) &&  bullet.getBulletY() == ship.getShipY()){ //if bullet is not in the air, then return false right away
            return null;
        }
        
        for(Invaders i: aliens){
            if(bullet.getBulletX() >= i.getInvaderX() && bullet.getBulletY() >= i.getInvaderY()){
                if(bullet.getBulletX() <= (i.getInvaderX() + i.getLength()) && bullet.getBulletY() <= (i.getInvaderY() + i.getWidth())){
                    aliens.remove(i);
                    if(i.getLowestAlien() == true){
                        i.setLowestAlien(false);
                        int indexOldAlien = masterAliens.indexOf(i); //find old pos of alien that was shot
                        int indexNewAlien = indexOldAlien - 11; //find the new lowest alien above the one that was shot
                        if(indexNewAlien >= 0){
                          Invaders newAlien = masterAliens.get(indexNewAlien);
                          newAlien.setLowestAlien(true);
                        }
                    }
                    return i;
                }
            }
        }
        return null;
    }
    
    public boolean checkPlayerHit(Bullet eBullet, Player ship){        
        boolean hit = true; //hit variable so once the player is hit, it cannot be touched by the same bullet again
        if(eBullet.getBulletX() >= ship.getShipX() && eBullet.getBulletY() >= ship.getShipY()){
            if(eBullet.getBulletX() <= (ship.getShipX() + ship.getLength()) && eBullet.getBulletY() <= (ship.getShipY() + ship.getWidth())){
                hit = true;
                if(hit = true){
                    if(ship.getLives() > 0){ //once lives are 0, the game is over and no more lives can be lost
                        ship.setLives(ship.getLives() - 1); //loses a life every time it shot
                        hit = false;
                        ship.setShipX(460);
                        ship.setShipY(625);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}


//the shields class sets up the shields that protect the player from the aliens
//however, they are slowly destroyed with every bullet shot at them from both the player and the aliens
class Shields{
    private int shieldX, shieldY;
    private int shieldWidth, shieldHeight;
    private static int WIDTH = 10;
    private static int HEIGHT = 10;
    
    public Shields(int shieldX, int shieldY){
        this.shieldX = shieldX;
        this.shieldY = shieldY;
        shieldWidth = WIDTH;
        shieldHeight = HEIGHT;
    }
    
    public static int getWidth(){
        return WIDTH;
    }
    
    public static int getHeight(){
        return HEIGHT;
    }
   
    public int getShieldX(){
        return shieldX;
    }
    
    public int getShieldY(){
        return shieldY;
    }
    
    public boolean checkShieldHit(Bullet bullet){
        if(bullet.getBulletX() >= shieldX && bullet.getBulletY() >= shieldY){
            if((bullet.getBulletX() <= (shieldX + shieldWidth)) && bullet.getBulletY() <= (shieldY + shieldHeight)){
                return true;
            }
        }
        return false;
    }
    
    public void draw(Graphics g){
        g.setColor(Color.GREEN);
        g.fillRect(shieldX, shieldY, shieldWidth, shieldHeight); 
        g.setColor(Color.BLACK);
        return;
    }
}