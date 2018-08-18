package strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyMailPool6 implements IMailPool {
	// put mails into 4 different MailPool by mail's priority and mail's weight
	// nonPriorityHeavyPool store mails without priority and weight over 2000
	// nonPriorityLightPool store mails without priority and weight below 2000
	// priorityHeavyPool store mails with priority and weight over 2000
	// priorityLightPool store mails with priority and weight below 2000
	private List<MailItem> nonPriorityHeavyPool;
	private List<MailItem> nonPriorityLightPool;
	private List<MailItem> priorityHeavyPool;
	private List<MailItem> priorityLightPool;
	private static final int MAX_TAKE = 4;
	// robots store a list of robot
	private List<Robot> robots;

	public MyMailPool6() {
		// Start empty
		nonPriorityHeavyPool = new ArrayList<MailItem>();
		nonPriorityLightPool = new ArrayList<MailItem>();
		priorityHeavyPool = new ArrayList<MailItem>();
		priorityLightPool = new ArrayList<MailItem>();
		robots = new ArrayList<Robot>();
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
				nonPriorityHeavyPool.add(mailItem);
			} else {
				nonPriorityLightPool.add(mailItem);
			}
		}
	}

	/**
	 * get the size of nonPriorityHeavyPool
	 * 
	 * @return the size of nonPriorityHeavyPool
	 */
	private int getNonPriorityHeavyPoolSize() {
		return nonPriorityHeavyPool.size();
	}

	/**
	 * get the size of nonPriorityLightPool
	 * 
	 * @return the size of nonPriorityLightPool
	 */
	private int getNonPriorityLightPoolSize() {
		return nonPriorityLightPool.size();
	}

	/**
	 * get the size of priorityHeavyPool
	 * 
	 * @return the size of priorityHeavyPool
	 */
	private int getPriorityHeavyPoolSize() {
		return priorityHeavyPool.size();
	}

	/**
	 * get the size of priorityLightPool
	 * 
	 * @return the size of priorityLightPool
	 */
	private int getPriorityLightPoolSize() {
		return priorityLightPool.size();
	}

	/**
	 * if nonPriorityHeavyPool is not null, get a MailItem from the list after
	 * sorting the list by DestFloor
	 * 
	 * @return a MailItem or null
	 */
	private MailItem getNonPriorityHeavyMail() {
		if (getNonPriorityHeavyPoolSize() > 0) {
			Collections.sort(nonPriorityHeavyPool, new SortByDestFloor());
			MailItem mailItem = nonPriorityHeavyPool.get(0);
			nonPriorityHeavyPool.remove(0);
			return mailItem;
		} else {
			return null;
		}
	}

	/**
	 * if nonPriorityLightPool is not null, get a MailItem from the list after
	 * sorting the list by DestFloor
	 * 
	 * @return a MailItem or null
	 */
	private MailItem getNonPriorityLightMail() {
		if (getNonPriorityLightPoolSize() > 0) {
			Collections.sort(nonPriorityLightPool, new SortByDestFloor());
			MailItem mailItem = nonPriorityLightPool.get(0);
			nonPriorityLightPool.remove(0);
			return mailItem;
		} else {
			return null;
		}
	}

	/**
	 * if priorityLightPool is not null, get the highest PRIORITY_LEVEL mailItem
	 * from the priorityHeavyPool
	 * 
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
			// remove the highest mailItem from the list before return
			MailItem highestPriorityMail = priorityHeavyPool.get(maxLevelIndex);
			priorityHeavyPool.remove(maxLevelIndex);
			return highestPriorityMail;
		} else {
			return null;
		}
	}

	/**
	 * if priorityLightPool is not null, get the highest PRIORITY_LEVEL mailItem
	 * from the priorityLightPool
	 * 
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
			// remove the highest mailItem from the list before return
			MailItem highestPriorityMail = priorityLightPool.get(maxLevelIndex);
			priorityLightPool.remove(maxLevelIndex);
			return highestPriorityMail;
		} else {
			return null;
		}
	}

	@Override
	public void step() {
		for (int i = 0; i < robots.size(); i++) {
			fillStorageTube(robots.get(i));
		}
	}

	/**
	 * fill robot's tube
	 * 
	 * @param robot the robot to be loaded mailItem and dispatched
	 */
	private void fillStorageTube(Robot robot) {
		StorageTube tube = robot.getTube();
		boolean isStrong = robot.isStrong();
		try {
			// if the robot is strong, put mailItems into tube from mailPool by sequence of
			// priorityHeavyPool,nonPriorityHeavyPool,priorityHeavyLightPool,nonPriorityLightPool
			// until the tube is full or no mailItem in mailPool
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
			// if the robot is weak, put mailItems into tube from mailPool by sequence of
			// priorityHeavyLightPool,nonPriorityLightPool
			// until the tube is full or no mailItem in mailPool
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
			e.printStackTrace();
		}

	}

	/**
	 * sort mailItems in tube
	 * 
	 * @param backpack unsorted tube
	 * @return sorted tube by DestFloor
	 */
	public StorageTube sortTube(StorageTube backpack) {
		if (!backpack.tube.isEmpty()) {
			Stack<MailItem> orderTude = new Stack<MailItem>();
			while (!backpack.tube.isEmpty()) {
				MailItem temp = backpack.tube.pop();
				while (!orderTude.isEmpty() && orderTude.peek().getDestFloor() > temp.getDestFloor()) {
					backpack.tube.push(orderTude.pop());
				}
				orderTude.push(temp);
			}
			while (!orderTude.isEmpty()) {
				backpack.tube.push(orderTude.pop());
			}
			return backpack;
		} else {
			return backpack;
		}
	}

	@Override
	public void registerWaiting(Robot robot) {
		robots.add(robot);
	}

	@Override
	public void deregisterWaiting(Robot robot) {
		for (int i = 0; i < robots.size(); i++) {
			if (robots.get(i) == robot) {
				robots.remove(i);
			}
		}
	}

	/**
	 * sort list by DestFloor
	 */
	class SortByDestFloor implements Comparator {
		public int compare(Object o1, Object o2) {
			MailItem m1 = (MailItem) o1;
			MailItem m2 = (MailItem) o2;
			if (m1.getDestFloor() > m2.getDestFloor())
				return 1;
			return -1;
		}
	}

}
