/**
 * @author Alexander Wang - bippity
 * FishThief - OSBot
 * Exploits the fishing bots that exploit the game
 */

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(author = "Bippity", info = "Barbville fish script", name = "Fish Thief", version = 1.0, logo = "")
public class main extends Script
{
	int deposited = 0; //Amount of fish deposited into bank
	boolean returning = true; //Whether player is heading back to barbarian village fishing spot
	private long startTime;
	private String status;
	
	//An array of Positions from the Barbarian village fishing spot to the GE
	private final Position[] bankPath = {
			new Position(3106, 3433, 0), new Position(3116, 3433, 0), new Position(3124, 3432, 0),
			new Position(3134, 3434, 0), new Position(3143, 3439, 0), new Position(3150, 3444, 0),
			new Position(3158, 3451, 0), new Position(3162, 3457, 0), new Position(3164, 3465, 0),
			new Position(3164, 3475, 0), new Position(3164, 3481, 0), new Position(3164, 3485, 0)
	};
	private final Position[] returnPath = reversePath(bankPath); //Reverses the path, heads back to barb village
	
	@Override
	//This method runs when the script starts
	public void onStart()
	{
		log("[Fishing Thief] Let's get started!");
		startTime = System.currentTimeMillis();
	}
	
	private enum State { STEAL, BANK, FISH, WAIT };
	private State getState()
	{
		GroundItem fish = getGroundItems().closest("Raw salmon");
		GroundItem fish2 = getGroundItems().closest("Salmon");
		
		if (inventory.isFull())
			return State.BANK;
		
		if (returning)
		{
			return State.FISH;
		}
		
		if (fish != null || fish2 != null)
			return State.STEAL;
		return State.WAIT;
	}
	
	
	@Override
	public int onLoop() throws InterruptedException
	{
		switch (getState())
		{
			case STEAL:
				status = "Getting Salmon";
				GroundItem fish = getGroundItems().closest("Raw salmon");
				GroundItem fish2 = getGroundItems().closest("Salmon"); //priority
				if (fish2 != null)
				{
					fish.interact("Take");
				}
				else if (fish != null)
				{
					fish.interact("Take");
				}
				break;
				
			case FISH:
				status = "Returning to fish";
				log("[Fishing Thief] Deposited inventory, going back to fishing.");
				localWalker.walkPath(returnPath); //walks back to fishing spot
				returning = false;
				break;
				
			case BANK: //store inv into bank
				status = "Depositing inventory.";
				//int amt = (int)getInventory().getAmount("Raw salmon");
				localWalker.walkPath(bankPath);
				log("[Fishing Thief] Inventory full! Going to bank.");
				
				NPC banker = getNpcs().closest("Banker");
				if (banker != null)
				{
					banker.interact("Bank");
					sleep(9000);
					if (getBank().isOpen())
					{
						getBank().depositAll();
						returning = true;
					}
					getBank().close();
				}
				deposited += 28;
				
				long runtime = System.currentTimeMillis() - startTime;
				if (runtime >= 3 * 3600000) //3 hours
				{
					logoutTab.logOut();
					stop();
				}
				break;
				
			case WAIT:
				sleep(random(500, 700));
				break;
		}
		return random(200, 300);
	}
	
	@Override
	public void onExit()
	{
		log("[Fishing Thief] Thanks for running my Fishing Thief!");
	}
	
	@Override
	public void onPaint(Graphics2D g)
	{
		long runTime = System.currentTimeMillis() - startTime;
		g.setColor(Color.white);
		g.drawString("Time Elapsed: " + formatTime(runTime), 342, 257);
		g.drawString("Salmon Deposited: " + deposited, 342, 270);
		g.drawString("Status: " + status, 15, 320);
	}
	
	public Position[] reversePath(Position[] p)
	{
		Position[] path = new Position[p.length];
		System.arraycopy(p, 0, path, 0, p.length);
		for (int i = 0; i < path.length /2; i++) //reverses path
		{
			Position temp = path[i];
			path[i]= path[path.length - i - 1];
			path[path.length - i - 1] = temp;
		}
		return path;
	}
	
	public String formatTime(long ms)
	{
		long s = ms / 1000, m = s / 60, h = m / 60;
		s %= 60; m %= 60; h %= 24;
		return String.format("%02d:%02d:%02d", h, m, s);
	}
}
