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

package org.matsim.contrib.taxi.data;

import java.util.Collection;

import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.taxi.data.TaxiRequest.TaxiRequestStatus;

import com.google.common.collect.Iterables;

public class TaxiRequests {
	@SuppressWarnings("unchecked")
	public static int countRequestsWithStatus(Iterable<? extends Request> requests, TaxiRequestStatus status) {
		return Iterables.size(Iterables.filter((Collection<TaxiRequest>)requests, req -> req.getStatus() == status));
	}
}
