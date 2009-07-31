/* *********************************************************************** *
 * project: org.matsim.*
 * ParallelReplanner.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.christoph.events.algorithms;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.matsim.population.algorithms.PlanAlgorithm;

import playground.christoph.router.DijkstraWrapper;
import playground.christoph.router.KnowledgePlansCalcRoute;
import playground.christoph.router.costcalculators.KnowledgeTravelCostWrapper;
import playground.christoph.router.costcalculators.KnowledgeTravelTimeWrapper;

/*
 * Abstract class that contains the basic elements that are needed 
 * to do parallel replanning within the QueueSimulation.
 * 
 * Features like the creation of parallel running threads and the
 * split up of the replanning actions have to be implemented in 
 * the subclasses.
 */

public abstract class ParallelReplanner {
	
	private final static Logger log = Logger.getLogger(ParallelReplanner.class);
	
	protected static ArrayList<PlanAlgorithm> replanners;
	protected static int numOfThreads = 1;	// use by default only one thread
	protected static PlanAlgorithm[][] replannerArray;
	
	// Set the Replanners ArrayList here - this can be done once from the Controler
	public static void init(ArrayList<PlanAlgorithm> replannerArrayList)
	{
		replanners = replannerArrayList;
		
		// create new Array of Replanners
		createReplannerArray();
	}
		
	/* 
	 * Creates an Array that contains the Replanners that were initialized in the Controler
	 * and clone of them for every additional replanning thread.
	 * 1 replanning thread -> uses existing replanners
	 * 2 replanning thread -> uses existing replanners and one clone of each
	 * 
	 * If the Replanners from the replanners ArrayList have changed, an update of the
	 * ArrayList can be initiated by using this method.
	 */
	public static void createReplannerArray()
	{	
		// create and fill Array of PlanAlgorithms used in the threads
		replannerArray = new PlanAlgorithm[replanners.size()][numOfThreads];
		
		for(int i = 0; i < replanners.size(); i++)
		{
			PlanAlgorithm replanner = replanners.get(i);
		
			// fill first row with already defined selectors
			replannerArray[i][0] = replanner;
			
			// fill the other fields in the current row with clones
			for(int j = 1; j < numOfThreads; j++)
			{
				// insert clone
				if (replanner instanceof KnowledgePlansCalcRoute)
				{
					replannerArray[i][j] = ((KnowledgePlansCalcRoute)replanner).clone();
				}
				else
				{
					log.error("replanner class " + replanner.getClass());
					log.error("Could not clone the Replanner - use reference to the existing Replanner and hope the best...");
					replannerArray[i][j] = replanner;
				}
				
			}
			
		}
	}
	
	
	public static void setNumberOfThreads(int numberOfThreads)
	{
		int currentNumOfThreads = numOfThreads;
		
		numOfThreads = Math.max(numberOfThreads, 1); // it should be at least 1 here; we allow 0 in other places for "no threads"
		
		log.info("Using " + numOfThreads + " parallel threads to replan routes.");
		
		// if the number of used threads has changed -> PlanAlgorithms Array has to be recreated
		if (numOfThreads != currentNumOfThreads)
		{
			createReplannerArray();
		}
		
		/*
		 *  Throw error message if the number of threads is bigger than the number of available CPUs.
		 *  This should not speed up calculation anymore.
		 */
		if (numOfThreads > Runtime.getRuntime().availableProcessors())
		{
			log.error("The number of parallel running replanning threads is bigger than the number of available CPUs!");
		}
		
	}
	
	/*
	 * Using LookupTables for the LinktravelTimes should speed up the WithinDayReplanning.
	 * Now using update instead!
	 */
	@Deprecated
	public static void resetLinkTravelTimesLookupTables()
	{	
		for(int i = 0; i < replannerArray.length; i++)
		{			
			// fill the other fields in the current row with clones
			for(int j = 0; j < replannerArray[i].length; j++)
			{
				// insert clone
				if (replannerArray[i][j] instanceof KnowledgePlansCalcRoute)
				{
					KnowledgePlansCalcRoute replanner = (KnowledgePlansCalcRoute)replannerArray[i][j];
					
					//if (replanner.getLeastCostPathCalculator() instanceof KnowledgeTravelCostWrapper)
					if (replanner.getLeastCostPathCalculator() instanceof DijkstraWrapper)
					{
						DijkstraWrapper dijstraWrapper = (DijkstraWrapper)replanner.getLeastCostPathCalculator();
						
						if (dijstraWrapper.getTravelCostCalculator() instanceof KnowledgeTravelCostWrapper)
						{
							((KnowledgeTravelCostWrapper)dijstraWrapper.getTravelCostCalculator()).resetLookupTable();
						}
					}
					
					//if (replanner.getPtFreeflowLeastCostPathCalculator() instanceof KnowledgeTravelCostWrapper)
					if (replanner.getPtFreeflowLeastCostPathCalculator() instanceof DijkstraWrapper)
					{
						DijkstraWrapper dijstraWrapper = (DijkstraWrapper)replanner.getPtFreeflowLeastCostPathCalculator();
						
						if (dijstraWrapper.getTravelCostCalculator() instanceof KnowledgeTravelCostWrapper)
						{
							((KnowledgeTravelCostWrapper)dijstraWrapper.getTravelCostCalculator()).resetLookupTable();
						}
					} 
				}				
			}
			
		}
	}
	
	/*
	 * Using LookupTables for the LinktravelTimes should speed up the WithinDayReplanning.
	 */
	public static void updateLinkTravelTimesLookupTables(double time)
	{	
		/*
		 * We update the LookupTables only in the "Parent Replanners".
		 * Their Children are clones that use the same Maps to store
		 * the LinkTravelTimes.
		 */
		for(int i = 0; i < replannerArray.length; i++)
		{	
			// insert clone
			if (replannerArray[i][0] instanceof KnowledgePlansCalcRoute)
			{
				KnowledgePlansCalcRoute replanner = (KnowledgePlansCalcRoute)replannerArray[i][0];
				
				if (replanner.getLeastCostPathCalculator() instanceof DijkstraWrapper)
				{
					DijkstraWrapper dijstraWrapper = (DijkstraWrapper)replanner.getLeastCostPathCalculator();
					
					if (dijstraWrapper.getTravelTimeCalculator() instanceof KnowledgeTravelTimeWrapper)
					{
						((KnowledgeTravelTimeWrapper)dijstraWrapper.getTravelTimeCalculator()).updateLookupTable(time);
					}
				}
				
				if (replanner.getPtFreeflowLeastCostPathCalculator() instanceof DijkstraWrapper)
				{
					DijkstraWrapper dijstraWrapper = (DijkstraWrapper)replanner.getPtFreeflowLeastCostPathCalculator();
					
					if (dijstraWrapper.getTravelTimeCalculator() instanceof KnowledgeTravelTimeWrapper)
					{
						((KnowledgeTravelTimeWrapper)dijstraWrapper.getTravelTimeCalculator()).updateLookupTable(time);
					}
				} 
			}
		}
	}
	
	/*
	 * Using LookupTables for the LinktravelCosts should speed up the WithinDayReplanning.
	 */
	public static void updateLinkTravelCostsLookupTables(double time)
	{	
		/*
		 * We update the LookupTables only in the "Parent Replanners".
		 * Their Children are clones that use the same Maps to store
		 * the LinkTravelTimes.
		 */
		for(int i = 0; i < replannerArray.length; i++)
		{	
			// insert clone
			if (replannerArray[i][0] instanceof KnowledgePlansCalcRoute)
			{
				KnowledgePlansCalcRoute replanner = (KnowledgePlansCalcRoute)replannerArray[i][0];
				
				//if (replanner.getLeastCostPathCalculator() instanceof KnowledgeTravelCostWrapper)
				if (replanner.getLeastCostPathCalculator() instanceof DijkstraWrapper)
				{
					DijkstraWrapper dijstraWrapper = (DijkstraWrapper)replanner.getLeastCostPathCalculator();
					
					if (dijstraWrapper.getTravelCostCalculator() instanceof KnowledgeTravelCostWrapper)
					{
						((KnowledgeTravelCostWrapper)dijstraWrapper.getTravelCostCalculator()).updateLookupTable(time);
					}
				}
				
				//if (replanner.getPtFreeflowLeastCostPathCalculator() instanceof KnowledgeTravelCostWrapper)
				if (replanner.getPtFreeflowLeastCostPathCalculator() instanceof DijkstraWrapper)
				{
					DijkstraWrapper dijstraWrapper = (DijkstraWrapper)replanner.getPtFreeflowLeastCostPathCalculator();
					
					if (dijstraWrapper.getTravelCostCalculator() instanceof KnowledgeTravelCostWrapper)
					{
						((KnowledgeTravelCostWrapper)dijstraWrapper.getTravelCostCalculator()).updateLookupTable(time);
					}
				} 
			}
		}
	}
}
