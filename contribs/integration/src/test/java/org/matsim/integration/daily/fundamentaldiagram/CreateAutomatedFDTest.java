/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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
package org.matsim.integration.daily.fundamentaldiagram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup.LinkDynamics;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.config.groups.VspExperimentalConfigGroup.VspDefaultsCheckingLevel;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimAgent.State;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.qsim.ActivityEngine;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.TeleportationEngine;
import org.matsim.core.mobsim.qsim.agents.AgentFactory;
import org.matsim.core.mobsim.qsim.agents.PersonDriverAgentImpl;
import org.matsim.core.mobsim.qsim.agents.PopulationAgentSource;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngine;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.testcases.MatsimTestUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

/**
 * Generates fundamental diagrams for the all combination of link and traffic dynamics for car/bike and car/truck groups.
 * Also generates car/bike FDs using fast capacity update method.
 * @author amit
 */

@RunWith(Parameterized.class)
public class CreateAutomatedFDTest {

	static class MySimplifiedRoundAndRoundAgent implements MobsimAgent, MobsimDriverAgent {
		private static int counter = 0 ;
		private MobsimVehicle vehicle;

		@Override
		public Id<Link> getCurrentLinkId() {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public Id<Link> getDestinationLinkId() {
			return null ;
		}

		@Override
		public Id<Person> getId() {
			counter++ ;
			return Id.createPersonId("simpleRoundAndRoundAgent" + counter ) ;
		}

		@Override
		public Id<Link> chooseNextLinkId() {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public void notifyMoveOverNode(Id<Link> newLinkId) {
		}

		@Override
		public boolean isWantingToArriveOnCurrentLink() {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public void setVehicle(MobsimVehicle veh) {
			this.vehicle = veh ;
		}

		@Override
		public MobsimVehicle getVehicle() {
			return this.vehicle ;
		}

		@Override
		public Id<Vehicle> getPlannedVehicleId() {
			return null ;
		}

		@Override
		public State getState() {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public double getActivityEndTime() {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public void endActivityAndComputeNextState(double now) {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public void endLegAndComputeNextState(double now) {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public void setStateToAbort(double now) {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public Double getExpectedTravelTime() {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public Double getExpectedTravelDistance() {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public String getMode() {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

		@Override
		public void notifyArrivalOnLinkByNonNetworkMode(Id<Link> linkId) {
			// TODO Auto-generated method stub
			throw new RuntimeException("not implemented");
		}

	}

	public CreateAutomatedFDTest(LinkDynamics linkDynamics, TrafficDynamics trafficDynamics) {
		this.linkDynamics = linkDynamics;
		this.trafficDynamics = trafficDynamics;
		this.travelModes = new String [] {"car","bike"};
	}

	private LinkDynamics linkDynamics;
	private TrafficDynamics trafficDynamics;

	@Parameters
	public static Collection<Object[]> createFds() {
		Object[] [] fdData = new Object [][] { 
				{LinkDynamics.FIFO, TrafficDynamics.queue},
				{LinkDynamics.FIFO, TrafficDynamics.withHoles}, 
				{LinkDynamics.PassingQ,TrafficDynamics.queue},
				{LinkDynamics.PassingQ,TrafficDynamics.withHoles}
		};
		return Arrays.asList(fdData);
	}

	@Test
	public void FDs_carTruck(){
		this.travelModes = new String [] {"car","truck"};
		run(this.linkDynamics, this.trafficDynamics,false);
	}

	@Test
	public void FDs_carBike(){
		run(this.linkDynamics, this.trafficDynamics,false);
	}

	@Test 
	public void Fds_carBike_fastCapacityUpdate(){
		run(this.linkDynamics,this.trafficDynamics,true);
	}

	@Rule public MatsimTestUtils helper = new MatsimTestUtils();

	private String [] travelModes;
	public final Id<Link> flowDynamicsMeasurementLinkId = Id.createLinkId(0);
	private Scenario scenario;
	private Map<String, VehicleType> modeVehicleTypes;
	private Map<Id<VehicleType>, TravelModesFlowDynamicsUpdator> mode2FlowData;
	static GlobalFlowDynamicsUpdator globalFlowDynamicsUpdator;

	private final Logger log = Logger.getLogger(CreateAutomatedFDTest.class);

	private void run(final LinkDynamics linkDynamics, final TrafficDynamics trafficDynamics, final boolean isUsingFastCapacityUpdate) {

		scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
		createNetwork();

		storeVehicleTypeInfo();

		scenario.getConfig().qsim().setMainModes(Arrays.asList(travelModes));
		scenario.getConfig().qsim().setEndTime(14*3600);
		scenario.getConfig().qsim().setLinkDynamics(linkDynamics.name());
		scenario.getConfig().vspExperimental().setVspDefaultsCheckingLevel( VspDefaultsCheckingLevel.abort );
		scenario.getConfig().qsim().setTrafficDynamics(trafficDynamics);

		scenario.getConfig().qsim().setUsingFastCapacityUpdate(isUsingFastCapacityUpdate);

		//equal modal split run
		Map<String, Integer> minSteps = new HashMap<String, Integer>();

		double pcu1 = modeVehicleTypes.get(travelModes[0]).getPcuEquivalents();
		double pcu2 = modeVehicleTypes.get(travelModes[1]).getPcuEquivalents();

		if(pcu1==1 && pcu2 == 0.25) { //car bike
			minSteps.put(travelModes[0], 1);
			minSteps.put(travelModes[1], 4);
		} else { //car truck
			minSteps.put(travelModes[0], 3);
			minSteps.put(travelModes[1], 1);
		}

		int reduceNoOfDataPointsInPlot = 4; // 1--> will generate all possible data points;

		double networkDensity = 3.*(1000./7.5);
		double sumOfPCUInEachStep = (modeVehicleTypes.get(travelModes[0]).getPcuEquivalents() * minSteps.get(travelModes[0]) ) + 
				(modeVehicleTypes.get(travelModes[1]).getPcuEquivalents() * minSteps.get(travelModes[1]) );
		int numberOfPoints = (int) Math.ceil(networkDensity/(reduceNoOfDataPointsInPlot * sumOfPCUInEachStep))+5;

		List<Map<String,Integer>> points2Run = new ArrayList<Map<String,Integer>>();

		for (int m=1; m<numberOfPoints; m++){
			Map<String,Integer> pointToRun = new HashMap<>();
			for (String mode:travelModes){
				pointToRun.put(mode,minSteps.get(mode)*m*reduceNoOfDataPointsInPlot);
			}
			System.out.println("Number of Agents - \t"+pointToRun.toString());
			points2Run.add(pointToRun);
		}

		Map<Double, Map<String, Tuple<Double, Double>>> outData = new HashMap<Double, Map<String,Tuple<Double,Double>>>();

		for(Map<String, Integer> point2run : points2Run){

			System.out.println("\n \n \t \t Running points "+point2run.toString()+"\n \n");

			createPopulationAndVehicles(point2run);

			for(String mode : point2run.keySet()){
				this.mode2FlowData.get(modeVehicleTypes.get(mode).getId()).setnumberOfAgents(point2run.get(mode));
			}

			EventsManager events = EventsUtils.createEventsManager();
			globalFlowDynamicsUpdator = new GlobalFlowDynamicsUpdator(scenario, mode2FlowData);
			events.addHandler(globalFlowDynamicsUpdator);

			final QSim qSim = new QSim(scenario, events);
			ActivityEngine activityEngine = new ActivityEngine(events, qSim.getAgentCounter());
			qSim.addMobsimEngine(activityEngine);
			qSim.addActivityHandler(activityEngine);
			QNetsimEngine netsimEngine = new QNetsimEngine(qSim);
			qSim.addMobsimEngine(netsimEngine);
			qSim.addDepartureHandler(netsimEngine.getDepartureHandler());
			TeleportationEngine teleportationEngine = new TeleportationEngine(scenario, events);
			qSim.addMobsimEngine(teleportationEngine);

			AgentFactory agentFactory = new MyAgentFactory(qSim);

			PopulationAgentSource agentSource = new PopulationAgentSource(scenario.getPopulation(), agentFactory, qSim);

//			AgentSource agentSource = new AgentSource(){
//				@Override
//				public void insertAgentsIntoMobsim() {
//					for ( int ii=0 ; ii<2000 ; ii++ ) {
//						MobsimAgent agent = new MySimplifiedRoundAndRoundAgent() ;
//						qSim.insertAgentIntoMobsim(agent);
//					}
//
//				}
//			} ;

			Map<String, VehicleType> travelModesTypes = new HashMap<String, VehicleType>();
			for(String mode :travelModes){
				travelModesTypes.put(mode, modeVehicleTypes.get(mode));
			}
			agentSource.setModeVehicleTypes(travelModesTypes);

			qSim.addAgentSource(agentSource);

			qSim.run();

			Map<String, Tuple<Double, Double>> mode2FlowSpeed = new HashMap<String, Tuple<Double,Double>>();
			for(int i=0; i < travelModes.length; i++){

				Tuple<Double, Double> flowSpeed = 
						new Tuple<Double, Double>(this.mode2FlowData.get(Id.create(travelModes[i],VehicleType.class)).getPermanentFlow(),
								this.mode2FlowData.get(Id.create(travelModes[i],VehicleType.class)).getPermanentAverageVelocity());
				mode2FlowSpeed.put(travelModes[i], flowSpeed);
				outData.put(globalFlowDynamicsUpdator.getGlobalData().getPermanentDensity(), mode2FlowSpeed);
			}
		}

		/*
		 *	Basically overriding the helper.getOutputDirectory() method, such that,
		 *	if file directory does not exists or same file already exists, remove and re-creates the whole dir hierarchy so that
		 *	all existing files are re-written 
		 *	else, just keep adding files in the directory.	
		 *	This is necessary in order to allow writing different tests results from JUnit parameterization.
		 */

		String outDir  = "test/output/" + CreateAutomatedFDTest.class.getCanonicalName().replace('.', '/') + "/" + helper.getMethodName() + "/";
		String fileName = linkDynamics+"_"+trafficDynamics+".png";
		String outFile ;

		if(!new File(outDir).exists() || new File(outDir+fileName).exists()){
			outFile = helper.getOutputDirectory()+fileName;
		} else {
			outFile = outDir+fileName;
		}

		//plotting data
		scatterPlot(outData,outFile);
	}

	private void createPopulationAndVehicles(Map<String, Integer> point2run){
		Population pop = scenario.getPopulation();
		pop.getPersons().clear();

		for(String mode : point2run.keySet()){
			for(int j=0;j<point2run.get(mode);j++){
				int popSizeSoFar = pop.getPersons().size();
				Person p = pop.getFactory().createPerson(Id.createPersonId(popSizeSoFar));
				Plan plan = pop.getFactory().createPlan();

				Activity actHome = pop.getFactory().createActivityFromLinkId("home", Id.createLinkId("home"));
				actHome.setEndTime((new Random().nextDouble())*900);
				plan.addActivity(actHome);

				Leg leg = pop.getFactory().createLeg(mode);
				NetworkRoute route = new LinkNetworkRouteImpl(Id.createLinkId("home"), Id.createLinkId("work"));
				List<Id<Link>> routeLinks = new ArrayList<Id<Link>>();
				routeLinks.add(Id.createLinkId(0));
				routeLinks.add(Id.createLinkId(1));
				routeLinks.add(Id.createLinkId(2));
				route.setLinkIds(Id.createLinkId("home"), routeLinks, Id.createLinkId("work"));
				leg.setRoute(route);
				plan.addLeg(leg);

				Activity workHome = pop.getFactory().createActivityFromLinkId("work", Id.createLinkId("work"));
				plan.addActivity(workHome);

				p.addPlan(plan);
				pop.addPerson(p);
			}
		}
	}

	private void storeVehicleTypeInfo() {
		modeVehicleTypes = new HashMap<String, VehicleType>();
		mode2FlowData = new HashMap<>();

		VehicleType car = VehicleUtils.getFactory().createVehicleType(Id.create("car", VehicleType.class));
		car.setMaximumVelocity(16.667);
		car.setPcuEquivalents(1.0);
		modeVehicleTypes.put("car", car);

		VehicleType bike = VehicleUtils.getFactory().createVehicleType(Id.create("bike", VehicleType.class));
		bike.setMaximumVelocity(4.167);
		bike.setPcuEquivalents(0.25);
		modeVehicleTypes.put("bike", bike);

		VehicleType truck = VehicleUtils.getFactory().createVehicleType(Id.create("truck",VehicleType.class));
		truck.setMaximumVelocity(8.33);
		truck.setPcuEquivalents(3.);
		modeVehicleTypes.put("truck", truck);

		for (String mode :travelModes){
			TravelModesFlowDynamicsUpdator modeUpdator = new TravelModesFlowDynamicsUpdator(modeVehicleTypes.get(mode));
			mode2FlowData.put(modeVehicleTypes.get(mode).getId(), modeUpdator);
		}
	}

	private void createNetwork(){
		NetworkImpl network = (NetworkImpl) scenario.getNetwork();

		Node nodeHome = network.createAndAddNode(Id.createNodeId("home"), scenario.createCoord(-50, 0));
		Node node1 = network.createAndAddNode(Id.createNodeId(0), scenario.createCoord(0, 0));
		Node node2 = network.createAndAddNode(Id.createNodeId(1), scenario.createCoord(1000, 0));
		Node node3 = network.createAndAddNode(Id.createNodeId(2), scenario.createCoord(500, 866.0));
		Node nodeWork = network.createAndAddNode(Id.createNodeId("work"), scenario.createCoord(1050,0));

		network.createAndAddLink(Id.createLinkId("home"), nodeHome, node1, 25, 60, 27000, 1);
		network.createAndAddLink(Id.createLinkId(0), node1, node2, 1000, 60, 2700, 1);
		network.createAndAddLink(Id.createLinkId(1), node2, node3, 1000, 60, 2700, 1);
		network.createAndAddLink(Id.createLinkId(2), node3, node1, 1000, 60, 2700, 1);
		network.createAndAddLink(Id.createLinkId("work"), node2, nodeWork, 25, 60, 27000, 1);

		Set<String> allowedModes = new HashSet<String>();
		allowedModes.addAll(Arrays.asList(travelModes));

		for(Link l:network.getLinks().values()){
			l.setAllowedModes(allowedModes);
		}
	}

	private void scatterPlot (Map<Double, Map<String, Tuple<Double, Double>>> inputData, String outFile){

		String mode1 = travelModes[0];
		String mode2 = travelModes[1];

		XYSeries carFlow = new XYSeries(mode1+" flow");
		XYSeries bikeFlow = new XYSeries(mode2+" flow");
		XYSeries carSpeed = new XYSeries(mode1+" speed");
		XYSeries bikeSpeed = new XYSeries(mode2+" speed");

		for(double d :inputData.keySet()){
			carFlow.add(d, inputData.get(d).get(mode1).getFirst());
			carSpeed.add(d, inputData.get(d).get(mode1).getSecond());

			bikeFlow.add(d, inputData.get(d).get(mode2).getFirst());
			bikeSpeed.add(d, inputData.get(d).get(mode2).getSecond());
		}

		// flow vs density
		XYSeriesCollection flowDataset = new XYSeriesCollection();
		flowDataset.addSeries(carFlow);
		flowDataset.addSeries(bikeFlow);

		NumberAxis flowAxis = new NumberAxis("Flow (PCU/h)");
		flowAxis.setRange(0.0, 2100.0);

		XYPlot plot1 = new XYPlot(flowDataset, null, flowAxis, new XYLineAndShapeRenderer(false,true));
		plot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

		// speed vs density
		XYSeriesCollection speedDataset = new XYSeriesCollection();
		speedDataset.addSeries(carSpeed);
		speedDataset.addSeries(bikeSpeed);

		NumberAxis speedAxis = new NumberAxis("Speed (m/s)");
		speedAxis.setRange(0.0, 17.0);

		XYPlot plot2 = new XYPlot(speedDataset, null, speedAxis, new XYLineAndShapeRenderer(false,true));
		plot2.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

		NumberAxis densityAxis = new NumberAxis("Overall density (PCU/km)");
		densityAxis.setRange(0.0,150.00);

		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(densityAxis);
		plot.setGap(10.);
		plot.add(plot1);
		plot.add(plot2);
		plot.setOrientation(PlotOrientation.VERTICAL);

		JFreeChart chart = new JFreeChart("Fundamental diagrams", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

		try {
			ChartUtilities.saveChartAsPNG(new File(outFile), chart, 800, 600);
		} catch (IOException e) {
			throw new RuntimeException("Data is not plotted. Reason "+e);
		}
	}

	//==============================================

	class TravelModesFlowDynamicsUpdator {

		private final int NUMBER_OF_MEMORIZED_FLOWS = 10;
		private Id<VehicleType> modeId;
		private VehicleType vehicleType=null;//      Maybe keeping global data in the EventHandler can be smart (ssix, 25.09.13)
		//	     So far programmed to contain also global data, i.e. data without a specific vehicleType (ssix, 30.09.13)
		public int numberOfAgents;
		private double permanentDensity;
		private double permanentAverageVelocity;
		private double permanentFlow;

		private Map<Id<Person>,Double> lastSeenOnStudiedLinkEnter;//records last entry time for every person, but also useful for getting actual number of people in the simulation
		private int speedTableSize;
		private List<Double> speedTable;
		private Double flowTime;
		private List<Double> flowTable900;
		private List<Double> lastXFlows900;;//recording a number of flows to ensure stability
		private boolean speedStability;
		private boolean flowStability;

		public TravelModesFlowDynamicsUpdator(){}

		public TravelModesFlowDynamicsUpdator(VehicleType vT){
			this.vehicleType = vT;
			this.modeId = this.vehicleType.getId();
		}

		public void handle(LinkEnterEvent event){
			if (event.getLinkId().equals(flowDynamicsMeasurementLinkId)){
				Id<Person> personId = Id.createPersonId(event.getVehicleId());
				double nowTime = event.getTime();

				this.updateFlow900(nowTime, this.vehicleType.getPcuEquivalents());
				this.updateSpeedTable(nowTime, personId);

				//Checking for stability
				//Making sure all agents are on the track before testing stability
				//Also waiting half an hour to let the database build itself.

				if ((this.getNumberOfDrivingAgents() == this.numberOfAgents) && (nowTime > 1800)){//TODO empirical factor
					if (!(this.speedStability)){
						this.checkSpeedStability();
					}
					if (!(this.flowStability)){
						this.checkFlowStability900();
					}
				}
			}
		}

		private void updateFlow900(double nowTime, double pcu_person){
			if (nowTime == this.flowTime.doubleValue()){//Still measuring the flow of the same second
				Double nowFlow = this.flowTable900.get(0);
				this.flowTable900.set(0, nowFlow.doubleValue()+pcu_person);
			} else {//Need to offset the new flow table from existing flow table.
				int timeDifference = (int) (nowTime-this.flowTime.doubleValue());
				if (timeDifference<900){
					for (int i=899-timeDifference; i>=0; i--){
						this.flowTable900.set(i+timeDifference, this.flowTable900.get(i).doubleValue());
					}
					if (timeDifference > 1){
						for (int i = 1; i<timeDifference; i++){
							this.flowTable900.set(i, 0.);
						}
					}
					this.flowTable900.set(0, pcu_person);
				} else {
					flowTableReset();
				}
				this.flowTime = new Double(nowTime);
			}
			updateLastXFlows900();
		}

		private void updateLastXFlows900(){
			Double nowFlow = new Double(this.getCurrentHourlyFlow());
			for (int i=NUMBER_OF_MEMORIZED_FLOWS-2; i>=0; i--){
				this.lastXFlows900.set(i+1, this.lastXFlows900.get(i).doubleValue());
			}
			this.lastXFlows900.set(0, nowFlow);
		}

		private void updateSpeedTable(double nowTime, Id<Person> personId){
			if (this.lastSeenOnStudiedLinkEnter.containsKey(personId)){
				double lastSeenTime = lastSeenOnStudiedLinkEnter.get(personId);
				double speed = 1000 * 3 / (nowTime-lastSeenTime);//in m/s!!
				for (int i=speedTableSize-2; i>=0; i--){
					this.speedTable.set(i+1, this.speedTable.get(i).doubleValue());
				}
				this.speedTable.set(0, speed);

				this.lastSeenOnStudiedLinkEnter.put(personId,nowTime);
			} else {
				this.lastSeenOnStudiedLinkEnter.put(personId, nowTime);
			}
			//this.numberOfDrivingAgents = this.lastSeenOnStudiedLinkEnter.size();
		}

		private void checkSpeedStability(){
			double relativeDeviances = 0.;
			double averageSpeed = 0;
			for (int i=0; i<this.speedTableSize; i++){
				averageSpeed += this.speedTable.get(i).doubleValue();
			}
			averageSpeed /= this.speedTableSize;
			for (int i=0; i<this.speedTableSize; i++){
				relativeDeviances += Math.pow( ((this.speedTable.get(i).doubleValue() - averageSpeed) / averageSpeed) , 2);
			}
			relativeDeviances /= travelModes.length;//taking dependence on number of modes away
			if (relativeDeviances < 0.0005){
				this.speedStability = true;
			} else {
				this.speedStability = false;
			}
		}

		private void checkFlowStability900(){
			double absoluteDeviances = this.lastXFlows900.get(this.lastXFlows900.size()-1) - this.lastXFlows900.get(0);
			if (Math.abs(absoluteDeviances) < 1){
				this.flowStability = true;
				if(modeId==null) log.info("========== Reaching a certain flow stability for global flow.");
				else log.info("========== Reaching a certain flow stability in mode: "+modeId.toString());
			} else {
				this.flowStability = false;
			}
		}

		private void initDynamicVariables() {
			//numberOfAgents for each mode should be initialized at this point
			this.decideSpeedTableSize();
			this.speedTable = new LinkedList<Double>();
			for (int i=0; i<this.speedTableSize; i++){
				this.speedTable.add(0.);
			}
			this.flowTime = 0.;
			this.flowTable900 = new LinkedList<Double>();

			flowTableReset();

			this.lastXFlows900 = new LinkedList<Double>();
			for (int i=0; i<NUMBER_OF_MEMORIZED_FLOWS; i++){
				this.lastXFlows900.add(0.);
			}
			this.speedStability = false;
			this.flowStability = false;
			this.lastSeenOnStudiedLinkEnter = new TreeMap<>();
			this.permanentDensity = 0.;
			this.permanentAverageVelocity =0.;
			this.permanentFlow = 0.;
		}

		private void reset(){
			this.speedTable.clear();
			this.speedStability = false;
			this.flowStability = false;
		}

		private void decideSpeedTableSize() {
			//Ensures a significant speed sampling for every mode size
			//Is pretty empirical and can be changed if necessary (ssix, 16.10.13)
			if (this.numberOfAgents >= 500) {
				this.speedTableSize = 50;
			} else if (this.numberOfAgents >= 100) {
				this.speedTableSize = 20;
			} else if (this.numberOfAgents >= 10) {
				this.speedTableSize = 10;
			} else if (this.numberOfAgents >  0) {
				this.speedTableSize = this.numberOfAgents;
			} else { //case no agents in mode
				this.speedTableSize = 1;
			}
		}

		@Override
		public String toString(){
			VehicleType vT = this.vehicleType;
			String str = "(id="+this.modeId+", max_v="+vT.getMaximumVelocity()+", pcu="+vT.getPcuEquivalents()+")";
			return str;
		}

		private void flowTableReset() {
			for (int i=0; i<900; i++){
				this.flowTable900.add(0.);
			}
		}

		private void saveDynamicVariables(){
			//NB: Should not be called upon a modeData without a vehicleType, as this.vehicleType will be null and will throw an exception.
			this.permanentDensity = this.numberOfAgents / (1000.*3) *1000. * this.vehicleType.getPcuEquivalents();
			this.permanentAverageVelocity = this.getActualAverageVelocity();
			log.info("Calculated permanent Speed from "+modeId+"'s lastXSpeeds : "+speedTable+"\nResult is : "+this.permanentAverageVelocity);
			//this.permanentFlow = this.getActualFlow();
			this.permanentFlow = /*this.getActualFlow900();*///Done: Sliding average instead of taking just the last value (seen to be sometimes farther from the average than expected)
					this.getSlidingAverageLastXFlows900();
			log.info("Calculated permanent Flow from "+modeId+"'s lastXFlows900 : "+lastXFlows900+"\nResult is :"+this.permanentFlow);	
		}

		//Getters/Setters
		public double getActualAverageVelocity(){
			double nowSpeed = 0.;
			for (int i=0; i<this.speedTableSize; i++){
				nowSpeed += this.speedTable.get(i);
			}
			nowSpeed /= this.speedTableSize;
			return nowSpeed;
		}

		public double getCurrentHourlyFlow(){
			double nowFlow = 0.;
			for (int i=0; i<900; i++){
				nowFlow += this.flowTable900.get(i);
			}
			return nowFlow*4;
		}

		public double getSlidingAverageLastXFlows900(){
			double average = 0;
			for (double flow : this.lastXFlows900){ average += flow; }
			return average / NUMBER_OF_MEMORIZED_FLOWS;
		}

		public boolean isSpeedStable(){
			return this.speedStability;
		}

		public boolean isFlowStable(){
			return this.flowStability;
		}

		public void setnumberOfAgents(int n){
			this.numberOfAgents = n;
		}

		public double getPermanentDensity(){
			return this.permanentDensity;
		}

		public void setPermanentDensity(double permanentDensity) {
			this.permanentDensity = permanentDensity;
		}

		public double getPermanentAverageVelocity(){
			return this.permanentAverageVelocity;
		}

		public void setPermanentAverageVelocity(double permanentAverageVelocity) {
			this.permanentAverageVelocity = permanentAverageVelocity;
		}

		public double getPermanentFlow(){
			return this.permanentFlow;
		}

		public void setPermanentFlow(double permanentFlow) {
			this.permanentFlow = permanentFlow;
		}

		public int getNumberOfDrivingAgents() {
			return this.lastSeenOnStudiedLinkEnter.size();
		}
	}

	//=======================================

	private static class MyRoundAndRoundAgent implements MobsimDriverAgent{

		private PersonDriverAgentImpl delegate;
		public boolean isArriving;

		public MyRoundAndRoundAgent(Person p, Plan unmodifiablePlan, QSim qSim) {
			this.delegate = new PersonDriverAgentImpl(PopulationUtils.unmodifiablePlan(p.getSelectedPlan()), qSim);
			this.isArriving = false;//false at start, modified when all data is extracted.
		}

		@Override
		public final void endActivityAndComputeNextState(double now) {
			delegate.endActivityAndComputeNextState(now);
		}

		@Override
		public final void endLegAndComputeNextState(double now) {
			delegate.endLegAndComputeNextState(now);
		}

		@Override
		public final void setStateToAbort(double now) {
			delegate.setStateToAbort(now);
		}

		@Override
		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}

		@Override
		public final double getActivityEndTime() {
			return delegate.getActivityEndTime();
		}

		@Override
		public final Id<Link> getCurrentLinkId() {
			return delegate.getCurrentLinkId();
		}

		@Override
		public final Double getExpectedTravelTime() {
			return delegate.getExpectedTravelTime();
		}

		@Override
		public Double getExpectedTravelDistance() {
			return delegate.getExpectedTravelDistance();
		}

		@Override
		public final String getMode() {
			return delegate.getMode();
		}

		@Override
		public final Id<Link> getDestinationLinkId() {
			return delegate.getDestinationLinkId();
		}

		@Override
		public final Id<Person> getId() {
			return delegate.getId();
		}

		@Override
		public State getState() {
			return delegate.getState();
		}

		@Override
		public final void notifyArrivalOnLinkByNonNetworkMode(Id<Link> linkId) {
			delegate.notifyArrivalOnLinkByNonNetworkMode(linkId);
		}

		@Override
		public Id<Link> chooseNextLinkId() {
			if (globalFlowDynamicsUpdator.isPermanent()){ 
				isArriving = true; 
			}

			if(delegate.getCurrentLinkId().equals(Id.createLinkId("home"))){
				return Id.createLinkId(0);
			} else if(delegate.getCurrentLinkId().equals(Id.createLinkId(0))){
				if ( isArriving) {
					return Id.createLinkId("work") ;
				} else {
					return Id.createLinkId(1) ;
				}
			} else if(delegate.getCurrentLinkId().equals(Id.createLinkId(1))){
				return Id.createLinkId(2);
			} else if(delegate.getCurrentLinkId().equals(Id.createLinkId(2))){
				return Id.createLinkId(0);
			} else return delegate.chooseNextLinkId();
			
		} 

		@Override
		public void notifyMoveOverNode(Id<Link> newLinkId) {
			delegate.notifyMoveOverNode(newLinkId);
		}

		@Override
		public void setVehicle(MobsimVehicle veh) {
			delegate.setVehicle(veh);
		}

		@Override
		public MobsimVehicle getVehicle() {
			return delegate.getVehicle();
		}

		@Override
		public Id<Vehicle > getPlannedVehicleId() {
			return delegate.getPlannedVehicleId();
		}

		@Override
		public boolean isWantingToArriveOnCurrentLink() {
			// The following is the old condition: Being at the end of the plan means you arrive anyways, no matter if you are on the right or wrong link.
			// kai, nov'14
			if ( this.chooseNextLinkId()==null ) {
				return true ;
			} else {
				return false ;
			}
		}

	}

	//=======================================

	private static class MyAgentFactory implements AgentFactory {

		private QSim qSim;

		public MyAgentFactory(QSim qSim) {
			this.qSim = qSim;
		}

		@Override
		public MobsimAgent createMobsimAgentFromPerson(Person p) {
			MyRoundAndRoundAgent agent = new MyRoundAndRoundAgent(p, PopulationUtils.unmodifiablePlan(p.getSelectedPlan()), this.qSim);
			return agent;
		}
	}

	//=======================================

	class GlobalFlowDynamicsUpdator implements LinkEnterEventHandler {

		private Scenario scenario;
		private Map<Id<VehicleType>, TravelModesFlowDynamicsUpdator> travelModesFlowData;
		private TravelModesFlowDynamicsUpdator globalFlowData;

		private boolean permanentRegime;

		/**
		 * container to store static properties of vehicles and dynamic flow properties during simulation 
		 */
		public GlobalFlowDynamicsUpdator(Scenario sc, Map<Id<VehicleType>, TravelModesFlowDynamicsUpdator> travelModeFlowDataContainer){
			this.scenario =  sc;
			this.travelModesFlowData = travelModeFlowDataContainer;
			for (int i=0; i<travelModes.length; i++){
				this.travelModesFlowData.get(Id.create(travelModes[i],VehicleType.class)).initDynamicVariables();
			}
			this.globalFlowData = new TravelModesFlowDynamicsUpdator();
			this.globalFlowData.setnumberOfAgents(sc.getPopulation().getPersons().size());
			this.globalFlowData.initDynamicVariables();
			this.permanentRegime = false;
		}

		@Override
		public void reset(int iteration) {	
			for (int i=0; i<travelModes.length; i++){
				this.travelModesFlowData.get(Id.create(travelModes[i],VehicleType.class)).reset();
			}
			this.globalFlowData.reset();
			this.permanentRegime = false;
		}

		public void handleEvent(LinkEnterEvent event) {
			if (!(permanentRegime)){
				Id<Person> personId = Id.createPersonId(event.getVehicleId());
				double pcu_person = 0.;

				//Disaggregated data updating methods
				String travelMode = "NA";
				List<PlanElement> pes = scenario.getPopulation().getPersons().get(personId).getSelectedPlan().getPlanElements();
				for(PlanElement pe :pes){
					if(pe instanceof Leg){
						travelMode =((Leg)pe).getMode().toString();
						break;
					}
				}
				Id<VehicleType> transportMode = modeVehicleTypes.get(travelMode).getId();
				this.travelModesFlowData.get(transportMode).handle(event);
				pcu_person = modeVehicleTypes.get(travelMode).getPcuEquivalents();

				//Aggregated data update
				double nowTime = event.getTime();
				if (event.getLinkId().equals(flowDynamicsMeasurementLinkId)){				
					this.globalFlowData.updateFlow900(nowTime, pcu_person);
					this.globalFlowData.updateSpeedTable(nowTime, personId);
					//Waiting for all agents to be on the track before studying stability
					if ((this.globalFlowData.getNumberOfDrivingAgents() == this.globalFlowData.numberOfAgents) && (nowTime>1800)){	//TODO parametrize this correctly
						/*//Taking speed check out, as it is not reliable on the global speed table
						 *  Maybe making a list of moving averages could be smart, 
						 *  but there is no reliable converging process even in that case. (ssix, 25.10.13)
						 * if (!(this.globalData.isSpeedStable())){
							this.globalData.checkSpeedStability(); 
							System.out.println("Checking speed stability in global data for: "+this.globalData.getSpeedTable());
						}*/
						if (!(this.globalFlowData.isFlowStable())){
							this.globalFlowData.checkFlowStability900();
						}

						//Checking modes stability
						boolean modesStable = true;
						for (int i=0; i<travelModes.length; i++){
							Id<VehicleType> localVehicleType = Id.create(travelModes[i],VehicleType.class);
							if (this.travelModesFlowData.get(localVehicleType).numberOfAgents != 0){
								if (! this.travelModesFlowData.get(localVehicleType).isSpeedStable() || !(this.travelModesFlowData.get(localVehicleType).isFlowStable())) {
									modesStable = false;
									break;
								} 
							}
						}
						if (modesStable){
							//Checking global stability
							if ( /*this.globalData.isSpeedStable() &&*/ this.globalFlowData.isFlowStable() ){
								log.info("========== Global permanent regime is attained");
								for (int i=0; i<travelModes.length; i++){
									this.travelModesFlowData.get(Id.create(travelModes[i],VehicleType.class)).saveDynamicVariables();
								}
								this.globalFlowData.setPermanentAverageVelocity(this.globalFlowData.getActualAverageVelocity());
								//this.permanentFlow = this.getActualFlow();
								this.globalFlowData.setPermanentFlow(this.globalFlowData.getCurrentHourlyFlow());
								double globalDensity = 0.;
								for (TravelModesFlowDynamicsUpdator mode : this.travelModesFlowData.values()){
									globalDensity += mode.getPermanentDensity();
								}
								this.globalFlowData.setPermanentDensity(globalDensity);
								this.permanentRegime = true;
							}
						}
					}
				}
			}
		}

		public boolean isPermanent(){
			return permanentRegime;
		}

		public TravelModesFlowDynamicsUpdator getGlobalData(){
			return this.globalFlowData;
		}
	}

}