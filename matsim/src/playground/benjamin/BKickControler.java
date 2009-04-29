/* *********************************************************************** *
 * project: org.matsim.*																															*
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
package playground.benjamin;

import org.matsim.core.api.population.Population;
import org.matsim.core.controler.Controler;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.population.algorithms.PlanCalcType;
import org.matsim.run.OTFVis;


/**
 * @author dgrether
 *
 */
public class BKickControler extends Controler {

	public BKickControler(String configFileName) {
		super(configFileName);
	}

	@Override
	protected ScoringFunctionFactory loadScoringFunctionFactory() {
		return new BKickScoringFunctionFactory(this.config.charyparNagelScoring());
	}

	@Override
	protected Population loadPopulation() {
		Population pop = super.loadPopulation();
		new PlanCalcType().run(pop);
		return pop;
	}
	
	
	
	 public static void main(String[] args){
			String equilExampleConfig = "examples/equil/configOTF.xml";
			String oneRouteTwoModeTest = "../bkick/oneRouteTwoModeTest/config.xml";
			String oneRouteNoModeTest = "../bkick/oneRouteNoModeTest/config.xml";
			String oneRouteNoModeTestOld = "../bkick/oneRouteNoModeTest/configOldUtlFctn.xml";
			String scoreTest = "../bkick/scoreTest/configScoreTest.xml";
//			String config = equilExampleConfig;
//			String config = oneRouteTwoModeTest;
			String config = oneRouteNoModeTest;
//			String config = oneRouteNoModeTestOld;
//			String config = scoreTest;
			
			BKickControler c = new BKickControler(config);
			c.setOverwriteFiles(true);
			c.run();
			
			int lastIteration = c.getConfig().controler().getLastIteration();
			
//			String out = c.getConfig().controler().getOutputDirectory() + "/ITERS/it."+lastIteration+"/Snapshot";
			String out = c.getConfig().controler().getOutputDirectory() + "/ITERS/it."+lastIteration+"/"+lastIteration+".otfvis.mvi";
			
			String[] visargs = {out};
			
			OTFVis.main(new String[] {out});	
	 }

	
	
}
