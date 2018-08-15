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

public class MyMailPool4 implements IMailPool{
	// My first job with Robotic Mailing Solutions Inc.!
	// 2 kinds of items so two structures
	// Remember stacks from 1st year - easy to use, not sure if good choice
	private Queue<MailItem> nonPriorityHeavyPool;
	private Queue<MailItem> nonPriorityLightPool;
	private List<MailItem> priorityHeavyPool;
	private List<MailItem> priorityLightPool;
	private static final int MAX_TAKE = 4;
	private Robot robot1, robot2, robot3;  // There's only three of these.

	public MyMailPool4(){
		// Start empty
		nonPriorityHeavyPool = new LinkedList<MailItem>();
		nonPriorityLightPool = new LinkedList<MailItem>();
		priorityHeavyPool = new ArrayList<MailItem>();
		priorityLightPool = new ArrayList<MailItem>();
	}

	@Override
	public void addToPool(MailItem mailItem) {
		// Check whether it has a priority or not
		if(mailItem instanceof PriorityMailItem){
			// Add to priority items
			// Kinda feel like I should be sorting or something
			if(mailItem.getWeight()>2000) {
				priorityHeavyPool.add(mailItem);
			}
			else {
				priorityLightPool.add(mailItem);
			}
		}
		else{
			// Add to nonpriority items
			// Maybe I need to sort here as well? Bit confused now
			if(mailItem.getWeight()>2000) {
				nonPriorityHeavyPool.offer(mailItem);
			}
			else {
				nonPriorityLightPool.offer(mailItem);
			}
		}
	}
	
	private int getNonPriorityHeavyPoolSize() {
		return nonPriorityHeavyPool.size();
	}
	private int getNonPriorityLightPoolSize() {
		return nonPriorityLightPool.size();
	}
	private int getPriorityHeavyPoolSize() {
		return priorityHeavyPool.size();
	}
	private int getPriorityLightPoolSize() {
		return priorityLightPool.size();
	}
	
	private MailItem getNonPriorityHeavyMail() {
		if(getNonPriorityHeavyPoolSize()>0) {
			return nonPriorityHeavyPool.poll();
		}
		else {
			return null;
		}
	}	
	private MailItem getNonPriorityLightMail() {
		if(getNonPriorityLightPoolSize()>0) {
			return nonPriorityLightPool.poll();
		}
		else {
			return null;
		}
	}
	private MailItem getHighestPriorityHeavyMail() {
		if(getPriorityHeavyPoolSize()>0) {
			int maxLevel=0;
			int maxLevelIndex=0;
			for(int i=0;i<priorityHeavyPool.size();i++) {
				PriorityMailItem onePriorityMail = (PriorityMailItem)priorityHeavyPool.get(i);
				if(onePriorityMail.getPriorityLevel()>maxLevel) {
					maxLevel = onePriorityMail.getPriorityLevel();
					maxLevelIndex = i;
				}
			}
			MailItem highestPriorityMail = priorityHeavyPool.get(maxLevelIndex);
			priorityHeavyPool.remove(maxLevelIndex);
			return highestPriorityMail;
		}
		else {
			return null;
		}
	}
	private MailItem getHighestPriorityLightMail() {
		if(getPriorityLightPoolSize()>0) {
			int maxLevel=0;
			int maxLevelIndex=0;
			for(int i=0;i<priorityLightPool.size();i++) {
				PriorityMailItem onePriorityMail = (PriorityMailItem)priorityLightPool.get(i);
				if(onePriorityMail.getPriorityLevel()>maxLevel) {
					maxLevel = onePriorityMail.getPriorityLevel();
					maxLevelIndex = i;
				}
			}
			MailItem highestPriorityMail = priorityLightPool.get(maxLevelIndex);
			priorityLightPool.remove(maxLevelIndex);
			return highestPriorityMail;
		}
		else {
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
		boolean isStrong = robot.isStrong();
		try {
			if(isStrong) {
//				System.out.println(getPriorityHeavyPoolSize());
//				System.out.println(getNonPriorityHeavyPoolSize());
//				System.out.println(getPriorityLightPoolSize());
//				System.out.println(getNonPriorityLightPoolSize());
				while(getPriorityHeavyPoolSize()>0 && tube.getSize()<MAX_TAKE) {
					tube.addItem(getHighestPriorityHeavyMail());
				}
				while(getNonPriorityHeavyPoolSize()>0 && tube.getSize()<MAX_TAKE) {
					tube.addItem(getNonPriorityHeavyMail());
				}
				
				while(getPriorityLightPoolSize()>0 && tube.getSize()<MAX_TAKE) {
					tube.addItem(getHighestPriorityLightMail());
				}
				while(getNonPriorityLightPoolSize()>0 && tube.getSize()<MAX_TAKE) {
					tube.addItem(getNonPriorityLightMail());
				}
				if (tube.getSize() > 0) {
					robot.dispatch();
				}
			}
			else {
				while(getPriorityLightPoolSize()>0 && tube.getSize()<MAX_TAKE) {
					tube.addItem(getHighestPriorityLightMail());
				}
				while(getNonPriorityLightPoolSize()>0 && tube.getSize()<MAX_TAKE) {
					tube.addItem(getNonPriorityLightMail());
				}
				if (tube.getSize() > 0) {
					robot.dispatch();
				}
			}
		}
		catch (TubeFullException e) {
			// TODO Auto-generated catch block
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
