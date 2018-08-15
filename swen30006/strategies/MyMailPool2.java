package strategies;

import java.util.Stack;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyMailPool2 implements IMailPool{
	//
	private Stack<MailItem> nonPriorityPool;
	private Stack<MailItem> priorityPool;
	private Stack<PriorityMailItem> temporaryPriorityPool;
	private Stack<MailItem> temporaryPool;
	private static final int MAX_TAKE = 4;
	private Robot robot1, robot2, robot3;  // There's only three of these.
	public MyMailPool2(){
		// Start empty
		nonPriorityPool = new Stack<MailItem>();
		priorityPool = new Stack<MailItem>();
		temporaryPool= new Stack<MailItem>();
		temporaryPriorityPool=new Stack<PriorityMailItem>();
		
		
	}

	public void addToPool(MailItem mailItem) {
		/*Puting the priority MailItem into priorityPool, and sort out the MailItems in the priorityPool
		 *  according to their values of priority.
		 */
		// Check whether it has a priority or not
		if(mailItem instanceof PriorityMailItem){
			//First step: put MailItem into the priorityPool
			priorityPool.push(mailItem);
			//Second step: sort out the priorityPool
			this.sortStackByStack(priorityPool);
			
		}
		else{
			
			nonPriorityPool.add(mailItem);
		}
	}
	
	
	public void sortStackByStack(Stack<MailItem> stack) {
		//Sort out the stack
		
  		  while(!stack.isEmpty()) {
  			  PriorityMailItem topItem = (PriorityMailItem)stack.pop();
  			  int topPriorityValue=topItem.getPriorityLevel();
  			  while(!temporaryPriorityPool.isEmpty()&&temporaryPriorityPool.peek().getPriorityLevel()<topPriorityValue) {
  				  stack.push(temporaryPriorityPool.pop());
  			  }
  			  temporaryPriorityPool.push(topItem);
  		  }
  		  while(!temporaryPriorityPool.isEmpty()) {
  			  stack.push(temporaryPriorityPool.pop());
  		  }  
    }

	private int getOverweightPriorityMaillItemNum() {
		//Recording the number of priorityMailitems which are overweight(>2000)
		int count=0;
		
		while(getPriorityPoolSize()>0) {
			if(priorityPool.peek().getWeight()>2000) {
				count++;
				}
			temporaryPriorityPool.push((PriorityMailItem)priorityPool.peek());
		    priorityPool.pop();
		}
		while(!temporaryPriorityPool.isEmpty()) {
			priorityPool.push(temporaryPriorityPool.peek());
			temporaryPriorityPool.pop();
		}
		
		return count;
		
	}
	private int getOverweightNonPriorityMailItemNum() {
		//Recording the number of nonPriorityMailitems which are overweight(>2000)
		int count=0;
		
		while(getNonPriorityPoolSize()>0) {
			if(nonPriorityPool.peek().getWeight()>2000) {
				count++;
				}
			temporaryPool.push(nonPriorityPool.pop());
		   
		}
		while(!temporaryPool.isEmpty()) {
			nonPriorityPool.push(temporaryPool.pop());
	
		}
		
		return count;
		
	}
	private int getNonPriorityPoolSize() {
		// This was easy until we got two kinds of robots and weight became an issue
		// Oh well, there's not that many heavy mail items -- this should be close enough
		return nonPriorityPool.size();
	}
	
	private int getPriorityPoolSize(){
		// Same as above, but even less heavy priority items -- hope this works too
		return priorityPool.size();
	}

	private MailItem getNonPriorityMail(){
		if(getNonPriorityPoolSize() > 0){
			// Should I be getting the earliest one? 
			// Surely the risk of the weak robot getting a heavy item is small!
			return nonPriorityPool.pop();
		}
		else{
			return null;
		}
	}
	
	private MailItem getHighestPriorityMail(){
		if(getPriorityPoolSize() > 0){
			// How am I supposed to know if this is the highest/earliest?
			return priorityPool.pop();
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
		boolean StrongRobot=robot.isStrong();
		try{
			
			if(StrongRobot) {//if the robot is a strong one. why don't the company
				//replace all the weak robots with strong ones?!!!!
				while((getPriorityPoolSize()>0||getNonPriorityPoolSize()>0)&&tube.getSize() < MAX_TAKE) {
				if(getPriorityPoolSize()>0) {
					tube.addItem(getHighestPriorityMail());
					
				}
				else {
					tube.addItem(getNonPriorityMail());
					
				}
				robot.dispatch();
				
				}
			}
			
			else {//If the robot is a weak one. A complicated situation
				//the first situation: weak robot can get the MailItems in the PriorityPool
				while(getPriorityPoolSize()>0&&(getPriorityPoolSize()!=getOverweightPriorityMaillItemNum())&&tube.getSize() < MAX_TAKE){
					PriorityMailItem topmail=(PriorityMailItem)priorityPool.peek();
					if(topmail.getWeight()<2000) {
						tube.addItem(getHighestPriorityMail());
					}
					else {
						while(priorityPool.peek().getWeight()>2000)//Finding the MailItem which is less than 2000 in PriorityPool
						{//temporaryPool will be used to save MailItem which is overweight(>2000)
						
							temporaryPriorityPool.push((PriorityMailItem)priorityPool.pop());
													}
						tube.addItem(getHighestPriorityMail());
						while(!temporaryPriorityPool.isEmpty()) {
							priorityPool.push(temporaryPriorityPool.pop());
							
						}
						
					}
					
				}//end the first situation
				
				//the second situation: the weak robot can't get the MailItem in the PriorityPool
				while(getNonPriorityPoolSize()>0&&(getNonPriorityPoolSize()!=getOverweightNonPriorityMailItemNum())&&tube.getSize() < MAX_TAKE) {
					MailItem topMail=nonPriorityPool.peek();
					if(topMail.getWeight()<2000) {
						tube.addItem(getNonPriorityMail());
					}
					else {
						while(nonPriorityPool.peek().getWeight()>2000)//Finding the MailItem which is less than 2000 in PriorityPool
						{//temporaryPool will be used to save MailItem which is overweight(>2000)
						
							temporaryPool.push(nonPriorityPool.pop());
							
						}
						tube.addItem(getNonPriorityMail());
						while(!temporaryPool.isEmpty()) {
							nonPriorityPool.push(temporaryPool.pop());
							
						}
					}
				}//end the second situation
				robot.dispatch();
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
	
}


