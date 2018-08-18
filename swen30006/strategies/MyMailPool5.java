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

public class MyMailPool5 implements IMailPool {
	// put mails into 4 different MailPool by mail's priority and mail's weight
	// nonPriorityHeavyPool store mails without priority and weight over 2000
	// nonPriorityLightPool store mails without priority and weight below 2000
	// priorityHeavyPool store mails with priority and weight over 2000
	// priorityLightPool store mails with priority and weight below 2000
	private Queue<MailItem> nonPriorityHeavyPool;
	private Queue<MailItem> nonPriorityLightPool;
	private List<MailItem> priorityHeavyPool;
	private List<MailItem> priorityLightPool;
	private static final int MAX_TAKE = 4;
	private Robot robot1, robot2, robot3;

	public MyMailPool5() {
		// Start empty
		nonPriorityHeavyPool = new LinkedList<MailItem>();
		nonPriorityLightPool = new LinkedList<MailItem>();
		priorityHeavyPool = new ArrayList<MailItem>();
		priorityLightPool = new ArrayList<MailItem>();
	}

	@Override
	public void addToPool(MailItem mailItem) {
		// Check mail's feature and add into different MailPool
		if (mailItem instanceof PriorityMailItem) {
			if (mailItem.getWeight() > 2000) {
				priorityHeavyPool.add(mailItem);
			} else {
				priorityLightPool.add(mailItem);
			}
		} else {
			if (mailItem.getWeight() > 2000) {
				nonPriorityHeavyPool.offer(mailItem);
			} else {
				nonPriorityLightPool.offer(mailItem);
			}
		}
	}
	/**
	 * get the size of nonPriorityHeavyPool
	 * @return the size of nonPriorityHeavyPool
	 */
	private int getNonPriorityHeavyPoolSize() {
		return nonPriorityHeavyPool.size();
	}

	/**
	 * get the size of nonPriorityLightPool
	 * @return the size of nonPriorityLightPool
	 */
	private int getNonPriorityLightPoolSize() {
		return nonPriorityLightPool.size();
	}

	/**
	 * get the size of priorityHeavyPool
	 * @return the size of priorityHeavyPool
	 */
	private int getPriorityHeavyPoolSize() {
		return priorityHeavyPool.size();
	}

	/**
	 * get the size of priorityLightPool
	 * @return the size of priorityLightPool
	 */
	private int getPriorityLightPoolSize() {
		return priorityLightPool.size();
	}

	/**
	 * if nonPriorityHeavyPool is not null, get a MailItem from the queue
	 * @return a MailItem or null
	 */
	private MailItem getNonPriorityHeavyMail() {
		if (getNonPriorityHeavyPoolSize() > 0) {
			return nonPriorityHeavyPool.poll();
		} else {
			return null;
		}
	}

	/**
	 * if nonPriorityLightPool is not null, get a MailItem from the queue
	 * @return a MailItem or null
	 */
	private MailItem getNonPriorityLightMail() {
		if (getNonPriorityLightPoolSize() > 0) {
			return nonPriorityLightPool.poll();
		} else {
			return null;
		}
	}

	/**
	 * if priorityLightPool is not null, get the highest PRIORITY_LEVEL mailItem from the priorityHeavyPool
	 * @return a MailItem or null
	 */
	private MailItem getHighestPriorityHeavyMail() {
		if (getPriorityHeavyPoolSize() > 0) {
			int maxLevel = 0;
			int maxLevelIndex = 0;
			// find the highest PRIORITY_LEVEL mailItem's index from the priorityHeavyPool
			for (int i = 0; i < priorityHeavyPool.size(); i++) {
				PriorityMailItem onePriorityMail = (PriorityMailItem) priorityHeavyPool.get(i);
				if (onePriorityMail.getPriorityLevel() > maxLevel) {
					maxLevel = onePriorityMail.getPriorityLevel();
					maxLevelIndex = i;
				}
			}
			//remove the highest mailItem from the list before return 
			MailItem highestPriorityMail = priorityHeavyPool.get(maxLevelIndex);
			priorityHeavyPool.remove(maxLevelIndex);
			return highestPriorityMail;
		} else {
			return null;
		}
	}

	/**
	 * if priorityLightPool is not null, get the highest PRIORITY_LEVEL mailItem from the priorityLightPool
	 * @return a MailItem or null
	 */
	private MailItem getHighestPriorityLightMail() {
		if (getPriorityLightPoolSize() > 0) {
			int maxLevel = 0;
			int maxLevelIndex = 0;
			// find the highest PRIORITY_LEVEL mailItem's index from the priorityLightPool
			for (int i = 0; i < priorityLightPool.size(); i++) {
				PriorityMailItem onePriorityMail = (PriorityMailItem) priorityLightPool.get(i);
				if (onePriorityMail.getPriorityLevel() > maxLevel) {
					maxLevel = onePriorityMail.getPriorityLevel();
					maxLevelIndex = i;
				}
			}
			//remove the highest mailItem from the list before return 
			MailItem highestPriorityMail = priorityLightPool.get(maxLevelIndex);
			priorityLightPool.remove(maxLevelIndex);
			return highestPriorityMail;
		} else {
			return null;
		}
	}

	@Override
	public void step() {
		if (robot1 != null)
			fillStorageTube(robot1);
		if (robot2 != null)
			fillStorageTube(robot2);
		if (robot3 != null)
			fillStorageTube(robot3);
	}

	/**
	 * 
	 * @param robot the robot to be loaded mailItem and dispatched
	 */
	private void fillStorageTube(Robot robot) {
		StorageTube tube = robot.getTube();
		boolean isStrong = robot.isStrong();
		try {
			// if the robot is strong, put mailItems into tube from mailPool by sequence of priorityHeavyPool,nonPriorityHeavyPool,priorityHeavyLightPool,nonPriorityLightPool
			// until the tube is full
			if (isStrong) {
				while (getPriorityHeavyPoolSize() > 0 && tube.getSize() < MAX_TAKE) {
					tube.addItem(getHighestPriorityHeavyMail());
				}
				while (getNonPriorityHeavyPoolSize() > 0 && tube.getSize() < MAX_TAKE) {
					tube.addItem(getNonPriorityHeavyMail());
				}
				while (getPriorityLightPoolSize() > 0 && tube.getSize() < MAX_TAKE) {
					tube.addItem(getHighestPriorityLightMail());
				}
				while (getNonPriorityLightPoolSize() > 0 && tube.getSize() < MAX_TAKE) {
					tube.addItem(getNonPriorityLightMail());
				}
				if (tube.getSize() > 0) {
					tube = sortTube(tube);
					robot.dispatch();
				}
			} 
			// if the robot is weak, put mailItems into tube from mailPool by sequence of priorityHeavyLightPool,nonPriorityLightPool
			// until the tube is full
			else {
				while (getPriorityLightPoolSize() > 0 && tube.getSize() < MAX_TAKE) {
					tube.addItem(getHighestPriorityLightMail());
				}
				while (getNonPriorityLightPoolSize() > 0 && tube.getSize() < MAX_TAKE) {
					tube.addItem(getNonPriorityLightMail());
				}
				if (tube.getSize() > 0) {
					tube = sortTube(tube);
					robot.dispatch();
				}
			}
		} catch (TubeFullException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/** 
	 * sort mailItems in tube
	 * @param backpack unsorted tube
	 * @param a sorted tube by DestFloor
	 * */
	public StorageTube sortTube(StorageTube backpack) {
		if(!backpack.tube.isEmpty()) {
			Stack<MailItem> orderTude = new Stack<MailItem>();
		    while(!backpack.tube.isEmpty()){
		        MailItem cur=backpack.tube.pop();
		        while(!orderTude.isEmpty()&&orderTude.peek().getDestFloor()<cur.getDestFloor()){
		            backpack.tube.push(orderTude.pop());
		        }
		        orderTude.push(cur);
		    }
		    while(!orderTude.isEmpty()){
		        backpack.tube.push(orderTude.pop());
		    }
			return backpack;
		}
		else {
			return backpack;
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

}
