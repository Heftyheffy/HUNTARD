import java.awt.*;
import javax.swing.JFrame;
import java.lang.*;
import java.util.*;
import java.io.*;
import java.awt.image.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;

public class Hmg extends Canvas implements Runnable{
    public static final int width = 400;
    public static final int height = 300;
    public static final int scale = 2;
    public final String title = "Hunting Mini-Game";
    public String lastKey = "";

    private boolean running = false;
    private Thread thread;

    private BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
    private BufferedImage sSheet = null;
    private BufferedImage background = null;

    private int enemyCount;
    private int enemyKilled;

    private Hplayer p;
    private Controller c;
    private Texture tex;
    private Score scr;

    public LinkedList<EntityA> ea;
    public LinkedList<EntityB> eb;

    public static int health;
    private int timeLeft = 60;

    public static Hmg game;
    public static JFrame frame;
    public static int strength;
    public static int endFood;
    public static int map;

    public enum STATE{
	GAME,
	SCORE,
	END
    };
    public static STATE State = STATE.GAME;



    public void init(){
	requestFocus();
	BufferedImageLoader loader = new BufferedImageLoader();
	try{
	    sSheet = loader.loadImage("spritesheet.png");
	    background = loader.loadImage("background.png");
	}catch(IOException e){
	    System.out.println("ya didnt get the sprites lad");
	}
	addKeyListener(new KeyInput(this));
	this.addMouseListener(new MouseInput());
	tex = new Texture(this);
	c = new Controller(tex, this);
	p = new Hplayer(200,200, tex, this, c);
	scr = new Score();
	ea = c.getEntityA();
	eb = c.getEntityB();
	c.createEnemy(enemyCount);
		
    }
    //the core loop to run the game
    public void run(){
	init();
	//for the runnable stuffs
	long lastTime = System.nanoTime();
	final double tickFrame = 60.0;
	double ns = 1000000000 / tickFrame;
	double delta = 0;
	int updates = 0;
	int frames = 0;
	long timer = System.currentTimeMillis();
	//the variables above stay outside the loop to handle the games frame rate
	while(running){
	    long now = System.nanoTime();
	    delta += (now - lastTime)/ns;
	    lastTime=now;
	    //the above code synchronizes the frames between when the variables are created and when the loop starts... because nano seconds and all.
	    if(delta>=1){	        
		tick();
		updates++;
		delta--;
	    }
	    render();
	    frames++;
	    if(System.currentTimeMillis() - timer >1000){
		timer +=1000;
		timeLeft = timeLeft - 10;
		updates=0;
		frames = 0;
		//printable system updates for the running game
	    }	
	}
	stop();	
    }
    private void tick(){
	if(State == STATE.GAME){
	    p.tick();
	    c.tick();
	}
	if(enemyKilled>=enemyCount){
	    enemyCount+=1;
	    enemyKilled = 0;
	    c.createEnemy(enemyCount);
	}
	if(State == STATE.END){
	    game.stop();
	}
	if(timeLeft<= 0){
	    State = STATE.SCORE;
	}
    }
    //method for making the pictures on the screen woooooo!!@$@#!$!@$
    private void render(){
	BufferStrategy bs = this.getBufferStrategy(); 

	if(bs == null){
	    createBufferStrategy(3);
	    return;
	}
	Graphics g = bs.getDrawGraphics();
	//Start loading graphics
	g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
	g.drawImage(background, 0, 0, null);

	if(State == STATE.GAME){
	    p.render(g);
	    c.render(g);
	    
	    //character health bar
	    g.setColor(Color.gray);
	    g.fillRect(5,5,200,50);

	    g.setColor(Color.green);
	    g.fillRect(5,5,health,50);

	    g.setColor(Color.white);
	    g.drawRect(5,5,200,50);

	    //game timing bar
	    g.setColor(Color.gray);
	    g.fillRect(5,550,600,50);

	    g.setColor(Color.red);
	    g.fillRect(5,550,timeLeft,50);

	    g.setColor(Color.white);
	    g.drawRect(5,550,600,50);

	}else if(State == STATE.SCORE){
	    scr.render(g);
	    
	    Font fnt0 = new Font("arial", Font.BOLD, 40);
	    g.setFont(fnt0);
	    g.setColor(Color.white);
	    g.drawString("Score" + Enemy.kc, Hmg.width/2+50, 400);

	    endFood = Enemy.kc /10;
	}
	//Stop loading graphics
	g.dispose();
	bs.show();

    }
    //Starts the game, if the game is already running, the running variable is set to true and the method is exited.
    private synchronized void start(){
	if(running)
	    return;
	running = true;
	thread = new Thread(this);
	thread.start();
    }
    //stops the game and kills threads.
    private synchronized void stop(){
	/*
	if(!running)
	    return;
	*/
	running = false;
	frame.dispose();
	HuntardMainScreen.sendResults(endFood, health/2);
	try{
	    thread.join();
	}
	catch(InterruptedException e){
	    System.out.println("THE THREADS ARE DEAD SEND HELP");
	}	
        		
    }
    //Overloaded key methods wew
    public void keyPressed(KeyEvent e){
	int key = e.getKeyCode();

	if(State == STATE.GAME){
	    if(key == KeyEvent.VK_UP){
		p.setVelY(-5);
		lastKey = "up";
	    }else if(key == KeyEvent.VK_DOWN){
		p.setVelY(5);
		lastKey = "down";
	    }else if(key == KeyEvent.VK_LEFT){
		p.setVelX(-5);
		lastKey = "left";
	    }else if(key == KeyEvent.VK_RIGHT){
		p.setVelX(5);
		lastKey = "right";
	    }else if (key == KeyEvent.VK_SPACE){
		c.addEntityA(new Bullet(p.getX(), p.getY(), tex, this, lastKey));
	    }
	}
    }
    public void keyReleased(KeyEvent e){
	int key = e.getKeyCode();
	if(key == KeyEvent.VK_UP){
	    p.setVelY(0);
	}else if(key == KeyEvent.VK_DOWN){
	    p.setVelY(0);
	}else if(key == KeyEvent.VK_LEFT){
	    p.setVelX(0);
	}else if(key == KeyEvent.VK_RIGHT){
	    p.setVelX(0);
	}
    }
    //temporary main method
    public static void startHmg(int s, int h, int m){
        health = h*2;
	strength = s;
	map = m;

	game = new Hmg();
	frame = new JFrame(game.title);
	State = STATE.GAME;

	endFood = 0;
	game.enemyCount = 1;
	game.enemyKilled = 0;
	Enemy.kc = 0;

	game.setPreferredSize(new Dimension(width*scale,height*scale));
	game.setMaximumSize(new Dimension(width*scale,height*scale));
	game.setMinimumSize(new Dimension(width*scale,height*scale));

	frame.add(game);
	frame.pack();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setResizable(false);
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
	game.start();
    }	
    public BufferedImage getSS(){
	return sSheet;
    }
    public int getEnemyKilled(){
	return enemyKilled;
    }
    public int getEnemyCount(){
	return enemyCount;
    }
    public void setEnemyKilled(int enemyKilled){
	this.enemyKilled=enemyKilled;
    }
    public void setEnemyCount(int enemyCount){
	this.enemyCount=enemyCount;
    }
    public String getlastKey(){
	return lastKey;
    }

    public static void main(String[] args){
	startHmg(9, 80, 1);
    }
}
