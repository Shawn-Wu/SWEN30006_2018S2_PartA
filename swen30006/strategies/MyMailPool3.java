package strategies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyMailPool3 implements IMailPool {
	// My first job with Robotic Mailing Solutions Inc.!
	// 2 kinds of items so two structures
	// Remember stacks from 1st year - easy to use, not sure if good choice
	private Queue<MailItem> nonPriorityPool;
	private List<MailItem> priorityPool;
	private static final int MAX_TAKE = 4;
	private Robot robot1, robot2, robot3;  // There's only three of these.

	public MyMailPool3(){
		// Start empty
		nonPriorityPool = new LinkedList<MailItem>();
		priorityPool = new ArrayList<MailItem>();
	}

	public void addToPool(MailItem mailItem) {
		// Check whether it has a priority or not
		if(mailItem instanceof PriorityMailItem){
			// Add to priority items
			// Kinda feel like I should be sorting or something
			priorityPool.add(mailItem);
		}
		else{
			// Add to nonpriority items
			// Maybe I need to sort here as well? Bit confused now
			nonPriorityPool.offer(mailItem);
		}
	}
	
	private int getNonPriorityPoolSize(int weightLimit) {
		// This was easy until we got two kinds of robots and weight became an issue
		// Oh well, there's not that many heavy mail items -- this should be close enough
		return nonPriorityPool.size();
	}
	
	private int getPriorityPoolSize(int weightLimit){
		// Same as above, but even less heavy priority items -- hope this works too
		return priorityPool.size();
	}

	private MailItem getNonPriorityMail(int weightLimit){
		if(getNonPriorityPoolSize(weightLimit) > 0){
			// Should I be getting the earliest one? 
			// Surely the risk of the weak robot getting a heavy item is small!
			return nonPriorityPool.poll();
		}
		else{
			return null;
		}
	}
	
	private MailItem getHighestPriorityMail(int weightLimit){
		if(getPriorityPoolSize(weightLimit) > 0){
			//***get the highest priority mail
			int maxLevel=0;
			int maxLevelIndex=0;
			for(int i=0;i<priorityPool.size();i++) {
				PriorityMailItem onePriorityMail = (PriorityMailItem)priorityPool.get(i);
				if(onePriorityMail.getPriorityLevel()>maxLevel) {
					maxLevel = onePriorityMail.getPriorityLevel();
					maxLevelIndex = i;
				}
				System.out.println("***********"+maxLevel+"***index:"+maxLevelIndex);
			}
			System.out.println("#############");
			MailItem maxLevelMail = priorityPool.get(maxLevelIndex);
			priorityPool.remove(maxLevelIndex);
			return maxLevelMail;
		}
		else{
			return null;
		}	
	}
	
	@Override
	public void step() {
		// Bit repetitive - just glad there isn't 10 robots!!
		if (robot1 != null) fillStorageTube(robot1);
		if (robot2 != null) fillStorageTube(robot2);
		if (robot3 != null) fillStorageTube(robot3);
	}
	
	private void fillStorageTube(Robot robot) {
		StorageTube tube = robot.getTube();
		int max = robot.isStrong() ? Integer.MAX_VALUE : 2000; // max weight - I feel like I should use this somewhere
		// Priority items are important;
		// if there are some, grab one and go, otherwise take as many items as we can and go
		try{
			// Check for a top priority item
			if (getPriorityPoolSize(max) > 0) {
				// Add priority mail item
//				tube.addItem(getHighestPriorityMail(max));
//				// Won't add any more - want it delivered ASAP
//				robot.dispatch();
				while(tube.getSize() < MAX_TAKE && getPriorityPoolSize(max) > 0) {
					tube.addItem(getHighestPriorityMail(max));
				}
				if (tube.getSize() > 0) robot.dispatch();
			}
			else{
				// Get as many nonpriority items as available or as fit
				while(tube.getSize() < MAX_TAKE && getNonPriorityPoolSize(max) > 0) {
					tube.addItem(getNonPriorityMail(max));
				}
				if (tube.getSize() > 0) robot.dispatch();
			}
		}
		catch(TubeFullException e){
			e.printStackTrace();
		}
	}

	@Override
	public void registerWaiting(Robot robot) {
		// Also repetitive - what if there was more than 10 robots?!!
		if (robot1 == null) {
			robot1 = robot;
		} else if (robot2 == null) {
			robot2 = robot;
		} else if (robot3 == null) {
			robot3 = robot;
		} else {
			/* This can't happen, can it? What do I do here?!? */
		}
	}

	@Override
	public void deregisterWaiting(Robot robot) {
		if (robot1 == robot) {
			robot1 = null;
		} else if (robot2 == robot) {
			robot2 = null;
		} else if (robot3 == robot) {
			robot3 = null;
		} else {
			/* This can't happen, can it? What do I do here?!? */
		}
		
	}
	// Argghhh - never really wanted to be a programmer any way ...
}
